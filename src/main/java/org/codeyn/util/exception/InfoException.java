package org.codeyn.util.exception;

/**
 * @author yukun
 * 如果触发了此异常，表示程序执行流程需要中止，但此中止是正常的，界面上应该将此异常当作提示信息友好的提示给用户
 */
public final class InfoException extends RuntimeException {
  public InfoException() {
  }

  public InfoException(String message) {
    super(message);
  }
}
