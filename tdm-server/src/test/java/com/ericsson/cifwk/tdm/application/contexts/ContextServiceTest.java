package com.ericsson.cifwk.tdm.application.contexts;

import com.ericsson.cifwk.tdm.api.model.Context;
import com.ericsson.cifwk.tdm.model.ContextEntity;
import com.google.common.collect.Lists;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 05/05/2016
 */
@RunWith(MockitoJUnitRunner.class)
public class ContextServiceTest {

    @InjectMocks
    ContextService contextService;

    @Mock
    ContextRepository contextRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    MapperFactory mapperFactory;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    MapperFacade mapperFacade;

    @Mock
    ContextEntity mockedContextEntity;
    @Mock
    Context mockedContext;

    List<ContextEntity> contexts = Lists.newArrayList();
    List<Context> mappedContexts = Lists.newArrayList();

    @Before
    public void setUp() {
        when(mapperFactory.getMapperFacade()).thenReturn(mapperFacade);
        when(mapperFacade.mapAsList(eq(contexts), eq(Context.class))).thenReturn(mappedContexts);

        when(contextRepository.findAll()).thenReturn(contexts);

        when(contextRepository.findBySystemId("id1")).thenReturn(mockedContextEntity);
        when(contextRepository.findBySystemId("id2")).thenReturn(null);
        when(contextRepository.findByName("name1")).thenReturn(mockedContextEntity);
        when(contextRepository.findByName("name2")).thenReturn(null);

        when(mapperFacade.map(eq(mockedContextEntity), eq(Context.class))).thenReturn(mockedContext);

        contexts.add(mockContextEntity("root-1", null));
        contexts.add(mockContextEntity("parent-1", "root-1"));
        contexts.add(mockContextEntity("parent-2", "root-1"));
        contexts.add(mockContextEntity("parent-parent-1", "parent-1"));
        contexts.add(mockContextEntity("parent-parent-2", "parent-2"));
        contexts.add(mockContextEntity("not-root", null));
    }

    @Test
    public void testFindAll() {
        mappedContexts.add(new Context());

        List<Context> all = contextService.findAll();

        assertThat(all).hasSize(1);
    }

    @Test
    public void shouldFindBySystemId() {
        Optional<Context> context = contextService.findBySystemId("id1");

        assertThat(context.isPresent()).isTrue();
        assertThat(context.get()).isEqualTo(mockedContext);
    }

    @Test
    public void shouldFailToFindBySystemId() {
        Optional<Context> context = contextService.findBySystemId("id2");

        assertThat(context.isPresent()).isFalse();
    }

    @Test
    public void shouldFindByName() {
        Optional<Context> context = contextService.findByName("name1");

        assertThat(context.isPresent()).isTrue();
        assertThat(context.get()).isEqualTo(mockedContext);
    }

    @Test
    public void shouldFailToFindByName() {
        Optional<Context> context = contextService.findByName("name2");

        assertThat(context.isPresent()).isFalse();
    }

    @Test
    public void shouldFindAllParentContextIdsForRoot() {
        List<String> allParentContextIds = contextService.findAllParentContextIds("root-1");

        assertThat(allParentContextIds).containsExactly(
                "root-1",
                "parent-1", "parent-2",
                "parent-parent-1", "parent-parent-2");
    }

    @Test
    public void shouldFindAllSubContextIdsForSubContext() {
        List<String> allParentContextIds = contextService.findAllParentContextIds("parent-1");

        assertThat(allParentContextIds).containsExactly(
                "parent-1", "parent-parent-1");
    }

    @Test
    public void shouldNotFindAllSubContextIds() {
        List<String> allParentContextIds = contextService.findAllParentContextIds("no-such-id-1");

        assertThat(allParentContextIds).hasSize(0);
    }

    private ContextEntity mockContextEntity(String id, String parentId) {
        ContextEntity mock = Mockito.mock(ContextEntity.class, RETURNS_DEEP_STUBS);
        when(mock.getSystemId()).thenReturn(id);
        when(mock.getParentId()).thenReturn(parentId);
        return mock;
    }

}
