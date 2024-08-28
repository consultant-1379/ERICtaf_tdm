package com.ericsson.cifwk.tdm.presentation.controllers.client;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.api.model.DataSource;
import com.ericsson.cifwk.tdm.api.model.DataSourceAction;
import com.ericsson.cifwk.tdm.api.model.DataSourceCopyRequest;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.api.model.DataSourceLabel;
import com.ericsson.cifwk.tdm.api.model.GroupView;
import com.ericsson.cifwk.tdm.api.model.Records;
import com.ericsson.cifwk.tdm.api.model.UserCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Service
public class DataSourceControllerClient extends ControllerClientCommon {

    private static final String CONTEXT = "context={contextId}";
    private static final String NAME = "name={name}";
    private static final String APPROVED = "approved={approved}";
    private static final String PREDICATE = "predicates={predicates}";

    private static final String QUERY_PARAM = "query={query}";

    private static final String DATA_SOURCES = "/api/datasources";
    private static final String DATA_SOURCE_LATEST = DATA_SOURCES + "/latest";
    private static final String DATA_SOURCE_COPY = DATA_SOURCES + "/copy";
    private static final String DATA_SOURCE_BY_ID = DATA_SOURCES + "/{dataSourceId}";
    private static final String DATA_SOURCE_BY_CONTEXT = DATA_SOURCES + "?" + CONTEXT;
    private static final String DATA_SOURCE_BY_CONTEXT_PATH_AND_NAME = DATA_SOURCES + "?contextPath={contextPath}&dataSourceName={dataSourceName}";
    private static final String DATA_SOURCE_LATEST_BY_CONTEXT_AND_NAME =
            DATA_SOURCE_LATEST + "?" + CONTEXT + "&" + NAME;
    private static final String DATA_SOURCE_LATEST_BY_CONTEXT_AND_NAME_APPROVED =
            DATA_SOURCE_LATEST_BY_CONTEXT_AND_NAME + "&" + APPROVED;
    private static final String DATA_SOURCE_GROUPS = DATA_SOURCES + "/groups?context={contextId}&view={view}";

    private static final String DATA_SOURCE_APPROVED = DATA_SOURCE_BY_ID + "?" + APPROVED;
    private static final String DATA_SOURCE_APPROVAL = DATA_SOURCE_BY_ID + "/approval";
    private static final String DATA_SOURCE_RECORDS = DATA_SOURCE_BY_ID + "/records";
    private static final String DATA_SOURCE_RECORDS_INCLUDE_DELETED = DATA_SOURCE_RECORDS +
            "?includeDeleted={includeDeleted}";
    private static final String DATA_SOURCE_RECORDS_WITH_SPECIFIED_COLUMNS = DATA_SOURCE_RECORDS + "?columns={columns}";
    private static final String DATA_SOURCE_VERSIONS = DATA_SOURCE_BY_ID + "/versions";

    private static final String DATA_SOURCE_VERSION = DATA_SOURCE_VERSIONS + "/{version}";
    private static final String DATA_SOURCE_VERSION_RECORDS = DATA_SOURCE_VERSION + "/records";

    private static final String DATA_SOURCE_LABEL = DATA_SOURCES + "/labels";
    private static final String DELETE_DATA_SOURCE_LABEL = DATA_SOURCE_LABEL +"/{label}/contexts/{contextId}";

    private static final String DATA_SOURCE_VERSION_RECORDS_WITH_SPECIFIED_COLUMNS =
            DATA_SOURCE_VERSION_RECORDS + "?columns={columns}";
    private static final String DATA_SOURCE_VERSION_RECORDS_WITH_PREDICATE =
            DATA_SOURCE_VERSION_RECORDS + "?" + PREDICATE;
    private static final String DATA_SOURCE_VERSION_RECORDS_WITH_COLUMNS_AND_PREDICATE =
            DATA_SOURCE_VERSION_RECORDS_WITH_SPECIFIED_COLUMNS + "&" + PREDICATE;
    @Autowired
    public DataSourceControllerClient(WebApplicationContext webApplicationContext) {
        super(webApplicationContext);
    }

