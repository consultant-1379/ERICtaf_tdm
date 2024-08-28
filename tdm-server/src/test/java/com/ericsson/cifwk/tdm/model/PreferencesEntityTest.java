package com.ericsson.cifwk.tdm.model;

import com.ericsson.cifwk.tdm.api.model.Preferences;
import com.ericsson.cifwk.tdm.infrastructure.mapping.MapperFacadeProvider;
import ma.glasnost.orika.MapperFacade;
import org.junit.Before;
import org.junit.Test;

import static com.ericsson.cifwk.tdm.api.model.Preferences.PreferencesBuilder.aPreferences;
import static com.ericsson.cifwk.tdm.model.PreferencesEntity.PreferencesEntityBuilder.aPreferencesEntity;
import static com.google.common.truth.Truth.assertThat;

public class PreferencesEntityTest {

    private MapperFacade mapperFacade;

    @Before
    public void setUp() {
        mapperFacade = new MapperFacadeProvider().mapperFacade();
    }

    @Test
    public void shouldMap_preferencesEntity_toPreferences() throws Exception {
        PreferencesEntity entity = aPreferencesEntity()
                .withUserId("userId")
                .withContextId("contextId")
                .build();

        Preferences preferences = mapperFacade.map(entity, Preferences.class);

        assertThat(preferences.getUserId()).isEqualTo("userId");
        assertThat(preferences.getContextId()).isEqualTo("contextId");
        assertThat(preferences.getContextName()).isNull();
    }

    @Test
    public void shouldMap_preferences_toPreferencesEntity() throws Exception {
        Preferences preferences = aPreferences()
                .withUserId("userId")
                .withContextId("contextId")
                .build();

        PreferencesEntity entity = mapperFacade.map(preferences, PreferencesEntity.class);

        assertThat(entity.getUserId()).isEqualTo("userId");
        assertThat(entity.getContextId()).isEqualTo("contextId");
    }
}
