package com.ericsson.cifwk.tdm.model;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 15/02/2016
 */
public class RecordPredicate {

    public static final String TEMPLATE = "values.%s: {%s:%s}";
    public static final String DEFAULT_TEMPLATE = "values.%s: %s";

    String property;
    String operator;
    String value;

    public RecordPredicate(String property, String operator, String value) {
        this.property = property;
        this.operator = operator;

        if (StringUtils.isNumeric(value)) {
            this.value = value;
        } else {
            this.value = "'" + value + "'";
        }
    }

    @Override
    public String toString() {
        if ("$eg".equals(operator)) {
            return String.format(DEFAULT_TEMPLATE, property, value);
        }
        return String.format(TEMPLATE, property, operator, value);
    }
}
