package com.ericsson.cifwk.tdm.api.model;

public class ContextRole {

    private String contextId;

    private String role;

    public ContextRole() {
        //NO SONAR
    }

    public ContextRole(String contextId, String role) {
        this.contextId = contextId;
        this.role = role;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
