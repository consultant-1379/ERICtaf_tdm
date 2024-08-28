package com.ericsson.cifwk.tdm.presentation.controllers;

import com.ericsson.cifwk.tdm.db.MongoBee;
import com.ericsson.cifwk.tdm.model.Execution;
import com.ericsson.cifwk.tdm.presentation.controllers.client.ExecutionControllerClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ericsson.cifwk.tdm.application.util.JsonParser.parseList;
import static com.ericsson.cifwk.tdm.application.util.JsonParser.parseObject;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(INTEGRATION_TEST)
@MongoBee(location = "com.ericsson.cifwk.tdm.infrastructure.changelogs")
public class ExecutionControllerITest {

    @Autowired
    ExecutionControllerClient executionControllerClient;

    @Test
    public void shouldCreateExecution() throws Exception {
        Execution execution = new Execution();
        execution.getProperties().put("team", "Washington Capitals");

        MockHttpServletResponse response = executionControllerClient.createExecution(execution);

        Execution createdExecution = parseObject(response.getContentAsString(), Execution.class);

        assertThat(createdExecution.getProperties().get("team")).isEqualTo("Washington Capitals");
    }

    @Test
    public void shouldReturnAllExecutions() throws Exception {
        Execution execution = new Execution();
        execution.getProperties().put("team", "Washington Capitals");

        executionControllerClient.createExecution(execution);

        execution.getProperties().put("team", "Red Wings");
        executionControllerClient.createExecution(execution);

        MockHttpServletResponse response = executionControllerClient.getAllExecutions();

        List<Execution> executions = parseList(response.getContentAsString(), Execution.class);

        Set<String> executionTeams = executions.stream()
                .map(e -> e.getProperties().get("team").toString())
                .collect(Collectors.toSet());

        assertTrue(executionTeams.contains("TAF"));
        assertTrue(executionTeams.contains("Red Wings"));
        assertTrue(executionTeams.contains("Washington Capitals"));
    }

    @Test
    public void shouldFinishExecution() throws Exception {
        MockHttpServletResponse response = executionControllerClient.finishExecution("56c5ddf29759e577fc68ab7f");
        Execution execution = parseObject(response.getContentAsString(), Execution.class);
        assertThat(execution.getEndTime()).isNotNull();
    }
}
