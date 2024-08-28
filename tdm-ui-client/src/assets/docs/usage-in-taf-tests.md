## Usage in TAF tests

- TDM Data Sources are meant to be used the same way as [CSV Data Sources](https://taf.seli.wh.rnd.internal.ericsson.com/taflanding/userdocs/Latest/taf_concepts/data-driven-testing.html):
    - Include TDM Data Source maven dependency in pom.xml:
      ```xml
              <dependency>
                  <groupId>com.ericsson.cifwk.taf.testdatamanagement</groupId>
                  <artifactId>tdm-datasource</artifactId>
                  <version>...</version>
              </dependency>
      ```
      
      **Current version can be found in the bottom right on the UI.**

    - Define TDM Data Source in the `datadriven.properties` either using context and 
    name combination:

           ```
           # Loading data from TDM by Context and Name
           dataprovider.tdm-name.type=tdm
           dataprovider.tdm-name.context=ENM
           dataprovider.tdm-name.name=netsim-ds
           ```

      ...or Datasource Label:

      ```
      # Loading data from TDM by Label
      dataprovider.tdm-name.type=tdm
      dataprovider.tdm-name.label=wookie
      dataprovider.tdm-name.context=System/BUCI/DUAC/NAM/ENM
      ```

      This will retrieve the Data Source associated with the label

      **Labels are only unique within a context so the context must be specified**

      The examples above highlight the two ways the context can be specified:
      * by name
      * by full path

      **The full path may be required as a context name can be duplicated across branches in the context tree.** 

      ...or Datasource ID:

      ```
      # Loading data from TDM by ID
      dataprovider.tdm-id.type=tdm
      dataprovider.tdm-id.id=57b45244a78eb0783b4e9440
      ```
      This will retrieve the latest approved Data Source. 

      To retrieve an unapproved version "-snapshot" by Data Source ID:
      
      ```
       Loading data from TDM by ID and approved
       dataprovider.tdm-id.type=tdm
       dataprovider.tdm-id.id=57b45244a78eb0783b4e9440
       dataprovider.tdm-id.approved=false
      ``` 
      
      This will retrieve the latest snapshot version.
      
      All examples above can be used with property "version" to retrieve a particular version, just add the following:
      
      ```
      dataprovider.tdm-name.version=0.0.1
      ```
      
      (Optionally) TDM application url is set by default in TAF api but this can be overridden by:
      
      ```
       # Setting TDM datasource base url
       tdm.api.host=https://taf-tdm-prod.seli.wh.rnd.internal.ericsson.com/api/
       ```  
    - Link the Data Sources in your tests either via annotation `@DataDriven(name = "tdm-id")` or
        [binding](https://taf.seli.wh.rnd.internal.ericsson.com/taflanding/userdocs/Latest/taf_concepts/taf_scenarios/manipulating_data.html)
        `TestScenarios.dataSource("tdm-named")`
- TDM Data Source **Context** and **ID** can be found in <a ui-sref="documentation.datasource-view">View Data Source</a> screen URL:
    <img src="assets/images/screens/datasource_view_url.png" class="docs-screenshot">
    
### Filtering a Data Source
  
Data Sources can be:

 - Filtered by column
    - returns Data Source with only the specified columns
    - Columns entered as a comma separated string array
    - Column example:
        - name,diameter
 - Filtered predicate
    - returns Data Source with only the rows matching the predicate
    - predicate is String only
    - Supported predicates examples:
        - "a=1", "b>2", "c<3", "text=abrakadabra", "positive_value>=99", "negative-key<=0"
 - Filtered by both column and predicate.
 
 All examples above can be used with filtering. To use filtering specify the following datadriven properties:
 
 ```
 dataprovider.tdm-id-version-filter.filter=name=Hoth
 dataprovider.tdm-id-version-filter.columns=name,diameter
 ```
 
 ####Filtering Advanced
 Users can add more than one filter. This is a good example if they wanted to drill down to a specific 
 data source piece of data.
 
 By using the function key "*;*" users can add another filter to the data source and drill down to what data they specifically need.

  ```
  dataprovider.tdm-id-version-filter.filter=name=Kamino;climate=temperate
  ```
 
The filter syntax can also pass a comma "*,*". What this means is that you want to do another separate 
search. In this case its possible to search and get the same data source row. 

 ```
  dataprovider.tdm-id-version-filter.filter=name=Kamino,name=Hoth
 ```
