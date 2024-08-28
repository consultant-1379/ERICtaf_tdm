package com.ericsson.cifwk.tdm.presentation.controllers;

import static com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder.aRequestApproval;
import static com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder.anApprove;
import static com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder.anUnApprove;
import static com.ericsson.cifwk.tdm.api.model.DataSourceAction.DataSourceActionBuilder.aDataSourceAction;
import static com.ericsson.cifwk.tdm.api.model.UserBuilder.anUser;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType.IDENTITY_APPROVAL_STATUS;
import static com.ericsson.cifwk.tdm.configuration.MockRestRepositoriesConfiguration.MANAGER_USER;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder;
import com.ericsson.cifwk.tdm.api.model.DataRecord;
import com.ericsson.cifwk.tdm.api.model.DataSourceAction;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.api.model.Records;
import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType;
import com.ericsson.cifwk.tdm.presentation.controllers.client.DataSourceControllerClient;

public abstract class AbstractControllerITest {
    @Autowired
    protected DataSourceControllerClient dataSourceControllerClient;

    protected List<DataRecord> deleteARecord(final String dataSourceId, final String snapshotVersion) throws Exception {
        Records records = dataSourceControllerClient.getRecords(dataSourceId, snapshotVersion);

        DataSourceAction deleteRecord = aDataSourceAction()
                .withId(records.getData().get(0).getId())
                .withType(DataSourceActionType.RECORD_DELETE.name())
                .withVersion(snapshotVersion)
                .withLocalTimestamp(System.currentTimeMillis())
                .build();

        dataSourceControllerClient.edit(dataSourceId, deleteRecord, newVersionAction(dataSourceId, snapshotVersion));
        return records.getData();
    }

    /**
     * Taken from resources: /contexts/users-contexts.json
     */
    protected User tceManager() {
        return anUser()
                .withId(1130L)
                .withUsername(MANAGER_USER)
                .build();
    }

    protected DataSourceIdentity createDataSource() throws Exception {
        return dataSourceControllerClient.createFromResource("datasources/planets.json");
    }

    protected DataSourceIdentity requestApproval(String dataSourceId, final User approver) throws Exception {
        return handleApprovalExpectingSuccess(dataSourceId, aRequestApproval(approver));
    }

    protected DataSourceIdentity handleApprovalExpectingSuccess(String dataSourceId,
            ApprovalRequestBuilder requestBuilder) throws Exception {
        return dataSourceControllerClient.handleApproval(dataSourceId, requestBuilder.build());
    }

    protected DataSourceIdentity approve(String dataSourceId) throws Exception {
        return handleApprovalExpectingSuccess(dataSourceId, anApprove());
    }

    protected DataSourceIdentity unapprove(String dataSourceId) throws Exception {
        return handleApprovalExpectingSuccess(dataSourceId, anUnApprove());
    }

    protected DataSourceAction newVersionAction(final String dataSourceId, final String version) {
        return aDataSourceAction()
                .withVersion(version)
                .withId(dataSourceId)
                .withType(IDENTITY_APPROVAL_STATUS.name())
                .withKey("approvalStatus")
                .withNewValue("UNAPPROVED")
                .build();
    }
}
