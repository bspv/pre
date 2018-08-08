package com.bazzi.core.generic;

import java.io.Serializable;

public class Result<T extends Serializable> implements Serializable {
	private static final long serialVersionUID = -6219043917099576984L;
	private T data;
	private boolean success;
	private String message = "";
	private String code = "";

	public Result() {
		this.success = true;
	}

	public Result(T data) {
		super();
		this.data = data;
		this.success = true;
	}

	public void setError(String code, String message) {
		this.code = code;
		this.message = message;
		this.success = false;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
