package com.flightspring.exception;

public class SeatNotAvailableException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public SeatNotAvailableException(String msg) {
		super(msg);
	}

}
