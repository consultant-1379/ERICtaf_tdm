package com.ericsson.cifwk.tdm.presentation.controllers.client;

import com.ericsson.cifwk.tdm.api.model.Lock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.ericsson.cifwk.tdm.application.util.JsonParser.toJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 22/02/2016
 */
@Service
public class LockControllerClient {

    private MockMvc mockMvc;

    @Autowired
    public LockControllerClient(WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    public MockHttpServletResponse createLock(Lock lock) throws Exception {
        return mockMvc.perform(post("/api/locks")
                .contentType(MediaType.APPLICATION_JSON).content(toJson(lock)))
                .andReturn().getResponse();
    }

    public MockHttpServletResponse getDataRecordsForLock(String lockId) throws Exception {
        return mockMvc.perform(get("/api/locks/" + lockId + "/records"))
                .andReturn().getResponse();
    }

    public MockHttpServletResponse releaseLock(String lockId) throws Exception {
        return mockMvc.perform(delete("/api/locks/" + lockId ))
                .andReturn().getResponse();
    }
}
