/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kenai.redminenb.issue;

import org.netbeans.modules.bugtracking.spi.IssuePriorityInfo;
import org.netbeans.modules.bugtracking.spi.IssuePriorityProvider;

/**
 *
 * @author Eric
 */
public class RedmineIssuePriorityProvider implements IssuePriorityProvider<RedmineIssue> {

    @Override
    public String getPriorityID(RedmineIssue i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IssuePriorityInfo[] getPriorityInfos() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
