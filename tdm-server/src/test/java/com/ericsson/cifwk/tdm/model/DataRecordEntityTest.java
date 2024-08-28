package com.ericsson.cifwk.tdm.model;

import com.ericsson.cifwk.tdm.api.model.DataRecord;
import com.ericsson.cifwk.tdm.infrastructure.mapping.MapperFacadeProvider;
import com.google.common.collect.Maps;
import ma.glasnost.orika.MapperFacade;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 31/03/2016
 */
public class DataRecordEntityTest {

    private MapperFacadeProvider mapperFacadeProvider;

    @Before
    public void setUp() {
        mapperFacadeProvider = new MapperFacadeProvider();
    }

    @Test
    public void mappingTest() {
        DataRecord dataRecord = new DataRecord();
        dataRecord.setId("id-1");
        dataRecord.setDataSourceId("ds-1");

        HashMap<String, Object> values = Maps.newHashMap();
        values.put("col-1", "value-1");
        values.put("col-2", "value-2");
        values.put("col-3", "value-3");
        dataRecord.setValues(values);

        MapperFacade mapperFacade = mapperFacadeProvider.mapperFacade();
        DataRecordEntity mappedObject = mapperFacade.map(dataRecord, DataRecordEntity.class);

        assertThat(mappedObject.getId()).isEqualTo("id-1");
        assertThat(mappedObject.getDataSourceId()).isEqualTo("ds-1");

        assertThat(mappedObject.getValues()).containsEntry("col-1", "value-1");
        assertThat(mappedObject.getValues()).containsEntry("col-2", "value-2");
        assertThat(mappedObject.getValues()).containsEntry("col-3", "value-3");
    }
}
