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

package edp.davinci.dto.cronJobDto;

import com.google.common.collect.Sets;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class CronJobConfig {
    private String to;
    private String cc;
    private String bcc;
    private String subject;
    private String type;
    private List<CronJobContent> contentList;
    private String time_range;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CronJobConfig that = (CronJobConfig) o;
        return Objects.equals(to, that.to) &&
                Objects.equals(cc, that.cc) &&
                Objects.equals(bcc, that.bcc) &&
                Objects.equals(subject, that.subject) &&
                Objects.equals(type, that.type) &&
                Sets.difference(Sets.newHashSet(contentList), Sets.newHashSet(that.contentList)).isEmpty();
    }

    public boolean sameContent(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CronJobConfig that = (CronJobConfig) o;
        for(CronJobContent content : contentList){
            boolean foundSame = false;
            for(CronJobContent thatContent : that.contentList){
                if(content.equals(thatContent)){
                    foundSame = true;
                }
            }
            if(!foundSame){
                return false;
            }
        }
        return true;
    }

    public boolean isFixedTime(){
        return "Day".equals(time_range) || "Week".equals(time_range) || "Month".equals(time_range);
    }
}
