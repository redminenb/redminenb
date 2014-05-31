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

import com.kenai.redminenb.Redmine;
import com.kenai.redminenb.RedmineConnector;
import com.kenai.redminenb.issue.RedmineIssue;
import com.kenai.redminenb.repository.RedmineRepository;
import com.taskadapter.redmineapi.AuthenticationException;
import com.taskadapter.redmineapi.NotFoundException;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.Issue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.apache.commons.lang.StringUtils;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.openide.util.Exceptions;

/**
 * Redmine Query.
 *
 * @author Anchialas <anchialas@gmail.com>
 */
public final class RedmineQuery {
    private static final Logger LOG = Logger.getLogger(RedmineQuery.class.getName());

    private String name;
    private final RedmineRepository repository;
    private final Set<RedmineIssue> issues = new HashSet<>();
    //
    private boolean firstRun = true;
    private boolean saved;
    protected long lastRefresh;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    //
    private Map<String, ParameterValue[]> parameters = Collections.EMPTY_MAP;
    private RedmineQueryController queryController;
    
    public synchronized RedmineQueryController getController() {
        if (queryController == null) {
            queryController = new RedmineQueryController(repository, this);
        }
        return queryController;
    }
    
    public RedmineQuery(RedmineRepository repository) {
        this.repository = repository;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    private void firePropertyChanged() {
        support.firePropertyChange(QueryController.PROP_CHANGED, null, null);
    }

    public Map<String, ParameterValue[]> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, ParameterValue[]> parameters) {
        if(parameters == null) {
            parameters = Collections.EMPTY_MAP;
        }
        boolean changed = ! parameters.equals(this.parameters);
        this.parameters = parameters;
        if (changed) {
            firePropertyChanged();
            setSaved(false);
        }
    }

    private QueryProvider.IssueContainer<RedmineIssue> delegateContainer;

    void setIssueContainer(QueryProvider.IssueContainer<RedmineIssue> ic) {
        delegateContainer = ic;
    }

    public String getDisplayName() {
        return name;
    }

    public String getTooltip() {
        return name + " - " + repository.getDisplayName(); // NOI18N
    }

    public RedmineRepository getRepository() {
        return repository;
    }

    void refresh(boolean autoReresh) {
        doRefresh(autoReresh);
    }

    public void refresh() {
        // @todo what if already running! - cancel task
        doRefresh(false);
    }

    private boolean doRefresh(final boolean autoRefresh) {
        // XXX what if already running! - cancel task
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        final boolean ret[] = new boolean[1];
        executeQuery(new Runnable() {
            @Override
            public void run() {
                Redmine.LOG.log(Level.FINE, "refresh start - {0}", name); // NOI18N
                try {
                    if (delegateContainer != null) {
                        delegateContainer.refreshingStarted();
                    }

                    delegateContainer.clear();
                    issues.clear();

                    firstRun = false;
                    try {
                        List<Issue> issueArr = doSearch();
                        for (Issue issue : issueArr) {
                            RedmineIssue redmineIssue = new RedmineIssue(repository, issue);
                            issues.add(redmineIssue);
                            if (delegateContainer != null) {
                                delegateContainer.add(redmineIssue);
                            }
                            fireNotifyData(redmineIssue); // XXX - !!! triggers getIssues()
                        }

                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }

                    if (delegateContainer != null) {
                        delegateContainer.refreshingFinished();
                    }
                } finally {
                    logQueryEvent(issues.size(), autoRefresh);
                    Redmine.LOG.log(Level.FINE, "refresh finish - {0}", name); // NOI18N
                }
            }
        });

        return ret[0];
    }

    protected void logQueryEvent(int count, boolean autoRefresh) {
        LOG.fine(String.format("Query '%s-%s', Count: %d, Autorefresh: %b",
                RedmineConnector.NAME,
                name,
                count,
                autoRefresh
                ));
    }

