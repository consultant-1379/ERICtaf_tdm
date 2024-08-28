package com.ericsson.cifwk.tdm.api.model;

public final class ContextBuilder {
    private String id;
    private String type;
    private String name;
    private String parentId;
    private String activeModelId;

    private ContextBuilder() {
    }

    public static ContextBuilder aContextBuilder() {
        return new ContextBuilder();
    }

    public ContextBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public ContextBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public ContextBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ContextBuilder withParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public ContextBuilder withActiveModelId(String activeModelId) {
        this.activeModelId = activeModelId;
        return this;
    }

    public Context build() {
        Context context = new Context();
        context.setId(id);
        context.setName(name);
        context.setType(type);
        context.setParentId(parentId);
        context.setActiveModelId(activeModelId);
        return context;
    }

}
