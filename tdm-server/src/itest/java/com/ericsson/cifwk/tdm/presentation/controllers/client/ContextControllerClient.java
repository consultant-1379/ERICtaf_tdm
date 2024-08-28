package com.ericsson.cifwk.tdm.presentation.controllers.client;

import com.ericsson.cifwk.tdm.api.model.Context;
import com.ericsson.cifwk.tdm.api.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

@Service
public class ContextControllerClient extends ControllerClientCommon {

    private static final String CONTEXTS = "/api/contexts";
    private static final String CONTEXTS_BY_NAME = CONTEXTS + "?name={name}";
    private static final String CONTEXTS_BY_PATH = CONTEXTS + "?path={path}";

    private static final String QUERY_PARAM = "query={query}";
    private static final String LIMIT_PARAM = "limit={limit}";

    private static final String CONTEXT_USERS = CONTEXTS + "/{systemId}/users";
    private static final String CONTEXT_USER = CONTEXTS + "/{contextId}/validateUser";
    private static final String CONTEXT_USERS_QUERY = CONTEXT_USERS + "?" + QUERY_PARAM;
    private static final String CONTEXT_USERS_LIMIT = CONTEXT_USERS + "?" + LIMIT_PARAM;
    private static final String CONTEXT_USERS_QUERY_LIMIT = CONTEXT_USERS_QUERY + "&" + LIMIT_PARAM;

    @Autowired
    public ContextControllerClient(WebApplicationContext webApplicationContext) {
        super(webApplicationContext);
    }

    public List<Context> getContexts() throws Exception {
        return toList(get(CONTEXTS), Context.class);
    }

    public Context getContextByName(String name) throws Exception {
        return toObject(get(CONTEXTS_BY_NAME, name), Context.class);
    }

    public Context getContextByPath(final String path) throws Exception {
        return toObject(get(CONTEXTS_BY_PATH, path), Context.class);
    }

    public List<User> findContextUsers(String contextId) throws Exception {
        return findUsersByUrl(CONTEXT_USERS, contextId);
    }

    public Map validateContextUser(String contextId) throws Exception {
        return  toObject(get(CONTEXT_USER, contextId), Map.class);
    }
    public List<User> findContextUsers(String contextId, String query) throws Exception {
        return findUsersByUrl(CONTEXT_USERS_QUERY, contextId, query);
    }

    public List<User> findContextUsers(String contextId, int limit) throws Exception {
        return findUsersByUrl(CONTEXT_USERS_LIMIT, contextId, limit);
    }

    public List<User> findContextUsers(String contextId, String query, int limit) throws Exception {
        return findUsersByUrl(CONTEXT_USERS_QUERY_LIMIT, contextId, query, limit);
    }

    private List<User> findUsersByUrl(String url, Object... uriVariables) throws Exception {
        return toList(get(url, uriVariables), User.class);
    }
}
