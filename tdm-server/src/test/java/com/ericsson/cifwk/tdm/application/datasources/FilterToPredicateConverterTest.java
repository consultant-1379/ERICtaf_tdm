package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.model.RecordPredicate;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;


/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 19/02/2016
 */
public class FilterToPredicateConverterTest {

    FilterToPredicateConverter converter;
    List<String> predicates;

    @Before
    public void setUp() {
        converter = new FilterToPredicateConverter();
        predicates = Lists.newArrayList("a=1", "b>2", "c<3", "text=abrakadabra", "positive_value>=99", "negative-key<=0");
    }

    @Test
    public void shouldConvertToPredicates() {
        List<RecordPredicate> recordPredicates = converter.convert(this.predicates);

        assertThat(recordPredicates).hasSize(6);

        assertThat(recordPredicates.get(0).toString()).isEqualTo("values.a: 1");
        assertThat(recordPredicates.get(1).toString()).isEqualTo("values.b: {$gt:2}");
        assertThat(recordPredicates.get(2).toString()).isEqualTo("values.c: {$lt:3}");
        assertThat(recordPredicates.get(3).toString()).isEqualTo("values.text: 'abrakadabra'");
        assertThat(recordPredicates.get(4).toString()).isEqualTo("values.positive_value: {$gte:99}");
        assertThat(recordPredicates.get(5).toString()).isEqualTo("values.negative-key: {$lte:0}");
    }
}
