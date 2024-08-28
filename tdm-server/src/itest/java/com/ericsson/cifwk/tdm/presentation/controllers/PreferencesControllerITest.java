package com.ericsson.cifwk.tdm.presentation.controllers;

import com.ericsson.cifwk.tdm.api.model.Preferences;
import com.ericsson.cifwk.tdm.api.model.validation.FieldError;
import com.ericsson.cifwk.tdm.api.model.validation.ValidationError;
import com.ericsson.cifwk.tdm.application.contexts.TceContextRepository;
import com.ericsson.cifwk.tdm.db.MongoBee;
import com.ericsson.cifwk.tdm.infrastructure.ScheduledTasks;
import com.ericsson.cifwk.tdm.presentation.controllers.client.PreferencesControllerClient;
import com.ericsson.gic.tms.presentation.dto.ContextBean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.ericsson.cifwk.tdm.api.model.Preferences.PreferencesBuilder.aPreferences;
import static com.ericsson.cifwk.tdm.application.util.JsonParser.parseList;
import static com.ericsson.cifwk.tdm.application.util.JsonParser.parseObject;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(INTEGRATION_TEST)
@MongoBee(location = "com.ericsson.cifwk.tdm.infrastructure.changelogs", invokeCleanBeforeMethod = true)
public class PreferencesControllerITest {

    @Autowired
    private PreferencesControllerClient client;

    @MockBean
    private TceContextRepository contextClient;

    @Autowired
    private ScheduledTasks scheduledTasks;

    @Before
    public void setUp() throws Exception {
        presetContextList();
    }

    @Test
    public void getByUserId_shouldFail_when_preferences_doNotExist() throws Exception {
        MockHttpServletResponse response = client.getByUserId("userId");

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void getByUserId_shouldSucceed_when_preferences_doExist() throws Exception {
        Preferences preferences = saveNewPreferences();

        MockHttpServletResponse response = client.getByUserId(preferences.getUserId());
        Preferences responseBody = parseObject(response.getContentAsString(), Preferences.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(responseBody.getUserId()).isEqualTo(preferences.getUserId());
        assertThat(responseBody.getContextId()).isEqualTo(preferences.getContextId());
    }

    @Test
    public void save_shouldFail_when_notNull_constraintViolated() throws Exception {
        Preferences preferences = aPreferences()
                .withUserId(null)
                .withContextId(null)
                .withContextName(null)
                .build();

        MockHttpServletResponse response = client.save(preferences);
        ValidationError error = parseObject(response.getContentAsString(), ValidationError.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(error.getFieldErrors()).containsExactly(
                new FieldError("userId", "may not be null"),
                new FieldError("contextId", "may not be null")
        );
    }

    @Test
    public void save_shouldFail_when_contextId_doesNotExist() throws Exception {
        Preferences preferences = aPreferences()
                .withUserId("userId")
                .withContextId("contextId")
                .build();

        MockHttpServletResponse response = client.save(preferences);
        List<String> errors = parseList(response.getContentAsString(), String.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(errors).containsExactly("ContextId doesn't exist");
    }

    @Test
    public void save_shouldSucceed_andReturn_status_CREATED_whenPreferences_doNotExist() throws Exception {
        saveNewPreferences();
    }

    @Test
    public void save_shouldSucceed_andReturn_status_NO_CONTENT_whenPreferences_doExist() throws Exception {
        Preferences preferences = saveNewPreferences();

        MockHttpServletResponse response = client.save(preferences);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    private Preferences saveNewPreferences() throws Exception {
        ContextBean tceContext = new ContextBean();
        tceContext.setId("contextId");
        presetContextList(tceContext);

        Preferences preferences = aPreferences()
                .withUserId("userId")
                .withContextId("contextId")
                .build();

        MockHttpServletResponse response = client.save(preferences);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        return preferences;
    }

    private void presetContextList(ContextBean... tceContexts) {
        doReturn(newArrayList(tceContexts)).when(contextClient).getContexts();
        scheduledTasks.runJobLoadContexts();
    }
}
