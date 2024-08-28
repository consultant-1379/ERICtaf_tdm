package com.ericsson.cifwk.tdm.model;

import com.google.common.base.Objects;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import java.util.Date;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Objects.equal;

/**
 * Created by egergle on 30/08/2017.
 */
public class UserSessionEntity {

    @MongoId
    @MongoObjectId
    private String id;

    private String username;

    private String sessionId;

    private Date createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, username, sessionId);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        UserSessionEntity userEntity = (UserSessionEntity) other;
        return equal(id, userEntity.id) &&
                equal(username, userEntity.username) &&
                equal(sessionId, userEntity.sessionId);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", id)
                .add("username", username)
                .add("sessionId", sessionId)
                .toString();
    }

}
