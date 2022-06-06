> Visualis send mail design
## 1. brief introduction
&nbsp;&nbsp;&nbsp;&nbsp;The mail function is a data output function provided by DSS, which can be used by dragging in the workflow. Currently, the mail node supports sending visualis's data display nodes, namely, the display node and the dashboard node. Currently, the mail is sent in the way of picture sending. After the configuration is completed, a picture of the preview effect of display and dashboard will be received in the mailbox. In mail sending, DSS adopts the spring mail sending toolkit javamailsenderimpl, which is implemented in the springjavaemailsender class.


## 2. Implementation process of mail sending
&nbsp;&nbsp;&nbsp;&nbsp;Mail sending is the last step in developing workflow reports. In the sendemail node, data output is realized by linking sending items and binding sending nodes. Its function depends on the CS service of linkis. Since the mail node belongs to a type of appconn, and there are also instances of related appconns, when configuring mail sending, the following mail configurations need to be configured, where enhance_ JSON is the related sending configuration item of sendemail, mainly including the IP, port, user name, password and protocol of the mail server. For related configurations, refer to the following SQL:
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
    '{"email.host":"smtp.163.com","email.port":"25","email.username":"xxx@163.com","email.password":"xxxxx","email.protocol":"smtp"}',
    NULL,
    NULL
);
```

&nbsp;&nbsp;&nbsp;&nbsp;The process of sending e-mail requires the cooperation of upper and lower nodes. Before sendemail is executed, the data visualization node has already prepared the relevant sending results. On the DSS workflow side, the execution of display and dashboard is actually to request the preview interface. For relevant implementation, please refer to [display dashboard preview principle] (), Use the downloadaction of linlis to request a large result set (the image we request preview by default belongs to a large result set). The following is the core logic of display and dashboard implemented in DSS appconn.
```scala
 private ResponseRef executePreview(AsyncExecutionRequestRef ref, String previewUrl, String metaUrl) 
         throws ExternalOperationFailedException {
// Partial code omission...
HttpResult metaResult = this.ssoRequestOperation.requestWithSSO(ssoUrlBuilderOperationMeta, metadataDownloadAction);
            String metadata = StringUtils.chomp(IOUtils.toString(metadataDownloadAction.getInputStream(),
                              ServerConfiguration.BDP_SERVER_ENCODING().getValue())); // Obtain the output stream data of the metadatadownloadaction
            ResultSetWriter resultSetWriter = ref.getExecutionRequestRefContext().createPictureResultSetWriter();
            resultSetWriter.addMetaData(new LineMetaData(metadata)); // Write result set to CS
            resultSetWriter.addRecord(new LineRecord(response)); // Write result set to CS
            resultSetWriter.flush(); // Refresh stream
            IOUtils.closeQuietly(resultSetWriter); // Close stream
            ref.getExecutionRequestRefContext().sendResultSet(resultSetWriter);
// Partial code omission...
 }
```

&nbsp;&nbsp;&nbsp;&nbsp;在After the visualization nodes dispaly and dashboard perform preview, the result set is written to the CS service of linkis. When you have the results to send, you only need to get the corresponding content from the CS service of linkis when sendemail is executed. The mail node