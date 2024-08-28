package com.ericsson.cifwk.tdm.application.contexts;

import com.ericsson.cifwk.tdm.application.common.repository.BaseRepository;
import com.ericsson.cifwk.tdm.model.ContextEntity;
import org.springframework.stereotype.Repository;

@Repository
public class ContextRepository extends BaseRepository<ContextEntity> {

    public static final String CONTEXTS = "contexts";

    public ContextRepository() {
        super(CONTEXTS, ContextEntity.class);
    }

    public ContextEntity findBySystemId(String systemId) {
        return findOne("{systemId:#}", systemId);
    }

    public ContextEntity findByName(String name) {
        return findOne("{name:#}", name);
    }

    public ContextEntity findByParentIdAndName(String parentId, String name) {
        return findOne("{parentId:#, name:#}", parentId, name);
    }
}
