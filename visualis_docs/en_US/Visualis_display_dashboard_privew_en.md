> Visualis Display and Dashboard preview mechanism

## 1 Introduction
&nbsp;&nbsp;&nbsp;&nbsp;The preview mechanism of Display and Dashboard provides the function of previewing the mail to be sent. In use, after the development of Display and Dashboard is completed, click the preview button in the toolbar above the component, and the browser will create a new tab and open the preview page. When the page is fully opened, you can see the final image effect. The following figure is the final preview effect after Display development is completed, that is, the rendering effect of the final email report.
![Preview result](../images/preview_page.png)

## 2. Design principle
&nbsp;&nbsp;&nbsp;&nbsp;The Visualis backend provides a preview interface, which is divided into two usage scenarios, the first is the front-end preview function that supports Visualis, and the second is when the DSS workflow is connected, Display and Dashboard execute interface to call. The request value is mainly the primary key ID of Display and Dashboard, and the return value is the output stream of the image.
![Preview overall process](../images/preview.png)
&nbsp;&nbsp;&nbsp;&nbsp;Display preview and Dashboard preview interface are similar, the preview interface of Dashboard can view the previewPortal method of DashboardPreviewController class in the source code, but the preview of Dashboard has multiple panel pages, and the images are aggregated, other logic Basically the same, the preview interface code of Display:
````java
    @MethodLog
    @GetMapping(value = "/{id}/preview", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public void previewDisplay(@PathVariable Long id,
                                        @RequestParam(required = false) String username,
                                        @CurrentUser User user,
                                        HttpServletRequest request,
                                        HttpServletResponse response) throws IOException {
        Display display = displayMapper.getById(id);
        Project project = projectMapper.getById(display.getProjectId());

        FileInputStream inputStream = null;
        try {
            List<ImageContent> imageFiles = scheduleService.getPreviewImage(user.getId(), "display", id);
            File imageFile = Iterables.getFirst(imageFiles, null).getImageFile();
            if(null != imageFile) {
                inputStream = new FileInputStream(imageFile);
                response.setContentType(MediaType.IMAGE_PNG_VALUE);
                IOUtils.copy(inputStream, response.getOutputStream());
            } else {
                log.error("Execute display failed, because image file is null.");
                response.sendError(504, "Execute display failed, because image file is null.");
            }
        } catch (Exception e) {
            log.error("display preview error: ", e);
        } finally {
            if(null != inputStream) {
                inputStream.close();
            }
        }
    }
````
&nbsp;&nbsp;&nbsp;&nbsp;The core of preview is to take screenshots of Display page and Dashboard page. Its main function relies on the implementation of PhantomJS. Visualis uses Java's Selenium library to call PhantomJS to take screenshots, and its core logic is implemented in the ScreenshotUtil class . The screenshot needs to rely on the binary file named phantomjs in the bin directory. This is the Driver driver provided by Selenium for PhantomJS, and its related packages can be downloaded from the Selenium official website.
&nbsp;&nbsp;&nbsp;&nbsp;Since PhantomJS is in an unmaintained state, there is a possibility of migrating to Chrome in the future. You can also download the corresponding driver on the Selenium official website, but to use Chrome, you need to install the real Chrome browser on the Linux machine , if you want to switch to Chromer, you need to perform adaptation testing and compatibility testing.

## 3. Preview optimization
&nbsp;&nbsp;&nbsp;&nbsp;In the actual production and use, the occasional scene will appear the screenshot of the wrong page execution error, resulting in the occasional report as an error result when the email is sent. This is a production problem in the usage scenario. In order to solve this problem, we introduce a failure tag monitoring mechanism, add **WidgetExecuteFailedTag** front-end tag elements to the front and back ends, and detect them by the back end.  
![Preview result](../images/preview_bug_fix_1.png)