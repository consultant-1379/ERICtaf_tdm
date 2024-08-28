package com.ericsson.cifwk.tdm.presentation.controllers.client;

import com.ericsson.cifwk.tdm.model.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.ericsson.cifwk.tdm.application.util.JsonParser.toJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 22/02/2016
 */
@Service
public class ExecutionControllerClient {

    private MockMvc mockMvc;

    @Autowired
    public ExecutionControllerClient(WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    public MockHttpServletResponse createExecution(Execution execution) throws Exception {
        return mockMvc.perform(post("/api/executions")
                .contentType(MediaType.APPLICATION_JSON).content(toJson(execution)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
    }

    public MockHttpServletResponse getAllExecutions() throws Exception {
        return mockMvc.perform(get("/api/executions"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
    }

    public MockHttpServletResponse finishExecution(String executionId) throws Exception {
        return mockMvc.perform(patch("/api/executions/" + executionId))
                .andExpect(status().isOk())
                .andReturn().getResponse();
    }
}
