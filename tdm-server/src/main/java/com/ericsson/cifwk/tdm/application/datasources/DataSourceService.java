package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder;
import com.ericsson.cifwk.tdm.api.model.ApprovalStatus;
import com.ericsson.cifwk.tdm.api.model.Context;
import com.ericsson.cifwk.tdm.api.model.DataRecord;
import com.ericsson.cifwk.tdm.api.model.DataSource;
import com.ericsson.cifwk.tdm.api.model.DataSourceAction;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.api.model.DataSourceLabel;
import com.ericsson.cifwk.tdm.api.model.DataSourceMetricsObject;
import com.ericsson.cifwk.tdm.api.model.Meta;
import com.ericsson.cifwk.tdm.api.model.Records;
import com.ericsson.cifwk.tdm.api.model.StatisticsObject;
import com.ericsson.cifwk.tdm.application.contexts.ContextService;
import com.ericsson.cifwk.tdm.application.locks.LockRepository;
import com.ericsson.cifwk.tdm.application.notification.NotificationService;
import com.ericsson.cifwk.tdm.application.notification.NotificationType;
import com.ericsson.cifwk.tdm.infrastructure.security.SecurityService;
import com.ericsson.cifwk.tdm.model.DataRecordEntity;
import com.ericsson.cifwk.tdm.model.DataSourceActionEntity;
import com.ericsson.cifwk.tdm.model.DataSourceExecution;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import com.ericsson.cifwk.tdm.model.DataSourceLabelEntity;
import com.ericsson.cifwk.tdm.model.DataSourceMetricEntity;
import com.ericsson.cifwk.tdm.model.History;
import com.ericsson.cifwk.tdm.model.Lock;
import com.ericsson.cifwk.tdm.model.RecordPredicate;
import com.ericsson.cifwk.tdm.model.Version;
import com.google.common.annotations.VisibleForTesting;
import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFactory;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.APPROVED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.REJECTED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.api.model.UserAgent.getRequestType;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType.IDENTITY_APPROVAL_STATUS;
import static com.ericsson.cifwk.tdm.presentation.exceptions.NotFoundException.verifyFound;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newTreeMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.reverseOrder;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Service
public class DataSourceService {

    static final String ERR_EDIT_NO_ACTIONS = "At least one action must be applied on data source";
    static final String ERR_EDIT_PENDING_APPROVAL = "Cannot edit a Data Source with a pending approval request";
    static final String ERR_EDIT_VERSION_MISMATCH = "Current data source version is less than previous version";
    static final String ERR_EDIT_VERSION_SNAPSHOT = "Current data source version is a snapshot and cannot be updated";
    static final String ERR_LABEL_NAME_EMPTY = "Data source label name is empty";
    static final String COLUMN_NAMES_SEPARATOR = ",";
    static final int PREVIOUS_MONTHS = 6;
    public static final String PROFILE = System.getenv("SPRING_PROFILES_ACTIVE");


    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Autowired
    private DataSourceHelper dataSourceHelper;

    @Autowired
    private DataRecordRepository dataRecordRepository;

    @Autowired
    private DataSourceLabelRepository dataSourceLabelRepository;

    @Autowired
    private DataSourceActionRepository dataSourceActionRepository;

    @Autowired
    private LockRepository lockRepository;

    @Autowired
    private DataSourceMetricRepository dataSourceMetricRepository;

    @Autowired
    @VisibleForTesting
    FilterToPredicateConverter filterToPredicateConverter;

    @Autowired
    @VisibleForTesting
    MapperFactory mapperFactory;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ContextService contextService;

    @Autowired
    private DataSourceActionsService dataSourceActionsService;

    @Autowired
    private ApprovalRequestValidationService approvalRequestValidationService;

    public DataSourceIdentity create(DataSource dataSource) {
        securityService.validateUserAuthorization(dataSource.getIdentity().getContextId());
        DataSourceIdentity persistedIdentity = persistIdentity(dataSource);
        dataSource.setIdentity(persistedIdentity);
        persistRecordsWithIdentityReference(dataSource);
        return persistedIdentity;
    }

