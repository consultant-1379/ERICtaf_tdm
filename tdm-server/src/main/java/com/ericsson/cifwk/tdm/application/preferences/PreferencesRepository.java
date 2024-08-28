package com.ericsson.cifwk.tdm.application.preferences;

import com.ericsson.cifwk.tdm.application.common.repository.BaseRepository;
import com.ericsson.cifwk.tdm.model.PreferencesEntity;
import org.springframework.stereotype.Repository;

@Repository
public class PreferencesRepository extends BaseRepository<PreferencesEntity> {

    public static final String PREFERENCES_COLLECTION = "preferences";

    public PreferencesRepository() {
        super(PREFERENCES_COLLECTION, PreferencesEntity.class);
    }

    public PreferencesEntity findByUserId(String userId) {
        return findOne("{_id:#}", userId);
    }
}
