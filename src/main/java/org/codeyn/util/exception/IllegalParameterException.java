package org.codeyn.util.exception;


/**
 * 不合法的参数
 * 所有的从客户端传入的参数都使用此类抛出异常,此类抛出的异常不会被日志系统捕获
 * @author work
 */
public class IllegalParameterException extends BusinessLogicException {

  public IllegalParameterException() {
    super();
  }

  public IllegalParameterException(String msg) {
    super(msg);
  }

  public IllegalParameterException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public IllegalParameterException(Throwable e) {
    super(e);
  }

}
