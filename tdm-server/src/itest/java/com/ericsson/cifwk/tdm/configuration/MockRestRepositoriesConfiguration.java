package com.ericsson.cifwk.tdm.configuration;

import com.ericsson.cifwk.tdm.application.ciportal.testware.CIPortalTestwareRepository;
import com.ericsson.cifwk.tdm.application.user.TceUserRepository;
import com.ericsson.cifwk.tdm.model.testware.ArtifactItems;
import com.ericsson.gic.tms.presentation.dto.users.UserBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

import static com.ericsson.cifwk.tdm.application.util.JsonParser.parseListFile;
import static com.ericsson.cifwk.tdm.application.util.XmlParser.parseObjectFile;
import static com.ericsson.cifwk.tdm.configuration.ITestsProfiles.MOCK_REST_REPOSITORIES;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@Profile(MOCK_REST_REPOSITORIES)
public class MockRestRepositoriesConfiguration {

    public static final String TAF_USER = "taf";
    public static final String TAF_2 = "taf2";
    public static final String TEST_USER = "testUser";

    public static final String MANAGER_USER = "manager";
    public static final String CONTEXT_ID_1 = "systemId-1";
    public static final String CONTEXT_ID_2 = "systemId-2";
    public static final String CONTEXT_ID_3 = "systemId-3";
    public static final String CONTEXT_ID_4 = "systemId-4";
    public static final String CONTEXT_ID_5 = "systemId-5";
    public static final String CONTEXT_ID_6 = "systemId-6";
    public static final String CONTEXT_WITH_NO_USERS = "noUserContext";
    public static final String CONTEXT_NOT_EXIST = "notExistingContextId";

    @Bean
    @Primary
    public TceUserRepository mockTceUserRepository() throws Exception {
        TceUserRepository mockRepo = mock(TceUserRepository.class);
        List<UserBean> userList = parseListFile("contexts/users-contexts.json", UserBean.class);
        List<UserBean> emptyUserList = new ArrayList<>();

        when(mockRepo.findByContext(eq(CONTEXT_ID_1))).thenReturn(userList);
        when(mockRepo.findByContext(eq(CONTEXT_ID_2))).thenReturn(userList);
        when(mockRepo.findByContext(eq(CONTEXT_ID_3))).thenReturn(userList);
        when(mockRepo.findByContext(eq(CONTEXT_ID_4))).thenReturn(userList);
        when(mockRepo.findByContext(eq(CONTEXT_ID_5))).thenReturn(userList);
        when(mockRepo.findByContext(eq(CONTEXT_ID_6))).thenReturn(userList);
        when(mockRepo.findByContext(eq(CONTEXT_NOT_EXIST))).thenReturn(userList);
        when(mockRepo.findByContext(eq(CONTEXT_WITH_NO_USERS))).thenReturn(emptyUserList);

        UserBean tafUser = filterUserByUsername(userList, TAF_USER);
        UserBean taf2User = filterUserByUsername(userList, TAF_2);

        UserBean managerUser = filterUserByUsername(userList, MANAGER_USER);
        when(mockRepo.findByUsername(eq(TAF_USER))).thenReturn(tafUser);
        when(mockRepo.findByUsername(eq(TAF_2))).thenReturn(taf2User);
        when(mockRepo.findByUsername(eq(MANAGER_USER))).thenReturn(managerUser);

        return mockRepo;
    }

    @Bean
    @Primary
    public CIPortalTestwareRepository ciPortalTestwareGroupsClient() {
        CIPortalTestwareRepository testwareGroupsClientMock = mock(CIPortalTestwareRepository.class);
        when(testwareGroupsClientMock.getTestwareArtifacts()).thenReturn(parseObjectFile(
                "testware-groups/artifact-items.xml", ArtifactItems.class));
        return testwareGroupsClientMock;
    }

    private UserBean filterUserByUsername(List<UserBean> userList, String username) {
        return userList.stream()
            .filter(user -> username.equalsIgnoreCase(user.getUsername()))
            .findFirst()
            .orElseGet(UserBean::new);
    }
}
