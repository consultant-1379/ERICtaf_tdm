package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.application.common.repository.BaseRepository;
import com.ericsson.cifwk.tdm.model.DataSourceLabelEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DataSourceLabelRepository extends BaseRepository<DataSourceLabelEntity> {

    public static final String DATA_SOURCES_LABELS = "dataSourceLabels";

    public DataSourceLabelRepository() {
        super(DATA_SOURCES_LABELS, DataSourceLabelEntity.class);
    }

    public DataSourceLabelEntity findByNameAndContextId(String name, String contextId) {
        return findOne("{contextId:#, name:#}", contextId, name);
    }

    public List<DataSourceLabelEntity> searchByName(String name) {
        return find("{name:{$regex : '.*#.*'}}", name);
    }

    public DataSourceLabelEntity findByDataSourceIdAndVersion(String dataSourceId, String version) {
        return findOne("{dataSourceId:#, version:#}", dataSourceId, version);
    }

    public List<DataSourceLabelEntity> findAllByDataSourceId(String dataSourceId) {
        return find("{dataSourceId:#}", dataSourceId);
    }
}
