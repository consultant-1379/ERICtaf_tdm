package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.model.DataSourceActionEntity;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;

/**
 * In TDM data sources can be in one of many states and those states are limited in the states they can transition to.
 * This interface separates the states and their transitions so that it is easy to visualize and updates are isolated
 * to the state in question.
 */
interface DataSourceState {

    /**
     * Call this method to transition to one of the possible states.
     */
    void transition();

    /**
     * Create the action for the state transition action in question.
     * The values for the transition are populated in the above methods.
     * This method creates the action with the values.
     * @param identity the datasource to transition.
     * @param request the approval request being made
     * @return the action to apply to the datasource.
     */
    DataSourceActionEntity createDataSourceAction(DataSourceIdentityEntity identity, ApprovalRequest request);
}
