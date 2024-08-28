package com.ericsson.cifwk.tdm.presentation.controllers;

import static com.ericsson.cifwk.tdm.presentation.exceptions.NotFoundException.verifyFound;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.ericsson.cifwk.tdm.api.model.Records;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.api.model.DataRecord;
import com.ericsson.cifwk.tdm.api.model.DataSource;
import com.ericsson.cifwk.tdm.api.model.DataSourceAction;
import com.ericsson.cifwk.tdm.api.model.DataSourceCopyRequest;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.api.model.DataSourceLabel;
import com.ericsson.cifwk.tdm.api.model.FilterCriteria;
import com.ericsson.cifwk.tdm.api.model.GroupView;
import com.ericsson.cifwk.tdm.application.datasources.DataSourceCopyService;
import com.ericsson.cifwk.tdm.application.datasources.DataSourceGroupService;
import com.ericsson.cifwk.tdm.application.datasources.DataSourceService;
import com.ericsson.cifwk.tdm.model.DataRecordEntity;
import com.ericsson.cifwk.tdm.model.History;
import com.ericsson.cifwk.tdm.model.Lock;
import com.ericsson.cifwk.tdm.presentation.validation.DataSourceCopyRequestValid;

import springfox.documentation.annotations.ApiIgnore;

@Validated
@RestController
@RequestMapping("/api/datasources")
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private DataSourceCopyService dataSourceCopyService;

    @Autowired
    private DataSourceGroupService dataSourceGroupService;

    /**
     * <p>Finds all datasources. If contextId is provided, it
     * finds all datasources matching that contextId and all the parent contextId's of that context</p>
     *
     * @param contextId optional contextId of the datasource
     * @return list of datasources
     */
    @GetMapping
    public List<DataSourceIdentity> getDataSourceIdentities(@RequestParam(value = "context", required = false)
                                                                    String contextId) {
        if (contextId == null) {
            return dataSourceService.findAll();
        } else {
            return dataSourceService.findByAncestorContextId(contextId);
        }
    }

    /**
     * <p>Finds the data source matching the contextPath and dataSourceName</p>
     *
     * @param contextPath    required contextPath of the datasource
     * @param dataSourceName required dataSourceName of the datasource
     * @return data source
     */
    @GetMapping(params = {"contextPath", "dataSourceName"})
    public DataSourceIdentity getDatasourceByContextPathAndName(
            @RequestParam(value = "contextPath")
                    String contextPath,
            @RequestParam(
                    value = "dataSourceName")
                    String dataSourceName) {

        return verifyFound(dataSourceService.findByContextPathAndName(contextPath, dataSourceName));
    }

    /**
     * <p>Finds the latest datasource by contextId and name. If approved value is true,
     * then it returns the latest approved datasource, otherwise it returns the latest datasource,
     * regardless of the approval status</p>
     *
     * @param contextId required contextId of the datasource
     * @param name      required name of the datasource
     * @param approved  optional boolean for approval status. For 'APPROVED', value must be true. For all other
     *                  approval status's, value is false. default value is true
     * @return single datasource
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/latest")
    public DataSourceIdentity getDataSourceIdentityByContextAndName(@RequestParam(value = "context")
                                                                            String contextId,
                                                                    @RequestParam(value = "name") String name,
                                                                    @RequestParam(name = "approved", required = false,
                                                                            defaultValue = "true") boolean approved) {
        Optional<DataSourceIdentity> identity;
        if (approved) {
            identity = dataSourceService.findLatestApprovedByContextIdAndName(contextId, name);
        } else {
            identity = dataSourceService.findByContextIdAndName(contextId, name);
        }
        return verifyFound(identity);
    }

    @GetMapping("/groups")
    public Collection getGroupsByContext(@RequestParam("context") String contextId,
                                         @RequestParam(value = "view", defaultValue = "TREE") GroupView view) {
        if (GroupView.TREE.equals(view)) {
            return dataSourceGroupService.getGroupTreeByContextId(contextId);
        } else {
            return dataSourceGroupService.getGroupsByContextId(contextId);
        }
    }

    @ResponseStatus(code = HttpStatus.CREATED)
    @PostMapping("/copy")
    public DataSourceIdentity copyDataSource(@RequestBody
                                             @Valid
                                             @DataSourceCopyRequestValid DataSourceCopyRequest dataSourceCopyRequest) {
        return dataSourceCopyService.copy(dataSourceCopyRequest);
    }

    @ResponseStatus(code = HttpStatus.CREATED)
    @PostMapping
    public DataSourceIdentity createDataSource(@RequestBody DataSource dataSource) {
        return dataSourceService.create(dataSource);
    }

    @GetMapping("/{dataSourceId}")
    public ResponseEntity<DataSourceIdentity> getDataSourceById(@PathVariable("dataSourceId") String dataSourceId,
                                                                @RequestParam(name = "approved", defaultValue = "false")
                                                                        boolean approved) {
        Optional<DataSourceIdentity> identity;
        if (approved) {
            identity = dataSourceService.findApprovedById(dataSourceId);
        } else {
            identity = dataSourceService.findById(dataSourceId);
        }
        return new ResponseEntity<>(verifyFound(identity), HttpStatus.OK);
    }

    @DeleteMapping("/{dataSourceId}")
    public ResponseEntity<DataSourceIdentity> deleteDataSource(@PathVariable("dataSourceId") String dataSourceId) {
        Optional<DataSourceIdentity> identity = dataSourceService.delete(dataSourceId);
        if (identity.isPresent()) {
            return new ResponseEntity<>(identity.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{dataSourceId}/versions")
    public List<String> getDataSourceVersions(@PathVariable("dataSourceId") String dataSourceId) {
        return dataSourceService.getVersions(dataSourceId);
    }

    @GetMapping("/{dataSourceId}/history")
    public List<History> getDataSourceHistory(@PathVariable("dataSourceId") String dataSourceId) {
        return dataSourceService.getHistory(dataSourceId);
    }

    @GetMapping("/{dataSourceId}/records")
    public Records getRecords(@PathVariable("dataSourceId") String dataSourceId,
                @RequestParam(value = "columns", required = false, defaultValue = StringUtils.EMPTY) String columns,
                FilterCriteria filterCriteria,
                @RequestHeader(value = "User-Agent", defaultValue = "Rest") String userAgent,
                @RequestParam(value = "includeDeleted", required = false, defaultValue = "false")
                                          boolean includeDeleted) {

        dataSourceService.addRequestToMetrics(dataSourceId, userAgent);
        return dataSourceService.findDataRecords(dataSourceId, filterCriteria.getPredicates(), columns, includeDeleted);
    }

    @PatchMapping("/{dataSourceId}")
    public DataSourceIdentity edit(@PathVariable("dataSourceId") String dataSourceId,
                                   @RequestBody @Size(min = 1) List<DataSourceAction> actions) {
        return dataSourceService.edit(dataSourceId, actions);
    }

    /**
     * Change the Data Source approval status according to the {@code request}.
     *
     * @see com.ericsson.cifwk.tdm.api.model.ApprovalStatus
     */
    @ResponseStatus(code = HttpStatus.OK)
    @PostMapping("/{dataSourceId}/approval")
    public DataSourceIdentity approval(@PathVariable("dataSourceId") String dataSourceId,
                                       @Valid @RequestBody ApprovalRequest request) {
        return dataSourceService.handleApproval(dataSourceId, request);
    }

    @GetMapping("/{dataSourceId}/versions/{version:.+}")
    public DataSourceIdentity getDataSourceByIdAndVersion(@PathVariable("dataSourceId") String dataSourceId,
                                                          @PathVariable("version") String version) {
        return dataSourceService.findDataSourceByVersion(dataSourceId, version);
    }

    @GetMapping("/{dataSourceId}/versions/{version}/records")
    public Records getDataSourceByIdAndVersion(@PathVariable("dataSourceId") String dataSourceId,
            @PathVariable("version") String version,
            @RequestParam(value = "columns", required = false, defaultValue = StringUtils.EMPTY) String columns,
            FilterCriteria filterCriteria, @RequestHeader(value = "User-Agent", defaultValue = "Rest") String userAgent,
            @RequestParam(value = "includeDeleted", required = false, defaultValue = "false") boolean includeDeleted) {

        dataSourceService.addRequestToMetrics(dataSourceId, userAgent);
        return dataSourceService.findRecordsByVersion(dataSourceId, version, filterCriteria.getPredicates(), columns,
                includeDeleted);
    }

    /**
     * <p>Finds all Data source labels matching the query, if query is absent returns all labels found until
     * limit is reached.</p>
     * @param query optional string to search for, will return all if not supplied
     * @param limit optional limit on number of labels returned, default value is 20
     * @return list of Data Source labels
     */
    @GetMapping(value = "/labels")
    public Collection<DataSourceLabel> getDataSourceLabels(
            @RequestParam(value = "query", required = false, defaultValue = "?") String query,
            @RequestParam(value = "limit", required = false, defaultValue = "20") String limit) {

        return dataSourceService.findDataSourceLabels(query, limit);
    }

    /**
     * <p>Finds the datasource by label.</p>
     * @param label label of the data source
     * @param contextId the id of the context it resides in
     * @return single datasource
     */
    @GetMapping(value = "/labels/{label}/contexts/{contextId}")
    public DataSourceIdentity getDataSourceFromLabel(@PathVariable("label") String label,
            @PathVariable("contextId") String contextId) {
        return dataSourceService.findDataSourceFromLabel(label, contextId);
    }

    /**
     * <p>Adds a new Datasource label if not found in database or updates an existing label.</p>
     * @param dataSourceLabel the dataSourceLabel to add or update
     * @return dataSourceLabel
     */
    @PostMapping(value = "/labels")
    public DataSourceLabel addLabel(@Valid @RequestBody DataSourceLabel dataSourceLabel) {
        return dataSourceService.updateLabel(dataSourceLabel);
    }

    /**
     * Deletes a specified label or returns 404 if not found.
     * @param label the label to delete.
     * @param contextId the id of the context it resides in
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/labels/{label}/contexts/{contextId}")
    public void deleteLabel(@PathVariable("label") String label, @PathVariable("contextId") String contextId) {
        dataSourceService.deleteLabel(label, contextId);
    }

    /**
     *
     * @param dataSourceId
     * @param version
     * @return
     * @deprecated
     */
    @ApiIgnore
    @Deprecated
    @GetMapping("/{dataSourceId}/versions/{version}/locks")
    public Collection<DataRecord> getLocksForVersion(@PathVariable("dataSourceId") String dataSourceId,
                                                     @PathVariable("version") String version) {
        // TODO: remove it
        return Collections.emptyList();
    }

    /**
     *
     * @param dataSourceId
     * @param dataSource
     * @return
     * @deprecated
     */
    @ApiIgnore
    @Deprecated
    @PatchMapping("/{dataSourceId}/records")
    public DataSourceIdentity appendRecordsToDataSource(@PathVariable("dataSourceId") String dataSourceId,
                                                        @RequestBody DataRecordEntity dataSource) {
        return null; // TODO: remove it
    }

    /**
     *
     * @param dataSourceId
     * @return
     * @deprecated
     */
    @ApiIgnore
    @Deprecated
    @DeleteMapping("/{dataSourceId}/records")
    public DataSourceIdentity deleteRecordsFromDataSource(@PathVariable("dataSourceId") String dataSourceId) {
        return null; // TODO: remove it
    }

    /**
     *
     * @param dataSourceId
     * @return
     * @deprecated
     */
    @ApiIgnore
    @Deprecated
    @GetMapping("/{dataSourceId}/locks")
    public List<Lock> getLocks(@PathVariable("dataSourceId") String dataSourceId) {
        return Collections.emptyList();
    }
}
