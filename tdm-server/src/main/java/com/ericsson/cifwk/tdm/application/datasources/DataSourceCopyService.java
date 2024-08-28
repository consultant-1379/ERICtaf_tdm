package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.api.model.DataRecord;
import com.ericsson.cifwk.tdm.api.model.DataSourceCopyRequest;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.api.model.Records;
import com.ericsson.cifwk.tdm.application.contexts.ContextService;
import com.ericsson.cifwk.tdm.infrastructure.security.SecurityService;
import com.ericsson.cifwk.tdm.model.DataRecordEntity;
import com.ericsson.cifwk.tdm.model.DataSourceActionEntity;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import com.ericsson.cifwk.tdm.model.Version;
import com.google.common.base.Preconditions;
import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.APPROVED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceService.PROFILE;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;

@Service
public class DataSourceCopyService {

    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Autowired
    private DataRecordRepository dataRecordRepository;

    @Autowired
    private DataSourceActionRepository dataSourceActionRepository;

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    MapperFactory mapperFacade;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ContextService contextService;

    @Autowired
    private DataSourceActionsService dataSourceActionsService;

    public DataSourceIdentity copy(DataSourceCopyRequest dataSourceCopyRequest) {
        securityService.validateUserAuthorization(dataSourceCopyRequest.getNewContextId());
        String dataSourceId = dataSourceCopyRequest.getDataSourceId();
        DataSourceIdentityEntity dataSourceIdentity = dataSourceService.findDataSourceEntityByVersion(dataSourceId,
                dataSourceCopyRequest.getVersion());

        dataSourceIdentity.setId(null);

        setVersion(dataSourceCopyRequest, dataSourceIdentity);

        if (PROFILE != null && "customer".equalsIgnoreCase(PROFILE)) {
            dataSourceIdentity.setApprovalStatus(APPROVED);
            dataSourceIdentity.getVersion().setSnapshot(false);
            dataSourceIdentity.setApprover(securityService.getCurrentUser().getUsername());
        } else {
            dataSourceIdentity.setApprovalStatus(UNAPPROVED);
        }
        if (!isNullOrEmpty(dataSourceCopyRequest.getNewContextId())
                && !dataSourceCopyRequest.getNewContextId().equals(dataSourceIdentity.getContextId())) {

            contextService.findBySystemId(dataSourceCopyRequest.getNewContextId()).ifPresent(c -> {
                dataSourceIdentity.setContextId(c.getId());
                dataSourceIdentity.setContext(c.getName());
            });
        }

        Preconditions.checkArgument(!isNullOrEmpty(dataSourceCopyRequest.getNewName()));

        dataSourceIdentity.setName(dataSourceCopyRequest.getNewName().trim());

        if (!isNullOrEmpty(dataSourceCopyRequest.getNewGroup())) {
            dataSourceIdentity.setGroup(dataSourceCopyRequest.getNewGroup());
        }

        String userId = securityService.getCurrentUser().getUsername();
        dataSourceIdentity.setCreatedBy(userId);
        dataSourceIdentity.setCreateTime(new Date());

        dataSourceRepository.insert(dataSourceIdentity);

        dataSourceActionsService.insertInitialAction(dataSourceIdentity);

        copyRecords(dataSourceCopyRequest, dataSourceIdentity);

        BoundMapperFacade<DataSourceIdentity, DataSourceIdentityEntity> mapper =
                this.mapperFacade.getMapperFacade(DataSourceIdentity.class, DataSourceIdentityEntity.class);

        return mapper.mapReverse(dataSourceIdentity);
    }

    private static void setVersion(final DataSourceCopyRequest dataSourceCopyRequest,
            final DataSourceIdentityEntity identity) {
        Version copyRequestVersion = new Version(dataSourceCopyRequest.getVersion());
        if (dataSourceCopyRequest.isBaseVersion()) {
            if (copyRequestVersion.isSnapshot()) {
                identity.setVersion(copyRequestVersion);
            } else {
                identity.setVersion(copyRequestVersion.incrementMinor());
            }
        } else {
            identity.setVersion(Version.INITIAL_VERSION);
        }
    }

    private void copyRecords(DataSourceCopyRequest dataSourceCopyRequest, DataSourceIdentityEntity dataSourceIdentity) {
        Records records = dataSourceService
                .findRecordsByVersion(dataSourceCopyRequest.getDataSourceId(), dataSourceCopyRequest.getVersion());

        List<DataRecordEntity> recordEntities = insertRecordCopies(dataSourceIdentity, records.getData());

        AtomicInteger order = new AtomicInteger();
        insertInitialActionsForRecords(dataSourceIdentity, recordEntities, order);
        copyColumnInfo(dataSourceCopyRequest, dataSourceIdentity, order);
    }

    private void copyColumnInfo(DataSourceCopyRequest dataSourceCopyRequest,
                                DataSourceIdentityEntity dataSourceIdentity, AtomicInteger order) {
        List<DataSourceActionEntity> columnOrder =
                dataSourceActionRepository.findActionsByType(newArrayList(dataSourceCopyRequest.getDataSourceId()),
                        DataSourceActionType.COLUMN_ORDER_CHANGE);

        Version compareVersion = new Version(dataSourceCopyRequest.getVersion());

        Optional<DataSourceActionEntity> columnAction = columnOrder.stream()
                .filter(action -> action.getVersion().isLessThanOrEqual(compareVersion))
                .max(Comparator.comparing(DataSourceActionEntity::getCreateTime));

        if (columnAction.isPresent()) {
            columnAction.get().setId(null);
            columnAction.get().setParentId(dataSourceIdentity.getId());
            columnAction.get().setVersion(dataSourceIdentity.getVersion());
            columnAction.get().setAuditData(securityService.getCurrentUser().getUsername(), new Date());
            columnAction.get().setOrder(order.getAndIncrement());
            dataSourceActionRepository.insert(columnAction.get());
        }
    }

    private List<DataRecordEntity> insertRecordCopies(DataSourceIdentityEntity dataSourceIdentity,
                                                      Collection<DataRecord> recordsByVersion) {
        BoundMapperFacade<DataRecord, DataRecordEntity> mapper =
                this.mapperFacade.getMapperFacade(DataRecord.class, DataRecordEntity.class);

        List<DataRecordEntity> recordEntities = recordsByVersion
                .stream()
                .map(dataRecord -> {
                    DataRecordEntity dataRecordEntity = mapper.map(dataRecord);
                    dataRecordEntity.setId(null);
                    dataRecordEntity.setDataSourceId(dataSourceIdentity.getId());
                    return dataRecordEntity;
                })
                .collect(Collectors.toList());

        dataRecordRepository.insert(recordEntities);
        return recordEntities;
    }

    private void insertInitialActionsForRecords(DataSourceIdentityEntity dataSourceIdentity,
                                                List<DataRecordEntity> recordEntities, AtomicInteger order) {
        List<DataSourceActionEntity> actionEntities = recordEntities.stream()
                .map(DataSourceActionEntity::recordAdd)
                .peek(action -> action.setOrder(order.getAndIncrement()))
                .peek(action -> action.setVersion(dataSourceIdentity.getVersion()))
                .peek(action -> action.setAuditData(dataSourceIdentity.getCreatedBy(),
                        dataSourceIdentity.getCreateTime()))
                .collect(Collectors.toList());

        dataSourceActionRepository.insert(actionEntities);
    }
}
