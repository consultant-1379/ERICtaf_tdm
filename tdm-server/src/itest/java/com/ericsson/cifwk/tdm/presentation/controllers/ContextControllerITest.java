package com.ericsson.cifwk.tdm.presentation.controllers;

import com.ericsson.cifwk.tdm.api.model.Context;
import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.api.model.UserCredentials;
import com.ericsson.cifwk.tdm.application.contexts.TceContextRepository;
import com.ericsson.cifwk.tdm.configuration.MockRestRepositoriesConfiguration;
import com.ericsson.cifwk.tdm.infrastructure.ScheduledTasks;
import com.ericsson.cifwk.tdm.presentation.controllers.client.ContextControllerClient;
import com.ericsson.cifwk.tdm.presentation.controllers.client.DataSourceControllerClient;
import com.ericsson.gic.tms.presentation.dto.ContextBean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static com.ericsson.cifwk.tdm.application.util.JsonParser.parseListFile;
import static com.ericsson.cifwk.tdm.configuration.ITestsProfiles.MOCK_REST_REPOSITORIES;
import static com.ericsson.cifwk.tdm.configuration.MockRestRepositoriesConfiguration.CONTEXT_ID_1;
import static com.ericsson.cifwk.tdm.configuration.MockRestRepositoriesConfiguration.CONTEXT_ID_2;
import static com.ericsson.cifwk.tdm.configuration.MockRestRepositoriesConfiguration.CONTEXT_WITH_NO_USERS;
import static com.ericsson.cifwk.tdm.configuration.MockRestRepositoriesConfiguration.TEST_USER;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({INTEGRATION_TEST, MOCK_REST_REPOSITORIES})
public class ContextControllerITest {

    @MockBean
    private TceContextRepository contextClient;

    @Autowired
    private ScheduledTasks scheduledTasks;

    @Autowired
    ContextControllerClient contextControllerClient;

    @Autowired
    private DataSourceControllerClient dataSourceControllerClient;

    @Before
    public void setUp() throws Exception {
        List<ContextBean> contextList = parseListFile("contexts/contexts.json", ContextBean.class);
        when(contextClient.getContexts()).thenReturn(contextList);

        scheduledTasks.runJobLoadContexts();
    }

    @Test
    public void shouldGetUpdatedContexts() throws Exception {
        List<ContextBean> contextList = parseListFile("contexts/contexts-update.json", ContextBean.class);
        when(contextClient.getContexts()).thenReturn(contextList);

        List<Context> contextsBeforeUpdate =  contextControllerClient.getContexts();

        assertThat(contextsBeforeUpdate).contains(context("systemId-1", "System", null, null));
        assertThat(contextsBeforeUpdate).contains(context("systemId-3", "PDU_OSS", "systemId-1", "4"));
        assertThat(contextsBeforeUpdate).contains(context("systemId-2", "PDU-ABC", "systemId-1", "4"));
        assertThat(contextsBeforeUpdate).doesNotContain(context("systemId-6", "One More Department", "systemId-1", "4"));

        scheduledTasks.runJobLoadContexts();

        List<Context> contextsAfterUpdate =  contextControllerClient.getContexts();

        assertThat(contextsAfterUpdate).contains(context("systemId-1", "System-updated", null, null));
        assertThat(contextsAfterUpdate).contains(context("systemId-3", "PDU_OSS_Updated", "systemId-1", "4"));
        assertThat(contextsAfterUpdate).contains(context("systemId-2", "PDU-ABC_Updated", "systemId-1", "4"));
        assertThat(contextsAfterUpdate).contains(context("systemId-6", "One More Department", "systemId-1", "4"));
    }

    @Test
    public void shouldGetContextByName() throws  Exception {
        Context context = contextControllerClient.getContextByName("PDU-CBA");
        assertThat(context.getName()).isEqualTo("PDU-CBA");
    }

    @Test
    public void shouldGetContextByPath() throws Exception {
        Context context = contextControllerClient.getContextByPath("System/PDU-ABC/PDU-CBA");
        assertThat(context.getName()).isEqualTo("PDU-CBA");
    }

    @Test
    public void findAllContextUsersByNonExistingContextId() throws  Exception {
        List<User> userContexts = contextControllerClient.findContextUsers("noneExistingContextId");
        assertThat(userContexts).isEmpty();
    }

    @Test
    public void findAllContextUsersByContextId() throws  Exception {
        List<User> userContexts = contextControllerClient.findContextUsers(CONTEXT_ID_2);

        assertThat(userContexts).hasSize(6);
        assertThat(userContexts)
            .extracting("username")
            .containsExactly("manager", "admin", "security", "taf", "taf2", "anonymousUser")
            .doesNotContain("viewer");
        assertThat(userContexts)
            .extracting("firstName")
            .containsExactly("Anders", "Brian", "Charles", null, null, null);
        assertThat(userContexts)
            .extracting("lastName")
            .containsExactly("Broman", "Moran", "Karlsson", null, null, null);
        assertThat(userContexts)
            .extracting("email")
            .doesNotContainNull();
        assertThat(userContexts)
            .extracting("id")
            .containsExactly(1130L, 1131L, 1132L, null, 1134L, 1133L);
    }

    @Test
    public void findContextUsersByUsername() throws  Exception {
        String query = "adm";
        List<User> userContexts = contextControllerClient.findContextUsers(CONTEXT_ID_2, query);
        assertThat(userContexts).hasSize(1);
        assertThat(userContexts)
            .extracting("username")
            .containsExactly(query + "in");
    }

    @Test
    public void findContextUsersByEmail() throws  Exception {
        String query = "charles.karlsson";
        List<User> userContexts = contextControllerClient.findContextUsers(CONTEXT_ID_2, query);
        assertThat(userContexts).hasSize(1);
        assertThat(userContexts)
            .extracting("email")
            .containsExactly(query + "@ericsson.se");
    }

    @Test
    public void findContextUsersByEmailAndLimit() throws  Exception {
        String query = "charles.karlsson";
        List<User> userContexts = contextControllerClient.findContextUsers(CONTEXT_ID_2, query, 1);
        assertThat(userContexts).hasSize(1);
        assertThat(userContexts)
            .extracting("email")
            .containsExactly(query + "@ericsson.se");
    }

    @Test
    public void findNthUsers() throws  Exception {
        int limit = 2;
        List<User> userContexts = contextControllerClient.findContextUsers(CONTEXT_ID_2, limit);
        assertThat(userContexts).hasSize(limit);
    }

    private static Context context(String id, String name, String parentId, String activeModelId) {
        Context context = new Context();
        context.setId(id);
        context.setName(name);
        context.setParentId(parentId);
        context.setActiveModelId(activeModelId);
        return context;
    }

    @Test
    public void shouldValidateUserForContextAccess() throws Exception {
        Map map = contextControllerClient.validateContextUser(CONTEXT_ID_1);
        assertThat(map.get("validated")).isEqualTo(true);
    }

    @Test
    public void shouldNotValidateUserForContextAccess() throws Exception {
        Map map = contextControllerClient.validateContextUser(CONTEXT_WITH_NO_USERS);
        assertThat(map.get("validated")).isEqualTo(false);
    }
}
