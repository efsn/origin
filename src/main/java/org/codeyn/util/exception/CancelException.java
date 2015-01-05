package org.codeyn.util.exception;


/**
 * 用于执行取消的异常
 */

public class CancelException extends BusinessLogicException {
  public CancelException() {
  }

  public CancelException(String message) {
    super(message);
  }
}
