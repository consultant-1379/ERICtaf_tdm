package com.ericsson.cifwk.tdm.api.model;

import javax.validation.constraints.NotNull;

public class UserCredentials {

    @NotNull
    private String username;
    @NotNull
    private String password;

    public UserCredentials() { //NOSONAR
    }

    public UserCredentials(String username, String password) {  //NOSONAR
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
