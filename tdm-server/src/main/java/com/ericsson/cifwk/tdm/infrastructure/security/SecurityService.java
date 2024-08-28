package com.ericsson.cifwk.tdm.infrastructure.security;

import com.ericsson.cifwk.tdm.api.model.AuthenticationStatus;
import com.ericsson.cifwk.tdm.api.model.ContextRole;
import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.api.model.UserCredentials;
import com.ericsson.cifwk.tdm.application.user.UserService;
import com.ericsson.cifwk.tdm.application.user.UserSessionService;
import java.util.ArrayList;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.ericsson.cifwk.tdm.application.datasources.DataSourceService.PROFILE;

@Service
public class SecurityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityService.class);

    private static final Authentication ANONYMOUS = new AnonymousAuthenticationToken(
            "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private UserSessionService userSessionService;

    public AuthenticationStatus getCurrentUser() {
        Authentication auth = Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .orElse(ANONYMOUS);
        return new AuthenticationStatus(auth.getName(), isAuthenticated(auth), getRoles(auth), false);
    }

    private static boolean isAuthenticated(Authentication auth) {
        boolean anonymous = auth instanceof AnonymousAuthenticationToken;
        return !anonymous && auth.isAuthenticated();
    }

    public AuthenticationStatus login(UserCredentials credentials) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                credentials.getUsername(), credentials.getPassword());

        try {
            authentication = authenticationManager.authenticate(authentication);
        } catch (BadCredentialsException ex) { // NOSONAR
            LOGGER.error("Bad credentials for user - " + credentials.getUsername());
            return new AuthenticationStatus(credentials.getUsername(), false, Collections.emptyList(), false);
        }

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
        String name = securityContext.getAuthentication().getName();

        userSessionService.addUserSession(sessionId, name);

        if (PROFILE != null && "customer".equalsIgnoreCase(PROFILE)) {
            return new AuthenticationStatus(credentials.getUsername(), true, getRoles(authentication), true);
        } else {
            return new AuthenticationStatus(credentials.getUsername(), true, getRoles(authentication), false);
        }
    }

    public void validateUserAuthorization(String contextId) {
        boolean present = isPresent(contextId);
        if (!present) {
            throw new AccessDeniedException("User does not have permissions for this context");
        }
    }

    public Map<String, Boolean> validateUser(String contextId) {
        boolean present = isPresent(contextId);
        Map<String, Boolean> map = new HashMap<>();
        map.put("validated", present);
        return map;
    }

    public List<User> findUser(String username) {
        return new ArrayList<>(Arrays.asList(userService.findByUsername(username)));
    }

    private boolean isPresent(final String contextId) {
        AuthenticationStatus currentUser = getCurrentUser();
        List<User> byContextId = userService.findByContextIdAndStandardUser(contextId);
        return byContextId.stream().filter(user -> user.getUsername().equalsIgnoreCase(currentUser.getUsername()))
                .findFirst().isPresent();
    }

    private List<ContextRole> getRoles(Authentication auth) {
        return userService.findUserContextRolesByUsername(auth.getName());
    }
}
