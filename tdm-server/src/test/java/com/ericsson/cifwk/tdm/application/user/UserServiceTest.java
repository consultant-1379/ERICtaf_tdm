package com.ericsson.cifwk.tdm.application.user;

import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.gic.tms.presentation.dto.users.GrantedUserRole;
import com.ericsson.gic.tms.presentation.dto.users.RoleBean;
import com.ericsson.gic.tms.presentation.dto.users.UserBean;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @InjectMocks
    private UserService service;

    @Spy
    @SuppressWarnings("unused")
    private MapperFactory mapperFacade = new DefaultMapperFactory.Builder().build();

    @Mock
    private TceUserRepository tceUserRepository;

    @Test
    public void findByContextId_shouldFilterByApproverRoles() throws Exception {
        expectUserBeans(
                userBean(1L, "foo", grantedUserRole(approverRole())),
                userBean(2L, "bar", grantedUserRole(nonApproverRole())),
                userBean(3L, "baz", grantedUserRole(approverRole()))
        );

        List<User> users = service.findByContextId("contextId");

        assertThat(users).containsExactly(
                user(1L, "foo"),
                user(3L, "baz")
        );
    }

    @Test
    public void findByContextId_shouldIgnoreNulls_inUserBeans() throws Exception {
        expectUserBeans(
                userBean(1L, "foo", grantedUserRole(approverRole())),
                null,
                userBean(2L, "bar", grantedUserRole(approverRole()))
        );

        List<User> users = service.findByContextId("contextId");

        assertThat(users).containsExactly(
                user(1L, "foo"),
                user(2L, "bar")
        );
    }

    @Test
    public void findByContextId_shouldIgnoreNulls_inGrantedUserRoles() throws Exception {
        expectUserBeans(userBean(1L, "foo",
                grantedUserRole(nonApproverRole()),
                null,
                grantedUserRole(approverRole())
        ));

        List<User> users = service.findByContextId("contextId");

        assertThat(users).containsExactly(
                user(1L, "foo")
        );
    }

    @Test
    public void findByContextId_shouldIgnoreNulls_inRoleBeans() throws Exception {
        expectUserBeans(userBean(1L, "foo",
                grantedUserRole(nonApproverRole()),
                grantedUserRole(null),
                grantedUserRole(approverRole())
        ));

        List<User> users = service.findByContextId("contextId");

        assertThat(users).containsExactly(
                user(1L, "foo")
        );
    }

    private void expectUserBeans(UserBean... userBeans) {
        doReturn(newArrayList(userBeans)).when(tceUserRepository).findByContext(anyString());
    }

    private User user(long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    private UserBean userBean(long id, String username,
                              GrantedUserRole... grantedUserRoles) {
        UserBean userBean = new UserBean();
        userBean.setId(id);
        userBean.setUsername(username);
        userBean.setFirstName("firstName");
        userBean.setLastName("lastName");
        userBean.setEmail("email");
        userBean.setCanonical("canonical");
        userBean.setRoles(newArrayList(grantedUserRoles));
        return userBean;
    }

    private GrantedUserRole grantedUserRole(RoleBean roleBean) {
        GrantedUserRole grantedUserRole = new GrantedUserRole();
        grantedUserRole.setRoleBean(roleBean);
        grantedUserRole.setContextId("contextId");
        grantedUserRole.setGranted(true);
        return grantedUserRole;
    }

    /**
     * @see UserService#APPROVER_ROLES
     */
    private RoleBean approverRole() {
        return roleBean("ROLE_TEST_MANAGER");
    }

    private RoleBean nonApproverRole() {
        return roleBean("ROLE_TEST_ENGINEER");
    }

    private RoleBean roleBean(String name) {
        RoleBean roleBean = new RoleBean();
        roleBean.setId(42L);
        roleBean.setName(name);
        roleBean.setAssignable(true);
        return roleBean;
    }
}