package com.ericsson.cifwk.tdm.db;

import org.springframework.test.context.TestExecutionListeners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For each test method invoke Flyway.clean command
 * and apply migrations from specified locations.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@TestExecutionListeners(mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS, listeners = {
        MongoBeeTestExecutionListener.class
})
public @interface MongoBee {
    String location() default "";

    boolean invokeCleanBeforeMethod() default false;
}
