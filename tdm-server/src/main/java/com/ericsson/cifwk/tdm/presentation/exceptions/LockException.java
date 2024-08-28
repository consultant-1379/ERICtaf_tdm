package com.ericsson.cifwk.tdm.presentation.exceptions;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 22/02/2016
 */
public class LockException extends RuntimeException {
    public LockException(String message) {
        super(message);
    }

    public LockException(String message, Throwable e) {
        super(message, e);
    }
}
