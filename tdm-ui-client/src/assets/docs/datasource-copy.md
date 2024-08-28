## Copy Data Source

<a name="copy"></a>
#### **Copy Data Source**

- The **Copy Data Source** popup looks the following way:
  <img src="assets/images/popups/copy_datasource.png" class="docs-screenshot">
- You can specify the following attributes for the Data Source copy:
    - **Version to copy** - Data Source version to be copied
    - **Set as base version** - Upon selecting this, Version to be copied will be set as initial version
    - **New Data Source Title** - Copied Data Source name
    - **New Context** - Context to which the Data Source to be copied
    - **Group** - Group under which the Data Source to be copied and this text field supports autocompletion  
<i class="fa fa-info-circle text-info"></i> **Note**: Data Source copy version will have initial version 0.0.1-SNAPSHOT if "Set as base version" is not 
selected. if "Set as base version" is selected, initial version will be set to "Version to copy". If "Version to
copy" is a released version then the copied datasource version will be incremented and snapshot appended.

```
example1: 
"Version to copy" = 0.0.2, "Set as base version" = selected
Copied datasource version 0.0.3-SNAPSHOT
example2: 
"Version to copy" = 0.0.2, "Set as base version" = not selected
Copied datasource version 0.0.1-SNAPSHOT
example3: 
"Version to copy" = 0.0.2-SNAPSHOT, "Set as base version" = selected
Copied datasource version 0.0.2-SNAPSHOT
```


- Click ![](assets/images/buttons/copy_confirm.png) to confirm creation of a Data Source copy

Read next: <a ui-sref="documentation.datasource-approve">Approve Data Source</a>
