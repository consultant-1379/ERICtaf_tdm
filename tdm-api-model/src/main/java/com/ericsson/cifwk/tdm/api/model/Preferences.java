package com.ericsson.cifwk.tdm.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;

import static com.google.common.base.MoreObjects.toStringHelper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Preferences implements HasContextId {

    @NotNull
    private String userId;

    @NotNull
    private String contextId;

    private String contextName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getContextName() {
        return contextName;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("userId", userId)
                .add("contextId", contextId)
                .add("contextName", contextName)
                .toString();
    }

    public static final class PreferencesBuilder {

        private String userId;
        private String contextId;
        private String contextName;

        private PreferencesBuilder() {
        }

        public static PreferencesBuilder aPreferences() {
            return new PreferencesBuilder();
        }

        public PreferencesBuilder withUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public PreferencesBuilder withContextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public PreferencesBuilder withContextName(String contextName) {
            this.contextName = contextName;
            return this;
        }

        public Preferences build() {
            Preferences preferences = new Preferences();
            preferences.setUserId(userId);
            preferences.setContextId(contextId);
            preferences.setContextName(contextName);
            return preferences;
        }
    }
}
