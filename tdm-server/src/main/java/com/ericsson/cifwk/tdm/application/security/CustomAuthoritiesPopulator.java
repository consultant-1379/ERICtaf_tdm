package com.ericsson.cifwk.tdm.application.security;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

@Component
public class CustomAuthoritiesPopulator implements LdapAuthoritiesPopulator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthoritiesPopulator.class);
    private static final Pattern TEAM_VALUE_PATTERN = Pattern.compile("IEAT-VCD-(.+)-(Admin|User)");

    @Override
    public List<? extends GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {
        Set<String> teams = getUserTeams(userData);
        return teams
            .stream()
            .map(SimpleGrantedAuthority::new)
            .collect(toList());
    }

    private static Set<String> getUserTeams(DirContextOperations ctx) {
        Set<String> teams = Sets.newHashSet();

        Attribute memberOfAttr = ctx.getAttributes().get("memberof");
        if (memberOfAttr != null) {
            try {
                NamingEnumeration<?> memberOfValues = memberOfAttr.getAll();
                while (memberOfValues.hasMore()) {
                    String value = memberOfValues.nextElement().toString();

                    Optional<String> team = findTeamValue(value);
                    team.ifPresent(teams::add);
                }
            } catch (NamingException e) {
                LOGGER.error("Exception parsing LDAP 'memberOf' attribute", e);
            }
        }
        return teams;
    }

    private static Optional<String> findTeamValue(String value) {
        Matcher matcher = TEAM_VALUE_PATTERN.matcher(value);
        if (matcher.find()) {
            Optional.of(matcher.group(1));
        }
        return Optional.empty();
    }
}
