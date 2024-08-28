## View Data Source

<img src="assets/images/screens/datasource_view.png" class="docs-screenshot">

- **View Data Source** consists of the following:
    - <a ng-click="vm.scrollTo('controls')">Control Panel</a>
    - <a ng-click="vm.scrollTo('approval')">Approval Panel</a>
    - <a ng-click="vm.scrollTo('datasourceinfo')">Data Source Info Panel</a>
    - <a ng-click="vm.scrollTo('version')">Version History</a>
    - <a ng-click="vm.scrollTo('label')">Add Label To Data Source</a>

<a name="controls"></a>
### Control Panel

- Click on <a ng-click="vm.scrollTo('versions')">Data Source Versions</a> to view previous versions
    <img src="assets/images/widgets/version_dropdown.png" class="docs-screenshot">
- Click ![](assets/images/buttons/copy_start.png) in order to <a ui-sref="documentation.datasource-copy">Copy Data Source</a>
- Click ![](assets/images/buttons/edit.png) to open <a ui-sref="documentation.datasource-edit">Data Source Edit</a> screen

<a name="versions"></a>
#### **Data Source Versions**

- After creation every Data Source gets initial version as described in
<a ui-sref="documentation.datasource-create({'#': 'version'})">Data Source Version</a>


<a name="approval"></a>
### Approval Panel

- **Approval Status** shows whether a Data Source has been reviewed
  and what was the outcome of this review
- Only ![](assets/images/other/approved_label.png) Data Sources can be
  used in TAF tests</a>
- Read more about Data Source  <a ui-sref="documentation.datasource-approve">Approval process</a>

<a name="datasourceinfo"></a>
### Data Source Info Panel

- **Context** shows the context full path
- **Data Source ID** shows unique ID which is assigned to the data source
- **Group** shows the group name under which the data source is created
- Click the ![](assets/images/buttons/copy_to_clipboard.png) icon to copy the value to clip board

<a name="version"></a>
### Version History

- Click ![](assets/images/buttons/sprint_version_history.png) to show **Version History** popup:
  <img src="assets/images/popups/version_history.png" class="docs-screenshot">
- The popup shows the <a ui-sref="documentation.datasource-create({'#': 'version'})">Version</a> history of the Data Source.

<a name="label"></a>
### Add Label To Data Source

- In order to add a label to a Data source it must be approved. For details click <a ui-sref="documentation.datasource-label">Add Label</a>

---

Read next: <a ui-sref="documentation.datasource-edit">Edit Data Source</a>
