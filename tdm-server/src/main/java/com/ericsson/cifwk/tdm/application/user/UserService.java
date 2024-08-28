package com.ericsson.cifwk.tdm.application.user;

import com.ericsson.cifwk.tdm.api.model.ContextRole;
import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.presentation.exceptions.NotFoundException;
import com.ericsson.gic.tms.presentation.dto.users.GrantedUserRole;
import com.ericsson.gic.tms.presentation.dto.users.RoleBean;
import com.ericsson.gic.tms.presentation.dto.users.UserBean;
import ma.glasnost.orika.MapperFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.ericsson.cifwk.tdm.presentation.exceptions.NotFoundException.verifyFound;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.StringUtils.startsWithIgnoreCase;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private static final List<String> APPROVER_ROLES;
    private static final List<String> STANDARD_ROLES;

    @Autowired
    private MapperFactory mapperFacade;

    @Autowired
    private TceUserRepository tceUserRepository;

    static {
        APPROVER_ROLES  = newArrayList("ROLE_TEST_MANAGER", "ROLE_CONTEXT_ADMIN");
        STANDARD_ROLES =
                newArrayList("ROLE_TEST_ENGINEER", "ROLE_TEST_MANAGER", "ROLE_CONTEXT_ADMIN");
    }

    public List<User> findUsers(List<String> usernameList) {
        return usernameList.stream().map(this::findByUsername).collect(toList());
    }

    public List<ContextRole> findUserContextRolesByUsername(String username) {
        List<ContextRole> userRoles = newArrayList();
        try {
            UserBean user = tceUserRepository.findByUsername(username);
            if (user != null) {
                user.getRoles().stream().forEach(eachRole -> userRoles
                        .add(mapperFacade.getMapperFacade(GrantedUserRole.class, ContextRole.class).map(eachRole)));
            }
        } catch (NotFoundException ex) {
            LOGGER.error("User not found returning an empty List", ex);
        }
        return userRoles;
    }

    public List<User> findByContextId(String contextId) {
        return findByContextIdAndPredicate(contextId, user -> true);
    }

    public List<User> findByContextIdAndQuery(String contextId, String query) {
        Predicate<UserBean> predicate = user -> startsWithIgnoreCase(user.getUsername(), query)
                || startsWithIgnoreCase(user.getEmail(), query);
        return findByContextIdAndPredicate(contextId, predicate);
    }

    private List<User> findByContextIdAndPredicate(String contextId, Predicate<UserBean> predicate) {
        return tceUserRepository.findByContext(contextId).stream()
                .filter(Objects::nonNull)
                .filter(predicate)
                .filter(UserService::hasApproverRole)
                .map(mapperFacade.getMapperFacade(UserBean.class, User.class)::map)
                .collect(toList());
    }

    public List<User> findByContextIdAndStandardUser(String contextId) {
        return tceUserRepository.findByContext(contextId).stream()
                .filter(Objects::nonNull)
                .filter(UserService::hasStandardRole)
                .map(mapperFacade.getMapperFacade(UserBean.class, User.class)::map)
                .collect(toList());
    }

    public User findByUsername(String username) {
        UserBean foundUser = verifyFound(tceUserRepository.findByUsername(username));
        return mapperFacade.getMapperFacade(UserBean.class, User.class).map(foundUser);
    }

    private static boolean hasApproverRole(UserBean userBean) {
        return userBean.getRoles().stream()
            .filter(Objects::nonNull)
            .map(GrantedUserRole::getRoleBean)
            .filter(Objects::nonNull)
            .map(RoleBean::getName)
            .anyMatch(APPROVER_ROLES::contains);
    }

    private static boolean hasStandardRole(UserBean userBean) {
        return userBean.getRoles().stream()
                .filter(Objects::nonNull)
                .map(GrantedUserRole::getRoleBean)
                .filter(Objects::nonNull)
                .map(RoleBean::getName)
                .anyMatch(STANDARD_ROLES::contains);
    }
}
