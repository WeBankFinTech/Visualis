> 虚拟View设计文档  

该文档主要阐述Visualis系统的Widget页面在运行时接收一个或多个元数据信息（即json格式的拼凑View，包括字段名称、字段类型、原始查询语句、数据源信息）作为参数，并根据选择的参数View中提供的信息进行动态数据查询的架构调整方案。

调整涉及以下几个方面：
1.	参数View和Source结构。
2.	Widget页面改造。
3.	查询逻辑改造。
## 参数化View和Source结构
1.	新增以下概念：  
a)	虚拟view：一个不含具体内容的view，绑定该view的widget即作为接收view参数进行动态渲染的widget。  
b)	参数view：以json的形式包含字段名称、字段类型、原始查询语句、数据源等信息，作为url参数传递给widget编辑页面。  
c)	参数source：作为参数view的一个栏位，同样为json结构，指定该view在进行查询时应当提交的引擎类型（spark、hive、jdbc等）、数据源类型（hive库表、SQL脚本、linkis结果集等）、数据源具体内容、数据来源（creator，如scriptis）。   
d)	以上概念中，widget需要能够被绑定一个虚拟View，实现对拼凑view的参数接收，参数View。
2.	虚拟view为在数据库中插入的一行，它的project_id和source_id为-1； sql、model、variable和config栏位均为null。
3.	仅支持被其它系统调用时，并在传递参数的情况下新建绑定虚拟view的widget，无法在建立widget的时候手工选择绑定虚拟view。
4.	参数view需要符合以下json格式。  
a)	数据源类型和数据具体内容的对应：

|dataSourceType |dataSourceContent |   
|---------------|------------------|  
resultset|结果集路径  
script|BML resource id + version  
table|库表名称  
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
        "engineType": "spark", //引擎类型
        
        "dataSourceType": "resultset", //数据源类型，结果集、脚本、库表
        "dataSourceContent": {
	     "resultLocation": "/tmp/linkis/resultset/_0.dolphin"
	 },
        "creator": "scriptis"
    }, 
    "params": "[]"
}
```
1. 传参方式  
a)	新建widget：/dss/visualis/#/project/3/widget/add?views=[{view1 json},{view2 json}]  
b)	编辑widget：/dss/visualis/#/project/3/widget/4?views=[{view1 json},{view2 json}]  
c)	考虑给出直接建立虚拟widget的post接口，然后打开widget就可以渲染  
## Widget页面改造
1.	新增以下概念：  
a)	虚拟widget：即为绑定虚拟view的widget，config中明确指明virtual=true。其config中增加source字段，存储参数source；其config的model字段，存储参数view中的字段信息。  
b)	Context ID：作为widget节点被创建时，widget的config字段中增加contextId字段，存储改widget节点所在flow对应的context id。（所有widget节点对应的widget都是虚拟widget，创建的时候就设置virtual=true）
2.	打开新建widget界面时：  
a)	如果无URL参数，则保持原有逻辑。  
b)	如果有URL参数，则认为是准备新建虚拟widget。如果参数只有一个，则选中这个参数view，如果有多个，则不选中，全部放进下拉列表。保存时，指明virtual=true。
3.	打开编辑widget界面时：  
a)	如果无URL参数，且该widget是虚拟widget：  
i.	检查config中model非空，如果有，则查询直接提交到config中的参数source。  
ii.	如果config中model为空，contextId非空，则根据contextId找到上游的所有metadata（后端提供接口），作为下拉列表的备选view。选中context中的metadata并保存后，config中的source更新为对应的context类型，并记录相应的key。  
iii.如果什么都没有，保持原有的编辑空widget页面的逻辑不变，没有什么有意义的内容可供操作。  
b)	如果有URL参数，且该widget是虚拟widget：如果参数只有一个，则选中这个参数view，如果有多个，则不选中，全部放进下拉列表。  
c)	不管是否有URL参数，只要打开的是非虚拟的正常widget，都保持原有逻辑不变。
4.	getdata和share/data接口，在虚拟widget下，都要多传一个source参数。后端处理时：  
a)	如果是虚拟widget，而且没有传source，会直接查询报错；  
b)	如果是非虚拟的正常widget，就算传了source，也直接忽略。
## 查询逻辑改造
针对新增的各种数据源，之前的查询方式需要作出以下改造：
1.	SourceInitializer：对数据源进行初始化，返回初始化后更加详细的source信息。  
a)	针对结果集数据源，在spark中生成或更新temp view，返回的source中补充select该temp view的sql。  
b)	针对SQL脚本数据源，从BML拉取对应的sql，放入返回的source中。  
c)	针对hive库表数据源，拼接select语句放入返回的source中。  
d)	针对CS数据源，根据context id拉取具体的元数据内容，拼接select语句放入返回的source中。  
e)	针对URL外部数据源，请求数据后，转换成dolphin格式，提交给spark建立temp view，返回的source中补充select该temp view的sql。  
i.	URL数据源提供方直接给出dolphin格式的数据。  
ii.	可以考虑初步实现csv等常见格式到dolphin的转换。  
iii.	其它格式，后续按对接其它系统的需求再实现。  
2.	QueryStatementGenerator：根据指标维度条件、数据源信息，生成相应的查询语句。  
a)	默认实现SQLQueryStatementGenerator，将指标维度转换成select语句的各个部分，from部分为source中包含的原始查询。  
b)	其它语言的后续按需求实现。
3.	QueryExecutor：将查询提交到相应的引擎，以及负责进度、状态和结果集的获取。  
a)	从外部系统跳转的source中，如果带有creator信息，则提交该creator的引擎；如果没有，默认提交给visualis引擎。  
b)	同步查询：直接查询，阻塞到结果集生成后，直接返回结果集。  
c)	异步查询：提交查询，返回查询id，提供进度、状态跟踪，最后通过id获取结果集。
4.	ResultParser：将引擎返回的原始结果集，转换为可以返回给前端渲染的结果集格式。  
a)	DolphinToVisualisResultParser，转换dolphin格式的结果集到Visualis前端格式。
