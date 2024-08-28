## Test Data Orchestration

### Introduction
  TDM controls the test data used by the test ware. This is achieved by locking rows of test data
  that the test ware has requested. Test data can be used in 2 ways:

   - To lock the data completely from other test wares.
   - To acquire a lock that allows other test ware to re-use the same row of data.


  Test data can be set to two lock types:

  - SHARED - This data will not effect the test environment and can be used by any test.
  - EXCLUSIVE - This test data can only be used by a single test ware and is locked until the test is completed, during the test execution.
  
  
  If the data cannot retrieve the requested data it will wait for a set period of time
  and retry to acquire the data that has been requested.
  
  This feature is turned off but to enable locking the following property needs to be set to true.
  
   ```
    # Enable locking
      tdm.enable.lock=true
   ```

### Configuration
  Configuration will take either label or Id of the data source.
  ```
  # Loading data from TDM by ID
    dataprovider.tdm-data.type=tdm
    dataprovider.tdm-data.id=57b45244a78eb0783b4e9440
  ```
  or
  ```
  # Loading data from TDM by label
    dataprovider.tdm-data.type=tdm
    dataprovider.tdm-data.label=wookie
  ```

  This will retrieve the latest approved Data Source. 

  ### Locking

   Locking a datasource row can be configured. All options for lock are optional and will take its defaults if not set.
   The default values will be used. <a ng-click="vm.scrollTo('default')">Defaults</a>


  - lock.type - the lock type to request i.e. EXCLUSIVE or SHARED
  - lock.timeout_seconds - time it takes for the lock to expire if not released by testware. i.e. 300
  - lock.wait_timeout_seconds - time it takes to retry fetching a data source. i.e. 30
 

    ```
      dataprovider.tdm-data.lock.type=EXCLUSIVE,SHARED
      dataprovider.tdm-data.lock.timeout_seconds=300
      dataprovider.tdm-data.lock.wait_timeout_seconds=60
    ``` 
 
 

  ### Rules
      - An exclusive lock type on a data source will lock it for a test run.
      - A shared lock type will allow other testware to use the same row of data.

      - An exclusive lock cannot use a shared lock.
      - A shared lock cannot use an exclusive lock.
      - A shared lock can use another shared lock.


  ### Defaults
  - By default the quantity of rows is 1.
  - By default the lock type is SHARED.
  - By default lock.timeout_seconds is 300 seconds.
  - By default lock.wait_timeout_seconds is 30 seconds.
  
  
  ### Filtering
  Filtering allows the user to request specific pieces of data from the datasource.
  All options for filter are optional.

  - quantity - the quantity of rows to retrieve.
  - filter - filter data in a column 
  
  Quantity if not set will use its default value. <a ng-click="vm.scrollTo('default')">Defaults</a>

  The filter and the quantity option can be used for multiple queries. 
  By using a comma to separate each value. TDM will do 2 separate searches of the datasource.
  In the example below the user is using multiple filters by: 
  - Filtering on the column name to find the value Kamino
  - Quantity is set to find 1 row of data.

  Seperated by a comma
  - Filtering on column climate to find tha value temperate.
  - Quantity is set to find 3 rows of data.


  Example
  ```
     dataprovider.tdm-data.quantity=1,3
     dataprovider.tdm-data.filter=name=Kamino,climate=temperate
  ``` 


  The user will receive 4 rows of data to be used by the testware.
    
### Defaults<a name="default"></a>
 - By default the quantity of rows is 1.
 - By default the lock type is SHARED.
 - By default lock.timeout_seconds is 300 seconds.
 - By default lock.wait_timeout_seconds is 30 seconds.


### Full Example

  In the example below the user is trying to retrieve 2 sets of data
   - 1 row of data, where column "name" matches Kamino which is exclusive 
   - 3 rows of data, where column "climate" matches temperate which is shared
   
   
  The user will then receive 4 rows of data for their test run.
   
  ```
     dataprovider.tdm-data.type=tdm
     dataprovider.tdm-data.id=57b45244a78eb0783b4e9440
     dataprovider.tdm-data.lock.type=EXCLUSIVE,SHARED
     dataprovider.tdm-data.quantity=1,3
     dataprovider.tdm-data.filter=name=Kamino,climate=temperate
  ``` 