    public Optional<DataSourceIdentity> delete(String dataSourceId) {
        DataSourceIdentityEntity identity = dataSourceRepository.findById(dataSourceId);

        if (identity == null) {
            return Optional.empty();
        }
        if (identity.getApprovalStatus() == ApprovalStatus.PENDING) {
            throw new IllegalArgumentException("Cannot delete a datasource that is in PENDING status");
        }
        securityService.validateUserAuthorization(identity.getContextId());
        dataSourceRepository.removeById(identity.getId());
        notificationService.notifyUsers(identity, NotificationType.DELETION);
        List<DataSourceLabelEntity> dataSourceLabels =
                dataSourceLabelRepository.findAllByDataSourceId(identity.getId());
        dataSourceLabels.forEach(item -> dataSourceLabelRepository.removeById(item.getId()));

        return of(toDataSourceIdentity(identity));

    }

    public List<DataSourceIdentity> findAll() {
        return toIdentities(dataSourceRepository.findAll());
    }

    public List<DataSourceIdentity> findByAncestorContextId(String contextId) {
        List<String> contextsWithSubContexts = contextService.findAllParentContextIds(contextId);
        return toIdentities(dataSourceRepository.findByContextIds(contextsWithSubContexts));
    }

    public void addRequestToMetrics(String dataSourceId, String userAgent) {
        Optional<DataSourceIdentityEntity> dataSourceIdentityEntity =
                ofNullable(dataSourceRepository.findById(dataSourceId));

        if (dataSourceIdentityEntity.isPresent()) {
            DataSourceIdentityEntity dataSource = dataSourceIdentityEntity.get();
            DataSourceMetricEntity dataSourceMetricEntity = new DataSourceMetricEntity();
            dataSourceMetricEntity.setDataSourceName(dataSource.getName());
            dataSourceMetricEntity.setDataSourceId(dataSource.getId());
            dataSourceMetricEntity.setContextName(dataSource.getContext());
            dataSourceMetricEntity.setUserAgent(getRequestType(userAgent));
            dataSourceMetricEntity.setCreatedAt(new Date());

            dataSourceMetricRepository.insert(dataSourceMetricEntity);
        }
    }

    public List<StatisticsObject> getDataSourceRequests(String type) {
        DateTime dateTime = new DateTime(new Date());
        List<DataSourceMetricsObject> byMonth =
                dataSourceMetricRepository.findByMonthAndAgent(
                        dateTime.minusMonths(PREVIOUS_MONTHS).toDate(),
                        type);
        return byMonth.stream()
                .map(item -> new StatisticsObject(item.getContext() + " - "
                        + item.getName() + " - " + item.getUserAgent(), item.getTotal()))
                .collect(toList());
    }

    public Optional<DataSourceIdentity> findLatestApprovedByContextIdAndName(String contextId, String name) {
        Optional<DataSourceIdentity> dataSourceIdentity  = findByContextIdAndName(contextId, name);
        if (dataSourceIdentity.isPresent()) {
            return toLatestApprovedDataSourceIdentity(dataSourceIdentity.get());
        }
        return dataSourceIdentity;
    }

    private Optional<DataSourceIdentity> toLatestApprovedDataSourceIdentity(DataSourceIdentity dataSourceIdentity) {
        if (dataSourceIdentity.isApproved()) {
            return of(dataSourceIdentity);
        } else {
            return findApprovedById(dataSourceIdentity.getId());
        }
    }

    public Optional<DataSourceIdentity> findByContextIdAndName(String contextId, String name) {
        DataSourceIdentityEntity entities = dataSourceRepository.findByContextIdAndName(contextId, name);
        return ofNullable(entities).map(this::toDataSourceIdentity);
    }

    public Optional<DataSourceIdentity> findById(String dataSourceId) {
        Optional<DataSourceIdentityEntity> byId = ofNullable(dataSourceRepository.findById(dataSourceId));
        Optional<DataSourceIdentity> dataSourceIdentity = byId.map(this::toDataSourceIdentity);
        addLabelIfExists(dataSourceIdentity);

        return dataSourceIdentity;
    }

    public Optional<DataSourceIdentity> findApprovedById(String dataSourceId) {
        Optional<Version> version = dataSourceActionsService.findLatestApprovedVersion(dataSourceId);
        return ofNullable(findDataSourceByVersion(dataSourceId, verifyFound(version).toString()));
    }

    public Optional<DataSourceIdentity> findByContextPathAndName(String contextPath, String dataSourceName) {
        Context context = contextService.findByPath(contextPath);
        return findIdentitiesByContextIdAndName(context.getId(), dataSourceName);
    }

