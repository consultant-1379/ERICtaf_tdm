package com.ericsson.cifwk.tdm.view;

import java.util.List;

import com.ericsson.cifwk.taf.ui.core.AbstractUiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;

public class DataRecordsTable extends AbstractUiComponent {

    @UiComponentMapping(".ui-grid-row")
    private List<DataSourceListRow> rows;

    public List<DataSourceListRow> getDataRecords(){
        return rows;
    }
}
