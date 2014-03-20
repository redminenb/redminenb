/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kenai.redminenb.issue;

import com.kenai.redminenb.repository.RedmineRepository;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;

/**
 *
 * @author Eric
 */
public class RedmineIssueStatusProvider implements IssueStatusProvider<RedmineRepository, RedmineIssue> {

    @Override
    public Status getStatus(RedmineIssue i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSeenIncoming(RedmineIssue i, boolean seen) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<RedmineIssue> getUnsubmittedIssues(RedmineRepository r) {
        return r.getUnsubmittedIssues();
    }

    @Override
    public void discardOutgoing(RedmineIssue i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean submit(RedmineIssue i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removePropertyChangeListener(RedmineIssue i, PropertyChangeListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPropertyChangeListener(RedmineIssue i, PropertyChangeListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
