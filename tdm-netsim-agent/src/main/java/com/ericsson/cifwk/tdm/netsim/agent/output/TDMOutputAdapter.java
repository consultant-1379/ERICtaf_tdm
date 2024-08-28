package com.ericsson.cifwk.tdm.netsim.agent.output;

import com.ericsson.cifwk.tdm.api.model.AuthenticationStatus;
import com.ericsson.cifwk.tdm.api.model.DataSource;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.api.model.UserCredentials;
import com.ericsson.cifwk.tdm.client.TDMClient;
import com.ericsson.cifwk.tdm.client.services.DataSourceService;
import com.ericsson.cifwk.tdm.client.services.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

import java.io.IOException;

/**
 * Created by ekonsla on 09/03/2016.
 */
public class TDMOutputAdapter implements DataSourceOutputAdapter {

    public static final String TDM_API_HOST_PARAM = "tdm.api.host";
    public static final String TDM_DEFAULT_HOST = "https://taf-tdm-prod.seli.wh.rnd.internal.ericsson.com/api/";
    public static final String TDM_USER_NAME_PARAM = "tdm.user.name";
    public static final String TDM_USER_AUTH_PARAM = "tdm.user.password";

    private static final Logger LOGGER = LoggerFactory.getLogger(TDMOutputAdapter.class);

    private TDMClient tdmClient;

    private DataSourceService dataSourceService;
    private LoginService loginService;

    public TDMOutputAdapter() {
        String tdmHost = System.getProperty(TDM_API_HOST_PARAM, TDM_DEFAULT_HOST);
        tdmClient = new TDMClient(tdmHost);
        initializeLoginService();
        initializeDataSourceService();
        loginToTDM();
    }

    private void initializeLoginService() {
        loginService = tdmClient.getLoginService();
    }

    private void initializeDataSourceService() {
        dataSourceService = tdmClient.getDataSourceService();
    }

    private void loginToTDM() {
        String tdmUserName = System.getProperty(TDM_USER_NAME_PARAM);
        String tdmUserAuth = System.getProperty(TDM_USER_AUTH_PARAM);
        if (tdmUserName != null && tdmUserAuth != null) {
            UserCredentials credentials = new UserCredentials(tdmUserName, tdmUserAuth);
            try {
                Response<AuthenticationStatus> response = loginService.login(credentials).execute();
                if (response.code() != 200) {
                    throw new IOException("Login failed");
                }
            } catch (IOException e) {
                LOGGER.error("login to TDM failed, Please provide the correct UserName & Password", e);
                throw new IllegalArgumentException("login to TDM failed", e);
            }
        } else {
            LOGGER.error("unable to login to TDM, Please provide the userid & password");
            throw new IllegalArgumentException("login to TDM failed");
        }
    }

    @Override
    public void output(DataSource dataSource) {
        try {
            Response<DataSourceIdentity> response = dataSourceService.createDataSource(dataSource).execute();
            DataSourceIdentity identity = response.body();
            LOGGER.info("Datasource {} created", identity);
        } catch (IOException e) {
            LOGGER.error("unable to create datasource", e);
        }
    }
}
