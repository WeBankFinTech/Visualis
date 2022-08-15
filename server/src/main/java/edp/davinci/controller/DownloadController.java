/*
 * <<
 *  Davinci
 *  ==
 *  Copyright (C) 2016 - 2019 EDP
 *  ==
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  >>
 *
 */

package edp.davinci.controller;

import com.alibaba.druid.util.StringUtils;
import edp.core.annotation.AuthIgnore;
import edp.core.annotation.CurrentUser;
import edp.core.annotation.MethodLog;
import edp.davinci.common.controller.BaseController;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import edp.davinci.core.enums.DownloadType;
import edp.davinci.core.enums.FileTypeEnum;
import edp.davinci.dto.viewDto.DownloadViewExecuteParam;
import edp.davinci.model.DownloadRecord;
import edp.davinci.model.ShareDownloadRecord;
import edp.davinci.model.User;
import edp.davinci.service.DownloadService;
import edp.davinci.service.ShareDownloadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.ListUtils;
import org.elasticsearch.common.io.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author daemon
 * @Date 19/5/27 20:30
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
@RestController
@RequestMapping(value = Constants.BASE_API_PATH + "/download")
public class DownloadController extends BaseController {

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private ShareDownloadService shareDownloadService;

    @GetMapping(value = "/page", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getDownloadRecordPage(@CurrentUser User user,
                                                HttpServletRequest request) {
        List<DownloadRecord> records = downloadService.queryDownloadRecordPage(user.getId());
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(records));
    }


    @MethodLog
    @GetMapping(value = "/record/file/{id}/{token:.*}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @AuthIgnore
    public ResponseEntity getDownloadRecordFile(@PathVariable Long id,
                                                @PathVariable String token,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {
        DownloadRecord record = downloadService.downloadById(id, token);
        try {
            encodeFileName(request, response, record.getName() + FileTypeEnum.XLSX.getFormat());
            Streams.copy(new FileInputStream(new File(record.getPath())), response.getOutputStream());
        } catch (Exception e) {
            log.error("getDownloadRecordFile error,id=" + id + ",e=", e);
        }
        return null;
    }


    @MethodLog
    @PostMapping(value = "/submit/{type}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity submitDownloadTask(@PathVariable String type,
                                             @PathVariable Long id,
                                             @CurrentUser User user,
                                             @Valid @RequestBody(required = false) DownloadViewExecuteParam[] params,
                                             HttpServletRequest request) {
        List<DownloadViewExecuteParam> downloadViewExecuteParams = Arrays.asList(params);
        boolean rst = downloadService.submit(DownloadType.getDownloadType(type), id, user, downloadViewExecuteParams);
        return ResponseEntity.ok(rst ? new ResultMap(tokenUtils).successAndRefreshToken(request).payload(null) :
                new ResultMap(tokenUtils).failAndRefreshToken(request).payload(null));
    }


    @MethodLog
    @PostMapping(value = "/share/submit/{type}/{uuid}/{dataToken:.*}", produces = MediaType.APPLICATION_JSON_VALUE)
    @AuthIgnore
    public ResponseEntity submitShareDownloadTask(@PathVariable(name = "type") String type,
                                                  @PathVariable(name = "uuid") String uuid,
                                                  @PathVariable(name = "dataToken") String dataToken,
                                                  @Valid @RequestBody(required = false) DownloadViewExecuteParam[] params,
                                                  @CurrentUser User user,
                                                  HttpServletRequest request) {


        if (StringUtils.isEmpty(dataToken)) {
            ResultMap resultMap = new ResultMap().fail().message("Invalid share token");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        List<DownloadViewExecuteParam> downloadViewExecuteParams = Arrays.asList(params);
        boolean rst = shareDownloadService.submit(DownloadType.getDownloadType(type), uuid, dataToken, user, downloadViewExecuteParams);

        return ResponseEntity.ok(rst ? new ResultMap().success() : new ResultMap().fail());
    }


    @GetMapping(value = "/share/page/{uuid}/{token:.*}", produces = MediaType.APPLICATION_JSON_VALUE)
    @AuthIgnore
    public ResponseEntity getShareDownloadRecordPage(@PathVariable(name = "uuid") String uuid,
                                                     @PathVariable(name = "token") String token,
                                                     HttpServletRequest request) {
        if (StringUtils.isEmpty(token)) {
            ResultMap resultMap = new ResultMap().fail().message("Invalid share token");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        List<ShareDownloadRecord> records = new ArrayList<>();
            return ResponseEntity.ok(new ResultMap(tokenUtils).payloads(records));
    }


    @MethodLog
    @GetMapping(value = "/share/record/file/{id}/{uuid}/{token:.*}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @AuthIgnore
    public ResponseEntity getShareDownloadRecordFile(@PathVariable(name = "id") String id,
                                                     @PathVariable(name = "uuid") String uuid,
                                                     @PathVariable(name = "token") String token,
                                                     @CurrentUser User user,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
        if (StringUtils.isEmpty(token)) {
            ResultMap resultMap = new ResultMap().fail().message("Invalid share token");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }

        ShareDownloadRecord record = shareDownloadService.downloadById(id, uuid, token, user);

        try {
            encodeFileName(request, response, record.getName() + FileTypeEnum.XLSX.getFormat());
            Streams.copy(new FileInputStream(new File(record.getPath())), response.getOutputStream());
        } catch (Exception e) {
            log.error("getDownloadRecordFile error,id=" + id + ",e=", e);
        }
        return null;
    }


    private void encodeFileName(HttpServletRequest request, HttpServletResponse response, String filename) throws UnsupportedEncodingException {
        response.setHeader("Content-Type", "application/force-download");
        // firefox浏览器
        if (request.getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
            filename = new String(filename.getBytes("UTF-8"), "ISO8859-1");
            //IE
        } else if (isIE(request)) {
            filename = URLEncoder.encode(filename, "UTF-8");
        } else {
            filename = new String(filename.getBytes("UTF-8"), "ISO8859-1");
        }
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    }

    private static boolean isIE(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent").toLowerCase();
        return ((ua.indexOf("rv") > 0 && ua.contains("like gecko")) || ua.indexOf("msie") > 0);
    }
}
