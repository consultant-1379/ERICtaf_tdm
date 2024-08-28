package com.ericsson.cifwk.tdm.presentation.controllers.client;

import com.ericsson.cifwk.tdm.api.model.StatisticsObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

@Service
public class StatisticsControllerClient extends ControllerClientCommon {

    private static final String TYPE_PARAM = "type={type}";

    private static final String STATISTICS = "/api/statistics";
    private static final String STATISTICS_USERS = STATISTICS + "/users";
    private static final String STATISTICS_DATASOURCES = STATISTICS + "/dataSources?" + TYPE_PARAM;

    @Autowired
    public StatisticsControllerClient(WebApplicationContext webApplicationContext) {
        super(webApplicationContext);
    }

    public List<StatisticsObject> getUserUsage() throws Exception {
        return toList(get(STATISTICS_USERS), StatisticsObject.class);
    }

    public List<StatisticsObject> getDataSourcesUsage(String type) throws Exception {
        return toList(get(STATISTICS_DATASOURCES, type), StatisticsObject.class);
    }
}
