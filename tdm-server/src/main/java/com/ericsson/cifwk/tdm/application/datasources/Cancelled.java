package com.ericsson.cifwk.tdm.application.datasources;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.APPROVAL_STATUS;

/**
 * When in the {@link com.ericsson.cifwk.tdm.api.model.ApprovalStatus#CANCELLED} state a datasource can only
 * transition to {@link com.ericsson.cifwk.tdm.api.model.ApprovalStatus#UNAPPROVED}.
 */
class Cancelled extends DataSourceStateTemplate implements DataSourceState {

    @Override
    public void transition() {
        values.put(APPROVAL_STATUS, UNAPPROVED.name());
    }

}
