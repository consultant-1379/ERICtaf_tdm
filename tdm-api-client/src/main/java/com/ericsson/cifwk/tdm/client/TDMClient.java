package com.ericsson.cifwk.tdm.client;

import com.ericsson.cifwk.tdm.client.services.ContextService;
import com.ericsson.cifwk.tdm.client.services.DataSourceExecutionService;
import com.ericsson.cifwk.tdm.client.services.DataSourceService;
import com.ericsson.cifwk.tdm.client.services.ExecutionService;
import com.ericsson.cifwk.tdm.client.services.LockService;
import com.ericsson.cifwk.tdm.client.services.LoginService;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 22/02/2016
 */
public class TDMClient {

    private final RestClientProvider restClientProvider;

    public TDMClient(String host) {
        restClientProvider = RestClientProvider.getInstance(host);
    }

    public ExecutionService getExecutionService() {
        return createService(ExecutionService.class);
    }

    public DataSourceService getDataSourceService() {
        return createService(DataSourceService.class);
    }

    public ContextService getContextService() {
        return createService(ContextService.class);
    }

    public DataSourceExecutionService getDataSourceExecutionService() {
        return createService(DataSourceExecutionService.class);
    }

    public LockService getLockService() {
        return createService(LockService.class);
    }


    public LoginService getLoginService() {
        return createService(LoginService.class);
    }

    private <T> T createService(Class<T> service) {
        return restClientProvider.getRetrofit().create(service);
    }

}
