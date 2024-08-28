package com.ericsson.cifwk.tdm;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;

public class HostResolver {

    public static final String TAF_TDM = "tdm";

    private HostResolver(){}

    public static Host resolve() {
        return DataHandler.getHostByName(TAF_TDM);
    }

}
