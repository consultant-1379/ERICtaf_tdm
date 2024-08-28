package com.ericsson.cifwk.tdm.api.model;

import java.util.List;

public class AuthenticationStatus {

    private boolean authenticated;
    private String username;
    private List<ContextRole> roles;
    private boolean customerProfile = false;

    public AuthenticationStatus() {
        //NO SONAR
    }

    public AuthenticationStatus(String username, boolean authenticated, List<ContextRole> roles,
                                boolean customerProfile) {
        this.username = username;
        this.authenticated = authenticated;
        this.roles = roles;
        this.customerProfile = customerProfile;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<ContextRole> getRoles() {
        return roles;
    }

    public void setRoles(List<ContextRole> roles) {
        this.roles = roles;
    }

    public boolean isCustomerProfile() {
        return customerProfile;
    }

    public void setCustomerProfile(boolean customerProfile) {
        this.customerProfile = customerProfile;
    }
}
