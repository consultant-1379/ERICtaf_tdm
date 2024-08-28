package com.ericsson.cifwk.tdm.adapter;

import com.ericsson.cifwk.taf.testapi.InvokedMethod;

import java.lang.reflect.Method;

import static com.ericsson.cifwk.tdm.adapter.TdmTestListener.getCurrentTestMethod;


public class TestwareResolver {

    private static final String PCKG_DELIMITER = ".jar";
    private static final String PCKG_DEL_REGEXP = "\\.jar.*";

    public String getTestwarePackage(Method testMethod) {
        Method javaMethod;
        if (testMethod != null) {
            javaMethod = testMethod;
        } else {
            InvokedMethod invokedMethod = getCurrentTestMethod();
            if (invokedMethod != null) {
                javaMethod = invokedMethod.getTestMethodExecutionResult().getTestMethod().getJavaMethod();
            } else {
                return null;
            }
        }

        String sourceUrl = getSourceUrl(javaMethod);
        String packageName = null;
        if (sourceUrl.contains(PCKG_DELIMITER)) {
            String[] urlParts = sourceUrl.replaceAll(PCKG_DEL_REGEXP, "").split("\\/");
            packageName = urlParts[urlParts.length - 1];
        }
        return packageName;
    }

    private static String getSourceUrl(Method method) {
        return method.getDeclaringClass().getProtectionDomain().getCodeSource().getLocation().toString();
    }
}
