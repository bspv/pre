package com.bazzi.core.ex;

public class ParameterException extends RuntimeException {
	private static final long serialVersionUID = 3941618802260071830L;
	private String code;
	private String message;

	public ParameterException(String code, String message) {
		this.message = message;
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
