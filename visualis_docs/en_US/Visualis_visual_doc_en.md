> Virtual view design document  

This document mainly describes that the widget page of visualis system receives one or more metadata information (i.e. JSON format patched view, including field name, field type, original query statement and data source information) as parameters at run time, and carries out dynamic data query architecture adjustment scheme according to the information provided in the selected parameter view.

The adjustment involves the following aspects:
1.	Parameter view and source structures.
2.	Widget page transformation.
3.	Query logic transformation.
## Parameterized view and source structures
1.	Add the following concepts:  
a) Virtual view: a view without specific content. The widget bound to the view is the widget that receives view parameters for dynamic rendering.  
b) Parameter view: contains the field name, field type, original query statement, data source and other information in the form of JSON, which is passed to the widget editing page as a URL parameter.  
c) Parameter source: as a field of the parameter view, it is also a JSON structure. It specifies the engine type (spark, hive, JDBC, etc.), data source type (hive library table, SQL script, linkis result set, etc.), specific content of the data source, and data source (creator, such as scripts) that the view should submit when querying.  
d) In the above concept, the widget needs to be able to be bound to a virtual view to receive the parameters of the patched view.
2.	The virtual view is a row inserted in the database, and its project_ ID and source_ ID is -1; The SQL, model, variable, and config fields are all null.
3.	 It only supports the creation of a widget bound to a virtual view when it is called by other systems and parameters are passed. You cannot manually select to bind a virtual view when creating a widget.
4.	参The parameter view needs to conform to the following JSON format.  
a) Correspondence between data source type and specific data content:

|dataSourceType |dataSourceContent |   
|---------------|------------------|  
resultset|Result set path  
script|BML resource id + version  
table|Library table name
context|context id,keyword  
url|url  
```
{ 
    "name": "test_view1", 
    "model": { 
        "data_id": { 
            "sqlType": "STRING", 
            "visualType": "string", 
            "modelType": "value"
        }, 
        "ds": { 
            "sqlType": "STRING", 
            "visualType": "string", 
            "modelType": "category"
        }
    }, 
    "source": { 
        "engineType": "spark", //Engine type
        
        "dataSourceType": "resultset", //Data source type, result set, script, library table
        "dataSourceContent": {
	     "resultLocation": "/tmp/linkis/resultset/_0.dolphin"
	 },
        "creator": "scriptis"
    }, 
    "params": "[]"
}
```
1. Parameter transfer mode  
a)	newly build widget：/dss/visualis/#/project/3/widget/add?views=[{view1 json},{view2 json}]  
b)	edit widget：/dss/visualis/#/project/3/widget/4?views=[{view1 json},{view2 json}]  
c)	Consider providing a post interface to directly create a virtual widget, and then open the widget to render
## Widget page transformation
1.	Add the following concepts:  
a) Virtual widget: refers to the widget bound to the virtual view. The config clearly indicates virtual=true. Add the source field in the config to store the parameter source; The model field of its config stores the field information in the parameter view.  
b) Context ID: when it is created as a widget node, the ContextID field is added to the config field of the widget to store the context ID corresponding to the flow where the widget node is located. (the widgets corresponding to all widget nodes are virtual widgets, and virtual=true is set when creating)  
2.	When opening the new widget interface:  
a) If there is no URL parameter, the original logic is maintained.  
b) If there is a URL parameter, it is considered that you are ready to create a new virtual widget. If there is only one parameter, select the parameter view. If there are multiple parameters, uncheck them and put them all in the drop-down list. When saving, indicate virtual=true.
3.	When the edit widget interface is opened:  
a) If there is no URL parameter and the widget is a virtual widget:  
i. Check that the model in config is not empty. If yes, query the parameter source directly submitted to config.  
ii. If the model in config is empty and the ContextID is not empty, all the upstream metadata (the backend provides interfaces) will be found according to the ContextID as an alternative view for the drop-down list. After selecting and saving the metadata in the context, the source in the config is updated to the corresponding context type and the corresponding key is recorded.  
III. If there is nothing, keep the original logic of editing an empty widget page unchanged, and there is nothing meaningful to operate.  
b) If there is a URL parameter and the widget is a virtual widget: if there is only one parameter, select the parameter view. If there are multiple parameters, uncheck them and put them all in the drop-down list.  
c) No matter whether there is a URL parameter or not, as long as a non virtual normal widget is opened, the original logic remains unchanged.
4.	For the GetData and share/data interfaces, under the virtual widget, an additional source parameter should be passed. During back-end processing:  
a) If it is a virtual widget and it is not transmitted to the source, it will directly query and report an error;  
b) If it is a normal non virtual widget, it will be ignored even if the source is passed.
## Query logic transformation
For the new data sources, the previous query method needs to be modified as follows:
1.	Sourceinitializer: initializes the data source and returns more detailed source information after initialization.  
a) For the result set data source, generate or update the temp view in spark, and supplement the SQL that selects the temp view in the returned source.  
b) For the SQL script data source, pull the corresponding SQL from BML and put it into the returned source.  
c) For the hive library table data source, the splice select statement is placed in the returned source.  
d) For the CS data source, pull the specific metadata content according to the context ID, and splice the select statement into the returned source.  
e) For the URL external data source, the requested data is converted into dolphin format, submitted to spark to create a temp view, and the returned source is supplemented with SQL to select the temp view.  
i. The URL data source provider directly provides data in dolphin format.  
ii. You can consider preliminarily implementing the conversion from CSV and other common formats to dolphin.  
iii. Other formats can be implemented later according to the requirements of docking with other systems.
2.	Querystatementgenerator: generates corresponding query statements according to indicator dimension conditions and data source information.  
a) The default implementation is sqlquerystatementgenerator, which converts the indicator dimension into each part of the select statement. The from part is the original query contained in the source.  
b) Subsequent implementation of other languages on demand.
3.	Queryexecution: submits the query to the corresponding engine and is responsible for obtaining the progress, status and result set.  
a) In the source jump from the external system, if there is creator information, submit the engine of the creator; If not, it will be submitted to the visualis engine by default.  
b) Synchronous query: direct query. The result set is returned directly after the result set is blocked.  
c) Asynchronous query: submit the query, return the query ID, provide progress and status tracking, and finally obtain the result set through the ID.
4.	Resultparser: converts the original result set returned by the engine into a result set format that can be returned to the front-end rendering.  
a) Dolphin visualissresultparser, which converts the result set in dolphin format to the visualiss front-end format.  
b) Dolphin to datawranglerresultparser, which converts the result set in dolphin format to datawrangler.
