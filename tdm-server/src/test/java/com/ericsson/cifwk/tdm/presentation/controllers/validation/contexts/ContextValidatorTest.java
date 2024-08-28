package com.ericsson.cifwk.tdm.presentation.controllers.validation.contexts;

import com.ericsson.cifwk.tdm.api.model.Context;
import com.ericsson.cifwk.tdm.api.model.HasContextId;
import com.ericsson.cifwk.tdm.application.contexts.ContextService;
import com.ericsson.cifwk.tdm.presentation.validation.contexts.ContextIdExists;
import com.ericsson.cifwk.tdm.presentation.validation.contexts.ContextValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.ConstraintValidatorContext;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class ContextValidatorTest {

    @InjectMocks
    private ContextValidator validator;

    @Mock
    private ContextService contextService;

    private HasContextId value;
    private ConstraintValidatorContext context;

    @Before
    public void setUp() throws Exception {
        value = mock(HasContextId.class);
        doReturn("contextId").when(value).getContextId();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    public void isValid_shouldReturnFalse_when_notAllowNull_andResolvedContext_notPresent() throws Exception {
        setupAllowNull(false);
        doReturn(empty()).when(contextService).findBySystemId(anyString());

        boolean result = validator.isValid(value, context);

        assertThat(result).isFalse();
        verify(contextService).findBySystemId("contextId");
    }

    @Test
    public void isValid_shouldReturnFalse_when_allowNull_andResolvedContext_notPresent() throws Exception {
        setupAllowNull(true);
        doReturn(empty()).when(contextService).findBySystemId(anyString());

        boolean result = validator.isValid(value, context);

        assertThat(result).isFalse();
        verify(contextService).findBySystemId("contextId");
    }

    @Test
    public void isValid_shouldReturnTrue_when_notAllowNull_andResolvedContext_present() throws Exception {
        setupAllowNull(false);
        doReturn(of(mock(Context.class))).when(contextService).findBySystemId(anyString());

        boolean result = validator.isValid(value, context);

        assertThat(result).isTrue();
        verify(contextService).findBySystemId("contextId");
    }

    @Test
    public void isValid_shouldReturnTrue_when_allowNull_andContextId_null() throws Exception {
        setupAllowNull(true);
        doReturn(null).when(value).getContextId();

        boolean result = validator.isValid(value, context);

        assertThat(result).isTrue();
        verifyZeroInteractions(contextService);
    }

    @Test
    public void isValid_shouldReturnTrue_when_allowNull_andContextId_empty() throws Exception {
        setupAllowNull(true);
        doReturn("").when(value).getContextId();

        boolean result = validator.isValid(value, context);

        assertThat(result).isTrue();
        verifyZeroInteractions(contextService);
    }

    @Test
    public void isValid_shouldReturnTrue_when_allowNull_andResolvedContext_present() throws Exception {
        setupAllowNull(true);
        doReturn(of(mock(Context.class))).when(contextService).findBySystemId(anyString());

        boolean result = validator.isValid(value, context);

        assertThat(result).isTrue();
        verify(contextService).findBySystemId("contextId");
    }

    private void setupAllowNull(boolean allowNull) {
        ContextIdExists contextIdExists = mock(ContextIdExists.class);
        doReturn(allowNull).when(contextIdExists).allowNull();
        validator.initialize(contextIdExists);
    }
}
