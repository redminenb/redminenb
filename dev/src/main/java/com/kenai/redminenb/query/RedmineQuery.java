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
import com.kenai.redminenb.repository.IssueCache;
import com.kenai.redminenb.repository.RedmineRepository;
import com.kenai.redminenb.util.ExceptionHandler;
import com.kenai.redminenb.util.NestedProject;
import com.kenai.redminenb.util.SafeAutoCloseable;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Project;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
import org.openide.util.Mutex;

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
    private long lastRefresh;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    //
    private Map<String, ParameterValue[]> parameters = new HashMap<>();
    private final RedmineQueryController queryController;
    
    private Integer busy = 0;
    
    private final SafeAutoCloseable busyHelper = new SafeAutoCloseable() {
        @Override
        public void close() {
            setBusy(false);
        }
    };
    
    public SafeAutoCloseable busy() {
        setBusy(true);
        return busyHelper;
    }

    public synchronized boolean isBusy() {
        return busy != 0;
    }
    
    public synchronized void setBusy(boolean busyBool) {
        final boolean oldBusy = isBusy();
        if (busyBool) {
            busy++;
        } else {
            busy--;
        }
        if (busy < 0) {
            throw new IllegalStateException("Inbalanced busy/nonbusy");
        }
        Mutex.EVENT.writeAccess(new Runnable() {
            @Override
            public void run() {
                 support.firePropertyChange("busy", oldBusy, busy != 0);
            }
        });
    }
    
    public RedmineQueryController getController() {
        return queryController;
    }
    
    public RedmineQuery(RedmineRepository repository) {
        this.repository = repository;
        this.queryController = new RedmineQueryController(repository, this);
        try {
            Project p = repository.getProject();
            NestedProject np = repository.getProjects().get(p.getId());
            parameters.put("project_id", new ParameterValue[]{
                new ParameterValue(np.toString(), p.getId())});
        } catch (RedmineException | NullPointerException ex) {
            // Happens when failing to retrieve project/no project set => swallow
        }
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
        try(SafeAutoCloseable sac = busy()) {
            executeQuery(new Runnable() {
                @Override
                public void run() {
                    Redmine.LOG.log(Level.FINE, "refresh start - {0}", name); // NOI18N
                    try {
                        if (delegateContainer != null) {
                            delegateContainer.refreshingStarted();
                            delegateContainer.clear();
                        }

                        issues.clear();

                        firstRun = false;
                        try {
                            List<Issue> issueArr = doSearch();
                            IssueCache issueCache = repository.getIssueCache();
                            for (Issue issue : issueArr) {
                                RedmineIssue redmineIssue = issueCache.cachedRedmineIssue(issue);
                                issues.add(redmineIssue);
                                if (delegateContainer != null) {
                                    delegateContainer.add(redmineIssue);
                                }
                                fireNotifyData(redmineIssue); // XXX - !!! triggers getIssues()
                            }

                        } catch (RedmineException | RuntimeException ex) {
                            ExceptionHandler.handleException(LOG, "Failed to search", ex);
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
        }

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
    private List<Issue> doSearch() throws RedmineException {
        boolean searchDescription = false;
        
        ParameterValue[] queryStringParameter = parameters.get("query");
        String queryStr = ParameterValue.flattenList(queryStringParameter);

        Map<String, String> m = new HashMap<>();

        for (Entry<String,ParameterValue[]> p : parameters.entrySet()) {
            String parameter = p.getKey();
            // Query parameter is handled seperatedly
            if("query".equals(parameter)) {
                continue;
            }
            ParameterValue[] paramValues = p.getValue();
            if (StringUtils.isNotBlank(ParameterValue.flattenList(paramValues))) {
                if ( "is_subject".equals(parameter) ) {
                    if( StringUtils.isNotBlank(queryStr) ) {
                        m.put("subject", "~" + queryStr);
                    }
                } else if ("is_description".equals(parameter)) {
                    searchDescription = "1".equals(paramValues[0].getValue());
                } else {
                    boolean isNone = false;
                    for (ParameterValue pv : paramValues) {
                        if (ParameterValue.NONE_PARAMETERVALUE.equals(pv)) {
                            isNone = true;
                        }
                    }
                    if (isNone) {
                        m.put(parameter, "!*");
                    } else if (paramValues.length == 1) {
                        m.put(parameter, paramValues[0].getValue());
                    } else if (paramValues.length > 1) {
                        if("project_id".equals(parameter)) {
                            m.put(parameter, paramValues[0].getValue());
                            LOG.warning("Redmine currently (2.6.0) does not allow multiple projects for querying - only using first project");
                        } else {
                            m.put(parameter, ParameterValue.flattenList(paramValues));
                        }
                    }
                }
            }
        }

        List<Issue> issueArr = new ArrayList<>();
        // Limit request count
        int offset = 0;
        for(int i = 0; i < 100; i++) {
            // Perform search
            // According to the documentation 100 is the maximum  
            m.put("limit", Integer.toString(100));
            m.put("offset", Integer.toString(offset));
            List<Issue> queryResult = repository.getIssueManager().getIssues(m);
            issueArr.addAll(queryResult);
            offset += queryResult.size();
            if(queryResult.isEmpty()) {
                break;
            }
        }

        // Post filtering: Query string for description
        if (searchDescription && StringUtils.isNotBlank(queryStr)) {
            List<Issue> newArr = new ArrayList<>(issueArr.size());
            for (Issue issue : issueArr) {
                if (StringUtils.containsIgnoreCase(issue.getDescription(), queryStr)) {
                    newArr.add(issue);
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
