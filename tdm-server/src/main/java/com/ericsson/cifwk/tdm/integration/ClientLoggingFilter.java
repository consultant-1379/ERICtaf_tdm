package com.ericsson.cifwk.tdm.integration;

import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Priority(Priorities.HEADER_DECORATOR)
public class ClientLoggingFilter implements ClientRequestFilter, ClientResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientLoggingFilter.class);

    @Override
    public void filter(ClientRequestContext req) throws IOException {
        LOGGER.info("{} {}", req.getMethod(), req.getUri());

        req.getHeaders().forEach((key, value) ->
                LOGGER.debug("Request header: {} = {}", key, value));
    }

    @Override
    public void filter(ClientRequestContext req, ClientResponseContext res) throws IOException {
        LOGGER.info("Response status: {}", res.getStatus());
        LOGGER.info("Response location: {}", res.getLocation());

        if (res.getMediaType() != null) {
            LOGGER.info("Response media-type: {}", res.getMediaType().getType());
        }
    }

}
