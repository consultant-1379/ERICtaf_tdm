package com.ericsson.cifwk.tdm.netsim.agent.netsim;

public final class NetsimConstants {

    public static final String NETSIM_DS_GROUP_PARAM = "netsim.datasource.group";
    public static final String NETSIM_DS_NAME_PARAM = "netsim.datasource.name";
    public static final String NETSIM_DS_CONTEXT_ID_PARAM = "netsim.datasource.Context.id";

    public static final String NETSIM_DEFAULT_GROUP = "com.netsim";
    public static final String NETSIM_DEFAULT_NAME = "netsim-ds";
    public static final String NETSIM_DEFAULT_CONTEXT_ID = "systemId-1";
    public static final String DS_GROUP = System.getProperty(NETSIM_DS_GROUP_PARAM, NETSIM_DEFAULT_GROUP);
    public static final String DS_NAME = System.getProperty(NETSIM_DS_NAME_PARAM, NETSIM_DEFAULT_NAME);
    public static final String DS_CONTEXT_ID = System.getProperty(NETSIM_DS_CONTEXT_ID_PARAM,
            NETSIM_DEFAULT_CONTEXT_ID);
    private NetsimConstants() {
    }
}
