package com.ericsson.cifwk.tdm.presentation.controllers;

import com.ericsson.cifwk.tdm.application.ciportal.testware.CIPortalTestwareRepository;
import com.ericsson.cifwk.tdm.application.contexts.TceContextRepository;
import com.ericsson.cifwk.tdm.application.user.TceUserRepository;
import com.ericsson.cifwk.tdm.model.testware.ArtifactItems;
import com.ericsson.gic.tms.presentation.dto.ContextBean;
import com.ericsson.gic.tms.presentation.dto.users.UserBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;
import static com.google.common.truth.Truth.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(INTEGRATION_TEST)
public class HystrixITest {

    @Autowired
    private TceContextRepository contextClient;

    @Autowired
    private CIPortalTestwareRepository ciPortalTestwareRepository;

    @Autowired
    private TceUserRepository tceUserRepository;

    @Test
    public void shouldUseHystrixFallbacks() throws Exception {
        List<ContextBean> contexts = contextClient.getContexts();
        assertThat(contexts.isEmpty()).isTrue();

        ArtifactItems testwareArtifacts = ciPortalTestwareRepository.getTestwareArtifacts();
        assertThat(testwareArtifacts.getArtifacts().isEmpty()).isTrue();
    }

    @Test
    public void shouldNotGetUsersThenCreatedUser() throws Exception {
        String username = "test";
        UserBean userBean = tceUserRepository.findByUsername(username);
        assertThat(userBean.getUsername()).isEqualTo(username);
        assertThat(userBean.getId()).isNull();
    }
}
