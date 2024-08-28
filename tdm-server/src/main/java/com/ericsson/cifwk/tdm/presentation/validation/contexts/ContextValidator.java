package com.ericsson.cifwk.tdm.presentation.validation.contexts;

import com.ericsson.cifwk.tdm.api.model.Context;
import com.ericsson.cifwk.tdm.api.model.HasContextId;
import com.ericsson.cifwk.tdm.application.contexts.ContextService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 09/05/2016
 */
public class ContextValidator implements ConstraintValidator<ContextIdExists, HasContextId> {

    @Autowired
    private ContextService contextService;

    private boolean allowNull;

    @Override
    public void initialize(ContextIdExists constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(HasContextId value, ConstraintValidatorContext context) {
        if (allowNull && isNullOrEmpty(value.getContextId())) {
            return true;
        }
        Optional<Context> resolvedContext = contextService.findBySystemId(value.getContextId());
        return resolvedContext.isPresent();
    }
}