    /**
     * Performs the issue search with the attributes and values provided by the
     * map.
     * <p>
     * Note: The Redmine REST API does not support full search support for all
     * fields. So the issues are post filtered here.
     *
     * @see http://www.redmine.org/projects/redmine/wiki/Rest_Issues
     * @see RedmineQueryController#RedmineQueryController
     * @param searchParameters
     */
    private List<Issue> doSearch()
            throws IOException, AuthenticationException, NotFoundException, RedmineException {

        boolean searchSubject = false;
        boolean searchDescription = false;
        boolean searchComments = false;
        ParameterValue[] queryStringParameter = parameters.remove("query");
        Map<String,ParameterValue[]> multiValueParameters = new HashMap<>();

        Map<String, String> m = new HashMap<>();
        m.put("project_id", String.valueOf(repository.getProject().getId()));

        for (Entry<String,ParameterValue[]> p : parameters.entrySet()) {
            String parameter = p.getKey();
            ParameterValue[] paramValues = p.getValue();
            if (StringUtils.isNotBlank(ParameterValue.flattenList(paramValues))) {
                if (paramValues.length == 1) {
                    if ("is_subject".equals(parameter)) {
                        searchSubject = "1".equals(paramValues[0].getValue());
                    } else if ("is_description".equals(parameter)) {
                        searchDescription = "1".equals(paramValues[0].getValue());
                    } else if ("is_comments".equals(parameter)) {
                        searchComments = "1".equals(paramValues[0].getValue());
                    } else {
                        m.put(parameter, paramValues[0].getValue());
                    }
                } else if (paramValues.length > 1) {
                    multiValueParameters.put(parameter, paramValues);
                }
            }
        }

        // Perform search
        List<Issue> issueArr = repository.getManager().getIssues(m);

        // Post filtering: Query string
        if (queryStringParameter != null && queryStringParameter.length != 0
                && (searchSubject || searchDescription || searchComments)) {
            String queryStr = ParameterValue.flattenList(queryStringParameter);

            List<Issue> newArr = new ArrayList<>(issueArr.size());
            for (Issue issue : issueArr) {
                if ((searchSubject && StringUtils.containsIgnoreCase(issue.getSubject(), queryStr))
                        || (searchDescription && StringUtils.containsIgnoreCase(issue.getDescription(), queryStr))
                   ) {
                    newArr.add(issue);
                }
            }
            issueArr = newArr;
        }

        // Post filtering: Multi-value parameters
        if (!multiValueParameters.isEmpty()) {
            List<Issue> newArr = new ArrayList<>(issueArr.size());
            for (Issue issue : issueArr) {
                for (Entry<String,ParameterValue[]> p : parameters.entrySet()) {
                    // TODO: map FIELD_xxx property to query parameter
                    String paramName = p.getKey();
                    ParameterValue[] parameterValues = p.getValue();
                    if ("tracker_id".equals(paramName)) {
                        for (ParameterValue pv : parameterValues) {
                            if (String.valueOf(issue.getTracker().getId()).equals(pv.getValue())) {
                                newArr.add(issue);
                                break;
                            }
                        }
                    } else if ("status_id".equals(paramName)) {
                        for (ParameterValue pv : parameterValues) {
                            if (String.valueOf(issue.getStatusId()).equals(pv.getValue())) {
                                newArr.add(issue);
                                break;
                            }
                        }
                    } else if ("priority_id".equals(paramName)) {
                        for (ParameterValue pv : parameterValues) {
                            if (String.valueOf(issue.getPriorityId()).equals(pv.getValue())) {
                                newArr.add(issue);
                                break;
                            }
                        }
                    } else if ("assigned_to_id".equals(paramName)) {
                        for (ParameterValue pv : parameterValues) {
                            if ((pv.equals(ParameterValue.NONE_PARAMETERVALUE) && issue.getAssignee() == null)
                                    || (issue.getAssignee() != null && String.valueOf(issue.getAssignee().getId()).equals(pv.getValue()))) {
                                newArr.add(issue);
                                break;
                            }
                        }
                    } else if ("category_id".equals(paramName)) {
                        for (ParameterValue pv : parameterValues) {
                            if ((pv.equals(ParameterValue.NONE_PARAMETERVALUE) && issue.getCategory() == null)
                                    || (issue.getCategory() != null && String.valueOf(issue.getCategory().getId()).equals(pv.getValue()))) {
                                newArr.add(issue);
                                break;
                            }
                        }
                    } else if ("fixed_version_id".equals(paramName)) {
                        for (ParameterValue pv : parameterValues) {
                            if ((pv.equals(ParameterValue.NONE_PARAMETERVALUE) && issue.getTargetVersion() == null)
                                    || (issue.getTargetVersion() != null && String.valueOf(issue.getTargetVersion().getId()).equals(pv.getValue()))) {
                                newArr.add(issue);
                                break;
                            }
                        }
                    } else {
                        Redmine.LOG.log(Level.WARNING, "Unsupported multi-value parameter ''{0}''", paramName);
                    }

                }
            }
            issueArr = newArr;
        }

        return issueArr;
    }

    public void remove() {
        repository.removeQuery(this.getDisplayName());
        firePropertyChanged();
    }

    boolean wasRun() {
        return !firstRun;
    }

    long getLastRefresh() {
        return lastRefresh;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
        firePropertyChanged();
    }

    public boolean isSaved() {
        return saved;
    }

    public Collection<RedmineIssue> getIssues() {
        return Collections.unmodifiableSet(issues);
    }

    public boolean contains(RedmineIssue issue) {
        return issues.contains(issue);
    }

    public void addNotifyListener(QueryNotifyListener l) {
        List<QueryNotifyListener> list = getNotifyListeners();
        synchronized (list) {
            list.add(l);
        }
    }

    public void removeNotifyListener(QueryNotifyListener l) {
        List<QueryNotifyListener> list = getNotifyListeners();
        synchronized (list) {
            list.remove(l);
        }
    }

    protected void fireNotifyData(RedmineIssue issue) {
        QueryNotifyListener[] listeners = getListeners();
        for (QueryNotifyListener l : listeners) {
            l.notifyData(issue);
        }
    }

    protected void fireStarted() {
        QueryNotifyListener[] listeners = getListeners();
        for (QueryNotifyListener l : listeners) {
            l.started();
        }
    }

    protected void fireFinished() {
        QueryNotifyListener[] listeners = getListeners();
        for (QueryNotifyListener l : listeners) {
            l.finished();
        }
    }

    // XXX move to API
    protected void executeQuery(Runnable r) {
        fireStarted();
        try {
            r.run();
        } finally {
            lastRefresh = System.currentTimeMillis();
            fireFinished();
            firePropertyChanged();
        }
    }

    private QueryNotifyListener[] getListeners() {
        List<QueryNotifyListener> list = getNotifyListeners();
        QueryNotifyListener[] listeners;
        synchronized (list) {
            listeners = list.toArray(new QueryNotifyListener[list.size()]);
        }
        return listeners;
    }
    private List<QueryNotifyListener> notifyListeners;

    private List<QueryNotifyListener> getNotifyListeners() {
        if (notifyListeners == null) {
            notifyListeners = new ArrayList<>();
        }
        return notifyListeners;
    }

    void rename(String newName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    boolean canRename() {
        return false;
    }

    boolean canRemove() {
        return true;
    }

}
