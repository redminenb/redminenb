/*
 * Copyright 2012 Anchialas.
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
package com.kenai.redminenb.api;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssuePriority;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * redmine-java-api helper class.
 *
 * @author Anchialas <anchialas@gmail.com>
 */
public final class Helper {

    private Helper() {
        // omitted  
    }

    private static Map<Integer, IssuePriority> issuePrioMap;

    public static Collection<IssuePriority> getIssuePriorities(RedmineManager mgr) throws RedmineException {
        Collection<IssuePriority> data;
        if (issuePrioMap == null) {
            data = storeIssuePriorities(mgr.getIssuePriorities());
        } else {
            data = issuePrioMap.values();
        }
        return data;
    }

    public static IssuePriority getIssuePriority(Integer id) {
        return issuePrioMap != null ? issuePrioMap.get(id) : null;
    }

    public static IssuePriority getIssuePriority(Issue issue) {
        IssuePriority ip = getIssuePriority(issue.getPriorityId());
        if (ip == null) {
            ip = createIssuePriority(issue.getPriorityId(), issue.getPriorityText(), false);
            if (issuePrioMap != null) {
                issuePrioMap.put(ip.getId(), ip);
            }
        }
        return ip;
    }

    public static Collection<IssuePriority> storeIssuePriorities(Collection<IssuePriority> data) {
        issuePrioMap = new HashMap<Integer, IssuePriority>(data.size());
        for (IssuePriority issuePriority : data) {
            issuePrioMap.put(issuePriority.getId(), issuePriority);
        }
        return data;
    }

    public static IssuePriority getDefaultIssuePriority() {
        if (issuePrioMap != null) {
            for (IssuePriority issuePriority : issuePrioMap.values()) {
                if (issuePriority.isDefault()) {
                    return issuePriority;
                }
            }
        }
        return null;
    }

    public static List<IssuePriority> getDefaultIssuePriorities() {
        return Arrays.asList(
                createIssuePriority(7, "Immediate", false),
                createIssuePriority(6, "Urgent", false),
                createIssuePriority(5, "High", false),
                createIssuePriority(4, "Normal", true),
                createIssuePriority(3, "Low", false));
    }

    public static IssuePriority createIssuePriority(Integer id, String name, boolean isDefault) {
        IssuePriority ip = new IssuePriority();
        ip.setId(id);
        ip.setName(name);
        ip.setDefault(isDefault);
        return ip;
    }

}
