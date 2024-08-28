package com.ericsson.cifwk.tdm.netsim.agent.output;

import com.ericsson.cifwk.tdm.api.model.DataSource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


/**
 * Created by ekonsla on 04/03/2016.
 */
public class TextOutputAdapter implements DataSourceOutputAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextOutputAdapter.class);

    @Override
    public void output(DataSource dataSource) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String text = objectMapper.writeValueAsString(dataSource);
            FileUtils.writeStringToFile(new File("output.json"), text);
        } catch (JsonProcessingException e) {
            LOGGER.error("json mapping failed", e);
        } catch (IOException ie) {
            LOGGER.error("write to file failed", ie);
        }
    }
}
