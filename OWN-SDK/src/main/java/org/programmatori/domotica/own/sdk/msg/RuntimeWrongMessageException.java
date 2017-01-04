package org.programmatori.domotica.own.sdk.msg;

public class RuntimeWrongMessageException extends RuntimeException {
	private static final long serialVersionUID = 8883156895481894266L;

	public RuntimeWrongMessageException(String message) {
		super(message);
	}
}
