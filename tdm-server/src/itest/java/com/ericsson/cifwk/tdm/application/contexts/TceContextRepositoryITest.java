package com.ericsson.cifwk.tdm.application.contexts;

import com.ericsson.cifwk.tdm.infrastructure.ScheduledTasks;
import com.ericsson.cifwk.tdm.integration.CustomRestClient;
import com.ericsson.gic.tms.presentation.dto.ContextBean;
import com.ericsson.gic.tms.presentation.dto.jsonapi.DocumentList;
import com.ericsson.gic.tms.presentation.dto.jsonapi.Resource;
import com.ericsson.gic.tms.presentation.resources.ContextResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.ericsson.cifwk.tdm.application.constants.Qualifiers.TCE_CLIENT;
import static com.ericsson.cifwk.tdm.application.contexts.TceContextRepository.CACHE_NAME;
import static com.ericsson.cifwk.tdm.infrastructure.HystrixConfiguration.TIMEOUT_MEDIUM;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;
import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles(INTEGRATION_TEST)
public class TceContextRepositoryITest {

    @Autowired
    private TceContextRepository tceContextRepository;

    @MockBean
    @Qualifier(TCE_CLIENT)
    private CustomRestClient tceRestClient;

    @MockBean
    @SuppressWarnings("unused")
    private ScheduledTasks scheduledTasks;

    @Autowired
    private CacheManager cacheManager;

    private ContextBean contextBean;

    @Before
    public void setUp() throws Exception {
        contextBean = new ContextBean();
        contextBean.setId("42");
    }

    @After
    public void tearDown() throws Exception {
        cacheManager.getCache(CACHE_NAME).clear();
    }

    @Test
    public void caching() throws Exception {
        doReturn(contextResource(contextBean))
                .when(tceRestClient).get(ContextResource.class);

        // 1st call - result taken from rest client and cached
        List<ContextBean> result = tceContextRepository.getContexts();

        assertThat(result).containsExactly(contextBean);
        verify(tceRestClient).get(ContextResource.class);

        // 2nd call - result taken from cache
        result = tceContextRepository.getContexts();

        assertThat(result).hasSize(1);
        ContextBean resultBean = result.iterator().next();
        assertThat(resultBean).isNotSameAs(contextBean);
        assertThat(resultBean.getId()).isEqualTo(contextBean.getId());
        verifyNoMoreInteractions(tceRestClient);
    }

    @Test
    public void circuitBreaker() throws Exception {
        doThrow(new RuntimeException())
                .when(tceRestClient).get(ContextResource.class);

        List<ContextBean> result = tceContextRepository.getContexts();

        assertThat(result).isEmpty();
        verify(tceRestClient).get(ContextResource.class);
    }

    @Test
    public void circuitBreaker_timeout() throws Exception {
        doAnswer(invocation -> {
            TimeUnit.MILLISECONDS.sleep(parseInt(TIMEOUT_MEDIUM) + 100);
            return contextResource(contextBean);
        }).when(tceRestClient).get(ContextResource.class);

        List<ContextBean> result = tceContextRepository.getContexts();

        assertThat(result).isEmpty();
        verify(tceRestClient).get(ContextResource.class);
    }

    @Test
    public void circuitBreaker_noCaching() throws Exception {
        circuitBreaker();
        doReturn(contextResource(contextBean))
                .when(tceRestClient).get(ContextResource.class);

        List<ContextBean> result = tceContextRepository.getContexts();

        assertThat(result).containsExactly(contextBean);
    }

    private ContextResource contextResource(ContextBean... contextBeans) {
        ContextResource contextResource = mock(ContextResource.class);
        List<Resource<ContextBean>> resources = Stream.of(contextBeans).map(Resource::new).collect(toList());
        doReturn(new DocumentList<>(resources)).when(contextResource).getContexts();
        return contextResource;
    }
}