    private Optional<DataSourceIdentity> findIdentitiesByContextIdAndName(String contextId, String dataSourceName) {
        DataSourceIdentityEntity dataSourceIdentityEntity =
                dataSourceRepository.findByContextIdAndName(contextId, dataSourceName);
        return ofNullable(toDataSourceIdentity(dataSourceIdentityEntity));
    }

    public Records findDataRecords(String dataSourceId, List<String> predicates, String columns,
            final boolean includedDeleted) {
        DataSourceIdentityEntity dataSource = verifyFound(dataSourceRepository.findById(dataSourceId));
        String dataSourceVersion = dataSource.getVersion().toString();
        return findRecordsByVersion(dataSourceId, dataSourceVersion, predicates, columns, includedDeleted);
    }

    private static List<DataRecord> removeDeleted(final Collection<DataRecord> allRecordsByVersion) {
        return allRecordsByVersion.stream().filter(dataRecord -> !dataRecord.isDeleted()).collect(toList());
    }

    public Records findRecordsByVersion(String dataSourceId, String version) {
        return findRecordsByVersion(dataSourceId, version, new ArrayList<>(), StringUtils.EMPTY, false);
    }

    public Records findRecordsByVersion(String dataSourceId, String version,
                                        List<String> predicates, String columns,
                                        final boolean includedDeleted) {
        DataSourceIdentityEntity identity = verifyFound(dataSourceRepository.findById(dataSourceId));
        List<DataRecordEntity> entities = findDataRecordEntities(identity.getId(), predicates, columns);

        List<String> recordsIds = includedDeleted ?
                getRecordIdsIncludingDeletedSinceLastApprovedVersion(identity, entities) :
                getRecordIds(entities);

        List<DataSourceActionEntity> actions = getDataSourceActionEntities(version, columns, recordsIds);
        Map<String, DataRecordEntity> recordsAtVersion = getStringDataRecordEntityMap(identity, actions);

        Meta meta = getRecordsMetaData(version, identity);

        final List<DataRecord> allDataRecords = convertDataRecordEntitiesAndSetModifiedFields(dataSourceId, version,
                actions, recordsAtVersion.values());
        Records records = new Records();
        records.setMeta(meta);
        records.setData(includedDeleted ? allDataRecords : removeDeleted(allDataRecords));
        return records;
    }

    private Meta getRecordsMetaData(String version, DataSourceIdentityEntity identity) {
        List<DataSourceActionEntity> columnOrder =
                dataSourceActionRepository.findActionsByType(newArrayList(identity.getId()),
                        DataSourceActionType.COLUMN_ORDER_CHANGE);

        Meta meta = new Meta();

        Version versionObj = new Version(version);

        Map<String, Object> metaColumns = columnOrder.stream()
                .filter(action -> action.getVersion().isLessThanOrEqual(versionObj))
                .max(Comparator.comparing(DataSourceActionEntity::getCreateTime))
                .map(item -> item.getValues()).orElse(newTreeMap());

        if (!metaColumns.isEmpty()) {
            meta.setColumnOrder(metaColumns);
        }
        return meta;
    }

    private static List<String> getRecordIds(final List<DataRecordEntity> entities) {
        final List<String> recordsIds;
        recordsIds = entities
                .stream()
                .map(DataRecordEntity::getId)
                .collect(toList());
        return recordsIds;
    }

    private static List<String> getRecordIdsIncludingDeletedSinceLastApprovedVersion(
            final DataSourceIdentityEntity identity, final List<DataRecordEntity> entities) {
        final List<String> recordsIds;
        recordsIds = entities
                .stream()
                .filter(entity -> notDeletedInPreviousVersion(identity, entity))
                .map(DataRecordEntity::getId)
                .collect(toList());
        return recordsIds;
    }

    private static boolean notDeletedInPreviousVersion(final DataSourceIdentityEntity identity, final DataRecordEntity
            entity) {
        return entity.deletedIn() == null || !entity.deletedIn().isLessThan(identity.getVersion());
    }

    public List<DataRecordEntity> findDataRecordEntitiesByVersion(final String dataSourceId, final String version,
            final List<String> predicates, final String columns) {
        final Records records = findRecordsByVersion(dataSourceId, version, predicates, columns, false);
        return records.getData().stream().map(dataRecordMapper()::map).collect(toList());
    }

