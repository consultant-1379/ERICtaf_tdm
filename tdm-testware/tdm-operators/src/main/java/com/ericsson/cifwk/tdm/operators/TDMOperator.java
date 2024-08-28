package com.ericsson.cifwk.tdm.operators;

import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.annotations.VUserScoped;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.ui.Browser;
import com.ericsson.cifwk.taf.ui.BrowserTab;
import com.ericsson.cifwk.taf.ui.UI;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.cifwk.tdm.view.ContextAside;
import com.ericsson.cifwk.tdm.view.CsvImportModalDialog;
import com.ericsson.cifwk.tdm.view.DataSourceEditPage;
import com.ericsson.cifwk.tdm.view.DataSourcesCreatePage;
import com.ericsson.cifwk.tdm.view.DataSourcesListPage;
import com.ericsson.cifwk.tdm.view.DatasourceViewPage;
import com.ericsson.cifwk.tdm.view.Dialogs;
import com.ericsson.cifwk.tdm.view.LoginPage;
import com.ericsson.cifwk.tdm.view.TDMHeaderSection;

import java.io.File;


/**
 * Created by ekonsla on 21/03/2016.
 */
@Operator
@VUserScoped
public class TDMOperator {

    private static final String BASE_PATH = "/#";
    private static final String LOGIN_PATH = "/login";

    private Host host;
    private Browser browser;
    private BrowserTab browserTab;

    public void init(Host host) {
        this.host = host;
        browser = UI.newBrowser();
        String loginUrl = getLoginUrl();
        browserTab = browser.open(loginUrl);
        browserTab.maximize();
    }

    private String getLoginUrl() {
        return getBaseUrl() + LOGIN_PATH;
    }

    private String getBaseUrl() {
        String port = host.getPort().get(Ports.HTTP);
        String hostAddress = host.getIp();
        return "http://" + hostAddress + ":" + port + BASE_PATH;
    }

    public Browser getBrowser() {
        return browser;
    }

    public Host getHost() {
        return host;
    }

    public LoginPage getLoginPage() {
        return browserTab.getView(LoginPage.class);
    }

    public TDMHeaderSection getHeaderSection() {
        return browserTab.getView(TDMHeaderSection.class);
    }

    public ContextAside getContextAside() {
        return browserTab.getView(ContextAside.class);
    }

    public DatasourceViewPage getDatasourceViewPage() {
        return browserTab.getView(DatasourceViewPage.class);
    }

    public DataSourceEditPage getDataSourceEditPAge(){
        return browserTab.getView(DataSourceEditPage.class);
    }

    public DataSourcesListPage getDatasourcesListPage() {
        return browserTab.getView(DataSourcesListPage.class);
    }

    public DataSourcesCreatePage getDataSourcesCreatePage() {
        return browserTab.getView(DataSourcesCreatePage.class);
    }

    public CsvImportModalDialog getCsvModalDialog() {
        return browserTab.getView(CsvImportModalDialog.class);
    }

    public void confirmDialog() {
        getDialogs().confirmDialog();
    }

    public void closeToast() {
        getDialogs().closeToast();
    }

    public Dialogs getDialogs() {
        return browserTab.getView(Dialogs.class);
    }

    public void navigateToCreateDataSourceView() {
        getDatasourcesListPage().clickCreateDataSourceButton();
    }

    public void navigateToContext(String context) {
        browserTab.open(getBaseUrl() + "/contexts/" + context + "/datasources");
    }

    public void enterDataSourceName(String name) {
        getDataSourcesCreatePage().setDataSourceName(name);
    }

    public void enterDataSourceGroup(String group) {
        getDataSourcesCreatePage().setDataSourceGroup(group);
    }

    public void importCsvData(final String csvFilename) {
        getDataSourcesCreatePage().clickImportCsvButton();
        uploadCsvFileToGrid(csvFilename);
        getDataSourcesCreatePage().waitUntilImportCsvButtonIsHidden();
    }

    public void saveDataSource() {
        getDataSourcesCreatePage().clickSaveButton();
        getDatasourceViewPage().waitUntilPageLoaded();
    }

    public int getNumberOfColumns() {
        return getDataSourcesCreatePage().getNumberOfTableColumns();
    }

    public int getNumberOfRows() {
        return getDataSourcesCreatePage().getNumberTableRows();
    }

    public boolean verifyNoImportErrorsDisplayed() {
        return getDataSourcesCreatePage().isErrorBlockDisplayed();
    }

    private void uploadCsvFileToGrid(final String csvFilename) {
        final String filePath = FileFinder.findFile(csvFilename).get(0);
        File uploadedFile = new File(filePath);
        getCsvModalDialog().setImportedFile(uploadedFile);
        getCsvModalDialog().clickAddFileButton();
    }

    public void openDataSource(final String dataSourceName) {
        DataSourcesListPage page = browserTab.getView(DataSourcesListPage.class);
        page.openDatasource(dataSourceName);
        DatasourceViewPage viewPage = browserTab.getView(DatasourceViewPage.class);
        viewPage.waitUntilPageLoaded();
    }

    public void goToHomePage() {
        DataSourcesListPage listPage = browserTab.getView(DataSourcesListPage.class);
        listPage.goHome();
    }
}
