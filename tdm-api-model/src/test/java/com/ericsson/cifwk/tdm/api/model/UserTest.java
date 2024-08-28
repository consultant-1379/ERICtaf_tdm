package com.ericsson.cifwk.tdm.api.model;

import org.junit.Test;

import static com.ericsson.cifwk.tdm.api.model.UserBuilder.anUser;
import static com.google.common.truth.Truth.assertThat;

public class UserTest {

    @Test
    public void equals_shouldReturnFalse_whenUsers_haveDifferentIds() throws Exception {
        User user1 = user()
                .withId(1L)
                .build();
        User user2 = user()
                .withId(2L)
                .build();

        assertThat(user1.equals(user2)).isFalse();
        assertThat(user2.equals(user1)).isFalse();
    }

    @Test
    public void equals_shouldReturnFalse_whenUsers_haveDifferentUsernames() throws Exception {
        User user1 = user()
                .withUsername("username1")
                .build();
        User user2 = user()
                .withUsername("username2")
                .build();

        assertThat(user1.equals(user2)).isFalse();
        assertThat(user2.equals(user1)).isFalse();

    }

    @Test
    public void equals_shouldReturnTrue_whenUsers_haveEqualIdsAndUsernames() throws Exception {
        User user1 = user()
                .withFirstName("firstName1")
                .withLastName("lastName1")
                .withEmail("email1")
                .build();
        User user2 = user()
                .withFirstName("firstName2")
                .withLastName("lastName2")
                .withEmail("email2")
                .build();

        assertThat(user1.equals(user2)).isTrue();
        assertThat(user2.equals(user1)).isTrue();
    }

    private UserBuilder user() {
        return anUser()
                .withId(42L)
                .withUsername("username")
                .withFirstName("firstName")
                .withLastName("lastName")
                .withEmail("email");
    }
}