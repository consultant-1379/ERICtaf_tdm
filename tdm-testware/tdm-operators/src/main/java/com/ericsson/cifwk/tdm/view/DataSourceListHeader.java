package com.ericsson.cifwk.tdm.view;

import com.ericsson.cifwk.taf.ui.core.AbstractUiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.core.UiComponentNotFoundException;

import java.util.List;

/**
 * Created by ekonsla on 12/05/2016.
 */
public class DataSourceListHeader extends AbstractUiComponent {

    @UiComponentMapping(".ui-grid-header-cell-row")
    UiComponent headerRow;

    public int getColumnIndex(String column) {
        List<UiComponent> headers = headerRow.getDescendantsBySelector(".ui-grid-cell-contents");
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).getText().equalsIgnoreCase(column)) {
                return i;
            }
        }
        throw new UiComponentNotFoundException(column);
    }
}
