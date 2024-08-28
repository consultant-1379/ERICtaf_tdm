package com.ericsson.cifwk.tdm.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Objects.equal;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Context {
    private String id;

    private String type;

    private String name;

    private String parentId;

    private String activeModelId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getActiveModelId() {
        return activeModelId;
    }

    public void setActiveModelId(String activeModelId) {
        this.activeModelId = activeModelId;
    }

    @Override
    public String toString() {
        return toStringHelper(getClass())
                .add("id", id)
                .add("type", type)
                .add("name", name)
                .add("parentId", parentId)
                .add("activeModelId", activeModelId)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, type, name, parentId, activeModelId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Context other = (Context) obj;
        return equal(this.id, other.id)
                && equal(this.name, other.getName())
                && equal(this.parentId, other.getParentId())
                && equal(this.activeModelId, other.getActiveModelId());
    }
}
