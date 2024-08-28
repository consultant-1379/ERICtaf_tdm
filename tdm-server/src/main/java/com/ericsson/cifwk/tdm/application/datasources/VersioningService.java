package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.model.DataSourceActionEntity;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import com.ericsson.cifwk.tdm.model.Version;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VersioningService {

    public void incrementVersion(DataSourceIdentityEntity identity,
                                 List<DataSourceActionEntity> actionEntities) {
        Version version = identity.getVersion();
        Version newVersion = getVersionIfRequired(version, actionEntities);
        actionEntities.forEach(e -> e.setVersion(newVersion));
        identity.setVersion(newVersion);
    }

    Version getVersionIfRequired(Version version, List<DataSourceActionEntity> actionEntities) {
        return actionEntities.stream()
                .filter(item -> item.getType() != null &&
                        item.getType().equals(DataSourceActionType.IDENTITY_VERSION_EDIT))
                .map(DataSourceActionEntity::getVersion)
                .findFirst()
                .orElse(version);
    }
}
