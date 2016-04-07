package com.deathrayresearch.dabu.server.io;

import com.deathrayresearch.dabu.shared.msg.Request;
import com.google.common.io.ByteSink;
import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;

import java.io.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Logs all write requests to a file
 */
public class WriteLog implements WriteAheadLog, Closeable, Iterator<byte[]> {

  private static WriteLog instance;

  static {
    try {
      instance = new WriteLog(System.getProperty("user.dir"));
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static WriteLog getInstance() {
    return instance;
  }

  //TODO(lwhite) handle locking/synchronization for these files

  private final static String FOLDER = "wal";
  private final static String DATA_FILE = "dataFile";
  private final static String INDEX_FILE = "indexFile";

  // The length of the current request in bytes
  private int length = -1;

  private BufferedReader indexReader;
  private BufferedInputStream requestInputByteStream;

  private OutputStream requestOutputByteStream;

  private Writer indexWriter;
  private DataInputStream dataInputStream;

  private WriteLog(String rootFolder) throws IOException {
    initializeLog(rootFolder);
  }

  @Override
  public void logRequest(Request request) throws IOException {
    log(request.toString().getBytes());
  }

  @Override
  public void replay() {

  }

  @Override
  public void close() throws IOException {

    requestInputByteStream.close();
    requestOutputByteStream.close();
    dataInputStream.close();
    indexWriter.close();
    indexReader.close();
  }

  @Override
  public boolean hasNext() {
    try {
      String lengthString = indexReader.readLine();
      if (lengthString != null) {
        length = Integer.parseInt(lengthString);
        return true;
      }
    } catch (IOException ex) {
      //TODO(lwhite): handle exception
    }
    length = -1;
    return false;
  }

  @Override
  public byte[] next() {
    if (length < 0) {
      throw new NoSuchElementException("Reached the end of the log file.");
    }
    byte[] requestBytes = new byte[length];
    try {
      requestInputByteStream.read(requestBytes);

    } catch (IOException e) {
      e.printStackTrace();
      // TODO(lwhite): Handle exception
    }

    return requestBytes;
  }

  private void log(byte[] request) throws IOException {
    requestOutputByteStream.write(request);
    requestOutputByteStream.flush();

    indexWriter.write(String.valueOf(request.length) + "\n");
    indexWriter.flush();
  }

  /**
   * Initializes the log file, creating tools for reading and writing it
   * @throws IOException
   */
  private void initializeLog(String logFileRoot) throws IOException {

    java.io.File targetDirectory = getFolderName(logFileRoot);
    if (!targetDirectory.exists()) {
      targetDirectory.mkdirs();
    }

    File indexFile = Paths.get(targetDirectory + String.valueOf(File.separatorChar) + INDEX_FILE).toFile();
    File dataFile = Paths.get(targetDirectory + String.valueOf(File.separatorChar) + DATA_FILE).toFile();

    if (!indexFile.exists() ) {
      indexFile.createNewFile();
    }
    if (!dataFile.exists()) {
      dataFile.createNewFile();
    }

    FileInputStream dataFis = new FileInputStream(dataFile);
    dataInputStream = new DataInputStream(dataFis);
    FileInputStream indexFis = new FileInputStream(indexFile);
    indexReader = new BufferedReader(new InputStreamReader(indexFis));

    ByteSink byteSink = Files.asByteSink(dataFile, FileWriteMode.APPEND);
    requestOutputByteStream = byteSink.openStream();

    CharSink charSink = Files.asCharSink(indexFile, StandardCharsets.UTF_8, FileWriteMode.APPEND);
    indexWriter = charSink.openStream();
  }

  private static java.io.File getFolderName(String logFileRoot) {
    String name = logFileRoot + java.io.File.separatorChar + FOLDER;
    return Paths.get(name).toFile();
  }
}
