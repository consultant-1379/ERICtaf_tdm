package com.ericsson.cifwk.tdm.datasource;

import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.client.TDMClient;

import java.util.List;

public class TdmDataSourceBuilder {

    private DataSourceIdentity dataSourceId;
    private String testwarePackage;
    private TDMClient tdmClient;
    private String columns;
    private List<String> filter;
    private List<Integer> limit;
    private String version;

    public TdmDataSourceBuilder(DataSourceIdentity dataSourceId, TDMClient tdmClient) {
        this.dataSourceId = dataSourceId;
        this.tdmClient = tdmClient;
    }

    public TdmDataSourceBuilder withTestwarePackage(String testwarePackage) {
        this.testwarePackage = testwarePackage;
        return this;
    }

    public TdmDataSourceBuilder withColumns(String columns) {
        this.columns = columns;
        return this;
    }

    public TdmDataSourceBuilder withFilter(List<String> filter) {
        this.filter = filter;
        return this;
    }

    public TdmDataSourceBuilder withLimit(List<Integer> limit) {
        this.limit = limit;
        return this;
    }

    public TdmDataSourceBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    public TdmDataSource build() {
        return new TdmDataSource(dataSourceId, testwarePackage, tdmClient, columns, filter, limit, version);
    }
}
