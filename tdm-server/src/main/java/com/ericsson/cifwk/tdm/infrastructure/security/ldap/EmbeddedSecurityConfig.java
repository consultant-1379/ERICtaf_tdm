package com.ericsson.cifwk.tdm.infrastructure.security.ldap;

import com.ericsson.cifwk.tdm.application.security.CustomAuthoritiesPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import static com.ericsson.cifwk.tdm.infrastructure.Profiles.LDAP_EMBEDDED;

@Configuration
@EnableWebSecurity
@Profile(LDAP_EMBEDDED)
public class EmbeddedSecurityConfig extends AbstractSecurityConfig {

    @Value("${ldap.url}")
    private String url;

    @Value("${ldap.dnPattern}")
    private String dnPattern;

    @Value("${ldap.ldiff}")
    private String ldiff;

    @Autowired
    private CustomAuthoritiesPopulator authoritiesPopulator;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .ldapAuthentication()
            .ldapAuthoritiesPopulator(authoritiesPopulator)
            .userDnPatterns(dnPattern)
            .contextSource().ldif("classpath:" + ldiff)
            .root("dc=springframework,dc=org");
    }

    @Bean
    @Override
    public LdapContextSource ldapContextSource() {
        LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl(url);
        ldapContextSource.afterPropertiesSet();
        return ldapContextSource;
    }
}
