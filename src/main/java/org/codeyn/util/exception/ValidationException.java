package org.codeyn.util.exception;

/**
 * 后台校验异常
 *
 */
public class ValidationException extends RuntimeException{
	
    private static final long serialVersionUID = -7374150633083306975L;

    public ValidationException(String msg){
		super(msg);
	}
	
	public ValidationException(String msg, Throwable cause){
		super(msg,cause);
	}

}
