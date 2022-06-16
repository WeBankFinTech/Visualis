package edp.davinci.controller;

import edp.core.annotation.CurrentUser;
import edp.core.annotation.MethodLog;
import edp.davinci.core.common.Constants;
import edp.davinci.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping(value = Constants.BASE_API_PATH + "/image", produces = MediaType.APPLICATION_JSON_VALUE)
public class ImageController {

    @Value("${file.userfiles-path}")
    private String fileBasePath;

    @MethodLog
    @GetMapping(value = "/display/{fileName}", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public void getImage(@PathVariable String fileName,
                         @RequestParam(required = false) String username,
                         @CurrentUser User user,
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
            log.error("get image io error: " + e);
        } catch (Exception e) {
            log.error("get image error: " + e);
        } finally {
            inputStream.close();
        }

    }
}
