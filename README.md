![DSS](images/visualis.png)
====

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

English | [中文](README-CH.md)

## Introduction

Visualis is a BI tool for data visualization. It is developed based on the open source project [Davinci](https://github.com/edp963/davinci) contributed by CreditEase.

Visualis has been integrated into the data application development portal [DataSphere Studio](https://github.com/WeBankFinTech/DataSphereStudio).

Visualis provides data development/exploration functionalities including drag & drop style report definition, diagram correlation analysis, data drilling, global filtering, multi-dimensional analysis and real-time query, with the enhancement of report watermark and data quality management.

## Features

Based on Davinci project, Visualis achieves below features with DataSphere Studio:
* Report water mark.
* Data quality inspection.
* Report display optimization.
* Linkis adaption for big-data queries.
* One-click visualization from Scriptis
* External application parameters support.
* Dashboard/Display as an appjoint of DataSphere Studio workflow

Visualis also supports most of the original features of Davinci.
* Data Source Support
  * Files in CSV format
  * JDBC data source
* Data View Support
  * Customized SQL template
  * SQL highlighting
  * SQL test
  * WriteBack mode
* Visual Components Support
  * Pre-defined charts
  * Controller components
  * Free Styles
* Interaction Support
  * Visual components displayed in full screen
  * Local controller for visual components
  * Filtering and cooperation among visual components
  * Group control for visual components
  * Local advanced filter for visual components
  * Paging mode and slider for huge volumes of data
* Integration Support
  * Upload visual components in CSV format
  * Share visual components in a common/authorized way
  * Share dashboard in a common/authorized way


## DataSphere Studio Integration
Visualis seamlessly integrates with the data develoment, workflow scheduling and data quality management modules of DataSphere Studio, achieving a smooth and consistent user experience across the whole data application development lifecycle.

For more detail, please visit [DataSphere Studio documentations]().

![Visualis](images/Visualis_AppJoint.gif)
 
## Quick start

Click to [Quick start]()

## Architecture

![Viusalis Architecture](images/architecture.png)

## Documents

[Deploy documentation](visualis_docs/en_US/Visualis_deploy_doc_en.md)

[Quick integration with DSS and Linkis](visualis_docs/zh_CN/Visualis_dss_integration_cn.md)

[The differences between Visualis and Davinci](visualis_docs/zh_CN/Visualis_Davinci_difference_cn.md)

## Communication

![communication](images/communication.png)

## License

DSS is under the Apache 2.0 license. See the [License](LICENSE) file for details.

