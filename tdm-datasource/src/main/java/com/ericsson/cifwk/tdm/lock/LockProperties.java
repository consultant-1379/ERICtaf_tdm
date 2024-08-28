package com.ericsson.cifwk.tdm.lock;

import com.ericsson.cifwk.taf.datasource.ConfigurationSource;
import com.ericsson.cifwk.tdm.api.model.Lock;
import com.ericsson.cifwk.tdm.api.model.LockType;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Locale;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;

import static com.ericsson.cifwk.tdm.adapter.TdmDataSourceConfiguration.TDM_ENABLE_LOCK;

/**
 * @author Vladimirs Iljins (vladimirs.iljins@ericsson.com)
 *         27/09/2017
 */
public class LockProperties {

    private static final String TDM_LOCK_TYPE = "lock.type";
    private static final String TDM_LOCK_TIMEOUT_SEC = "lock.timeout_seconds";
    private static final String TDM_LOCK_WAIT_TIMEOUT_SEC = "lock.wait_timeout_seconds";

    public static final LockType TDM_DEFAULT_LOCK_TYPE = LockType.SHARED;
    private static final int TDM_DEFAULT_LOCK_TIMEOUT_SEC = 300;
    private static final int TDM_DEFAULT_LOCK_WAIT_TIMEOUT_SEC = 30;
    private static final String DEFAULT_SEPARATOR = ",";

    static final int TDM_LOCK_RETRY_INTERVAL_SEC = 10;

    private ConfigurationSource configurationSource;

    public LockProperties(ConfigurationSource configurationSource) {

        this.configurationSource = configurationSource;
    }

    public void configure(Lock lock) {
        String lockTimeout = configurationSource.getProperty(TDM_LOCK_TIMEOUT_SEC);
        lock.setTimeoutSeconds(lockTimeout != null ? parseInt(lockTimeout) : TDM_DEFAULT_LOCK_TIMEOUT_SEC);
    }

    public int getWaitTimeout() {
        String waitTimeoutString = configurationSource.getProperty(TDM_LOCK_WAIT_TIMEOUT_SEC);
        return waitTimeoutString != null ? parseInt(waitTimeoutString) : TDM_DEFAULT_LOCK_WAIT_TIMEOUT_SEC;
    }

    public List<LockType> getLockTypes() {
        Object lockTypeProperty = configurationSource.getProperty(TDM_LOCK_TYPE);
        List<LockType> locks = Lists.newArrayList();
        if (lockTypeProperty == null) {
            return Lists.newArrayList();
        } else if (lockTypeProperty.toString().contains(DEFAULT_SEPARATOR)) {
            List<String> lockTypes = getPropertyStrings(lockTypeProperty);
            for (String lockType : lockTypes) {
                LockType lock = getLockType(lockType);
                locks.add(lock);
            }
            return locks;
        } else {
            return Lists.newArrayList(getLockType(lockTypeProperty.toString()));
        }

    }

    private static List<String> getPropertyStrings(Object lockTypeProperty) {
        String[] strings = lockTypeProperty.toString().split(DEFAULT_SEPARATOR);
        List<String> result = Lists.newArrayList();
        for (String item : strings) {
            result.add(item.trim());
        }
        return result;
    }

    private static LockType getLockType(String lockType) {
        if (parseBoolean(getProperty(TDM_ENABLE_LOCK, "false"))) {
            return lockType != null ? LockType.valueOf(lockType.toUpperCase(Locale.ENGLISH)) :
                    TDM_DEFAULT_LOCK_TYPE;
        } else {
            return TDM_DEFAULT_LOCK_TYPE;
        }
    }
}
