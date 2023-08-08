package com.webank.wedatasphere.dss.visualis.service.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import edp.core.common.job.ScheduleService;
import edp.davinci.dao.DashboardMapper;
import edp.davinci.model.Dashboard;
import edp.davinci.service.screenshot.ImageContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 抽象出该类，有如下几个作用：
 * 1. 抽象原有的display, dashboard preview restful产生图片逻辑，简化restful。
 * 2. 方便appconn异步请求时复用图片产生逻辑。
 */

@Slf4j
@Component
public class ImageFileGenerater {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Autowired
    ScheduleService scheduleService;

    public File getDisplayPreviewFile(Long userId, Long contentId) throws Exception {
        File imageFile = null;
        List<ImageContent> imageFiles = scheduleService.getPreviewImage(userId, "display", contentId);
        imageFile = Iterables.getFirst(imageFiles, null).getImageFile();
        if (null == imageFile) {
            log.error("Execute display failed, because image file is null.");
            return null;
        }
        return imageFile;
    }

    public BufferedImage getDashboardPreviewFiles(Long userId, Long contentId) throws Exception {
        List<Dashboard> dashboards = dashboardMapper.getByPortalId(contentId);
        List<File> finalFiles = Lists.newArrayList();
        for (Dashboard dashboard : dashboards) {
            List<ImageContent> imageFiles = scheduleService.getPreviewImage(userId, "dashboard", dashboard.getId());
            File imageFile = Iterables.getFirst(imageFiles, null).getImageFile();
            finalFiles.add(imageFile);
            if (null == imageFile) {
                log.error("reports an error when executing the dashboard: {}, and the picture is null", dashboard.getId());
                return null;
            }
        }
        BufferedImage merged = mergeImage(finalFiles.toArray(new File[0]));
        return merged;
    }

    private static BufferedImage mergeImage(File[] src) throws IOException {
        int len = src.length;
        if (len == 1) {
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
