package org.dabudb.dabu.shared.exceptions;

import org.dabudb.dabu.shared.protobufs.Request;

/**
 * Thrown when an attempt to write a value is determined to represent an update to an existing value, and another
 * thread has updated that document after this thread read it.
 */
public class OptimisticLockException extends DatastoreException {


  public OptimisticLockException(Request.ErrorCondition condition) {
    super(condition);
  }
}
