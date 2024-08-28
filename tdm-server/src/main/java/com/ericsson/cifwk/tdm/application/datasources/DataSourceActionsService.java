package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.api.model.DataSourceAction;
import com.ericsson.cifwk.tdm.infrastructure.security.SecurityService;
import com.ericsson.cifwk.tdm.model.DataRecordEntity;
import com.ericsson.cifwk.tdm.model.DataSourceActionEntity;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import com.ericsson.cifwk.tdm.model.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Map;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.model.DataSourceActionEntity.identityInitialCreate;
import static com.ericsson.cifwk.tdm.presentation.exceptions.NotFoundException.verifyFound;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySortedMap;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingLong;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;

@Service
public class DataSourceActionsService {

    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Autowired
    private DataSourceActionRepository dataSourceActionRepository;

    @Autowired
    private DataRecordRepository dataRecordRepository;

    @Autowired
    private DataSourceHelper dataSourceHelper;

    @Autowired
    private VersioningService versioningService;

    @Autowired
    private SecurityService securityService;

    public void insertInitialAction(DataSourceIdentityEntity persistedIdentity) {
        DataSourceActionEntity action = identityInitialCreate(
                persistedIdentity.getId(),
                persistedIdentity.createValueMap(),
                persistedIdentity.getVersion()
        );

        action.setAuditData(persistedIdentity.getCreatedBy(), persistedIdentity.getCreateTime());
        dataSourceActionRepository.insert(action);
    }

    public void applyActionsAndSave(DataSourceIdentityEntity identity, List<DataSourceAction> actions) {
        SortedMap<String, DataRecordEntity> idRecordsMap =
                dataRecordRepository.find(identity.getId(), emptyList(), emptyList());

        List<DataSourceActionEntity> actionEntities = getDataSourceActionEntities(actions);
        versioningService.incrementVersion(identity, actionEntities);

        AtomicInteger latestOrder = dataSourceActionRepository.findLatestOrder(idRecordsMap.keySet());

        AppliedDataSourceActionAccumulator accumulator = new AppliedDataSourceActionAccumulator();
        accumulator.setOrder(new AtomicInteger(latestOrder.incrementAndGet()));
        actionEntities.forEach(e -> e.apply(identity, idRecordsMap, accumulator));

        dataSourceHelper.validateIdentityNameAndContext(identity);

        for (Iterator<Map.Entry<String, DataRecordEntity>> iter = idRecordsMap.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<String, DataRecordEntity> entry = iter.next();
            entry.getValue().getValues().entrySet().removeIf(e ->e.getValue().equals(""));
        }

        dataSourceRepository.update(identity);
        dataRecordRepository.update(idRecordsMap);

        accumulator.updateRecordActionsParentIds(identity.getId(), idRecordsMap);
        accumulator.updateAuditData(securityService.getCurrentUser().getUsername());
        if (!accumulator.getActionEntities().isEmpty()) {
            dataSourceActionRepository.insert(accumulator.getActionEntities());
        }
    }

    private List<DataSourceActionEntity> getDataSourceActionEntities(List<DataSourceAction> actions) {
        List<DataSourceAction> columnOrderActions = actions.stream()
                .filter(action -> Objects.equals(DataSourceActionType.COLUMN_ORDER_CHANGE.toString(), action.getType()))
                .collect(toList());

        Optional<DataSourceActionEntity> columnActionEntity = Optional.ofNullable(toAction(columnOrderActions));

        List<DataSourceAction> otherActions = actions.stream()
                .filter(action -> !Objects.equals(DataSourceActionType.COLUMN_ORDER_CHANGE.toString(),
                        action.getType()))
                .collect(toList());

        List<DataSourceActionEntity> actionEntities = toActionEntities(otherActions);

        if (columnActionEntity.isPresent()) {
            actionEntities.add(columnActionEntity.get());
        }
        return actionEntities;
    }

