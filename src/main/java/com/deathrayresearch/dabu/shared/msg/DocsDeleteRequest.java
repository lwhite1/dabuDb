package com.deathrayresearch.dabu.shared.msg;

import java.util.Collection;

/**
 * A request to delete a collection of documents
 */
public class DocsDeleteRequest extends AbstractRequest {

  private final Collection<byte[]> keys;

  public DocsDeleteRequest(Collection<byte[]> keys) {
    super(RequestType.DOCUMENTS_DELETE);
    this.keys = keys;
  }

  public Collection<byte[]> getKey() {
    return keys;
  }

}
