package com.ericsson.cifwk.tdm.view;

import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;
import com.ericsson.cifwk.taf.ui.sdk.Link;

/**
 * Created by ekonsla on 21/03/2016.
 */
public class TDMHeaderSection extends GenericViewModel {

    @UiComponentMapping(selector = ".navbar-brand")
    private Link tdmHeaderLink;

    @UiComponentMapping(selector = "#signOutButton")
    private Link signOutButton;

    @UiComponentMapping(selector = ".breadcrumb a.ng-binding")
    private Link breadCrumb;

    public void clickTdmHeaderLink() {
        tdmHeaderLink.click();
    }

    public void logout() {
        signOutButton.click();
    }

    public void openContextMenu() {
        waitUntilComponentIsDisplayed(breadCrumb);
        breadCrumb.click();
        waitUntilComponentIsDisplayed(".aside-body");
    }

    public String getSelectedContextFromBreadcrumb(){
        return breadCrumb.getText();
    }
}
