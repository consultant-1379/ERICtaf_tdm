package com.ericsson.cifwk.tdm.application.contexts;

import com.ericsson.cifwk.tdm.api.model.Context;
import com.ericsson.cifwk.tdm.model.ContextEntity;
import com.ericsson.gic.tms.presentation.dto.ContextBean;
import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import static com.ericsson.cifwk.tdm.presentation.exceptions.NotFoundException.verifyFound;
import static com.google.common.collect.Lists.newArrayList;

@Service
public class ContextService {

    static final String CONTEXT_PATH_SEPARATOR = "/";

    @Autowired
    ContextRepository contextRepository;

    @Autowired
    MapperFactory mapperFacade;

    public Context createOrUpdate(ContextBean context) {
        ContextEntity foundBySystemId = contextRepository.findBySystemId(context.getId());

        BoundMapperFacade<ContextBean, ContextEntity> mapper =
                this.mapperFacade.getMapperFacade(ContextBean.class, ContextEntity.class);

        BoundMapperFacade<ContextEntity, Context> reverseMapper =
                this.mapperFacade.getMapperFacade(ContextEntity.class, Context.class);

        ContextEntity contextEntity = mapper.map(context);
        if (foundBySystemId != null) {
            contextEntity.setId(foundBySystemId.getId());
            contextRepository.update(contextEntity);
        } else {
            contextRepository.insert(contextEntity);
        }
        return reverseMapper.map(contextEntity);
    }

    public List<Context> findAll() {
        MapperFacade mapper = this.mapperFacade.getMapperFacade();
        return mapper.mapAsList(contextRepository.findAll(), Context.class);
    }

    public Optional<Context> findBySystemId(String systemId) {
        ContextEntity context = contextRepository.findBySystemId(systemId);
        if (context != null) {
            return Optional.of(toContext(context));
        }
        return Optional.empty();
    }

    public Optional<Context> findByName(String name) {
        ContextEntity context = contextRepository.findByName(name);
        if (context != null) {
            return Optional.of(toContext(context));
        }
        return Optional.empty();
    }

    public List<String> findAllParentContextIds(String contextId) {
        List<ContextEntity> allContexts = contextRepository.findAll();
        return collectSubContextsWithRoot(contextId, allContexts);
    }

    private static List<String> collectSubContextsWithRoot(String contextId, List<ContextEntity> allContexts) {
        List<String> accumulator = newArrayList();

        Deque<String> toReview = new ArrayDeque<>();
        toReview.add(contextId);

        while (!toReview.isEmpty()) {
            String parentContextId = toReview.pop();
            allContexts.stream()
                    .peek(c -> {
                        if ((accumulator.isEmpty()  || !accumulator.contains(contextId))
                                && contextId.equals(c.getSystemId())) {
                            accumulator.add(c.getSystemId());
                        }
                    })
                    .filter(c -> parentContextId.equals(c.getParentId()))
                    .peek(c -> toReview.push(c.getSystemId()))
                    .forEach(c -> accumulator.add(c.getSystemId()));
        }
        return accumulator;
    }

    public Context findByPath(String contextPath) {
        List<String> pathElements = newArrayList(contextPath.split(CONTEXT_PATH_SEPARATOR));
        String currentPath = pathElements.remove(0);
        Context context = verifyFound(findByName(currentPath));
        for (String element : pathElements) {
            Optional<Context> contextWrapper = findByParentIdAndName(context.getId(), element);
            context = verifyFound(contextWrapper);
        }

        return context;
    }

    private Optional<Context> findByParentIdAndName(String parentId, String name) {
        ContextEntity entity = contextRepository.findByParentIdAndName(parentId, name);
        return Optional.ofNullable(entity).map(this::toContext);
    }

    private Context toContext(ContextEntity entity) {
        return mapperFacade.getMapperFacade().map(entity, Context.class);
    }
}
