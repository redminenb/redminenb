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
        return i.getStatus();
    }

    @Override
    public void setSeenIncoming(RedmineIssue i, boolean seen) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<RedmineIssue> getUnsubmittedIssues(RedmineRepository r) {
        return r.getUnsubmittedIssues();
    }

    @Override
    public void discardOutgoing(RedmineIssue i) {
        i.discardOutgoing();
    }

    @Override
    public boolean submit(RedmineIssue i) {
        i.getController().saveChanges();
        return i.submit();
    }

    @Override
    public void removePropertyChangeListener(RedmineIssue i, PropertyChangeListener listener) {
        i.removePropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(RedmineIssue i, PropertyChangeListener listener) {
        i.addPropertyChangeListener(listener);
    }

}
