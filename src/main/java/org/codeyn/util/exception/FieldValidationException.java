package org.codeyn.util.exception;

/**
 * 字段校验异常
 */
public class FieldValidationException extends ValidationException {

    private String fieldname = null;

    public FieldValidationException(String fieldname, String msg) {
        super(msg);
        this.fieldname = fieldname;
    }

    public FieldValidationException(String fieldname, String msg, Throwable cause) {
        super(msg, cause);
        this.fieldname = fieldname;
    }

    /**
     * 返回出异常的字段名
     *
     * @return
     */
    public String getFieldName() {
        return fieldname;
    }

}
