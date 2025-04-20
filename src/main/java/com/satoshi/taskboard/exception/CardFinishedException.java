package com.satoshi.taskboard.exception;

@SuppressWarnings("serial")
public class CardFinishedException extends RuntimeException{
	 
    public CardFinishedException(final String message) {
        super(message);
    }
}