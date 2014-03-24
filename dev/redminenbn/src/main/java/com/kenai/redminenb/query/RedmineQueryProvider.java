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
package com.kenai.redminenb.query;

import com.kenai.redminenb.issue.RedmineIssue;

import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;

/**
 * Redmine {@link QueryProvider}.
 *
 * @author Anchialas <anchialas@gmail.com>
 */
public class RedmineQueryProvider implements QueryProvider<RedmineQuery, RedmineIssue> {

    @Override
    public String getDisplayName(RedmineQuery query) {
        return query.getDisplayName();
    }

    @Override
    public String getTooltip(RedmineQuery query) {
        return query.getTooltip();
    }

    @Override
    public QueryController getController(RedmineQuery query) {
        return query.getController();
    }

    @Override
    public void remove(RedmineQuery q) {
        q.remove();
    }

    /*@Override
     public boolean isSaved(RedmineQuery query) {
     return query.isSaved();
     }*/

    /*@Override
     public Collection<RedmineIssue> getIssues(RedmineQuery query) {
     return query.getIssues();
     }*/
    @Override
    public void refresh(RedmineQuery query) {
        query.getController().refresh(true);
    }

    /*@Override
     public void removePropertyChangeListener(RedmineQuery query, PropertyChangeListener listener) {
     query.removePropertyChangeListener(listener);
     }

     @Override
     public void addPropertyChangeListener(RedmineQuery query, PropertyChangeListener listener) {
     query.addPropertyChangeListener(listener);
     }
     */
    /*@Override
     public boolean contains(RedmineQuery query, String id) {
     return query.contains(id);
     }*/
    @Override
    public boolean canRemove(RedmineQuery q) {
        return q.canRemove();
    }

    @Override
    public boolean canRename(RedmineQuery q) {
        return q.canRename();
    }

    @Override
    public void rename(RedmineQuery q, String newName) {
        q.rename(newName);
    }

    @Override
    public void setIssueContainer(RedmineQuery q, IssueContainer<RedmineIssue> ic) {
        q.setIssueContainer(ic);
    }

}
