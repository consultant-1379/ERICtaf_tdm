package com.ericsson.cifwk.tdm.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import java.util.Properties;

@EnableAsync
@Configuration
public class EmailConfiguration {

    public static final String DEFAULT_ENCODING = "utf-8";

    @Value("${email.protocol}")
    private String protocol;

    @Value("${email.host}")
    private String host;

    @Value("${email.port}")
    private int port;

    @Value("${email.from}")
    private String from;

    @Value("${email.username}")
    private String username;

    @Value("${email.password}")
    private String password;

    @Value("${email.smtp.auth}")
    private boolean isAuthEnabled;

    @Value("${email.smtp.starttls.enable}")
    private boolean isTlsEnabled;


    @Bean
    @Primary
    public FreeMarkerConfigurationFactoryBean freemarkerConfig() {
        FreeMarkerConfigurationFactoryBean config = new FreeMarkerConfigurationFactoryBean();
        config.setTemplateLoaderPath("classpath:/templates");
        config.setDefaultEncoding(DEFAULT_ENCODING);
        return config;
    }

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setJavaMailProperties(getMailProperties());
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setProtocol(protocol);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        return mailSender;
    }

    private Properties getMailProperties() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", isAuthEnabled);
        properties.put("mail.smtp.starttls.enable", isTlsEnabled);
        properties.put("mail.smtp.from", from);
        properties.put("mail.debug", "false");
        return properties;
    }
}
