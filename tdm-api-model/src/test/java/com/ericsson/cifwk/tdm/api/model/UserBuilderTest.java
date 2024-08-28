package com.ericsson.cifwk.tdm.api.model;

import org.junit.Test;

import static com.ericsson.cifwk.tdm.api.model.UserBuilder.anUser;
import static com.google.common.truth.Truth.assertThat;

public class UserBuilderTest {

    @Test
    public void build_empty() throws Exception {
        User user = anUser().build();

        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isNull();
        assertThat(user.getFirstName()).isNull();
        assertThat(user.getLastName()).isNull();
        assertThat(user.getEmail()).isNull();
    }

    @Test
    public void build_full() throws Exception {
        User user = anUser()
                .withId(42L)
                .withUsername("username")
                .withFirstName("firstName")
                .withLastName("lastName")
                .withEmail("email")
                .build();

        assertThat(user.getId()).isEqualTo(42L);
        assertThat(user.getUsername()).isEqualTo("username");
        assertThat(user.getFirstName()).isEqualTo("firstName");
        assertThat(user.getLastName()).isEqualTo("lastName");
        assertThat(user.getEmail()).isEqualTo("email");
    }
}