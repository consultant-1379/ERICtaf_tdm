## Tutorials

- <a ng-click="vm.scrollTo('datasource-create')">Create Data Source</a>
- <a ng-click="vm.scrollTo('datasource-view')">View Data Source</a>
- <a ng-click="vm.scrollTo('datasource-edit')">Edit Data Source</a>
- <a ng-click="vm.scrollTo('datasource-copy')">Copy Data Source</a>
- <a ng-click="vm.scrollTo('datasource-delete')">Delete Data Source</a>
- <a ng-click="vm.scrollTo('datasource-label')">Label Data Source</a>


<a name="datasource-create"></a>

Create Data Source is used to create a new Data Source.
### Prerequisites
You should be logged in to TDM.

### Create a Data Source:

<a name="name"></a>
1) Click **New**.

2) Enter Data Source **Name**.  
<i class="fa fa-info-circle text-info"></i> **Note**: Existing data source name cannot be used while naming or renaming a data source.

3) Enter **Data Source Version** details.

4) Enter **Data Source Group** details. This field has auto-completion support.
<a name="grid"></a>

5) Enter **Data Records** details.

6) Click **Save** to confirm creation of a Data Source.
 
For more information on creating a data source, refer <a ui-sref="documentation.commons({'#': 'datasource-create'})">Create Data Source</a>


<a name="datasource-view"></a>
## View Data Source

View Data Source is used to view the details of a Data Source.

### Prerequisites
You should be logged in to TDM.

### View a Data Source:
1) Select a data source from <a ui-sref="documentation.commons({'#': 'datasource-list'})">Data Source grid</a>

2) Click **View** to view the data source

3) Click **Copy** to <a ng-click="vm.scrollTo('datasource-copy')">Copy Data Source</a>

4) Click **Edit** to <a ng-click="vm.scrollTo('datasource-edit')">Edit Data Source </a>  
  <i class="fa fa-info-circle text-info"></i> **Note**: After creation of a Data Source, it gets initial version as described in
<a ui-sref="documentation.commons({'#': 'version'})">Data Source Version</a>

5) **Context** shows the context full path.

6) **Data Source ID** shows unique ID which is assigned to the data source.

7) **Group** shows the group name under which the data source is created.

8) Click the ![](assets/images/buttons/copy_to_clipboard.png) icon to copy the value to clip board.

9) **Data records** shows list of data records that are added to the data source.

For more information on viewing a data source, refer <a ui-sref="documentation.commons({'#': 'datasource-view'})">View Data Source</a>

<a name="datasource-edit"></a>
## Edit Data Source

Edit Data Source is used to edit an existing Data Source.

### Prerequisites
You should be logged in to TDM.

### Edit a Data Source:

1) Select a data source and click **Edit**.

2) Click **Data Source Name** to change data source name.  
<i class="fa fa-info-circle text-info"></i> **Note**: Existing data source name cannot be used while naming or renaming a data source.

3) Click **Version** to change version details.
  Data Source will remain at same snapshot version until it is approved as per <a ui-sref="documentation.commons({'#': 'version'})">Data Source versioning</a>.

4) Click **Group** to change Group details.

5) Click **Add row** or **Add column** to add new rows or columns to the existing data source.

6) Click on existing row or column to edit the Data Record.

7) Click on **Import CSV** to import CSV data to the grid.

8) On import, previous data from the grid will be replaced with new CSV data.

9) Select record that you want to delete and click on **Delete selected rows** .
   Click on **confirm** to confirm deletion, on **cancel** to retain the data.
   
10) Click **Save** to confirm editing of the Data Source, Click **Cancel** to discard the changes.
<i class="fa fa-info-circle text-info"></i> **Note**: While editing of a datasource incase deletion of records is confirmed,Cancellation of Editing of data source will not retain the deleted data Records

<a name="datasource-copy"></a>
## Copy Data Source

Copy Data Source is used to copy an existing Data Source across contexts.

### Prerequisites
You should be logged in to TDM.

### Copy a Data Source:

<a name="copy"></a>
1) Select a data source and click **View**.

2) Click **Copy** to Copy Data Source.

3) Specify the following attributes to copy the Data Source:

 - **Version to copy** - Data Source version to be copied.
 - **Set as base version** - Upon selecting this, Version to be copied will be set as initial version.
 - **New Data Source Name** - Copied Data Source name.
 - **New Context** - Context to which the Data Source to be copied.
 - **Group** - Group under which the Data Source to be copied and this text field supports autocompletion.

4) Click **Copy** to confirm creation of a Data Source copy.

For more information on copying a data source, refer <a ui-sref="documentation.commons({'#': 'datasource-copy'})">Copy Data Source</a>.
<a name="datasource-delete"></a>
## Delete Data Source

Delete Data Source is used to delete an existing Data Source across contexts.

### Prerequisites
You should be logged in to TDM.

### Delete a Data Source:

<a name="delete"></a>
1) Select a data source from <a ui-sref="documentation.commons({'#': 'datasource-list'})">Data Source grid</a>.

2) Click **Delete** to Delete Data Source.

3) Message will be prompted to the user,before to confirm the delete.

4) Click **Confirm** to confirm deletion of the Data Source.

5) Click **Cancel** to cancel deletion of the Data Source.

<i class="fa fa-info-circle text-info"></i> **Note**: Data Source once deleted, all labels will be removed!


<a name="datasource-label"></a>
## Label Data Source

### Prerequisites
- You should be logged in to TDM.

- The **label Data Source** option only appears when a Data Source version is **Approved**.

<a name="label"></a>
### Add Label to a Data Source

1) Select required data source and click **View**.

2) Select **Click to edit** under label.

3) Enter Label details.

4) Click on **Save**.  
<i class="fa fa-info-circle text-info"></i> **Note**: If the label exists on another datasource in the context then this operation will fail.  
The error message gives information about the other datasource.  
Label can be removed by removing the text and click save.


