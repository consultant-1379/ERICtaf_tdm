package com.ericsson.cifwk.tdm.presentation.controllers;

import com.ericsson.cifwk.tdm.api.model.Context;
import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.application.contexts.ContextService;
import com.ericsson.cifwk.tdm.application.user.UserService;
import com.ericsson.cifwk.tdm.infrastructure.security.SecurityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/api/contexts")
public class ContextController {

    private static final String DEFAULT_USER_SEARCH_LIMIT = "10";

    @Autowired
    private UserService userService;

    @Autowired
    private ContextService contextService;

    @Autowired
    private SecurityService securityService;


    @RequestMapping(method = GET)
    public List<Context> getContexts() {
        return contextService.findAll();
    }

    @RequestMapping(value = "/{systemId}", method = GET)
    public ResponseEntity<Context> getContextBySystemId(@PathVariable("systemId") String systemId) {
        Optional<Context> context = contextService.findBySystemId(systemId);
        if (context.isPresent()) {
            return new ResponseEntity<>(context.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(NOT_FOUND);
    }

    @RequestMapping(params = "name", method = GET)
    public ResponseEntity<Context> getContextByName(@RequestParam("name") String name) {
        Optional<Context> context = contextService.findByName(name);
        if (context.isPresent()) {
            return new ResponseEntity<>(context.get(), HttpStatus.OK);
        }

        return new ResponseEntity<>(NOT_FOUND);
    }

    @RequestMapping(params = "path", method = GET)
    public ResponseEntity<Context> getContextByPath(@RequestParam("path") final String contextPath) {
        final Context context = contextService.findByPath(contextPath);
        if (context == null) {
            return new ResponseEntity<>(NOT_FOUND);
        }
        return new ResponseEntity<>(context, HttpStatus.OK);
    }

    /**
     * <p>Find users traversing up the given context which match the given parameters</p>
     *
     * @param query optional user signum or email
     * @param limit optional limit of found results. By default 10.
     * @return limited list of matched users within context
     */
    @RequestMapping(value = "/{systemId}/users", method = GET)
    public List<User> findContextUsers(@PathVariable("systemId") String systemId,
                                       @RequestParam(value = "query", required = false) String query,
                                       @RequestParam(value = "limit", required = false,
                                           defaultValue = DEFAULT_USER_SEARCH_LIMIT) Integer limit) {
        List<User> users = findContextUsers(systemId, query);
        return users.stream().limit(limit).collect(toList());
    }

    /**
     * <p>Verify user has permissions to create/delete datasources</p>
     *
     * @return if user has access or not
     */
    @RequestMapping(value = "/{contextId}/validateUser", method = GET)
    public Map<String, Boolean> validateContextUser(@PathVariable("contextId") String contextId) {
        return securityService.validateUser(contextId);
    }


    private List<User> findContextUsers(String systemId, String query) {
        if (isNullOrEmpty(query)) {
            return userService.findByContextId(systemId);
        }
        return userService.findByContextIdAndQuery(systemId, query);
    }
}
