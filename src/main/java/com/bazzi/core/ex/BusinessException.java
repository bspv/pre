package com.bazzi.core.ex;

public class BusinessException extends RuntimeException {
	private static final long serialVersionUID = -1842031384136789937L;
	private String code;
	private String message;

	public BusinessException(String code, String message) {
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
