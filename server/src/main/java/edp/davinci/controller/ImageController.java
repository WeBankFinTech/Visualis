package edp.davinci.controller;

import edp.core.annotation.CurrentUser;
import edp.davinci.core.common.Constants;
import edp.davinci.model.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Api(value = "/image", tags = "image", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@ApiResponses(@ApiResponse(code = 404, message = "image not found"))
@Slf4j
@RestController
@RequestMapping(value = Constants.BASE_API_PATH + "/image", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ImageController {

    @Value("${file.userfiles-path}")
    private String fileBasePath;

    @ApiOperation(value = "get display bg image")
    @GetMapping(value = "/display/{fileName}", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public void getImage(@PathVariable String fileName,
                               @RequestParam(required = false) String username,
                               @ApiIgnore @CurrentUser User user,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException {

        FileInputStream inputStream = null;
        try {
            String path = fileBasePath + File.separator + "image" + File.separator + "display" + File.separator + fileName + ".png";
            File file = new File(path);
            inputStream = new FileInputStream(file);
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
