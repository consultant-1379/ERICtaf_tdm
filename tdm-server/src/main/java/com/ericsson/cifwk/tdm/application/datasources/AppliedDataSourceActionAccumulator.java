package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.model.DataRecordEntity;
import com.ericsson.cifwk.tdm.model.DataSourceActionEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ericsson.cifwk.tdm.model.DataSourceActionEntity.copyFrom;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 14/04/2016
 */
public class AppliedDataSourceActionAccumulator {
    AtomicInteger order = new AtomicInteger();
    List<DataSourceActionEntity> actionEntities = new ArrayList<>();

    void addAction(DataSourceActionEntity action, String parentId) {
        DataSourceActionEntity copy = copyFrom(action);
        copy.setOrder(order.getAndIncrement());
        copy.setParentId(parentId);

        actionEntities.add(copy);
    }

    boolean removePreviousActionIfExists(DataSourceActionType type, String parentId, String actionKey) {
        return actionEntities.removeIf(a -> a.getType() == type &&
                Objects.equals(parentId, a.getParentId()) &&
                (actionKey == null || a.getValues().containsKey(actionKey)));
    }

    List<DataSourceActionEntity> getActionEntities() {
        return actionEntities;
    }

    public void updateRecordActionsParentIds(String dataSourceId, Map<String, DataRecordEntity> idRecordsMap) {
        actionEntities.stream()
                .filter(action -> notDataSourceAction(dataSourceId, action))
                .filter(action -> notDeleteAction(action))
                .forEach(action -> {
                    String recordId = getPersistedRecordId(idRecordsMap, action);
                    action.setParentId(recordId);
                });
    }

    private static boolean notDeleteAction(final DataSourceActionEntity action) {
        return !action.getType().equals(DataSourceActionType.RECORD_DELETE);
    }

    private static String getPersistedRecordId(Map<String, DataRecordEntity> idRecordsMap, DataSourceActionEntity
            action) {
        return idRecordsMap.get(action.getParentId()).getId();
    }

    private static boolean notDataSourceAction(String dataSourceId, DataSourceActionEntity a) {
        return !a.getParentId().equals(dataSourceId);
    }

    public void updateAuditData(String userId) {
        actionEntities.forEach(action -> action.setAuditData(userId, new Date()));
    }

    public void setOrder(AtomicInteger order) {
        this.order = order;
    }
}
