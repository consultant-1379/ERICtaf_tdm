package com.ericsson.cifwk.tdm.application.security;

import com.ericsson.cifwk.tdm.api.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.ldap.support.LdapEncoder;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * This is potential new functionality to looking for a users in LDAP directory.
 * At the moment it is not used.
 */
public abstract class LdapService {

    @Autowired
    private LdapTemplate ldapTemplate;

    public List<User> findUsers(String searchString, int count) {
        ldapTemplate.setDefaultCountLimit(count);

        String encodedSearchString = LdapEncoder.filterEncode(searchString);

        OrFilter filter = new OrFilter();
        getFilterAttributes().forEach(s -> filter.or(new LikeFilter(s, encodedSearchString + "*")));

        return ldapTemplate.search("", filter.encode(), this::mapFromAttributes);
    }

    public User mapFromAttributes(Attributes attributes) {
        User user = new User();
        getUserFieldMapping().forEach(
            (attribute, setter) -> setter.accept(user, getLdapValue(attributes, attribute)));
        return user;
    }

    private static String getLdapValue(Attributes attributes, String name) {
        try {
            if (attributes.get(name) == null) {
                return null;
            }
            return (String) attributes.get(name).get();
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract Map<String, BiConsumer<User, String>> getUserFieldMapping();

    protected abstract List<String> getFilterAttributes();
}
