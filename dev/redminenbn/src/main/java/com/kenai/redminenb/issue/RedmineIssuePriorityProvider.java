/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kenai.redminenb.issue;

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
    @SuppressWarnings("empty-statement")
    public IssuePriorityInfo[] getPriorityInfos() {
        // need per manager list
        List<IssuePriority> li = Helper.getDefaultIssuePriorities();
        List<IssuePriorityInfo> lipi = new ArrayList<>();
        for (IssuePriority ip : li) {
            IssuePriorityInfo ipi = new IssuePriorityInfo(ip.getId().toString(), ip.getName());
            lipi.add(ipi);
        }
        return lipi.toArray(
                new IssuePriorityInfo[lipi.size()]);

    }

}
