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
package com.kenai.redminenb.repository;

import com.kenai.redminenb.issue.RedmineIssue;
import com.kenai.redminenb.query.RedmineQuery;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;

/**
 * Redmine {@link RepositoryProvider}.
 *
 * @author Anchialas <anchialas@gmail.com>
 */
public class RedmineRepositoryProvider implements RepositoryProvider<RedmineRepository, RedmineQuery, RedmineIssue> {

    @Override
    public Image getIcon(RedmineRepository r) {
        return r.getIcon();
    }

    @Override
    public RepositoryInfo getInfo(RedmineRepository r) {
        return r.getInfo();
    }

    @Override
    public void removed(RedmineRepository r) {
        r.remove();
    }

    @Override
    public RepositoryController getController(RedmineRepository r) {
        return r.getController();
    }

    @Override
    public RedmineQuery createQuery(RedmineRepository r) {
        return r.createQuery();
    }

    @Override
    public RedmineIssue createIssue(RedmineRepository r) {
        return r.createIssue();
    }

    @Override
    public Collection<RedmineQuery> getQueries(RedmineRepository r) {
        return r.getQueries();
    }

    @Override
    public Collection<RedmineIssue> simpleSearch(RedmineRepository r, String criteria) {
        return r.simpleSearch(criteria);
    }

    @Override
    public Collection<RedmineIssue> getIssues(RedmineRepository r, String... id) {
        return r.getIssues(id);
    }

    /**
     * @since NetBeans V7.3
     */
    @Override
    public void removePropertyChangeListener(RedmineRepository r, PropertyChangeListener listener) {
        r.removePropertyChangeListener(listener);
    }

    /**
     * @since NetBeans V7.3
     */
    @Override
    public void addPropertyChangeListener(RedmineRepository r, PropertyChangeListener listener) {
        r.addPropertyChangeListener(listener);
    }

    /**
     * nb 8
     */
    @Override
    public RedmineIssue createIssue(RedmineRepository r, String summary, String description) {
        return r.createIssue(summary, description);
    }

    @Override
    public boolean canAttachFiles(RedmineRepository r) {
        return r.canAttachFiles();
    }

}
