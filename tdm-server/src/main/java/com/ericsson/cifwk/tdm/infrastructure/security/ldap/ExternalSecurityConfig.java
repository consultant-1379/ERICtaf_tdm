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

import static com.ericsson.cifwk.tdm.infrastructure.Profiles.LDAP;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.LDAP_CUSTOMER;

@Configuration
@EnableWebSecurity
@Profile({LDAP, LDAP_CUSTOMER})
public class ExternalSecurityConfig extends AbstractSecurityConfig {

    @Value("${ldap.url}")
    private String url;

    @Value("${ldap.search.base}")
    private String searchBase;

    @Value("${ldap.dnPattern1}")
    private String dnPattern1;

    @Value("${ldap.dnPattern2}")
    private String dnPattern2;

    @Value("${ldap.user}")
    private String user;

    @Value("${ldap.password}")
    private String password;

    @Autowired
    private CustomAuthoritiesPopulator authoritiesPopulator;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.ldapAuthentication()
            .ldapAuthoritiesPopulator(authoritiesPopulator)
            .userDnPatterns(dnPattern1, dnPattern2)
            .groupSearchBase(searchBase)
            .contextSource()
            .managerDn(user)
            .managerPassword(password)
            .url(url);
    }

    @Bean
    @Override
    public LdapContextSource ldapContextSource() {
        LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl(url);
        ldapContextSource.setBase(searchBase);
        ldapContextSource.setUserDn(user);
        ldapContextSource.setPassword(user);
        ldapContextSource.afterPropertiesSet();
        return ldapContextSource;
    }
}
