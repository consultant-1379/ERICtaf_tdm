package com.ericsson.cifwk.tdm.api.model.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 09/05/2016
 */
public class ValidationError {
    private List<FieldError> fieldErrors = new ArrayList<>();

    public ValidationError() {
        //NO SONAR
    }

    public void addFieldError(String path, String message) {
        FieldError error = new FieldError(path, message);
        fieldErrors.add(error);
    }

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }
}
