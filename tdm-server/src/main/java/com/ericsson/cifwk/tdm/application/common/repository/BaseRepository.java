package com.ericsson.cifwk.tdm.application.common.repository;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

import static org.jongo.Oid.withOid;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.WriteResult;

public class BaseRepository<T> {

    @Autowired
    protected Jongo jongo;
    protected static final Collector<CharSequence, ?, String> QUERY_PROPERTIES_COLLECTOR = joining(",", "{", "}");
    private final String collection;
    private final Class<T> entityClass;

    public BaseRepository(String collection, Class<T> entityClass) {
        this.collection = collection;
        this.entityClass = entityClass;
    }

    public T findById(String id) {
        return findOne(withOid(id));
    }

    protected T findOne(String query, Object... parameters) {
        return getCollection().findOne(query, parameters).as(entityClass);
    }

    public List<T> findAll() {
        return find("{}");
    }

    protected List<T> find(String query, Object... parameters) {
        MongoCursor<T> cursor = getCollection().find(query, parameters).as(entityClass);
        return newArrayList((Iterator<T>) cursor);
    }

    /**
     * @return {@code true} if existing entity updated, {@code false} if new entity has been created
     */
    // TODO: rename to save
    public boolean update(T entity) {
        WriteResult result = getCollection().save(entity);
        return result.isUpdateOfExisting();
    }

    public void insert(T entity) {
        getCollection().insert(entity);
    }

    public void insert(Collection<T> entities) {
        getCollection().insert(entities.toArray());
    }

    public void removeById(String id) {
        remove(withOid(id));
    }

    public void remove(String query, Object... parameters) {
        getCollection().remove(query, parameters);
    }

    protected final MongoCollection getCollection() {
        return jongo.getCollection(collection);
    }

    protected static String joinIntoProjection(List<String> requiredFields, List<String> visibleColumns, String
            requiredColumn) {
        return visibleColumns.stream()
                .distinct()
                .filter(Objects::nonNull)
                .map(column -> columnToQueryPropertyFormat(requiredFields, column, requiredColumn))
                .collect(QUERY_PROPERTIES_COLLECTOR);
    }

    private static String columnToQueryPropertyFormat(List<String> requiredFields, String column, String
            requiredColumn) {
        return requiredFields.contains(column) ?
                format("'%s': 1", column) : format("'%s.%s': 1", requiredColumn, column);
    }
}
