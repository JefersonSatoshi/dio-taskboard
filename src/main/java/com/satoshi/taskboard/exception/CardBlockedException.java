package com.satoshi.taskboard.exception;

@SuppressWarnings("serial")
public class CardBlockedException extends RuntimeException{
	 
    public CardBlockedException(final String message) {
        super(message);
    }
}