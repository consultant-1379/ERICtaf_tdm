package com.ericsson.cifwk.tdm.presentation.validation.contexts;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 09/05/2016
 */
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ContextValidator.class})
@Documented
public @interface ContextIdExists {
    String message() default "ContextId doesn't exist";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    boolean allowNull() default true;
}
