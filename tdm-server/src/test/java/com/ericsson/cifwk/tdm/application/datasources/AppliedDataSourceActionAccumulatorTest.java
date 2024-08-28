package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.model.DataSourceActionEntity;
import com.ericsson.cifwk.tdm.model.Version;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.ericsson.cifwk.tdm.api.model.DataSourceAction.DataSourceActionBuilder.aDataSourceAction;
import static com.ericsson.cifwk.tdm.model.DataSourceActionEntity.actionEntity;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.NAME;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 14/04/2016
 */
public class AppliedDataSourceActionAccumulatorTest {

    private AppliedDataSourceActionAccumulator accumulator = new AppliedDataSourceActionAccumulator();

    private DataSourceActionEntity action;

    @Before
    public void setUp() {
        action = actionEntity(
                aDataSourceAction()
                        .withId("dsId")
                        .withType(DataSourceActionType.IDENTITY_NAME_EDIT.name())
                        .withKey(NAME)
                        .withNewValue("newName")
                        .withVersion(Version.INITIAL_VERSION.toString())
                        .build()
        );
    }

    @Test
    public void shouldMaintainOrder() {
        accumulator.addAction(action, "parentId1");
        accumulator.addAction(action, "parentId2");

        List<DataSourceActionEntity> actions = accumulator.getActionEntities();
        assertThat(actions.size()).isEqualTo(2);

        assertThat(actions.get(0).getOrder()).isEqualTo(0);
        assertThat(actions.get(0).getParentId()).isEqualTo("parentId1");
        assertThat(actions.get(1).getOrder()).isEqualTo(1);
        assertThat(actions.get(1).getParentId()).isEqualTo("parentId2");
    }

    @Test
    public void shouldRemovePreviousActionIfExists() {
        accumulator.addAction(action, "parentId1");

        accumulator.removePreviousActionIfExists(DataSourceActionType.IDENTITY_NAME_EDIT, "parentId1", "name");

        assertThat(accumulator.getActionEntities()).hasSize(0);
    }

    @Test
    public void shouldNotRemovePreviousActionAsNotEqual() {
        accumulator.addAction(action, "parentId1");

        accumulator.removePreviousActionIfExists(DataSourceActionType.IDENTITY_KEY_ADD, "parentId2", "notName");

        assertThat(accumulator.getActionEntities()).hasSize(1);
    }
}
