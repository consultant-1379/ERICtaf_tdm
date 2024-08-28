package com.ericsson.cifwk.tdm.presentation.controllers;

import com.ericsson.cifwk.tdm.api.model.AuthenticationStatus;
import com.ericsson.cifwk.tdm.api.model.ContextRole;
import com.ericsson.cifwk.tdm.api.model.UserCredentials;
import com.ericsson.cifwk.tdm.application.contexts.TceContextRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import javax.servlet.http.HttpSession;

import static com.ericsson.cifwk.tdm.application.util.JsonParser.parseObject;
import static com.ericsson.cifwk.tdm.application.util.JsonParser.toJson;
import static com.ericsson.cifwk.tdm.configuration.ITestsProfiles.MOCK_REST_REPOSITORIES;
import static com.ericsson.cifwk.tdm.configuration.MockRestRepositoriesConfiguration.TAF_USER;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({INTEGRATION_TEST, MOCK_REST_REPOSITORIES})
public class LoginControllerITest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private Filter springSecurityFilterChain;

    @MockBean
    private TceContextRepository contextClient;

    private MockMvc mockMvc;
    private SessionHolder sessionHolder;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    public void testLogin() throws Exception {
        performLogin();

        AuthenticationStatus loginStatus = getAuthenticationStatus();
        assertThat(loginStatus.isAuthenticated()).isTrue();
        assertThat(loginStatus.getRoles())
            .extracting(ContextRole::getRole)
            .containsExactly("ROLE_VIEWER", "ROLE_TEST_ENGINEER", "ROLE_TEST_MANAGER");
        assertThat(loginStatus.getRoles())
            .extracting(ContextRole::getContextId)
            .containsOnly("systemId-1");
    }

    @Test
    public void testLogout() throws Exception {
        performLogin();

        AuthenticationStatus loginAuthStatus = getAuthenticationStatus();
        assertThat(loginAuthStatus.isAuthenticated()).isTrue();
        performLogout();

        AuthenticationStatus logoutAuthStatus = getAuthenticationStatus();
        assertThat(logoutAuthStatus.isAuthenticated()).isFalse();
        assertThat(logoutAuthStatus.getRoles()).isEmpty();
    }

    private AuthenticationStatus getAuthenticationStatus() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/login").session(sessionHolder))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        return parseObject(mvcResult.getResponse().getContentAsString(), AuthenticationStatus.class);
    }

    private void performLogin() throws Exception {
        String credentials = toJson(new UserCredentials(TAF_USER, "taf"));
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isOk()).andDo(result -> sessionHolder = new SessionHolder(result.getRequest().getSession()));
    }

    private void performLogout() throws Exception {
        mockMvc.perform(delete("/api/login").session(sessionHolder)) // /api is handles by spring security
                .andExpect(status().isOk())
                .andReturn();
    }

    private static class SessionHolder extends MockHttpSession {
        private HttpSession session;

        public SessionHolder(HttpSession session) {
            this.session = session;
        }

        @Override
        public Object getAttribute(String name) {
            return session.getAttribute(name);
        }
    }
}
