/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kenai.redminenb.issue;

import java.util.Date;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.spi.IssueScheduleProvider;

/**
 *
 * @author Eric
 */
public class RedmineIssueScheduleProvider implements IssueScheduleProvider<RedmineIssue> {

    @Override
    public void setSchedule(RedmineIssue i, IssueScheduleInfo scheduleInfo) {
        i.setSchedule(scheduleInfo);
    }

    @Override
    public Date getDueDate(RedmineIssue i) {
        return i.getDueDate();
    }

    @Override
    public IssueScheduleInfo getSchedule(RedmineIssue i) {
        return i.getSchedule();
    }

}
