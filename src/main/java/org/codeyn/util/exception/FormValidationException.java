package org.codeyn.util.exception;

/**
 * 表单校验异常
 *
 * @author wakeup
 */
public class FormValidationException extends ValidationException {

    public FormValidationException(String msg) {
        super(msg);
    }

    public FormValidationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
