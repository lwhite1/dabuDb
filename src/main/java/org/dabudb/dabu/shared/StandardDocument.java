package org.dabudb.dabu.shared;

import org.dabudb.dabu.client.Settings;

import java.util.Arrays;

/**
 * A basic document implementation
 */
public class StandardDocument implements Document {

  private static ContentsPipe contentsPipe;

  public static DocumentSerializer getDocumentSerializer() {
    if (documentSerializer == null) {
      documentSerializer = Settings.getInstance().getDocumentSerializer();
    }
    return documentSerializer;
  }

  public static ContentsPipe getContentsPipe() {
    if (contentsPipe == null) {
      contentsPipe = Settings.getInstance().getContentsPipe();
    }
    return contentsPipe;
  }

  private static DocumentSerializer documentSerializer;

  private final byte[] contents;

  private final int documentVersion;

  private final short schemaVersion = 0;

  private boolean deleted = false;

  /** The kind of document represented by the contents */
  private final String contentType;

  public StandardDocument(DocumentContents contents) {
    this.contents = getContentsPipe().contentsToBytes(contents);
    this.documentVersion = 0;
    this.contentType = contents.getType();
  }

  public StandardDocument(Document other) {
    this.contents = other.getContents();
    synchronized(this) {
      this.documentVersion = other.documentVersion() + 1;
    }
    this.contentType = other.getContentType();
  }

  @Override
  public byte[] key() {
    return new byte[0];
  }

  @Override
  public byte[] getContents() {
    return contents;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public int schemaVersion() {
    return schemaVersion;
  }

  @Override
  public int documentVersion() {
    return documentVersion;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("StandardDocument{");
    sb.append("contents=").append(Arrays.toString(contents));
    sb.append(", documentVersion=").append(documentVersion);
    sb.append(", schemaVersion=").append(schemaVersion);
    sb.append(", deleted=").append(deleted);
    sb.append(", contentType='").append(contentType).append('\'');
    sb.append('}');
    return sb.toString();
  }

  public DocumentContents documentContents() {
    DocumentContents documentContents = null;
    try {
      documentContents = getContentsPipe().bytesToContents(Class.forName(contentType), contents);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return documentContents;
  }

  @Override
  public byte[] marshall() {
    return getDocumentSerializer().documentToBytes(this);
  }
}