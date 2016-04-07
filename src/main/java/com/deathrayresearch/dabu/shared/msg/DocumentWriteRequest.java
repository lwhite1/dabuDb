package com.deathrayresearch.dabu.shared.msg;

import com.deathrayresearch.dabu.shared.Document;

/**
 *
 */
public class DocumentWriteRequest extends AbstractRequest {

  private final Document document;

  public DocumentWriteRequest(Document document) {
    super(RequestType.WRITE);
    this.document = document;
  }

  public Document getDocument() {
    return document;
  }
}