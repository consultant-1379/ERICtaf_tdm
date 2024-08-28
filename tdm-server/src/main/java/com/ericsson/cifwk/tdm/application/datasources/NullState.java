package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.model.DataSourceActionEntity;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;

/**
 * This is the special case implementation to remove the possibility of {@link NullPointerException}s
 */
class NullState implements DataSourceState {
    @Override
    public void transition() {
        throwUnsupportedException();
    }

    private static void throwUnsupportedException() {
        throw new UnsupportedOperationException("Special Null case, no operation allowed");
    }

    @Override
    public DataSourceActionEntity createDataSourceAction(final DataSourceIdentityEntity identity,
            final ApprovalRequest request) {
        throwUnsupportedException();
        return null;
    }
}
