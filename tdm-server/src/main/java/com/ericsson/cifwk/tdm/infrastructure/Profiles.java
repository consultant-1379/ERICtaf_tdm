package com.ericsson.cifwk.tdm.infrastructure;

public final class Profiles {

    public static final String DEVELOPMENT = "dev";
    public static final String INTEGRATION_TEST = "itest";
    public static final String TEST = "test";
    public static final String STAGE = "stage";
    public static final String PRODUCTION = "prod";
    public static final String CUSTOMER = "customer";

    public static final String LDAP = "ldap";
    public static final String LDAP_EMBEDDED = "ldap-embedded";
    public static final String LDAP_CUSTOMER = "ldap-customer";

    private Profiles() {
        // no constructor
    }
}
