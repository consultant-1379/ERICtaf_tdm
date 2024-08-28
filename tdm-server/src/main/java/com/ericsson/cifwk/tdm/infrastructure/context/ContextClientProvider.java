package com.ericsson.cifwk.tdm.infrastructure.context;

import com.ericsson.cifwk.tdm.application.contexts.TceContextRepository;
import com.ericsson.cifwk.tdm.application.user.TceUserRepository;
import com.ericsson.cifwk.tdm.presentation.exceptions.NotFoundException;
import com.ericsson.gic.tms.presentation.dto.ContextBean;
import com.ericsson.gic.tms.presentation.dto.users.UserBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.ericsson.cifwk.tdm.application.util.JsonParser.parseListFile;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.CUSTOMER;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.DEVELOPMENT;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.TEST;

@Configuration
@Profile({DEVELOPMENT, TEST, CUSTOMER})
public class ContextClientProvider {

    @Value("${mock.contexts}")
    private String contextsFile;

    @Value("${mock.users}")
    private String usersFile;

    @Bean
    @Primary
    public TceContextRepository mockTceContextRepository() {
        class MockTceContextRepository extends TceContextRepository {

            @Override
            public List<ContextBean> getContexts() {
                return parseListFile(contextsFile, ContextBean.class);
            }
        }
        return new MockTceContextRepository();
    }

    @Bean
    @Primary
    public TceUserRepository mockTceUserRepository() {
        class MockTceUserRepository extends TceUserRepository {

            @Override
            public List<UserBean> findByContext(String contextId) {
                return userList();
            }

            @Override
            public UserBean findByUsername(String username) {
                return userList().stream()
                        .filter(user -> Objects.equals(user.getUsername().toLowerCase(Locale.ENGLISH),
                                username.toLowerCase(Locale.ENGLISH)))
                        .findFirst()
                        .orElseThrow(NotFoundException::new);
            }

            private List<UserBean> userList() {
                return parseListFile(usersFile, UserBean.class);
            }
        }
        return new MockTceUserRepository();
    }
}
