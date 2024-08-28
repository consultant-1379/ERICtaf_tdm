package com.ericsson.cifwk.tdm.lock;

import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.api.model.LockType;

import java.util.List;

/**
 * Created by egergle on 30/01/2018.
 */
public class LockAttributes {

    private DataSourceIdentity dataSourceId;

    private String testwarePackage;

    private String columns;

    private List<String> filter;

    private int limit;

    private LockType lockType;

    private String version;

    public LockAttributes(DataSourceIdentity dataSourceId, String testwarePackage, int limit, List<String> filter,
                          String columns, LockType lockType, String version) {
        this.dataSourceId = dataSourceId;
        this.testwarePackage = testwarePackage;
        this.limit = limit;
        this.filter = filter;
        this.columns = columns;
        this.lockType = lockType;
        this.version = version;
    }

    public String getTestwarePackage() {
        return testwarePackage;
    }

    public void setTestwarePackage(String testwarePackage) {
        this.testwarePackage = testwarePackage;
    }

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public List<String> getFilter() {
        return filter;
    }

    public void setFilter(List<String> filter) {
        this.filter = filter;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public LockType getLockType() {
        return lockType;
    }

    public void setLockType(LockType lockType) {
        this.lockType = lockType;
    }

    public DataSourceIdentity getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(DataSourceIdentity dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
