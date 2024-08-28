package com.ericsson.cifwk.tdm;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.client.RestTemplate;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;

@EnableCaching
@SpringBootApplication
@EnableCircuitBreaker
@EnableEncryptableProperties
@EnableAutoConfiguration(exclude = {
        MongoDataAutoConfiguration.class,
        MongoAutoConfiguration.class,
        EmbeddedMongoAutoConfiguration.class})
public class Application extends SpringBootServletInitializer {

    @Bean
    public ValidatorFactory validatorFactory() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public Validator validator() {
        return validatorFactory().getValidator();
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor mvpp = new MethodValidationPostProcessor();
        mvpp.setValidator(validator());
        return mvpp;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
