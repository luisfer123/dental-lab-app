package com.dental.lab.exceptions;

public class AuthorityNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public AuthorityNotFoundException(String message) {
		super(message);
	}

}