    private List<DataSourceActionEntity> getDataSourceActionEntities(final String version, final String columns,
            final List<String> recordsIds) {
        List<String> visibleColumns = columnsToList(columns);
        return dataSourceActionRepository.find(recordsIds, version, visibleColumns);
    }

    private static Map<String, DataRecordEntity> getStringDataRecordEntityMap(final DataSourceIdentityEntity identity,
            final List<DataSourceActionEntity> actions) {
        AppliedDataSourceActionAccumulator mapper = new AppliedDataSourceActionAccumulator();
        SortedMap<String, DataRecordEntity> recordsAtVersion = newTreeMap();
        actions.forEach(action -> action.apply(identity, recordsAtVersion, mapper));
        return recordsAtVersion;
    }

    public List<DataRecordEntity> findDataRecordEntities(String dataSourceId, List<String> predicates, String columns) {
        List<RecordPredicate> recordPredicates = filterToPredicateConverter.convert(predicates);
        List<String> visibleColumns = columnsToList(columns);
        return newArrayList(dataRecordRepository.find(dataSourceId, recordPredicates, visibleColumns).values());
    }

    private static List<String> columnsToList(String columns) {
        return columns == null || columns.isEmpty() ?
                emptyList() : unmodifiableList(newArrayList(columns.split(COLUMN_NAMES_SEPARATOR)));
    }

    public List<DataRecordEntity> getRecordsForLock(String lockId) {
        Lock lock = verifyFound(lockRepository.findById(lockId));
        DataSourceExecution dataSourceExecution = lock.getDataSourceExecution();
        List<String> recordIds = dataSourceExecution.getRecords()
                .stream().map(DataRecordEntity::getId)
                .collect(toList());
        DataSourceIdentityEntity identity = verifyFound(
                dataSourceRepository.findById(dataSourceExecution.getDataSourceId()));
        return findDataRecordEntitiesByVersion(identity, dataSourceExecution.getVersion(),
                dataSourceExecution.getColumns(), recordIds);
    }


    private List<DataRecordEntity> findDataRecordEntitiesByVersion(final DataSourceIdentityEntity identity,
            final String version, final String columns, final List<String> recordIds) {
        List<DataSourceActionEntity> actions = getDataSourceActionEntities(version, columns, recordIds);
        Map<String, DataRecordEntity> recordsAtVersion = getStringDataRecordEntityMap(identity, actions);
        return recordsAtVersion.values().stream().collect(toList());
    }

    public DataSourceIdentity edit(String dataSourceId, List<DataSourceAction> actions) {
        checkArgument(!actions.isEmpty(), ERR_EDIT_NO_ACTIONS);
        DataSourceIdentityEntity identity = dataSourceRepository.findById(dataSourceId);
        securityService.validateUserAuthorization(identity.getContextId());
        checkArgument(!ApprovalStatus.PENDING.equals(identity.getApprovalStatus()), ERR_EDIT_PENDING_APPROVAL);
        if (isApproved(identity)) {
            final DataSourceAction unApproveAction = getApprovalStatusChanges(actions).get(0);
            createNextSnapshotVersion(identity, unApproveAction);
            actions.remove(unApproveAction);
        }
        checkVersion(identity, actions);
        applyActions(actions, identity);
        return toDataSourceIdentity(identity);
    }

    private void applyActions(final List<DataSourceAction> actions, final DataSourceIdentityEntity identity) {
        List<DataSourceAction> approvalStatusChanges = getApprovalStatusChanges(actions);
        // This is required to ensure all status changes go through the proper actions flow.
        if (REJECTED.equals(identity.getApprovalStatus())) {
            addUnApproveAction(identity);
            actions.remove(approvalStatusChanges.get(0));
        }
        dataSourceActionsService.applyActionsAndSave(identity, actions);
    }

    private static List<DataSourceAction> getApprovalStatusChanges(final List<DataSourceAction> actions) {
        return actions.stream().filter(action -> action.getType().equalsIgnoreCase(IDENTITY_APPROVAL_STATUS.name()))
                    .collect(toList());
    }

    private void addUnApproveAction(final DataSourceIdentityEntity identity) {
        final ApprovalRequest unApprove = ApprovalRequestBuilder
                .anUnApprove().build();
        dataSourceActionsService.applyApprovalStatusChange(identity, unApprove);
    }

    private static boolean isApproved(final DataSourceIdentityEntity identity) {
        return ApprovalStatus.APPROVED.equals(identity.getApprovalStatus());
    }

