package com.ericsson.cifwk.tdm.model;

import static com.google.common.base.MoreObjects.toStringHelper;

import org.jongo.marshall.jackson.oid.MongoId;

public class PreferencesEntity {

    @MongoId
    private String userId;

    private String contextId;

    public PreferencesEntity() {
        //NO SONAR
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("userId", userId)
                .add("contextId", contextId)
                .toString();
    }

    public static final class PreferencesEntityBuilder {

        private String userId;
        private String contextId;

        private PreferencesEntityBuilder() {
        }

        public static PreferencesEntityBuilder aPreferencesEntity() {
            return new PreferencesEntityBuilder();
        }

        public PreferencesEntityBuilder withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public PreferencesEntityBuilder withContextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public PreferencesEntity build() {
            PreferencesEntity preferencesEntity = new PreferencesEntity();
            preferencesEntity.setUserId(userId);
            preferencesEntity.setContextId(contextId);
            return preferencesEntity;
        }
    }
}
