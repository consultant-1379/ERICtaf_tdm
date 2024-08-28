package com.ericsson.cifwk.tdm.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static com.google.common.base.MoreObjects.toStringHelper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSourceAction {

    public static final String NEW_PREFIX = "local_";

    private String id;

    private String type;

    @NotNull
    private String key;

    private String newValue;

    @NotNull
    private String version;

    @NotNull
    @Min(1)
    private Long localTimestamp;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getNewValue() {
        if (newValue == null) {
            return "";
        }
        return newValue;
    }

    public String getVersion() {
        return version;
    }

    public Long getLocalTimestamp() {
        return localTimestamp;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", id)
                .add("type", type)
                .add("key", key)
                .add("newValue", newValue)
                .add("version", version)
                .add("localTimestamp", localTimestamp)
                .toString();
    }

    public static final class DataSourceActionBuilder {

        private String id;
        private String type;
        private String key = "";
        private String newValue;
        private String version;
        private Long localTimestamp = System.currentTimeMillis();

        private DataSourceActionBuilder() {
        }

        public static DataSourceActionBuilder aDataSourceAction() {
            return new DataSourceActionBuilder();
        }

        public DataSourceActionBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public DataSourceActionBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public DataSourceActionBuilder withKey(String key) {
            this.key = key;
            return this;
        }

        public DataSourceActionBuilder withNewValue(String newValue) {
            this.newValue = newValue;
            return this;
        }

        public DataSourceActionBuilder withVersion(String version) {
            this.version = version;
            return this;
        }

        public DataSourceActionBuilder withLocalTimestamp(Long localTimestamp) {
            this.localTimestamp = localTimestamp;
            return this;
        }

        public DataSourceAction build() {
            DataSourceAction dataSourceAction = new DataSourceAction();
            dataSourceAction.newValue = this.newValue;
            dataSourceAction.id = this.id;
            dataSourceAction.localTimestamp = this.localTimestamp;
            dataSourceAction.type = this.type;
            dataSourceAction.version = this.version;
            dataSourceAction.key = this.key;
            return dataSourceAction;
        }
    }
}
