package com.ericsson.cifwk.tdm.infrastructure.lock;

public interface LockingService {

    void lock(String jobName, Runnable job);

}