    protected void applyApprovalStatusChange(final DataSourceIdentityEntity identity, final ApprovalRequest request) {
        DataSourceState state = getCurrentState(identity, request);
        state.transition();
        DataSourceActionEntity action = state.createDataSourceAction(identity, request);
        updateAction(identity, action);

        // This check is required so that the un-approval of an approved datasource does not affect the subsequent
        // approvals. Only if the version to unapprove is not the latest version
        if (!UNAPPROVED.equals(request.getStatus()) || dataSourceVersionIsLatest(identity)) {
            dataSourceRepository.update(identity);
        }
        dataSourceActionRepository.insert(action);
    }

    private boolean dataSourceVersionIsLatest(final DataSourceIdentityEntity identity) {
        final DataSourceIdentityEntity latestVersion = dataSourceRepository.findById(identity.getId());
        return identity.getVersion().isTheSameNumericVersion(latestVersion.getVersion());
    }

    private void updateAction(final DataSourceIdentityEntity identity, final DataSourceActionEntity action) {
        action.apply(identity, emptySortedMap(), new AppliedDataSourceActionAccumulator());
        action.setOrder(approvalStatusOrder(identity));
        action.setAuditData(securityService.getCurrentUser().getUsername(), new Date());
    }

    void createNewVersion(final DataSourceIdentityEntity identity, final ApprovalRequest request,
            final DataSourceAction dataSourceAction) {
        final DataSourceState state = new Approved(identity, request, Optional.of(dataSourceAction));
        state.transition();
        final DataSourceActionEntity action = state.createDataSourceAction(identity, request);
        updateAction(identity, action);
        dataSourceRepository.update(identity);
        dataSourceActionRepository.insert(action);
    }

    private DataSourceState getCurrentState(final DataSourceIdentityEntity identity, final ApprovalRequest request) {
        final DataSourceState state;
        switch (identity.getApprovalStatus()) {
            case UNAPPROVED:
                state = new UnApproved(identity, request, securityService);
                break;
            case PENDING:
                state = new Pending(identity, request);
                break;
            case APPROVED:
                state = new Approved(identity, request, Optional.empty());
                break;
            case REJECTED:
                state = new Rejected();
                break;
            case CANCELLED:
                state = new Cancelled();
                break;
            default:
                state = new NullState();
                break;
        }
        return state;
    }

    private int approvalStatusOrder(DataSourceIdentityEntity identity) {
        return dataSourceActionRepository.numberOfActions(identity.getId(), identity.getVersion());
    }

    List<DataSourceActionEntity> toActionEntities(List<DataSourceAction> actions) {
        return actions.stream()
                .sorted(comparing(DataSourceAction::getLocalTimestamp))
                .map(DataSourceActionEntity::actionEntity)
                .collect(toList());
    }

    DataSourceActionEntity toAction(List<DataSourceAction> actions) {

        if (actions.isEmpty()) {
            return null;
        }

        SortedMap<String, Object> values = new TreeMap<>();
        actions.forEach(action -> values.put(action.getKey(), action.getNewValue()));
        return DataSourceActionEntity.actionEntities(actions.get(0), values);
    }

    public Optional<Version> findLatestApprovedVersion(String dataSourceId) {
        List<Version> versions = getActualVersions(dataSourceId);
        return versions.stream()
                .sorted(reverseOrder())
                .filter(version -> !version.isSnapshot())
                .findFirst();
    }

    public List<Version> getActualVersions(String dataSourceId) {
        List<Version> versions = dataSourceActionRepository.findVersions(newHashSet(dataSourceId));
        List<DataSourceActionEntity> actions = dataSourceActionRepository.findByDataSourceId(dataSourceId);

        return versions.stream()
                .map(version -> verifyFound(getLastActualAction(version, actions)).getVersion())
                .distinct()
                .collect(toList());
    }

    public List<DataSourceActionEntity> getHistory(String dataSourceId)  {
        return dataSourceActionRepository.findHistory(dataSourceId);
    }

    private static Optional<DataSourceActionEntity> getLastActualAction(Version version,
                                                                 List<DataSourceActionEntity> actions) {
        return actions.stream()
                .filter(Objects::nonNull)
                .filter(action -> version.isTheSameNumericVersion(action.getVersion()))
                .max(comparingLong(action -> {
                    if (action.getCreateTime() != null) {
                        return action.getCreateTime().getTime();
                    } else {
                        return 1;
                    }
                }));
    }
}
