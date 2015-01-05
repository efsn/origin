package org.codeyn.util.exception;

/**
 * 业务逻辑异常，一般是业务逻辑程序主动触发的，属于正常情况下的一场，例如参数不正确等
 * 这样的异常一般不需要记录日志，也不需要输出到控制台，但要反映到用户界面上，让用户察觉到。
 * 
 */
public class BusinessLogicException extends RuntimeException {
  public BusinessLogicException() {
    super();
  }

  public BusinessLogicException(String msg) {
    super(msg);
  }

  public BusinessLogicException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public BusinessLogicException(Throwable e) {
    super(e);
  }

}
