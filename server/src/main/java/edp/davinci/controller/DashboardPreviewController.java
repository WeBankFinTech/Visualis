package edp.davinci.controller;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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

        List<Dashboard> dashboards = dashboardMapper.getByPortalId(id);
        List<File> finalFiles = Lists.newArrayList();
        for(Dashboard dashboard : dashboards){
            List<ImageContent> imageFiles = scheduleService.getPreviewImage(user.getId(), "dashboard", dashboard.getId());
            File imageFile = Iterables.getFirst(imageFiles, null).getImageFile();
            finalFiles.add(imageFile);
            if(null == imageFile) {
                log.error("{} reports an error when executing the dashboard: {}, and the picture is null", username, dashboard.getId());
                response.sendError(504, "Execute dashboard failed, because image file is null.");
                return;
            }
        }
        BufferedImage merged = mergeImage(finalFiles.toArray(new File[0]));
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        ImageIO.write(merged, "png", response.getOutputStream());
    }

    public static BufferedImage mergeImage(File[] src) throws IOException {
        int len = src.length;
        if(len == 1){
            return ImageIO.read(src[0]);
        }
        BufferedImage[] images = new BufferedImage[len];
        int[][] ImageArrays = new int[len][];
        for (int i = 0; i < len; i++) {
            images[i] = ImageIO.read(src[i]);
            int width = images[i].getWidth();
            int height = images[i].getHeight();
            ImageArrays[i] = new int[width * height];
            ImageArrays[i] = images[i].getRGB(0, 0, width, height, ImageArrays[i], 0, width);
        }
        int newHeight = 0;
        int newWidth = 0;
        for (int i = 0; i < images.length; i++) {
            newWidth = newWidth > images[i].getWidth() ? newWidth : images[i].getWidth();
            newHeight += images[i].getHeight();
        }


        BufferedImage ImageNew = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        int height_i = 0;
        for (int i = 0; i < images.length; i++) {
            ImageNew.setRGB(0, height_i, newWidth, images[i].getHeight(), ImageArrays[i], 0, newWidth);
            height_i += images[i].getHeight();
        }
        return ImageNew;
    }


}
