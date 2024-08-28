package com.ericsson.cifwk.tdm.presentation.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@ResponseStatus(
        value = HttpStatus.NOT_FOUND,
        reason = "Resource not found"
    )
public class NotFoundException extends RuntimeException {

    public static <T> T verifyFound(T object) {
        return verifyFound(ofNullable(object));
    }

    public static <T> T verifyFound(Optional<T> object) {
        return object.orElseThrow(NotFoundException::new);
    }

    public static <T> List<T> verifyFound(List<T> list) {
        if (list == null || list.isEmpty()) {
            throw new NotFoundException();
        }

        return list;
    }
}
