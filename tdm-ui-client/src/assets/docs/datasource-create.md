## Create Data Source

<img src="assets/images/screens/datasource_create.png" class="docs-screenshot">

- **Create Data Source** screen consists of :
    1. <a ng-click="vm.scrollTo('name')">Data Source Name</a>
    1. <a ng-click="vm.scrollTo('buttons')">Save / Cancel Buttons</a>
    1. <a ng-click="vm.scrollTo('version')">Version</a>
    1. <a ng-click="vm.scrollTo('group')">Data Source Group</a>
    1. <a ng-click="vm.scrollTo('grid')">Data Records Grid</a>

<a name="name"></a>
### 1. Data Source Name

- This **Mandatory** attribute allows naming the Data Source  
<i class="fa fa-info-circle text-info"></i> **Note**: TDM does not allow usage of existing or deleted Data Source name while naming or renaming a Data Source.
If the below message is observed while naming/renaming a Data Source, please use a different name and hit save.

                    Data source in context XYZ with name ABC already exists but was
                      deleted and cannot be re-used Failed to update data source 

<a name="buttons"></a>
### 2. Save / Cancel Buttons

- Click ![](assets/images/buttons/save.png) to save the Data Source
- Click ![](assets/images/buttons/cancel.png) to cancel creation of a Data Source
- For a successful save you **must** fill in all **mandatory** fields:
    - <a ng-click="vm.scrollTo('name')">Data Source Name</a>
    - <a ng-click="vm.scrollTo('group')">Data Source Group</a>
    - At least one <a ng-click="vm.scrollTo('grid')">Data Record</a>
- Upon successful save of the Data Source, navigation moves to
    <a ui-sref="documentation.datasource-view">View Data Source</a> screen
- If Data Source creation is cancelled, **all entered data will be lost** and
    navigation goes to <a ui-sref="documentation.datasource-list">Data Source List</a> screen

<a name="version"></a>
### 3. Version

Optional **Version** textfield:
  <img src="assets/images/widgets/version.png" class="docs-screenshot">
Version attribute can be edited manually. If it is left empty, applications handles it by itself.If the version is set manually, it will be accepted by the application provided it is greater than the previous version.
For example 0.0.1 is the existing version and then it can be edited to 1.0.1, 2.0.0 etc. This allows users full control of how they want to version the datasource. Data Source will remain at the SNAPSHOT version until it is approved. 
Until Data Source is approved all edit actions will be accumulated in the same SNAPSHOT version. Once Data Source has been sent for approval no more edit actions will be allowed unless the request is cancelled. Once Data Source is approved, version will be released.

```
Example:
- Data Source version = 1.0.6-SNAPSHOT
- Data Source edited 4 times
- All edits are accumulated in version 1.0.6-SNAPSHOT
- Data Source sent for approval
- Data Source approved
- Data Source version 1.0.6 released
- Data Source version 1.0.7-SNAPSHOT will be created when someone clicks ![](assets/images/buttons/edit.png) on version 1.0.6
```
 

<a name="group"></a>
### 4. Data Source Group

- **Mandatory** text field with auto-completion support:
    <img src="assets/images/widgets/group_autocomplete.png" class="docs-screenshot">
- Auto-completion works based on previously saved Data Source groups

<a name="grid"></a>
### 5. Data Records Grid

- Each row in the grid represents one Data Record
- Every column represents attribute common to all Data Records
- You **must** fill in at least one cell in order to save the Data Source
- In order to add a new column:
    - Click ![](assets/images/buttons/add_column.png) to enter column name:
        <img src="assets/images/widgets/column_name.png" class="docs-screenshot">
    - Enter new column name and Click ![](assets/images/buttons/ok.png)
- In order to filter Data Records:
    - Click ![](assets/images/buttons/filter.png) to toggle filtering:
        <img src="assets/images/widgets/column_filter.png" class="docs-screenshot">
    - Enter some text you want to use for filtering:
        - Rows containing this text will stay visible
        - All other Data Records will get hidden
    - <i class="fa fa-info-circle text-info"></i> **Note**: Filtering is **not** case-sensitive
- In order to sort Data Records by an attribute:
    - Click on a header cell to set:
        1. ![](assets/images/buttons/sort_ascending.png) Ascending sort order
        1. ![](assets/images/buttons/sort_descending.png) Descending sort order
        1. No sorting for this column at all
    - Hold SHIFT while clicking for multi-column sorting: ![](assets/images/buttons/sort_multiple.png)
- In order to import Data Records from CSV file:
    - Click ![](assets/images/buttons/import_csv.png) to select an external CSV file:
        <img src="assets/images/popups/import_csv.png" class="docs-screenshot">
    - Click ![](assets/images/buttons/browse.png) to select CSV file
    - Click ![](assets/images/buttons/add_file.png) to confirm your selection
    - <i class="fa fa-exclamation-circle text-danger"></i> **Be careful**: CSV import will **overwrite** all Data Records
- In order to delete Data Records:
    - Click ![](assets/images/buttons/select_row.png) / ![](assets/images/buttons/deselect_row.png) to select / deselect corresponding row
    - Click ![](assets/images/buttons/delete_selected_rows.png) in order to delete selected rows
- Click ![](assets/images/buttons/column_options.png) next to the column name to access some other self-explanatory column options:
    <img src="assets/images/widgets/column_options_dropdown.png" class="docs-screenshot">
- Click ![](assets/images/buttons/grid_options.png) in order to access the following grid options:
    <img src="assets/images/widgets/grid_options_dropdown.png" class="docs-screenshot">
    Click ![](assets/images/buttons/hide_column.png) / ![](assets/images/buttons/show_column.png) to hide / show columns

---

Read next: <a ui-sref="documentation.datasource-view">View Data Source</a>
