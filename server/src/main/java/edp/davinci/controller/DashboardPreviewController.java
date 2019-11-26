package edp.davinci.controller;

import com.google.common.collect.Iterables;
import edp.core.annotation.CurrentUser;
import edp.core.common.job.ScheduleService;
import edp.davinci.core.common.Constants;
import edp.davinci.dao.DashboardMapper;
import edp.davinci.dto.dashboardDto.DashboardWithPortal;
import edp.davinci.model.User;
import edp.davinci.service.screenshot.ImageContent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Api(value = "/dashboard", tags = "dashboard", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@ApiResponses(@ApiResponse(code = 404, message = "dashboard not found"))
@Slf4j
@RestController
@RequestMapping(value = Constants.BASE_API_PATH + "/dashboard", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class DashboardPreviewController {

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    private DashboardMapper dashboardMapper;


    @Value("${file.userfiles-path}")
    private String fileBasePath;

    @ApiOperation(value = "preview dashboard")
    @GetMapping(value = "/{id}/preview", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public void previewDisplay(@PathVariable Long id,
                                 @RequestParam(required = false) String username,
                                 @ApiIgnore @CurrentUser User user,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {
        DashboardWithPortal dashboardWithPortalAndProject = dashboardMapper.getDashboardWithPortalAndProject(id);
        if(!user.getId().equals(dashboardWithPortalAndProject.getProject().getUserId())){
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.getWriter().write("You have no access to this dashboard.");
            return;
        }

        FileInputStream inputStream = null;
        try {
            List<ImageContent> imageFiles = scheduleService.getPreviewImage(user.getId(), "dashboard", id);
            File imageFile = Iterables.getFirst(imageFiles, null).getImageFile();
            inputStream = new FileInputStream(imageFile);
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            IOUtils.copy(inputStream, response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        } finally {
            inputStream.close();
        }
    }

}
