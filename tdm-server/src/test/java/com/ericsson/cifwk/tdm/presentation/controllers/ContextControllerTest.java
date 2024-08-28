package com.ericsson.cifwk.tdm.presentation.controllers;

import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.application.user.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static com.ericsson.cifwk.tdm.api.model.UserBuilder.anUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ContextControllerTest {

    @InjectMocks
    private ContextController controller = new ContextController();

    @Mock
    private UserService userService;

    private User user1 = anUser().withId(1L).build();
    private User user2 = anUser().withId(2L).build();

    @Test
    public void findContextUsers_findByContextId() throws Exception {
        contextUsersAre(user1, user2);

        List<User> result = controller.findContextUsers("contextId", null, 10);

        assertThat(result).containsExactly(user1, user2);
        verify(userService, only()).findByContextId("contextId");
    }

    @Test
    public void findContextUsers_findByContextIdAndQuery() throws Exception {
        contextUsersAre(user1, user2);

        List<User> result = controller.findContextUsers("contextId", "query", 10);

        assertThat(result).containsExactly(user1, user2);
        verify(userService, only()).findByContextIdAndQuery("contextId", "query");
    }

    @Test
    public void findContextUsers_limit() throws Exception {
        contextUsersAre(user1, user2);

        List<User> result = controller.findContextUsers("contextId", null, 1);

        assertThat(result).containsExactly(user1);
    }

    private void contextUsersAre(User... users) {
        List<User> userList = newArrayList(users);
        doReturn(userList).when(userService).findByContextId(anyString());
        doReturn(userList).when(userService).findByContextIdAndQuery(anyString(), anyString());
    }
}