    public ResultActions tryCreateFromResource(String resourceName) throws Exception {
        return postResource(resourceName, DATA_SOURCES);
    }
    public void login(final UserCredentials credentials) throws Exception {
        postObject(credentials, "/api/login")
                .andExpect(status().isOk());
    }

    public void logout() throws Exception {
        delete("/api/login");
    }

    public DataSourceIdentity createFromResource(String content) throws Exception {
        return toDataSource(postResource(content, DATA_SOURCES));
    }

    public DataSourceIdentity createFromObject(DataSource dataSource) throws Exception {
        return toDataSource(postObject(dataSource, DATA_SOURCES));
    }

    public DataSourceIdentity copy(DataSourceCopyRequest request) throws Exception {
        return toDataSource(tryCopy(request));
    }

    public ResultActions tryCopy(DataSourceCopyRequest request) throws Exception {
        return postObject(request, DATA_SOURCE_COPY);
    }

    public MockHttpServletResponse edit(String dataSourceId, DataSourceAction... actions) throws Exception {
        return getResponse(tryEdit(dataSourceId, actions));
    }

    public MockHttpServletResponse edit(String dataSourceId, List<DataSourceAction> dataSourceActions) throws Exception {
        return getResponse(tryEdit(dataSourceId, dataSourceActions));
    }

    public ResultActions tryEdit(String dataSourceId, DataSourceAction... actions) throws Exception {
        return patch(newArrayList(actions), DATA_SOURCE_BY_ID, dataSourceId);
    }

    public ResultActions tryEdit(String dataSourceId, List<DataSourceAction> dataSourceActions) throws Exception {
        return patch(dataSourceActions, DATA_SOURCE_BY_ID, dataSourceId);
    }

    public DataSourceIdentity getById(String dataSourceId) throws Exception {
        return toDataSource(get(DATA_SOURCE_BY_ID, dataSourceId));
    }

    public DataSourceIdentity getById(String id, String version) throws Exception {
        return toDataSource(get(DATA_SOURCE_VERSION, id, version));
    }

    public DataSourceIdentity getApprovedById(String dataSourceId) throws Exception {
        return toDataSource(tryGetApprovedById(dataSourceId));
    }

    public ResultActions tryGetApprovedById(String dataSourceId) throws Exception {
        return get(DATA_SOURCE_APPROVED, dataSourceId, true);
    }

    public <T> List<T> getGroupsByContext(String contextId, GroupView view, Class<T> type) throws Exception {
        return toList(get(DATA_SOURCE_GROUPS, contextId, view), type);
    }

    public Records getRecords(String dataSourceId) throws Exception {
        return toObject(get(DATA_SOURCE_RECORDS, dataSourceId), Records.class);
    }

    public Records getRecords(String dataSourceId, String version) throws Exception {
        return toObject(get(DATA_SOURCE_VERSION_RECORDS, dataSourceId, version), Records.class);
    }

    public Records getRecords(String dataSourceId, boolean includeDeleted) throws Exception {
        return toObject(get(DATA_SOURCE_RECORDS_INCLUDE_DELETED, dataSourceId, includeDeleted), Records.class);
    }

    public Records getRecordsWithSpecifiedColumns(String dataSourceId, String columns) throws Exception {
        return toObject(get(DATA_SOURCE_RECORDS_WITH_SPECIFIED_COLUMNS, dataSourceId, columns), Records.class);
    }

    public DataSourceIdentity handleApproval(String dataSourceId, ApprovalRequest request) throws Exception {
        return toDataSource(tryHandleApproval(dataSourceId, request));
    }

    public ResultActions tryHandleApproval(String dataSourceId, ApprovalRequest request) throws Exception {
        return postObject(request, DATA_SOURCE_APPROVAL, dataSourceId);
    }

    public List<String> getVersions(String dataSourceId) throws Exception {
        return toList(get(DATA_SOURCE_VERSIONS, dataSourceId), String.class);
    }

//    public DataSourceIdentity getDataSourceIdentityByContextAndName(String contextId, String name, boolean approved)
//    throws
//            Exception {
//        return toDataSource(get(DATA_SOURCE_LATEST_BY_CONTEXT_AND_NAME_APPROVED, contextId, name, approved));
//    }

