package com.ericsson.cifwk.tdm.adapter;

import com.ericsson.cifwk.taf.eventbus.Subscribe;
import com.ericsson.cifwk.taf.testapi.InvokedMethod;
import com.ericsson.cifwk.taf.testapi.events.AfterMethodInvocationEvent;
import com.ericsson.cifwk.taf.testapi.events.BeforeMethodInvocationEvent;

/**
 * @author Vladimirs Iljins (vladimirs.iljins@ericsson.com)
 *         19/07/2017
 */
public class TdmTestListener {

    private static ThreadLocal<InvokedMethod> currentTestMethod = new InheritableThreadLocal<>();

    public static InvokedMethod getCurrentTestMethod() {
        return currentTestMethod.get();
    }

    @Subscribe
    public void beforeTestMethodInvocation(BeforeMethodInvocationEvent event) {
        InvokedMethod method = event.getMethod();
        currentTestMethod.set(method);
    }

    @Subscribe
    public void afterTestMethodInvocation(AfterMethodInvocationEvent event) {
        currentTestMethod.remove();
    }
}
