package com.ericsson.cifwk.tdm.view;

import com.ericsson.cifwk.taf.ui.core.AbstractUiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponent;

import java.util.List;

/**
 * Created by ekonsla on 12/05/2016.
 */
public class DataSourceListRow extends AbstractUiComponent {

    public String getCellByIndex(int index) {
        List<UiComponent> cells = getDescendantsBySelector(".ui-grid-cell-contents");
        return cells.get(index).getText();
    }
}

