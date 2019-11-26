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

package edp.core.common.job;


import edp.davinci.service.screenshot.ImageContent;

import java.util.List;

/**
 * 通用schedule调度接口，业务层必须实现
 * 命名格式为{jobType}ScheduleService
 * 如： emailScheduleService
 */
public interface ScheduleService {

    void execute(long jobId) throws Exception;

    List<ImageContent> getPreviewImage(Long userId, String contentType, Long contentId) throws Exception;

    String getContentUrl(Long userId, String contentType, Long contengId);
}
