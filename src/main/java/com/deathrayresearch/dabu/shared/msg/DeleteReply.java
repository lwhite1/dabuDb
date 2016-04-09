package com.deathrayresearch.dabu.shared.msg;

import javax.annotation.concurrent.Immutable;

/**
 * A reply to a request to delete a single document
 */
@Immutable
public final class DeleteReply extends AbstractReply {

  public DeleteReply(Request request) {
    super(request);
  }
}