    private void createNextSnapshotVersion(final DataSourceIdentityEntity identity,
            final DataSourceAction dataSourceAction) {
        dataSourceActionsService.createNewVersion(identity, ApprovalRequestBuilder.anUnApprove().build(),
                dataSourceAction);
    }

    @VisibleForTesting
    void checkVersion(DataSourceIdentityEntity identity, List<DataSourceAction> actions) {
        Version currentVersion = identity.getVersion();

        actions.forEach(a -> {
            boolean validatedVersion = currentVersion.isLessThanOrEqual(new Version(a.getVersion()));
            checkArgument(validatedVersion, ERR_EDIT_VERSION_MISMATCH);
        });
    }

    private static void checkVersionSnapshot(String version) {
        Version validVersion = new Version(version);
        checkArgument(!validVersion.isSnapshot(), ERR_EDIT_VERSION_SNAPSHOT);
    }

    private static void checkIsEmptyString(String name) {
        checkArgument(!StringUtils.isBlank(name), ERR_LABEL_NAME_EMPTY);
    }

    public DataSourceIdentity handleApproval(String dataSourceId, ApprovalRequest request) {
        DataSourceIdentityEntity entity;
        if (UNAPPROVED == request.getStatus()) {
            entity = verifyFound(findLatestApprovedDataSource(dataSourceId));
        } else {
            entity = verifyFound(dataSourceRepository.findById(dataSourceId));
        }

        Optional<DataSourceLabelEntity> dataSourceLabelEntity = ofNullable(
                dataSourceLabelRepository.findByDataSourceIdAndVersion(
                        entity.getId(),
                        entity.getVersion().toString()));

        DataSourceIdentity dataSourceIdentity = handleApproval(entity, request);

        if (dataSourceLabelEntity.isPresent()) {
            deleteLabel(dataSourceLabelEntity.get().getName(), dataSourceLabelEntity.get().getContextId());
        }

        return dataSourceIdentity;
    }

    private DataSourceIdentityEntity findLatestApprovedDataSource(String dataSourceId) {
        Optional<Version> version = dataSourceActionsService.findLatestApprovedVersion(dataSourceId);
        return findDataSourceEntityByVersion(dataSourceId, version.toString());
    }

    private DataSourceIdentity handleApproval(DataSourceIdentityEntity entity, ApprovalRequest request) {
        approvalRequestValidationService.validate(entity, request);
        notificationService.notifyDataSourceChange(entity, request);
        dataSourceActionsService.applyApprovalStatusChange(entity, request);
        return dataSourceMapper().mapReverse(entity);
    }

    public List<String> getVersions(String dataSourceId) {
        List<Version> versions = dataSourceActionsService.getActualVersions(dataSourceId);
        return versions.stream()
                       .sorted(reverseOrder())
                       .map(Version::toString)
                       .collect(toList());
    }

    public List<History> getHistory(String dataSourceId) {
        List<History> actualHistory = getActualHistory(dataSourceId);
        return actualHistory.stream()
                .sorted(reverseOrder())
                .collect(toList());
    }


    private List<History> getActualHistory(String dataSourceId) {
        List<DataSourceActionEntity> actualHistory = dataSourceActionsService.getHistory(dataSourceId);
        return actualHistory.stream()
                .map(item -> new History(item.getVersion().toString(), item.getValues(), item.getCreateTime()))
        .collect(toList());
    }

    public DataSourceIdentity findDataSourceByVersion(String dataSourceId, String version) {
        DataSourceIdentity dataSourceIdentity = toDataSourceIdentity(
                findDataSourceEntityByVersion(dataSourceId, version));
        addLabelIfExists(ofNullable(dataSourceIdentity));
        return dataSourceIdentity;
    }

    @VisibleForTesting
    DataSourceIdentityEntity findDataSourceEntityByVersion(String dataSourceId, String version) {

        verifyFound(dataSourceActionRepository.findByDataSourceIdAndVersion(dataSourceId, version));
        List<DataSourceActionEntity> actions = dataSourceActionRepository.find(asList(dataSourceId), version);
        DataSourceIdentityEntity dataSourceIdentityEntity = new DataSourceIdentityEntity();

        AppliedDataSourceActionAccumulator mapper = new AppliedDataSourceActionAccumulator();
        actions.stream().forEachOrdered(action -> action.apply(dataSourceIdentityEntity, null, mapper));
        dataSourceIdentityEntity.setVersion(new Version(version));

        return dataSourceIdentityEntity;
    }

