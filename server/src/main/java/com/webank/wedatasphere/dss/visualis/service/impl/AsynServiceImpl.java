package com.webank.wedatasphere.dss.visualis.service.impl;


import com.webank.wedatasphere.dss.visualis.content.DashboardContant;
import com.webank.wedatasphere.dss.visualis.content.DisplayContant;
import com.webank.wedatasphere.dss.visualis.enums.VisualisStateEnum;
import com.webank.wedatasphere.dss.visualis.service.AsynService;
import edp.core.common.job.ScheduleService;
import edp.davinci.dao.PreviewResultMapper;
import edp.davinci.model.PreviewResult;
import edp.davinci.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;


@Slf4j
@Service
public class AsynServiceImpl implements AsynService {

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    PreviewResultMapper previewResultMapper;

    @Autowired
    @Qualifier("execPool")
    ThreadPoolExecutor threadPool;

    @Autowired
    ImageFileGenerater imageFileGenerater;

    @Override
    public String sumbmitPreviewTask(User user, String component, Long id) throws Exception {
        PreviewResult previewResult = generatePreviewResult(component, id, user);

        threadPool.execute(() -> {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // 1. 当任务开始时，插入一个init的记录
            previewResultMapper.insert(previewResult);

            if (component.equals(DisplayContant.DISPLAY)) {
                try {
                    // display截图会返回一个图片文件
                    File displayPreviewFile = imageFileGenerater.getDisplayPreviewFile(user.getId(), id);
                    if (displayPreviewFile == null) {
                        log.error("submit display execute error.  because image is null.");
                        previewResultMapper.updateResultStatusById(previewResult.getId(), VisualisStateEnum.FAILED.getValue());
                    }
                    // 2. 文件转换为ByteArrayOutputStream outputStream
                    InputStream is = new FileInputStream(displayPreviewFile);
                    byte[] bytes = new byte[1024];
                    int temp;
                    try {
                        while ((temp = is.read(bytes)) != -1) {
                            outputStream.write(bytes, 0, temp);
                        }
                    } catch (Exception e) {
                        log.error("submit component execute error. ", e);
                        // 状态置失败
                        previewResultMapper.updateResultStatusById(previewResult.getId(), VisualisStateEnum.FAILED.getValue());
                        return;
                    }
                } catch (Exception e) {
                    log.error("submit display execute error. ", e);
                    // 状态置失败
                    previewResultMapper.updateResultStatusById(previewResult.getId(), VisualisStateEnum.FAILED.getValue());
                    return;
                }
            } else if (component.equals(DashboardContant.DASHBOARD)) {
                try {
                    // dashboard截图使用dashboardprotal id遍历截图
                    BufferedImage dashboardPreviewFiles = imageFileGenerater.getDashboardPreviewFiles(user.getId(), id);
                    // 2. 文件转换为ByteArrayOutputStream outputStream
                    ImageIO.write(dashboardPreviewFiles, "png", outputStream);
                } catch (Exception e) {
                    log.error("submit dashboard execute error. ", e);
                    // 状态置失败
                    previewResultMapper.updateResultStatusById(previewResult.getId(), VisualisStateEnum.FAILED.getValue());
                    return;
                }
            }

            // 3. 插入结果集
            previewResult.setResult(outputStream.toByteArray());
            previewResultMapper.setResult(previewResult.getId(), previewResult.getExecId(), previewResult.getResult());

            // 4. 状态置为成功
            previewResult.setStatus(VisualisStateEnum.SUCCESS.getValue());
            previewResultMapper.updateResultStatusById(previewResult.getId(), VisualisStateEnum.SUCCESS.getValue());
        });


        return previewResult.getExecId();
    }


    /**
     * 执行id的格式为: exec_display_234_${uuid}, 用下换线_分割，
     * 第一位为预留，暂定为: exec
     * 第二位为组件名称，分别为display或dashboard
     * 第三位为组件id，为数据库中display或dashboard id
     * 第四位为uuid，唯一值
     */
    @Override
    public String state(String executeId, String component) throws Exception {
        if (StringUtils.isEmpty(executeId)) {
            throw new Exception("execute id is null, when get " + component + "execute state");
        }
        String executeState = previewResultMapper.checkStatus(executeId);
        return executeState;
    }

    @Override
    public PreviewResult getResult(String executeId, String component) throws Exception {
        // 查询结果集
        PreviewResult previewResult = previewResultMapper.selectByIdAndKeyWord(getIdByExecuteId(executeId), executeId);
        // 删除查询到的结果集
        previewResultMapper.deleteResult(getIdByExecuteId(executeId), executeId);
        return previewResult;
    }

    private PreviewResult generatePreviewResult(String component, Long componentId, User user) {
        // 1. 生成唯一的执行id, 即一些基础数据库字段信息
        String execId = "exec_" + component + "_" + componentId + "_" + UUID.randomUUID();
        String name = component + "_" + componentId;
        String status = VisualisStateEnum.INITED.getValue();
        String description = name;
        String createBy = user.getName();
        Date createTime = new Date();
        Boolean isArchive = false;
        // 2. 插入一条执行空记录
        PreviewResult previewResult = new PreviewResult(execId, name, status, description, createBy, createTime, isArchive);
        return previewResult;
    }

    private Long getIdByExecuteId(String executeId) {
        return Long.parseLong(executeId.split("_")[2]);
    }
}