    public DataSourceIdentity getApprovedDataSourceIdentityByContextAndName(String contextId, String name, boolean approved)
    throws
            Exception {
        return toDataSource(get(DATA_SOURCE_LATEST_BY_CONTEXT_AND_NAME_APPROVED, contextId, name, approved));
    }

    public List<DataSourceIdentity> getIdentities() throws Exception {
        return toList(get(DATA_SOURCES), DataSourceIdentity.class);
    }

    public List<DataSourceIdentity> getAllByContextId(String contextId) throws Exception {
        return toList(get(DATA_SOURCE_BY_CONTEXT, contextId), DataSourceIdentity.class);
    }

    public DataSourceIdentity deleteById(String dataSourceId) throws Exception {
        return toDataSource(delete(DATA_SOURCE_BY_ID, dataSourceId));
    }

    private DataSourceIdentity toDataSource(ResultActions resultActions) throws Exception {
        return toObject(resultActions, DataSourceIdentity.class);
    }

    private DataSourceLabel toDataSourceLabel(ResultActions resultActions) throws Exception {
        return toObject(resultActions, DataSourceLabel.class);
    }

    public DataSourceIdentity getDataSourceByContextPathAndName(String contextPath, String dataSourceName) throws Exception {
        return toObject(get(DATA_SOURCE_BY_CONTEXT_PATH_AND_NAME, contextPath, dataSourceName), DataSourceIdentity.class);
    }

    public ResultActions tryGetDataSourceByContextPathAndName(String contextPath, String dataSourceName) throws Exception {
        return get(DATA_SOURCE_BY_CONTEXT_PATH_AND_NAME, contextPath, dataSourceName);
    }

    public <T>T getResponseAs(MockHttpServletResponse response, Class<T> type) throws Exception {
        return readEntity(response, type);
    }

    public DataSourceIdentity getDataSourceByLabel(String label, String contextId) throws Exception {
        return toObject(get(DATA_SOURCE_LABEL + "/" + label + "/contexts/" + contextId), DataSourceIdentity.class);
    }

    public List<DataSourceLabel> getDataSourceLabels(String query) throws Exception {
        return toList(get(DATA_SOURCE_LABEL + "?" + QUERY_PARAM, query), DataSourceLabel.class);
    }

    public ResultActions tryGetDataSourceByLabel(String label) throws Exception {
        return get(DATA_SOURCE_LABEL + "/" + label);
    }

    public DataSourceLabel createLabel(DataSourceLabel dataSourceLabel) throws Exception {
        return toDataSourceLabel(postObject(dataSourceLabel, DATA_SOURCE_LABEL));
    }

    public ResultActions tryCreateLabel(DataSourceLabel dataSourceLabel) throws Exception {
        return postObject(dataSourceLabel, DATA_SOURCE_LABEL);
    }

    public ResultActions deleteLabel(String label, final String contextId) throws Exception {
        return delete(DELETE_DATA_SOURCE_LABEL, label, contextId);
    }

    public Records getRecordsByIdAndVersionAndSpecifiedColumns(String dataSourceId, String version,
            String columns) throws Exception {
        return toObject(get(DATA_SOURCE_VERSION_RECORDS_WITH_SPECIFIED_COLUMNS, dataSourceId, version, columns),
                Records.class);
    }

    public Records getRecordsByIdAndVersionWithFilter(String dataSourceId, String version,
            String predicate) throws Exception {
        return toObject(get(DATA_SOURCE_VERSION_RECORDS_WITH_PREDICATE, dataSourceId, version, predicate),
                Records.class);
    }

    public Records getRecordsByIdAndVersionWithFilterAndSpecifiedColumns(String dataSourceId, String version,
            String columns, String predicate) throws Exception {
        return toObject(get(DATA_SOURCE_VERSION_RECORDS_WITH_COLUMNS_AND_PREDICATE, dataSourceId, version, columns,
                predicate),
                Records.class);
    }
}
