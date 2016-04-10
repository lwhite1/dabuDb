package org.dabudb.dabu.shared.msg.reply;

import org.dabudb.dabu.shared.msg.request.Request;

import javax.annotation.concurrent.Immutable;

/**
 * A reply to a get request for a single document
 */
@Immutable
public final class DocGetReply extends AbstractReply {

  private final byte[] document;

  public DocGetReply(Request request, byte[] document) {
    super(request);
    this.document = document;
  }

  public byte[] getDocument() {
    return document;
  }
}