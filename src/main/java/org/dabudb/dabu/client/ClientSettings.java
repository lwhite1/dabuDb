package org.dabudb.dabu.client;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.dabudb.dabu.shared.ContentsPipe;
import org.dabudb.dabu.shared.StandardDocument;
import org.dabudb.dabu.shared.compression.CompressionType;
import org.dabudb.dabu.shared.encryption.EncryptionType;
import org.dabudb.dabu.shared.exceptions.StartupException;
import org.dabudb.dabu.shared.serialization.ContentSerializerType;
import org.dabudb.dabu.shared.serialization.DocumentJsonSerializer;
import org.dabudb.dabu.shared.serialization.DocumentSerializer;

import java.util.Properties;

/**
 * ClientSettings for a database client.
 * <p>
 * These should be the same for all clients,
 * and they should be consistent with the server for every value common to clients and servers.
 */
public class ClientSettings {

  private ContentsPipe contentsPipe = ContentsPipe.create(CompressionType.SNAPPY, ContentSerializerType.JSON);

  private Class documentClass = StandardDocument.class;

  private DocumentSerializer documentSerializer = new DocumentJsonSerializer();

  private CommClient commClient = new DirectCommClient();

  private static ClientSettings ourInstance;

  private ClientSettings() {}

  public static ClientSettings getInstance() {
    if (ourInstance == null) {
      ourInstance = new ClientSettings();
    }
    return ourInstance;
  }

  ClientSettings(Properties properties) {

    setDocumentClass(properties);
    setDocumentSerializer(properties);
    setCommClient(properties);
    setContentsPipe(properties);
  }

  public ContentsPipe getContentsPipe() {
    return contentsPipe;
  }

  public Class getDocumentClass() {
    return documentClass;
  }

  public DocumentSerializer getDocumentSerializer() {
    return documentSerializer;
  }

  public CommClient getCommClient() {
    return commClient;
  }

  private void setDocumentSerializer(Properties properties) {
    try {
      this.documentSerializer =
          (DocumentSerializer)
              Class.forName(String.valueOf(properties.getProperty("document.serializer.class"))).newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
      throw new StartupException("Unable to load DocumentSerializer as specified in client.properties", e);
    }
  }

  private void setDocumentClass(Properties properties) {
    try {
      this.documentClass = Class.forName(String.valueOf(properties.get("document.class")));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new StartupException("Unable to load document class specified in client.properties", e);
    }
  }

  private void setCommClient(Properties properties) {
    try {
      this.commClient =
          (CommClient)
              Class.forName(String.valueOf(properties.getProperty("comm.client.class"))).newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      e.printStackTrace();
      throw new StartupException("Unable to load CommClient as specified in client.properties", e);
    }
  }

  private void setContentsPipe(Properties properties) {
    CompressionType compressionType = CompressionType.valueOf(properties.getProperty("document.contents.compression"));
    ContentSerializerType serializerType =
        ContentSerializerType.valueOf(properties.getProperty("document.contents.serialization"));
    EncryptionType encryptionType = EncryptionType.valueOf(properties.getProperty("document.contents.encryption"));
    String encryptionPwd = String.valueOf(properties.getProperty("document.contents.encryption.pwd"));

    try {
      Preconditions.checkState(encryptionType == EncryptionType.NONE || !Strings.isNullOrEmpty(encryptionPwd));
    } catch (IllegalStateException e) {
      e.printStackTrace();
      throw new StartupException("Unable to load ContentsPipe as specified in client.properties", e);
    }

    this.contentsPipe = ContentsPipe.create(compressionType, serializerType);
  }
}
