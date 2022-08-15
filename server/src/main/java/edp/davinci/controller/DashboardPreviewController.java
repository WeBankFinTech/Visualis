package edp.davinci.controller;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.webank.wedatasphere.dss.visualis.service.impl.ImageFileGenerater;
import edp.core.annotation.CurrentUser;
import edp.core.annotation.MethodLog;
import edp.core.common.job.ScheduleService;
import edp.davinci.core.common.Constants;
import edp.davinci.dao.DashboardMapper;
import edp.davinci.dto.dashboardDto.DashboardWithPortal;
import edp.davinci.model.Dashboard;
import edp.davinci.model.User;
import edp.davinci.service.screenshot.ImageContent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = Constants.BASE_API_PATH + "/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
public class DashboardPreviewController {

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    ImageFileGenerater imageFileGenerater;

    @Value("${file.userfiles-path}")
    private String fileBasePath;

    @MethodLog
    @GetMapping(value = "/{id}/preview", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public void previewDisplay(@PathVariable Long id,
                                 @RequestParam(required = false) String username,
                                 @CurrentUser User user,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {
        DashboardWithPortal dashboardWithPortalAndProject = dashboardMapper.getDashboardWithPortalAndProject(id);

        FileInputStream inputStream = null;
        try {
            List<ImageContent> imageFiles = scheduleService.getPreviewImage(user.getId(), "dashboard", id);
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
            log.error("dashboard preview error: " + e);
        } finally {
            if(null != inputStream) {
                inputStream.close();
            }
        }
    }

    @MethodLog
    @GetMapping(value = "/portal/{id}/preview", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public void previewPortal(@PathVariable Long id,
                               @RequestParam(required = false) String username,
                               @CurrentUser User user,
                               HttpServletRequest request,
                               HttpServletResponse response) throws Exception {

        BufferedImage merged = imageFileGenerater.getDashboardPreviewFiles(user.getId(), id);
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        ImageIO.write(merged, "png", response.getOutputStream());
    }
}
