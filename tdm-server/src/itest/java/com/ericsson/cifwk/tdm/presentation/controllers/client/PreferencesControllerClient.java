package com.ericsson.cifwk.tdm.presentation.controllers.client;

import com.ericsson.cifwk.tdm.api.model.Preferences;
import com.ericsson.cifwk.tdm.presentation.controllers.PreferencesController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static com.ericsson.cifwk.tdm.application.util.JsonParser.toJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Service
public class PreferencesControllerClient {

    private static final String URL_TEMPLATE = PreferencesController.REQUEST_MAPPING;

    private MockMvc mockMvc;

    @Autowired
    public PreferencesControllerClient(WebApplicationContext webApplicationContext) {
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    public MockHttpServletResponse getByUserId(String userId) throws Exception {
        return mockMvc.perform(get(URL_TEMPLATE + "/" + userId))
                .andReturn().getResponse();
    }

    public MockHttpServletResponse save(Preferences preferences) throws Exception {
        return mockMvc.perform(put(URL_TEMPLATE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(preferences)))
                .andReturn().getResponse();
    }
}
