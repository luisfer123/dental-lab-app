package com.dental.lab.exceptions;

public class ImageNotValidException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ImageNotValidException(String message) {
		super(message);
	}
	
}
