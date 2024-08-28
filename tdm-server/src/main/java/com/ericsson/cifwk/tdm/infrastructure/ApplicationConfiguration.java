package com.ericsson.cifwk.tdm.infrastructure;


import com.google.common.collect.Sets;
import org.apache.catalina.connector.Connector;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.ericsson.cifwk.tdm.infrastructure.Profiles.LDAP_CUSTOMER;

@Configuration
@EnableSwagger2
@Profile(LDAP_CUSTOMER)
public class ApplicationConfiguration {
    @Value("${server.port}")
    private String serverPort;

    @Value("${management.port:${server.port}}")
    private String managementPort;

    @Value("${server.additionalPorts}")
    private String additionalPorts;

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
        Connector[] additionalConnectors = this.additionalConnector();
        if (additionalConnectors != null && additionalConnectors.length > 0) {
            tomcat.addAdditionalTomcatConnectors(additionalConnectors);
        }
        return tomcat;
    }

    private Connector[] additionalConnector() {
        if (StringUtils.isBlank(this.additionalPorts)) {
            return null;
        }
        Set<String> defaultPorts = Sets.newHashSet(this.serverPort, this.managementPort);
        String[] ports = this.additionalPorts.split(",");
        List<Connector> result = new ArrayList<>();
        for (String port : ports) {
            if (!defaultPorts.contains(port)) {
                Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
                connector.setScheme("http");
                connector.setPort(Integer.valueOf(port));
                result.add(connector);
            }
        }
        return result.toArray(new Connector[] {});
    }
}
