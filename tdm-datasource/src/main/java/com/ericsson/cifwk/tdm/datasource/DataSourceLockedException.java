package com.ericsson.cifwk.tdm.datasource;

/**
 * @author Vladimirs Iljins (vladimirs.iljins@ericsson.com)
 *         12/07/2017
 */
public class DataSourceLockedException extends RuntimeException {
    public DataSourceLockedException(String message) {
        super(message);
    }
}
