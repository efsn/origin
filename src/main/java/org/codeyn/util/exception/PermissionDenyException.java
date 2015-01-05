package org.codeyn.util.exception;


/**
 * 如果是没有权限的异常,则抛出此异常
 * @author zcx
 */
public class PermissionDenyException extends BusinessLogicException {
  public PermissionDenyException() {
    super();
  }

  public PermissionDenyException(String msg) {
    super(msg);
  }

  public PermissionDenyException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public PermissionDenyException(Throwable e) {
    super(e);
  }
}
