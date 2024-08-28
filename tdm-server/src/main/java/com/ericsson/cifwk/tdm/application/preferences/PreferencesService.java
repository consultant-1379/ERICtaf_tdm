package com.ericsson.cifwk.tdm.application.preferences;

import com.ericsson.cifwk.tdm.api.model.Context;
import com.ericsson.cifwk.tdm.api.model.Preferences;
import com.ericsson.cifwk.tdm.application.contexts.ContextService;
import com.ericsson.cifwk.tdm.model.PreferencesEntity;
import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.Optional.ofNullable;

import static com.ericsson.cifwk.tdm.api.model.Preferences.PreferencesBuilder.aPreferences;

@Service
public class PreferencesService {

    @Autowired private PreferencesRepository repository;
    @Autowired private ContextService contextService;
    @Autowired private MapperFactory mapperFactory;

    public Optional<Preferences> findByUserId(String userId) {
        Preferences preference = getPreferencesByUserId(userId);
        enrichContextName(preference);
        return ofNullable(preference);
    }

    private Preferences getPreferencesByUserId(final String userId) {
        PreferencesEntity entity = repository.findByUserId(userId);
        Preferences preferences = mapperFacade().mapReverse(entity);
        if (preferences == null) {
            preferences = setDefaultPreference(userId);
        }
        return preferences;
    }

    private Preferences setDefaultPreference(String userId) {
        Optional<Context> context = contextService.findBySystemId("systemId-1");
        if (context.isPresent()) {
            Preferences preference = aPreferences()
                    .withContextId("systemId-1")
                    .withUserId(userId)
                    .withContextName(context.get().getName())
                    .build();
            update(preference);
            return  preference;
        }
        return null;
    }

    void enrichContextName(Preferences preferences) {
        if (preferences != null) {
            String contextId = preferences.getContextId();
            Optional<Context> context = contextService.findBySystemId(contextId);
            if (context.isPresent()) {
                preferences.setContextName(context.get().getName());
            }
        }
    }

    public boolean update(Preferences preferences) {
        PreferencesEntity entity = mapperFacade().map(preferences);
        return repository.update(entity);
    }

    private BoundMapperFacade<Preferences, PreferencesEntity> mapperFacade() {
        return mapperFactory.getMapperFacade(Preferences.class, PreferencesEntity.class);
    }
}
