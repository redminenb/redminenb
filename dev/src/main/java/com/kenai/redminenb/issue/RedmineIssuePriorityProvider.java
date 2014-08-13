/*
 * Copyright 2014 Matthias Bläsing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kenai.redminenb.issue;

import com.kenai.redminenb.RedmineConfig;
import com.kenai.redminenb.api.Helper;
import com.taskadapter.redmineapi.bean.IssuePriority;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bugtracking.spi.IssuePriorityInfo;
import org.netbeans.modules.bugtracking.spi.IssuePriorityProvider;

/**
 *
 * @author Eric
 */
public class RedmineIssuePriorityProvider implements IssuePriorityProvider<RedmineIssue> {

    @Override
    public String getPriorityID(RedmineIssue i) {
        return Helper.getIssuePriority(i.getIssue()).getId().toString();
    }

    @Override
    public IssuePriorityInfo[] getPriorityInfos() {
        // need per manager list
        List<IssuePriority> li = Helper.getDefaultIssuePriorities();
        List<IssuePriorityInfo> lipi = new ArrayList<>();
        for (IssuePriority ip : li) {
            IssuePriorityInfo ipi = new IssuePriorityInfo(
                    ip.getId().toString(), 
                    ip.getName(),
                    RedmineConfig.getInstance().getPriorityImage(ip.getName())
            );
            lipi.add(ipi);
        }
        return lipi.toArray(
                new IssuePriorityInfo[lipi.size()]);

    }

}
