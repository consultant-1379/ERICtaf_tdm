package com.ericsson.cifwk.tdm.adapter;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.eventbus.TestEventBus;
import com.ericsson.cifwk.taf.spi.TafPlugin;

/**
 * @author Vladimirs Iljins (vladimirs.iljins@ericsson.com)
 *         19/07/2017
 */
public class TdmTafPlugin implements TafPlugin {


    @Override
    public void init() {
        TestEventBus testEventBus = ServiceRegistry.getTestEventBus();
        testEventBus.register(new TdmTestListener());
    }

    @Override
    public void shutdown() {
        // nothing
    }
}
