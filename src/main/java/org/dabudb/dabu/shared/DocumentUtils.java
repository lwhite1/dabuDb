package org.dabudb.dabu.shared;

import com.google.protobuf.ByteString;
import org.dabudb.dabu.shared.protobufs.Request;

/**
 * Utilities for working with Documents
 */
public final class DocumentUtils {

  private DocumentUtils() {}

  public static Request.Document getDocument(Document document) {
    return Request.Document.newBuilder()
        .setKey(ByteString.copyFrom(document.key()))
        .setContentBytes(ByteString.copyFrom(document.getContents()))
        .setContentClass(document.getContentClass())
        .setContentType(document.getContentType())
        .setInstanceVersion(document.documentVersion())
        .setSchemaVersion(document.schemaVersion())
        .build();
  }
}