    public Collection<DataSourceLabel> findDataSourceLabels(String label, String limit) {
        List<DataSourceLabelEntity> allByName = dataSourceLabelRepository.searchByName(label);
        return allByName.stream()
                .map(item -> dataLabelMapper().mapReverse(item))
                .limit(Long.parseLong(limit))
                .collect(toList());
    }

    public DataSourceIdentity findDataSourceFromLabel(String label, String contextId) {
        DataSourceLabelEntity dataSourceLabel = verifyFound(
                dataSourceLabelRepository.findByNameAndContextId(label, contextId));
        return findDataSourceByVersion(dataSourceLabel.getDataSourceId(), dataSourceLabel.getVersion());
    }

    public DataSourceLabel updateLabel(DataSourceLabel dataSourceLabel) {
        securityService.validateUserAuthorization(dataSourceLabel.getContextId());
        checkIsEmptyString(dataSourceLabel.getName());
        checkVersionSnapshot(dataSourceLabel.getVersion());
        checkIfLabelExistsInContext(dataSourceLabel);

        BoundMapperFacade<DataSourceLabel, DataSourceLabelEntity> mapper = dataLabelMapper();
        DataSourceLabelEntity mappedDataSourceLabelEntity = mapper.map(dataSourceLabel, new DataSourceLabelEntity());

        Optional<DataSourceLabelEntity> labelOnDatasourceInContext = ofNullable(
                dataSourceLabelRepository.findByDataSourceIdAndVersion(dataSourceLabel.getDataSourceId(),
                        dataSourceLabel.getVersion()));
        if (labelOnDatasourceInContext.isPresent()) {
            mappedDataSourceLabelEntity.setId(labelOnDatasourceInContext.get().getId());
            dataSourceLabelRepository.update(mappedDataSourceLabelEntity);
        } else {
            dataSourceLabelRepository.insert(mappedDataSourceLabelEntity);
        }

        return mapper.mapReverse(mappedDataSourceLabelEntity);
    }

    @VisibleForTesting
    protected void checkIfLabelExistsInContext(DataSourceLabel dataSourceLabel) {
        final Optional<DataSourceLabelEntity> dataSourceLabelEntity = ofNullable(
                dataSourceLabelRepository.findByNameAndContextId(dataSourceLabel.getName(),
                        dataSourceLabel.getContextId()));
        if (dataSourceLabelEntity.isPresent()) {
            final DataSourceLabelEntity existingLabel = dataSourceLabelEntity.get();
            final DataSourceIdentityEntity dataSourceIdentity = dataSourceRepository.findById(
                    existingLabel.getDataSourceId());
            String message;
            if (labelIsAlreadyAppliedToThisVersionOfTheDataSource(existingLabel, dataSourceLabel)) {
                message = String.format("%s is already applied to this version of the datasource",
                        existingLabel.getName());
            } else if (labelIsAlreadyAppliedToThisDataSource(existingLabel, dataSourceLabel)) {
                message = String.format("%s is already applied to version %s of this datasource",
                        existingLabel.getName(), existingLabel.getVersion());
            } else {
                message = String.format("%s is already applied to datasource %s", existingLabel.getName(),
                        dataSourceIdentity.getName());
            }
            throw new IllegalArgumentException(message);
        }
    }

    private static boolean labelIsAlreadyAppliedToThisDataSource(final DataSourceLabelEntity existingLabel,
            final DataSourceLabel dataSourceLabel) {
        return existingLabel.getDataSourceId().equalsIgnoreCase(dataSourceLabel.getDataSourceId());
    }

    private static boolean labelIsAlreadyAppliedToThisVersionOfTheDataSource(final DataSourceLabelEntity existingLabel,
            final DataSourceLabel dataSourceLabel) {
        return labelIsAlreadyAppliedToThisDataSource(existingLabel, dataSourceLabel) && existingLabel
                .getVersion().equalsIgnoreCase(dataSourceLabel.getVersion());
    }

    public void deleteLabel(String label, final String contextId) {
        securityService.validateUserAuthorization(contextId);
        DataSourceLabelEntity dataSourceLabel = verifyFound(
                dataSourceLabelRepository.findByNameAndContextId(label, contextId));
        dataSourceLabelRepository.removeById(dataSourceLabel.getId());
    }

