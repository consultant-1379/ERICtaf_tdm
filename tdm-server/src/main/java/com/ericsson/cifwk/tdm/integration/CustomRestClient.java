package com.ericsson.cifwk.tdm.integration;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.proxy.WebResourceFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.HttpMethod.PUT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.PROXY_URI;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.REQUEST_ENTITY_PROCESSING;
import static org.glassfish.jersey.client.ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION;
import static org.glassfish.jersey.client.RequestEntityProcessing.BUFFERED;

/**
 * This is instance of proxy REST client using direct external API as a resource objects.
 */
public class CustomRestClient {

    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final int DEFAULT_READ_TIMEOUT = 5000;

    private final WebTarget webTarget;

    public CustomRestClient(WebTarget webTarget) {
        this.webTarget = webTarget;
    }

    public <T> T get(Class<T> resourceClass) {
        return WebResourceFactory.newResource(resourceClass, webTarget);
    }

    public static class Builder {

        private final String url;

        private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private int readTimeout = DEFAULT_READ_TIMEOUT;
        private List<Object> components = newArrayList();
        private String proxyUri;

        public Builder(String url) {
            this.url = url;
        }

        public Builder withProxy(String proxyUri) {
            this.proxyUri = proxyUri;
            return this;
        }

        public Builder withConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder withReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder withComponent(Object component) {
            components.add(component);
            return this;
        }

        public CustomRestClient build() {

            ClientConfig clientConfig = new ClientConfig();

            if (proxyUri != null) {
                clientConfig.connectorProvider(new ApacheConnectorProvider());
                clientConfig.property(PROXY_URI, proxyUri);
            }

            Client client = ClientBuilder.newClient(clientConfig)
                // This must be set to allow PUT requests without entities
                .property(SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
                // Buffer entities to set Content-Length for proxy
                .property(REQUEST_ENTITY_PROCESSING, BUFFERED)
                .property(CONNECT_TIMEOUT, connectTimeout)
                .property(READ_TIMEOUT, readTimeout)
                .register(ClientLoggingFilter.class)
                // Add zero content length for put requests
                .register((ClientRequestFilter) req -> {
                    if (hasNoRequestEntity(req)) {
                        req.getHeaders().putSingle(CONTENT_LENGTH, 0);
                    }
                });

            for (Object component : components) {
                client = client.register(component);
            }
            WebTarget target = client.target(url);
            return new CustomRestClient(target);
        }
    }

    private static boolean hasNoRequestEntity(ClientRequestContext reqContext) {
        return !reqContext.hasEntity()
            && (PUT.equalsIgnoreCase(reqContext.getMethod()) || POST.equalsIgnoreCase(reqContext.getMethod()));
    }

}
