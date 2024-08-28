## User Interface

- <a ng-click="vm.scrollTo('navbar')">Navigation Bar</a>
- <a ng-click="vm.scrollTo('context')">Context Panel</a>
- <a ng-click="vm.scrollTo('datasource-list')">Data Source List</a>
- <a ng-click="vm.scrollTo('datasource-create')">Create Data Source</a>
- <a ng-click="vm.scrollTo('datasource-view')">View Data Source</a>
- <a ng-click="vm.scrollTo('datasource-edit')">Edit Data Source</a>
- <a ng-click="vm.scrollTo('datasource-copy')">Copy Data Source</a>


<a name="navbar"></a>
### Navigation Bar

The navigation bar contains the following buttons: 
- TAF Test Data Management
- User ID
- Help
- Sign in/Sign out

Click **TAF Test Data Management** in the navigation bar, to display the **Data Source List** table.
  <img src="assets/images/widgets/brand.png" class="docs-screenshot">

<a name="context"></a>
### Context Panel

Context is a concept of organization hierarchical structure unit representation.  
  <img src="assets/images/popups/choose_context.png" class="docs-screenshot">
All TDM actions occur under a chosen Context. All Data Sources belongs to a context.  
Available Data Sources will be limited to those created under chosen **Context**.  
After initial login, the root level context is displayed.  
The chosen Context is kept between user sessions, until you choose a different Context.  
To choose a different context click on a **Context name** in a
    <a ng-click="vm.scrollTo('navbar')">Navigation Bar</a>, the **Context panel** is displayed.

---


<a name="datasource-list"></a>
## Data Source List

The **Data Source List** screen displays the data sources stored in TDM.

It includes <a ng-click="vm.scrollTo('groups')">Group Tree</a> and <a ng-click="vm.scrollTo('sources')">Data Source Grid</a>

<a name="groups"></a>
### 1. Group Tree

The **Group Tree** displays the Data Source Groups hierarchy

The **Group Tree** consists of two kind of nodes:
   1. Intermediate - Sub-Groups  
   Click on a + / - to expand or collapse a Sub-Group.  
   Select a Sub-Group node to filter <a ng-click="vm.scrollTo('sources')">Data Sources Grid</a> contents.
   1. Leaf nodes - Data Sources  
   Click  ![](assets/images/buttons/external_link.png) to open a Data Source in a new browser tab.  
   Select a Data Source node to view it.

<a name="sources"></a>
### 2. Data Source Grid

<img src="assets/images/screens/datasource_grid.png" class="docs-screenshot">

In the **Data Source Grid**, each row represents one data source.  
Only data sources under the selected Sub-Group are displayed.  
The **Data Source Grid** contains the following attributes:
   - Name
   - Group
   - Context
   - Version - Current version of a Data Source.
  
- Select a Data Source by clicking on corresponding row:
    - Click **Delete** to delete selected Data Source
    - Click **View** to open <a ng-click="vm.scrollTo('datasource-view')">View Data Source</a> screen


---

<a name="datasource-create"></a>
## Create Data Source

<img src="assets/images/screens/datasource_create.png" class="docs-screenshot">

**Create Data Source** screen consists of :
   1. Data Source Name
   1. Save / Cancel Buttons
   1. Version
   1. Data Source Group
   1. Data Records Grid

<a name="version"></a>
### Version
The Version attribute can be edited manually. The version is accepted if it is greater than the previous version.  




### Data Records Grid
  - Each row in the grid represents one **Data Record**.
  - Every column represents attribute common to all Data Records.
  - Atleast one cell must be filled in, to save the data source.
  - To add a new column click **Add column** to enter column name:
      <img src="assets/images/widgets/column_name.png" class="docs-screenshot">
  - To filter Data Records:
    - Click ![](assets/images/buttons/filter.png) to toggle filtering:
        <img src="assets/images/widgets/column_filter.png" class="docs-screenshot">
    - Enter some text you want to use for filtering:
        - Rows containing this text will stay visible
        - All other Data Records will get hidden  
    <i class="fa fa-info-circle text-info"></i> **Note**: Filtering is **not** case-sensitive
  - To sort Data Records by an attribute:
    - Click on a header cell to set:
        1. ![](assets/images/buttons/sort_ascending.png) Ascending sort order
        1. ![](assets/images/buttons/sort_descending.png) Descending sort order
    - Hold SHIFT while clicking for multi-column sorting: ![](assets/images/buttons/sort_multiple.png)
  - To import Data Records from CSV file:
    - Click **Import CSV** to select an external CSV file:
        <img src="assets/images/popups/import_csv.png" class="docs-screenshot">
    - Click **Browse** to select CSV file
    - Click **Add file** to confirm your selection  
    <i class="fa fa-exclamation-circle text-danger"></i> **Note**: CSV import will **overwrite** all Data Records
  - To delete Data Records:
    - Click ![](assets/images/buttons/select_row.png) / ![](assets/images/buttons/deselect_row.png) to select / deselect corresponding row
    - Click **Delete selected rows** to delete selected rows
  - Click ![](assets/images/buttons/column_options.png) next to the column name to access some other self-explanatory column options:
    <img src="assets/images/widgets/column_options_dropdown.png" class="docs-screenshot">
  - Click ![](assets/images/buttons/grid_options.png) to access the following grid options:
    <img src="assets/images/widgets/grid_options_dropdown.png" class="docs-screenshot">
    Click ![](assets/images/buttons/hide_column.png) / ![](assets/images/buttons/show_column.png) to hide / show columns


---

<a name="datasource-view"></a>
## View Data Source

View Data source is used to view the details of a Data Source.

<img src="assets/images/screens/datasource_view.png" class="docs-screenshot">

<a name="controls"></a>

- **Data Source version** - Click on the drop down to view previous versions.
    <img src="assets/images/widgets/version_dropdown.png" class="docs-screenshot">


---

<a name="datasource-edit"></a>
## Edit Data Source

Edit Data source is used to edit the details of a Data Source.

<img src="assets/images/screens/edit-data-source.png" class="docs-screenshot">

---

<a name="datasource-copy"></a>
## Copy Data Source

<img src="assets/images/popups/copy_datasource.png" class="docs-screenshot">

Version to copy displays version 0.0.1 if **Set as base version** is not selected.  
If **Set as base version** is selected, the initial version will be set to **Version to copy**.  
If **Version to copy** is a released version, the copied datasource version will be incremented and snapshot appended.


```
example1:
"Version to copy" = 0.0.2, "Set as base version" = selected
Copied datasource version 0.0.3
example2:
"Version to copy" = 0.0.2, "Set as base version" = not selected
Copied datasource version 0.0.1

```

---


---


