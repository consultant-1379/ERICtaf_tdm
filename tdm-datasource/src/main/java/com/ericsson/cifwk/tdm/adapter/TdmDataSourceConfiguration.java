package com.ericsson.cifwk.tdm.adapter;

import java.util.List;

import com.ericsson.cifwk.taf.configuration.Configuration;
import com.ericsson.cifwk.taf.datasource.ConfigurationSource;
import com.google.common.collect.Lists;

public class TdmDataSourceConfiguration implements ConfigurationSource {

    private static final String DATA_PROVIDER_PROPERTY_PREFIX = "dataprovider";

    static final String DATA_SOURCE_TYPE = "type";
    static final String TDM_DATASOURCE_ID = "id";
    static final String TDM_DATASOURCE_VERSION = "version";
    static final String TDM_DATASOURCE_CONTEXT = "context";
    static final String TDM_DATASOURCE_NAME = "name";
    static final String TDM_DATASOURCE_LABEL = "label";
    static final String TDM_APPROVED = "approved";
    static final String TDM_DATASOURCE_FILTER = "filter";
    static final String TDM_DATASOURCE_COLUMNS = "columns";
    static final String TDM_DATASOURCE_QUANTITY = "quantity";
    public static final String TDM_ENABLE_LOCK = "tdm.enable.lock";

    private static final String TDM_HOST = "tdm.api.host";
    private static final String DEFAULT_TDM_HOST = "https://taf-tdm-prod.seli.wh.rnd.internal.ericsson.com/api/";

    private final String prefix;
    private final Configuration configuration;

    public TdmDataSourceConfiguration(String dataProviderName, Configuration configuration) {
        this.prefix = DATA_PROVIDER_PROPERTY_PREFIX + "." + dataProviderName;
        this.configuration = configuration;
    }

    @Override
    public String getProperty(String key) {
        final Object property = configuration.getProperty(prefix + "." + key);
        if (property instanceof List) {
            @SuppressWarnings("unchecked")
            final Iterable<? extends CharSequence> listValue = (Iterable<? extends CharSequence>) property;
            return String.join(",",  listValue);
        }
        return (String) property;
    }

    public <T> T getProperty(String key, T defaultValue) {
        return configuration.getProperty(prefix + "." + key, defaultValue);
    }

    public List<String> getListProperty(String key) {
        final Object property = configuration.getProperty(prefix + "." + key);
        if (property == null) {
            return Lists.newArrayList();
        }
        if (property instanceof List) {
            @SuppressWarnings("unchecked")
            final List<String> listValue = (List<String>) property;
            return listValue;
        }
        return Lists.newArrayList((String) property);
    }

    String getTdmHost() {
        return configuration.getString(TDM_HOST, DEFAULT_TDM_HOST);
    }
}