    private List<DataRecord> convertDataRecordEntitiesAndSetModifiedFields(
            String dataSourceId,
            String version,
            List<DataSourceActionEntity> actions,
            Collection<DataRecordEntity> dataRecordEntities) {

        return dataRecordEntities.stream()
                .map(dataRecordMapper()::mapReverse)
                .peek(record -> record.setDataSourceId(dataSourceId))
                .peek(record -> addModifiedFieldsToDataRecord(version, actions, record))
                .collect(toList());
    }

    private void addModifiedFieldsToDataRecord(String version,
                                               List<DataSourceActionEntity> actions,
                                               DataRecord record) {

        List<DataSourceActionEntity> columnActions = getModifiedActions(new Version(version), actions, record);
        Set<String> modifiedColumns = newHashSet();
        columnActions.forEach(action -> modifiedColumns.addAll(action.getValues().keySet()));
        record.setModifiedColumns(modifiedColumns);
        addOldValuesToDataRecord(actions, record);
    }

    private static void addOldValuesToDataRecord(final List<DataSourceActionEntity> actions, final DataRecord record) {
        Map<String, Object> oldValues = newHashMap();
        actions.stream()
               .filter(action -> actionAppliesToThisDataRecord(record, action))
               .forEach(action -> addAnyOldValues(record, oldValues, action));
        if (!oldValues.isEmpty()) {
            record.setOldValues(oldValues);
        }
    }

    private static boolean actionAppliesToThisDataRecord(final DataRecord record, final DataSourceActionEntity action) {
        return action.getParentId().equalsIgnoreCase(record.getId());
    }

    private static void addAnyOldValues(final DataRecord record, final Map<String, Object> oldValues,
            final DataSourceActionEntity action) {
        action.getValues().forEach((key, value) -> {
            if (valueHasChangedSinceLastVersion(record, key, value)) {
                oldValues.putIfAbsent(key, value);
            }
        });
    }

    private static boolean valueHasChangedSinceLastVersion(final DataRecord record, final String key, final Object
            value) {
        final Object latestValue = record.getValues().get(key);
        return latestValue != null && !latestValue.toString().equalsIgnoreCase(value.toString());
    }

    @VisibleForTesting
    List<DataSourceActionEntity> getModifiedActions(Version version,
                                                    List<DataSourceActionEntity> actions,
                                                    DataRecord record) {
        return actions.stream()
                .filter(action -> {
                    boolean isTheSameIdWithParent = Objects.equals(action.getParentId(), record.getId());
                    boolean isTheSameVersion = Objects.equals(action.getVersion(), version);
                    return isTheSameIdWithParent && isTheSameVersion && isRecordModified(action);
                })
                .collect(toList());
    }

    private static boolean isRecordModified(DataSourceActionEntity dataSourceActionEntity) {
        DataSourceActionType actionType = dataSourceActionEntity.getType();
        return actionType == DataSourceActionType.RECORD_ADD || actionType == DataSourceActionType.RECORD_VALUE_EDIT;
    }

    private DataSourceIdentity persistIdentity(DataSource dataSource) {
        BoundMapperFacade<DataSourceIdentity, DataSourceIdentityEntity> mapper = dataSourceMapper();
        DataSourceIdentityEntity dataSourceIdentityEntity = mapper.map(dataSource.getIdentity());
        Context context = resolveContext(dataSourceIdentityEntity);

        dataSourceIdentityEntity.setName(dataSourceIdentityEntity.getName().trim());

        dataSourceIdentityEntity.setContext(context.getName());
        dataSourceIdentityEntity.setContextId(context.getId());

        dataSourceIdentityEntity.setCreateTime(new Date());
        String userId = securityService.getCurrentUser().getUsername();
        dataSourceIdentityEntity.setCreatedBy(userId);
        if (dataSourceIdentityEntity.getVersion() == null) {
            dataSourceIdentityEntity.setVersion(Version.INITIAL_VERSION);
        }
        if (PROFILE != null && "customer".equalsIgnoreCase(PROFILE)) {
            dataSourceIdentityEntity.setApprovalStatus(APPROVED);
            dataSourceIdentityEntity.getVersion().setSnapshot(false);
            dataSourceIdentityEntity.setApprover(securityService.getCurrentUser().getUsername());

        } else {
            dataSourceIdentityEntity.setApprovalStatus(UNAPPROVED);
        }
        dataSourceHelper.validateDuplicateNameAndContextAndFail(dataSourceIdentityEntity);
        dataSourceRepository.insert(dataSourceIdentityEntity);
        dataSourceActionsService.insertInitialAction(dataSourceIdentityEntity);

        return mapper.mapReverse(dataSourceIdentityEntity);
    }

