package com.bazzi.core.ex;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ParameterException extends RuntimeException {
	private static final long serialVersionUID = 5576593478040005176L;
	private final String code;
	private final String message;

	public ParameterException(String code, String message) {
		this.message = message;
		this.code = code;
	}

}
