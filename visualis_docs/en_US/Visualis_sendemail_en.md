> Visualis Send Email Design
## 1 Introduction
&nbsp;&nbsp;&nbsp;&nbsp;The mail function is the data output function provided by DSS, which can be used by dragging and dropping in the workflow. At present, the mail node supports sending Visualis data display nodes, namely Display node and Dashboard node. Currently, the mail sending method adopts the method of sending pictures, and after the configuration is completed, you will receive a picture of the preview effect of Display and Dashboard in the mailbox. In mail sending, DSS uses Spring's mail sending toolkit JavaMailSenderImpl, which is implemented in the SpringJavaEmailSender class.


## 2. The implementation process of email sending
&nbsp;&nbsp;&nbsp;&nbsp;Email sending is the last step in the development of workflow reports. In the SendEmail node, data output is realized by linking the sending item and binding the sending node, and its function depends on the CS service of Linkis. Since the mail node belongs to a class of AppConn, it also has related AppConn instances. Therefore, when configuring mail sending, due to the needs of the mail, you need to configure the following mail configurations, of which enhance_json is the relevant sending configuration item of SendEmail, mainly the IP of the mail server, Port, username, password, protocol. Its related configuration can refer to the following SQL:
```sql
INSERT INTO dss_appconn_instance (
    appconn_id,
    label,
    url,
    enhance_json,
    homepage_url,
    redirect_url
) VALUES (
    7,
    'DEV',
    'sendemail',
    '{"email.host":"smtp.163.com","email.port":"25","email.username":"xxx@163.com","email.password":"xxxxx", "email.protocol":"smtp"}',
    NULL,
    NULL
);
```
&nbsp;&nbsp;&nbsp;&nbsp;The process of sending emails requires the cooperation of upper and lower nodes. Before SendEmail is executed, the data visualization node has already prepared the relevant sending results when it is executed. On the DSS workflow side, Display and Dashboard execute the actual The above is to request the preview interface. For related implementations, please refer to [Display Dashboard Preview Principle](), and use Linlis's DownloadAction to request a large result set (the pictures we request for preview by default belong to a large result set). Below is the core logic executed in DSS AppConn for Display and Dashboard.
![SendEmail](./../images/sendemail.png)
```scala
 private ResponseRef executePreview(AsyncExecutionRequestRef ref, String previewUrl, String metaUrl)
         throws ExternalOperationFailedException {
// Some code omitted...
HttpResult metaResult = this.ssoRequestOperation.requestWithSSO(ssoUrlBuilderOperationMeta, metadataDownloadAction);
            String metadata = StringUtils.chomp(IOUtils.toString(metadataDownloadAction.getInputStream(),
                              ServerConfiguration.BDP_SERVER_ENCODING().getValue())); // Get the output stream data of metadataDownloadAction
            ResultSetWriter resultSetWriter = ref.getExecutionRequestRefContext().createPictureResultSetWriter();
            resultSetWriter.addMetaData(new LineMetaData(metadata)); // write result set to CS
            resultSetWriter.addRecord(new LineRecord(response)); // write result set to CS
            resultSetWriter.flush(); // flush the stream
            IOUtils.closeQuietly(resultSetWriter); // close the stream
            ref.getExecutionRequestRefContext().sendResultSet(resultSetWriter);
// Some code omitted...
 }
```
&nbsp;&nbsp;&nbsp;&nbsp;After the visualization nodes Dispaly and Dashboard execute the preview, the result set will be written to the CS service of Linkis. With the result to be sent, when SendEmail is executed, it only needs to be obtained from the CS service of Linkis The corresponding content is enough. There are about two core logics in the mail node. First, through the on-line text, the id of each node is obtained from the on-line text CS of the workflow, which is an array of NodeIDs in the code, and then the data is traversed. Get the id of each node task, which is jobIds in the code. The relevant core code is as follows:

```scala
  def getJobIds(refContext: ExecutionRequestRefContext): Array[Long] = {
    val contextIDStr = ContextServiceUtils.getContextIDStrByMap(refContext.getRuntimeMap)
    val nodeIDs = refContext.getRuntimeMap.get("content") match {
      case string: String => JSONUtils.gson.fromJson(string, classOf[java.util.List[String]])
      case list: java.util.List[String] => list
    }
    if (null == nodeIDs || nodeIDs.length < 1){
      throw new EmailSendFailedException(80003 ,"empty result set is not allowed")
    }
    info(s"From cs to getJob ids $nodeIDs.")
    val jobIds = nodeIDs.map(ContextServiceUtils.getNodeNameByNodeID(contextIDStr, _)).map{ nodeName =>
      val contextKey = new CommonContextKey
      contextKey.setContextScope(ContextScope.PUBLIC)
      contextKey.setContextType(ContextType.DATA)
      contextKey.setKey(CSCommonUtils.NODE_PREFIX + nodeName + CSCommonUtils.JOB_ID)
      LinkisJobDataServiceImpl.getInstance().getLinkisJobData(contextIDStr, SerializeHelper.serializeContextKey(contextKey))
    }.map(_.getJobID).toArray
    if (null == jobIds || jobIds.length < 1){
      throw new EmailSendFailedException(80003 ,"empty result set is not allowed")
    }
    info(s"Job IDs is ${jobIds.toList}.")
    jobIds
  }
```
&nbsp;&nbsp;&nbsp;&nbsp;In the second step, since the job id of the job corresponds to the running result set path in the cs service, the result set path of the task execution can be obtained by calling the fetchLinkisJobResultSetPaths method, and its result set The path path, that is, the task result record stored in the CS service when the task is executed. After obtaining the relevant result set, the mail can be sent. The mail sending is one of the core functions of DSS and is the function of DSS data output. The core code of the interaction between Visualis and DSS report mail is described here. For other related logic, please refer to the related logic of the DSS SendEmail code.
```scala
  override protected def generateEmailContent(requestRef: ExecutionRequestRef, email: AbstractEmail): Unit = email match {
    case multiContentEmail: MultiContentEmail =>
      val runtimeMap = getRuntimeMap(requestRef)
      val refContext = getExecutionRequestRefContext(requestRef)
      runtimeMap.get("category") match {
        case "node" =>
          val resultSetFactory = ResultSetFactory.getInstance
          EmailCSHelper.getJobIds(refContext).foreach { jobId =>
            refContext.fetchLinkisJobResultSetPaths(jobId).foreach { fsPath =>
              val resultSet = resultSetFactory.getResultSetByPath(fsPath)
              val emailContent = resultSet.resultSetType() match {
                case ResultSetFactory.PICTURE_TYPE => new PictureEmailContent(fsPath)
                case ResultSetFactory.HTML_TYPE => throw new EmailSendFailedException(80003 ,"html result set is not allowed")//new HtmlEmailContent(fsPath)
                case ResultSetFactory.TABLE_TYPE => throw new EmailSendFailedException(80003 ,"table result set is not allowed")//new TableEmailContent(fsPath)
                case ResultSetFactory.TEXT_TYPE => throw new EmailSendFailedException(80003 ,"text result set is not allowed")//new FileEmailContent(fsPath)
              }
              multiContentEmail.addEmailContent(emailContent)
            }
          }
        case "file" => throw new EmailSendFailedException(80003 ,"file content is not allowed") //addContentEmail(c => new FileEmailContent(new FsPath(c)))
        case "text" => throw new EmailSendFailedException(80003 ,"text content is not allowed")//addContentEmail(new TextEmailContent(_))
        case "link" => throw new EmailSendFailedException(80003 ,"link content is not allowed")//addContentEmail(new UrlEmailContent(_))
      }
  }
```