    private Context resolveContext(DataSourceIdentityEntity dataSourceIdentityEntity) {
        return verifyFound(contextService.findBySystemId(dataSourceIdentityEntity.getContextId()));
    }

    private void persistRecordsWithIdentityReference(DataSource dataSource) {
        List<DataRecordEntity> recordEntities = dataSource.getRecords().stream()
                .map(dataRecordMapper()::map)
                .peek(record -> record.setDataSourceId(dataSource.getIdentity().getId()))
                .collect(toList());

        dataRecordRepository.insert(recordEntities);

        AtomicInteger order = new AtomicInteger();

        Version startVersion = new Version(dataSource.getIdentity().getVersion());

        List<DataSourceActionEntity> actions = recordEntities.stream()
                .map(DataSourceActionEntity::recordAdd)
                .peek(action -> action.setVersion(startVersion))
                .peek(action -> action.setOrder(order.getAndIncrement()))
                .peek(action -> action
                        .setAuditData(dataSource.getIdentity().getCreatedBy(),
                                dataSource.getIdentity().getCreateTime()))
                .collect(toList());

        dataSourceActionRepository.insert(actions);

        createColumnOrderActions(recordEntities, dataSource.getIdentity().getId(), startVersion, order);
    }

    private void addLabelIfExists(Optional<DataSourceIdentity> dataSourceIdentity) {
        if (dataSourceIdentity.isPresent()) {
            Optional<DataSourceLabelEntity> byDataSourceIdAndVersion = ofNullable(
                    dataSourceLabelRepository.findByDataSourceIdAndVersion(
                            dataSourceIdentity.get().getId(),
                            dataSourceIdentity.get().getVersion()));

            if (byDataSourceIdAndVersion.isPresent()) {
                dataSourceIdentity.get().setLabel(byDataSourceIdAndVersion.get().getName());
            }
        }
    }

    private void createColumnOrderActions(List<DataRecordEntity> recordEntities,
                                          String dataSourceId,
                                          Version version,
                                          AtomicInteger order) {

        Optional<DataRecordEntity> record = recordEntities.stream().findFirst();
        if (record.isPresent()) {
            Set<String> columns = record.get().getValues().keySet();
            int index = 0;
            SortedMap<String, Object> values = new TreeMap<>();
            for (String header : columns) {
                values.put(header, Integer.toString(index));
                index++;
            }
            DataSourceActionEntity dataSourceActionEntity =
                    DataSourceActionEntity.columnOrder(dataSourceId, values, version);
            dataSourceActionEntity.setOrder(order.getAndIncrement());

            String userId = securityService.getCurrentUser().getUsername();
            dataSourceActionEntity.setAuditData(userId, new Date());
            dataSourceActionRepository.insert(dataSourceActionEntity);
        }
    }

    /*---------------- Mapping ----------------*/

    private List<DataSourceIdentity> toIdentities(List<DataSourceIdentityEntity> entities) {
        return entities.stream()
                .map(this::toDataSourceIdentity)
                .collect(toList());
    }

    private DataSourceIdentity toDataSourceIdentity(DataSourceIdentityEntity entity) {
        return dataSourceMapper().mapReverse(entity);
    }

    private BoundMapperFacade<DataSourceIdentity, DataSourceIdentityEntity> dataSourceMapper() {
        return mapper(DataSourceIdentity.class, DataSourceIdentityEntity.class);
    }

    private BoundMapperFacade<DataRecord, DataRecordEntity> dataRecordMapper() {
        return mapper(DataRecord.class, DataRecordEntity.class);
    }

    private BoundMapperFacade<DataSourceLabel, DataSourceLabelEntity> dataLabelMapper() {
        return mapper(DataSourceLabel.class, DataSourceLabelEntity.class);
    }

    private <A, B> BoundMapperFacade<A, B> mapper(Class<A> aType, Class<B> bType) {
        return mapperFactory.getMapperFacade(aType, bType);
    }

}
