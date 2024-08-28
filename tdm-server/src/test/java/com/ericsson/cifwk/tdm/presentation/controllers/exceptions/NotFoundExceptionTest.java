package com.ericsson.cifwk.tdm.presentation.controllers.exceptions;

import com.ericsson.cifwk.tdm.presentation.exceptions.NotFoundException;
import org.junit.Test;

import static com.ericsson.cifwk.tdm.presentation.exceptions.NotFoundException.verifyFound;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class NotFoundExceptionTest {

    @Test(expected = NotFoundException.class)
    public void verifyFound_shouldThrowNotFoundException_whenOptional_empty() throws Exception {
        verifyFound(empty());
    }

    @Test
    public void verifyFound_shouldReturnObject_whenOptional_notEmpty() throws Exception {
        Object object = new Object();

        Object result = verifyFound(of(object));

        assertThat(result).isSameAs(object);
    }

    @Test(expected = NotFoundException.class)
    public void verifyFound_shouldThrowNotFoundException_whenObject_null() throws Exception {
        verifyFound((Object) null);
    }

    @Test
    public void verifyFound_shouldReturnObject_whenObject_notNull() throws Exception {
        Object object = new Object();

        Object result = verifyFound(object);

        assertThat(result).isSameAs(object);
    }
}
