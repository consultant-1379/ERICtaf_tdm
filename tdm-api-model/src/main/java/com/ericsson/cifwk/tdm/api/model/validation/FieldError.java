package com.ericsson.cifwk.tdm.api.model.validation;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.Objects;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 09/05/2016
 */
public class FieldError {

    private String path;
    private String message;

    public FieldError() {
        //NO SONAR
    }

    public FieldError(String path, String message) {
        this.path = path;
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FieldError that = (FieldError) o;
        return Objects.equals(path, that.path) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, message);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("path", path)
                .add("message", message)
                .toString();
    }
}
