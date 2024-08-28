package com.ericsson.cifwk.tdm.infrastructure.security.ldap;

import com.ericsson.cifwk.tdm.infrastructure.security.login.HttpAuthenticationEntryPoint;
import com.ericsson.cifwk.tdm.infrastructure.security.login.HttpLogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.ws.rs.HttpMethod;

public abstract class AbstractSecurityConfig extends WebSecurityConfigurerAdapter {


    private static final String LOGIN_API = "/api/login";
    private static final String STATISTICS_API = "/api/statistics";
    private static final String[] API_OPEN = {
        "/**"
    };

    @Autowired
    private HttpLogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    private HttpAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .exceptionHandling()
            .authenticationEntryPoint(authenticationEntryPoint)
            .and()
            .logout()
            .permitAll()
            .logoutRequestMatcher(new AntPathRequestMatcher(LOGIN_API, HttpMethod.DELETE))
            .logoutSuccessHandler(logoutSuccessHandler)
            .and()
            .sessionManagement()
            .maximumSessions(1); //only 1 session per user

        http.authorizeRequests()
            .antMatchers(API_OPEN).permitAll()
            .antMatchers(LOGIN_API).permitAll()
            .antMatchers(STATISTICS_API).permitAll()
            .anyRequest().authenticated();
    }

    @Bean(name = "authenticationManager")
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    public abstract LdapContextSource ldapContextSource();

    @Bean
    public LdapTemplate ldapTemplate() {
        return new LdapTemplate(ldapContextSource());
    }
}
