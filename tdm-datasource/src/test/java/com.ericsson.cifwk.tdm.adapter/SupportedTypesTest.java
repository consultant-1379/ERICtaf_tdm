package com.ericsson.cifwk.tdm.adapter;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 24/05/2016
 */
public class SupportedTypesTest {

    @Test
    public void contains_shouldReturnTrue_whenSupportedType_notTdm() {
        assertThat(SupportedTypes.contains("nottdm")).isFalse();
    }

    @Test
    public void contains_shouldReturnTrue_whenSupportedType_tdm_caseInsensitive() {
        assertThat(SupportedTypes.contains("tdm")).isTrue();
        assertThat(SupportedTypes.contains("Tdm")).isTrue();
        assertThat(SupportedTypes.contains("tDm")).isTrue();
        assertThat(SupportedTypes.contains("tdM")).isTrue();
        assertThat(SupportedTypes.contains("TDM")).isTrue();
    }
}
