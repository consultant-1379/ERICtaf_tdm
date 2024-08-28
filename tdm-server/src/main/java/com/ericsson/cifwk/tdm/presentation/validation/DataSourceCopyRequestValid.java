package com.ericsson.cifwk.tdm.presentation.validation;

import com.ericsson.cifwk.tdm.presentation.validation.contexts.ContextIdExists;
import com.ericsson.cifwk.tdm.presentation.validation.datasources.DataSourceIdExists;

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
@Constraint(validatedBy = {})
@Documented
@ContextIdExists(allowNull = true)
@DataSourceIdExists(allowNull = true)
public @interface DataSourceCopyRequestValid {
    String message() default "Invalid datasource copy request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
