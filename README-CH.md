![DSS](images/visualis.png)
====

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[English](README.md) | 中文

## 简介

&nbsp;&nbsp;&nbsp;&nbsp;Visualis是一个基于宜信的开源项目[Davinci](https://github.com/edp963/davinci)开发的数据可视化BI工具。现已被集成到数据应用开发门户[DataSphere Studio](https://github.com/WeBankFinTech/DataSphereStudio)中，此次发布的版本Visualis1.0.0版本支持Linkis1.1.1和DSS1.1.0版本。

&nbsp;&nbsp;&nbsp;&nbsp;Visualis支持拖拽式报表定义、图表联动、钻取、全局筛选、多维分析、实时查询等数据开发探索的分析模式，并做了水印、数据质量校验等金融级增强。

## 功能特性

&nbsp;&nbsp;&nbsp;&nbsp;基于达芬奇项目, Visualis与DataSphere Studio 1.1.0集成，实现了以下特性：
* 图表水印
* 数据质量校验
* 图表展示优化
* 对接Linkis计算中间件
* Scriptis结果集一键可视化
* 外部应用参数支持
* View/Widget/Dashboard/Display集成为DataSphere Studio的工作流节点

&nbsp;&nbsp;&nbsp;&nbsp;Visualis同时支持以下Davinci v0.3版本的原生功能：
* **数据源**
  * 支持JDBC数据源
  * 支持CSV文件上传
* **数据视图**
  * 支持定义SQL模版
  * 支持SQL高亮显示
  * 支持SQL测试
  * 支持回写操作
* **可视组件**
  * 支持预定义图表
  * 支持控制器组件
  * 支持自由样式
* **交互能力**
  * 支持可视组件全屏显示
  * 支持可视组件本地控制器
  * 支持可视组件间过滤联动
  * 支持群控控制器可视组件
  * 支持可视组件本地高级过滤器
  * 支持大数据量展示分页和滑块
* **集成能力**
  * 支持可视组件CSV下载
  * 支持可视组件公共分享
  * 支持可视组件授权分享
  * 支持仪表板公共分享
  * 支持仪表板授权分享


## 与DataSphere Studio集成

&nbsp;&nbsp;&nbsp;&nbsp;Visualis与DataSphere Studio的数据开发、工作流调度和数据质量校验等模块无缝衔接，实现数据应用开发全流程的连贯顺滑用户体验。

更多使用说明可参考: [Visualis User Manul Doc](./visualis_docs/zh_CN/Visualis_user_manul_cn.md)

![Visualis](images/visualis_workflow.gif)

 

## 架构设计

![Viusalis Architecture](images/architecture.png)

## 文档

## 安装部署文档
[编译部署文档](visualis_docs/zh_CN/Visualis_deploy_doc_cn.md)

[AppConn安装文档](visualis_docs/zh_CN/Visualis_appconn_install_cn.md)

## 使用文档
[用户使用文档](visualis_docs/zh_CN/Visualis_user_manul_cn.md)

[Visualis与Davinci的区别](visualis_docs/zh_CN/Visualis_Davinci_difference_cn.md)

## 设计文档
[Visualis设计文档](visualis_docs/zh_CN/Visualis_design_cn.md)

[Display和DashBoard预览原理](visualis_docs/zh_CN/Visualis_display_dashboard_privew_cn.md)

[Visualis接入DSS/Linkis注意点](visualis_docs/zh_CN/Visualis_dss_integration_cn.md)

[集成LinkisDatasource](visualis_docs/zh_CN/Visualis_linkisdatasource_cn.md)

[发送邮件实现原理](visualis_docs/zh_CN/Visualis_sendemail_cn.md)

[绑定sql节点原理](visualis_docs/zh_CN/Visualis_sql_databind_cn.md)

[虚拟视图设计文档](visualis_docs/zh_CN/Visualis_visual_doc_cn.md)

## 升级文档
[升级文档](visualis_docs/zh_CN/visualis_update_cn.md)

## 交流贡献

![communication](images/communication.png)

## License

Visualis is under the Apache 2.0 license. See the [License](LICENSE) file for details.

