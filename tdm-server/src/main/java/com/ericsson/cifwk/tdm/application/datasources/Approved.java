package com.ericsson.cifwk.tdm.application.datasources;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.APPROVED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceService.PROFILE;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.APPROVAL_STATUS;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.APPROVER;

import java.util.Optional;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.api.model.DataSourceAction;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import com.ericsson.cifwk.tdm.model.Version;

/**
 * When in the {@link com.ericsson.cifwk.tdm.api.model.ApprovalStatus#APPROVED} state a datasource can only
 * transition to {@link com.ericsson.cifwk.tdm.api.model.ApprovalStatus#UNAPPROVED}.
 * This is a special case though, as the move to unapproved state can be for two reason.
 * 1. The approved datasource has been unapproved
 * 2. A new version of the datasource has been created.
 */
class Approved extends DataSourceStateTemplate implements DataSourceState {

    private final DataSourceIdentityEntity dataSource;
    private final Optional<DataSourceAction> dataSourceAction;
    private final ApprovalRequest request;

    Approved(final DataSourceIdentityEntity dataSource, final ApprovalRequest request, final Optional<DataSourceAction>
            dataSourceAction) {
        super();
        this.dataSource = dataSource;
        this.dataSourceAction = dataSourceAction;
        this.request = request;
    }

    @Override
    public void transition() {
        if (dataSourceAction.isPresent()) {
            createNewVersion(dataSourceAction.get());
        } else {
            unApprove();
        }
    }

    private void unApprove() {
        values.put(APPROVAL_STATUS, UNAPPROVED.name());
        values.put(APPROVER, request.getApprover());
        dataSource.getVersion().setSnapshot(true);
    }

    private void createNewVersion(final DataSourceAction dataSourceAction) {
        dataSource.setVersion(new Version(dataSourceAction.getVersion()));

        if (PROFILE != null && "customer".equalsIgnoreCase(PROFILE)) {
            dataSource.setApprovalStatus(APPROVED);
            dataSource.setApprover(dataSource.getApprover());
            dataSource.getVersion().setSnapshot(false);
            dataSource.setApprover(dataSource.getApprover());

            values.put(APPROVAL_STATUS, APPROVED);
            values.put(APPROVER, dataSource.getApprover());
        } else {
            dataSource.setApprovalStatus(UNAPPROVED);
            dataSource.setReviewRequester("");
            dataSource.setApprover("");

            values.put(APPROVAL_STATUS, UNAPPROVED);
            values.put(APPROVER, "");

        }


    }
}
