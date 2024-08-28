package com.ericsson.cifwk.tdm.application.preferences;

import com.ericsson.cifwk.tdm.api.model.Context;
import com.ericsson.cifwk.tdm.api.model.Preferences;
import com.ericsson.cifwk.tdm.application.contexts.ContextService;
import com.ericsson.cifwk.tdm.model.PreferencesEntity;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static com.ericsson.cifwk.tdm.api.model.Preferences.PreferencesBuilder.aPreferences;
import static com.ericsson.cifwk.tdm.model.PreferencesEntity.PreferencesEntityBuilder.aPreferencesEntity;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class PreferencesServiceTest {

    @InjectMocks @Spy
    private PreferencesService service;

    @Mock private PreferencesRepository repository;
    @Mock private ContextService contextService;

    @Spy
    private MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

    @Test
    public void findByUserId_shouldReturn_emptyOptional_whenNotFound() throws Exception {
        doReturn(null).when(repository).findByUserId(anyString());
        doReturn(Optional.empty()).when(contextService).findBySystemId(anyString());
        Optional<Preferences> result = service.findByUserId("userId");

        assertThat(result.isPresent()).isFalse();
        verify(repository).findByUserId("userId");
        verify(contextService).findBySystemId("systemId-1");
        verify(service).enrichContextName(null);
    }

    @Test
    public void findByUserId_shouldReturn_fullOptional_whenFound() throws Exception {
        doReturn(aPreferencesEntity().build()).when(repository).findByUserId(anyString());
        doNothing().when(service).enrichContextName(any(Preferences.class));

        Optional<Preferences> result = service.findByUserId("userId");

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isInstanceOf(Preferences.class);
        verify(repository).findByUserId("userId");
        verify(service).enrichContextName(result.get());
    }

    @Test
    public void enrichContextName_shouldDoNothing_whenPreferences_null() throws Exception {
        service.enrichContextName(null);

        verifyZeroInteractions(contextService);
    }

    @Test
    public void enrichContextName_shouldNotEnrich_whenContext_notFound() throws Exception {
        Preferences preferences = aPreferences()
                .withContextId("contextId")
                .build();
        doReturn(empty()).when(contextService).findBySystemId(anyString());

        service.enrichContextName(preferences);

        assertThat(preferences.getContextName()).isNull();
        verify(contextService).findBySystemId("contextId");
    }

    @Test
    public void enrichContextName_shouldEnrich_whenContext_found() throws Exception {
        Preferences preferences = aPreferences()
                .withContextId("contextId")
                .build();
        Context context = new Context();
        context.setName("contextName");
        doReturn(of(context)).when(contextService).findBySystemId(anyString());

        service.enrichContextName(preferences);

        assertThat(preferences.getContextName()).isEqualTo("contextName");
        verify(contextService).findBySystemId("contextId");
    }

    @Test
    public void update_shouldReturnFalse_whenSaved_newPreferences() throws Exception {
        doReturn(false).when(repository).update(any(PreferencesEntity.class));

        boolean result = service.update(aPreferences().build());

        assertThat(result).isFalse();
        verify(repository, only()).update(any(PreferencesEntity.class));
    }

    @Test
    public void update_shouldReturnTrue_whenUpdated_existingPreferences() throws Exception {
        doReturn(true).when(repository).update(any(PreferencesEntity.class));

        boolean result = service.update(aPreferences().build());

        assertThat(result).isTrue();
        verify(repository, only()).update(any(PreferencesEntity.class));
    }
}
