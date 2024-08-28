package com.ericsson.cifwk.tdm.application.security;

import com.ericsson.cifwk.tdm.api.model.User;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.ericsson.cifwk.tdm.infrastructure.Profiles.DEVELOPMENT;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.TEST;

@Service
@Profile({DEVELOPMENT, TEST, INTEGRATION_TEST})
public class EmbeddedLdapService extends LdapService {

    private Map<String, BiConsumer<User, String>> ldapFieldMapping =
        ImmutableMap.<String, BiConsumer<User, String>>builder()
            .put("uid", User::setUsername)
            .put("givenName", User::setFirstName)
            .put("sn", User::setLastName)
            .put("mail", User::setEmail)
            .build();

    @Override
    protected Map<String, BiConsumer<User, String>> getUserFieldMapping() {
        return ldapFieldMapping;
    }

    @Override
    protected List<String> getFilterAttributes() {
        return ImmutableList.of("givenName", "sn", "uid", "cn");
    }

}
