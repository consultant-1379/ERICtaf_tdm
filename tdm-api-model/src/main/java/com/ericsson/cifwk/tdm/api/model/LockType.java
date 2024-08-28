package com.ericsson.cifwk.tdm.api.model;

/**
 *  Data source locking policy.
 *
 *  EXCLUSIVE - data source can be used by only one test ware at a time.
 *  SHARED - data source can be used by any test ware concurrently.
 */
public enum LockType {

    EXCLUSIVE, SHARED
}
