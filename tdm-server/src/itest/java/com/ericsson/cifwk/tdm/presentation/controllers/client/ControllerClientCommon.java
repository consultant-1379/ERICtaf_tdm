package com.ericsson.cifwk.tdm.presentation.controllers.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static com.ericsson.cifwk.tdm.application.util.DataParser.readTextFile;
import static com.ericsson.cifwk.tdm.application.util.JsonParser.parseList;
import static com.ericsson.cifwk.tdm.application.util.JsonParser.parseObject;
import static com.ericsson.cifwk.tdm.application.util.JsonParser.toJson;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Service
public class ControllerClientCommon {

    private MockMvc mockMvc;

    @Autowired
    public ControllerClientCommon(WebApplicationContext webApplicationContext) {
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    /*---------------- Common Mock MVC scenarios ----------------*/

    ResultActions get(String uriTemplate, Object... uriVariables) throws Exception {
        return requestJsonFrom(HttpMethod.GET, uriTemplate, uriVariables);
    }

    ResultActions delete(String urlTemplate, Object... uriVariables) throws Exception {
        return requestJsonFrom(HttpMethod.DELETE, urlTemplate, uriVariables);
    }

    ResultActions postResource(String resourceName, String uriTemplate, Object... uriVariables) throws Exception {
        String json = readTextFile(resourceName);
        return sendJsonTo(json, HttpMethod.POST, uriTemplate, uriVariables);
    }

    ResultActions postObject(Object value, String uriTemplate, Object... uriVariables) throws Exception {
        String json = toJson(value);
        return sendJsonTo(json, HttpMethod.POST, uriTemplate, uriVariables);
    }

    ResultActions patch(Object value, String urlTemplate, Object... uriVariables) throws Exception {
        String json = toJson(value);
        return sendJsonTo(json, HttpMethod.PATCH, urlTemplate, uriVariables);
    }

    /*---------------- Helper methods ----------------*/

    private ResultActions requestJsonFrom(HttpMethod method, String urlTemplate, Object... uriVariables) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = request(method, urlTemplate, uriVariables)
                .accept(MediaType.APPLICATION_JSON);
        return mockMvc.perform(requestBuilder);
    }

    private ResultActions sendJsonTo(String json, HttpMethod method,
                                     String urlTemplate, Object... uriVariables) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = request(method, urlTemplate, uriVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
        return mockMvc.perform(requestBuilder);
    }

    <T> T toObject(ResultActions resultActions, Class<T> clazz) throws Exception {
        MockHttpServletResponse response = getResponse(resultActions);
        return parseObject(response.getContentAsString(), clazz);
    }

    <T> List<T> toList(ResultActions resultActions, Class<T> clazz) throws Exception {
        MockHttpServletResponse response = getResponse(resultActions);
        return parseList(response.getContentAsString(), clazz);
    }

    MockHttpServletResponse getResponse(ResultActions resultActions) throws Exception {
        return resultActions

                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse();
    }

    <T> T readEntity(MockHttpServletResponse response, Class<T> clazz) throws Exception {
        return parseObject(response.getContentAsString(), clazz);
    }
}
