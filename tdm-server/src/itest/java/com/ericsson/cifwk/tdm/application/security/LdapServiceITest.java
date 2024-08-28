package com.ericsson.cifwk.tdm.application.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.application.contexts.TceContextRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(INTEGRATION_TEST)
public class LdapServiceITest {

    @Autowired
    private LdapService ldapService;

    @MockBean
    private TceContextRepository contextClient;

    @Test
    public void findCoupleOfUsers() {
        List<User> users = ldapService.findUsers("Charl", 2);
        assertThat(users)
            .hasSize(2)
            .extracting("firstName", "lastName")
            .containsExactlyInAnyOrder(
                tuple("Charles", "Karlsson"),
                tuple("Charles", "Borreson"));
    }

    @Test
    public void testCountLimit() {
        List<User> users = ldapService.findUsers("Oliv", 1);

        assertThat(users).hasSize(1);
    }

    @Test
    public void testFindByPrefix() {
        List<User> users = ldapService.findUsers("Borr", 1);

        assertThat(users)
            .hasSize(1)
            .extracting("firstName", "lastName")
            .containsExactly(tuple("Oliver", "Borreson"));
    }

    @Test
    public void testFindByContains() {
        List<User> users = ldapService.findUsers("liv", 1);
        assertThat(users).hasSize(0);
    }

    @Test
    public void findExactUserByLoginUsers() {
        List<User> users = ldapService.findUsers("taf", 1);

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getUsername()).isEqualTo("taf");
        assertThat(users.get(0).getFirstName()).isEqualTo("Oliver");
    }

    @Test
    public void findUserWithMissingEmail() {
        List<User> users = ldapService.findUsers("Matil", 1);
        assertThat(users).hasSize(1);
        assertThat(users).extracting(User::getEmail).containsNull();
    }

    @Test
    public void findUserByFullName() {
        List<User> users = ldapService.findUsers("Charles Karlsson", 1);
        assertThat(users).hasSize(1);
        assertThat(users).extracting(User::getUsername).as("username").contains("user");
        assertThat(users).extracting(User::getFirstName).as("firstName").contains("Charles");
        assertThat(users).extracting(User::getLastName).as("lastName").contains("Karlsson");
    }

    @Test
    public void testEncoding() {
        List<User> users = ldapService.findUsers("a*n", 10);

        assertThat(users).isEmpty();
    }
}
