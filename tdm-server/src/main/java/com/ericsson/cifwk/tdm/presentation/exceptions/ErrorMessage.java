package com.ericsson.cifwk.tdm.presentation.exceptions;

/**
 * Created by egergle on 12/07/2017.
 */
public class ErrorMessage {

    private String message = "";

    public ErrorMessage() {
        //NO SONAR
    }

    public ErrorMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
