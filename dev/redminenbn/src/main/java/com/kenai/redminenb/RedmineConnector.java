/*
 * Copyright 2012 Mykolas and Anchialas.
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
package com.kenai.redminenb;

import com.kenai.redminenb.issue.RedmineIssue;
import com.kenai.redminenb.issue.RedmineIssuePriorityProvider;
import com.kenai.redminenb.issue.RedmineIssueProvider;
import com.kenai.redminenb.issue.RedmineIssueScheduleProvider;
import com.kenai.redminenb.issue.RedmineIssueStatusProvider;
import com.kenai.redminenb.query.RedmineQuery;
import com.kenai.redminenb.query.RedmineQueryProvider;
import com.kenai.redminenb.repository.RedmineRepository;
import com.kenai.redminenb.repository.RedmineRepositoryProvider;

import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.spi.BugtrackingSupport;
import org.openide.util.NbBundle;

/**
 * RedmineNB {@link BugtrackingConnector connector to NetBeans Bugtracking SPI}.
 *
 * @author Anchialas <anchialas@gmail.com>
 */
@NbBundle.Messages({
    "LBL_ConnectorName=Redmine",
    "LBL_ConnectorTooltip=NetBeans plugin for integration with Redmine"
})
@BugtrackingConnector.Registration(id = RedmineConnector.ID,
        displayName = "#LBL_ConnectorName",
        tooltip = "#LBL_ConnectorTooltip",
        iconPath = "com/kenai/redminenb/resources/redmine.png")
public class RedmineConnector implements BugtrackingConnector {

    public static final String ID = "com.kenai.redminenb";
    private static RedmineIssueStatusProvider isp;
    private static RedmineIssuePriorityProvider ipp;
    private static RedmineIssueScheduleProvider issp;
    private RedmineIssueProvider rip;
    private RedmineQueryProvider rqp;
    private RedmineRepositoryProvider rrp;
    private BugtrackingSupport<RedmineRepository, RedmineQuery, RedmineIssue> support;

    public static String getConnectorName() {
        return Bundle.LBL_ConnectorName();
    }

    @Override
    public Repository createRepository(RepositoryInfo info) {
        RedmineRepository repo = new RedmineRepository(info);
        return createRepository(repo);
    }

    @Override
    public Repository createRepository() {
        RedmineRepository repo = new RedmineRepository();
        return createRepository(repo);
    }

    public BugtrackingSupport<RedmineRepository, RedmineQuery, RedmineIssue> getSupport() {
        if (support == null) {
            support = new BugtrackingSupport<>(getRepositoryProvider(), getQueryProvider(), getIssueProvider());
        }
        return support;
    }

    public RedmineIssuePriorityProvider getIssuePriorityProvider() {
        if (ipp == null) {
            ipp = new RedmineIssuePriorityProvider();
        }
        return ipp;
    }

    public RedmineIssueStatusProvider getIssueStatusProvider() {
        if (isp == null) {
            isp = new RedmineIssueStatusProvider();
        }
        return isp;
    }

    public RedmineIssueScheduleProvider getIssueScheduleProvider() {
        if (issp == null) {
            issp = new RedmineIssueScheduleProvider();
        }
        return issp;
    }

    private RedmineRepositoryProvider getRepositoryProvider() {
        if (rrp == null) {
            rrp = new RedmineRepositoryProvider();
        }
        return rrp;
    }

    public RedmineIssueProvider getIssueProvider() {
        if (rip == null) {
            rip = new RedmineIssueProvider();
        }
        return rip;
    }

    public RedmineQueryProvider getQueryProvider() {
        if (rqp == null) {
            rqp = new RedmineQueryProvider();
        }
        return rqp;
    }

    private Repository createRepository(RedmineRepository repo) {
        return getSupport().createRepository(repo, getIssueStatusProvider(), getIssueScheduleProvider(), getIssuePriorityProvider(), null);
    }
}
