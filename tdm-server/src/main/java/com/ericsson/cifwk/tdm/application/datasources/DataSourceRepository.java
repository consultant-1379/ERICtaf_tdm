package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.application.common.repository.BaseRepository;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public class DataSourceRepository extends BaseRepository<DataSourceIdentityEntity> {

    public static final String DATA_SOURCES_COLLECTION = "dataSources";

    public DataSourceRepository() {
        super(DATA_SOURCES_COLLECTION, DataSourceIdentityEntity.class);
    }

    @Override
    public DataSourceIdentityEntity findById(String id) {
        return findOne("{_id: {$oid:#}, deleted: false}", id);
    }

    @Override
    public List<DataSourceIdentityEntity> findAll() {
        return find("{deleted: false}");
    }

    public List<DataSourceIdentityEntity> findByContextIds(Collection<String> systemIds) {
        return find("{contextId:{$in:#}, deleted: false}", systemIds);
    }

    public DataSourceIdentityEntity findByContextIdAndName(String contextId, String name) {
        return findOne("{contextId:#, name:#, deleted: false}", contextId, name);
    }

    public DataSourceIdentityEntity findAllByContextIdAndName(String contextId, String name) {
        return findOne("{contextId:#, name:#}", contextId, name);
    }
}
