package com.ericsson.cifwk.tdm;

import static org.junit.Assert.fail;

public final class CustomAssertions {

    private CustomAssertions() {
    }

    public static void assertThatNoExceptionsThrownBy(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            fail("No exception was expected to be thrown, but got " + e);
        }
    }
}
