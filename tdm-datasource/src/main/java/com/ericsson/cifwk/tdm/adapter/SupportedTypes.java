package com.ericsson.cifwk.tdm.adapter;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 24/05/2016
 */
enum SupportedTypes {
    TDM;

    public static boolean contains(String type) {
        for (SupportedTypes supportedType : SupportedTypes.values()) {
            if (supportedType.name().compareToIgnoreCase(type) == 0) {
                return true;
            }
        }
        return false;
    }
}
