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
import com.kenai.redminenb.RedmineConfig;
import com.kenai.redminenb.RedmineConnector;
import com.kenai.redminenb.RedmineException;
import com.kenai.redminenb.issue.RedmineIssue;
import com.kenai.redminenb.query.RedmineQueryParameter.CheckBoxParameter;
import com.kenai.redminenb.query.RedmineQueryParameter.ListParameter;
import com.kenai.redminenb.query.RedmineQueryParameter.TextFieldParameter;
import com.kenai.redminenb.repository.RedmineRepository;
import com.kenai.redminenb.user.RedmineUser;
import com.kenai.redminenb.util.RedmineUtil;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssuePriority;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.Version;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.lang.StringUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.api.Util;
import org.netbeans.modules.bugtracking.commons.SaveQueryPanel;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugtracking.issuetable.IssueTable;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.team.commons.LogUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Anchialas <anchialas@gmail.com>
 */
@NbBundle.Messages({
    "MSG_SameName=Query with the same name already exists.",
    "MSG_NoResults=No Issues found",
    "# {0} - the issue number",
    "MSG_NotFound=Issue #{0} not found",
    "# {0} - the issue number",
    "MSG_Opening=Opening Issue #{0}...",
    "MSG_Searching=Searching...",
    "# {0} - the query name",
    "MSG_SearchingQuery=Searching {0}...",
    "# {0} - the query name",
    "MSG_RemoveQuery=Do you want to remove the query ''{0}''?",
    "# {0} - the display name of the repository",
    "MSG_Populating=Reading server data from Issue Tracker ''{0}''...",
    "CTL_RemoveQuery=Remove",
    "# {0} - the issue number",
    "LBL_RetrievingIssue=Retrieved issue #{0}",
    "LBL_Never=Never",
    "# {0} - the search hits count",
    "LBL_MatchingIssues=There {0,choice,0#are no issues|1#is one issue|1<are {0,number,integer} issues} matching this query.",
    "LBL_SelectKeywords=Select or deselect keywords."
})
public class RedmineQueryController
        implements QueryController, ItemListener, ListSelectionListener, ActionListener, FocusListener, KeyListener {

    final RedmineQueryPanel queryPanel;
    private final IssueTable issueTable;
    //
    private final RequestProcessor rp = new RequestProcessor("Redmine query", 1, true);  // NOI18N
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // NOI18N
    private final RedmineRepository repository;
    //
    private RedmineQuery query;
    //
    private final ListParameter versionParameter;
    private final ListParameter trackerParameter;
    private final ListParameter statusParameter;
    private final ListParameter categoryParameter;
    private final ListParameter priorityParameter;
    //private final ListParameter resolutionParameter;
    //private final ListParameter severityParameter;
    private final ListParameter assigneeParameter;
    private final Map<String, RedmineQueryParameter> parameters;
    //
    private final Object REFRESH_LOCK = new Object();
    private QueryTask refreshTask;

    public RedmineQueryController(RedmineRepository repository, RedmineQuery query) {
        this.repository = repository;
        this.query = query;

        issueTable = new IssueTable(PROP_CHANGED, PROP_CHANGED, this, null, true/*RedmineUtil.getRepository(repository),
         query, RedmineIssue.getColumnDescriptors(repository)*/);
        issueTable.setRenderer(new RedmineQueryCellRenderer(issueTable.getRenderer()));

        queryPanel = new RedmineQueryPanel(issueTable.getComponent(), this);

        // set parameters
        parameters = new LinkedHashMap<String, RedmineQueryParameter>();

        trackerParameter = registerQueryParameter(ListParameter.class, queryPanel.trackerList, "tracker_id");
        categoryParameter = registerQueryParameter(ListParameter.class, queryPanel.categoryList, "category_id");
        versionParameter = registerQueryParameter(ListParameter.class, queryPanel.versionList, "fixed_version_id");
        statusParameter = registerQueryParameter(ListParameter.class, queryPanel.statusList, "status_id");
        priorityParameter = registerQueryParameter(ListParameter.class, queryPanel.priorityList, "priority_id");
        //resolutionParameter = 
        //severityParameter = ...
        assigneeParameter = registerQueryParameter(ListParameter.class, queryPanel.assigneeList, "assigned_to_id");

        registerQueryParameter(TextFieldParameter.class, queryPanel.queryTextField, "query");
        registerQueryParameter(CheckBoxParameter.class, queryPanel.qSubjectCheckBox, "is_subject");
        registerQueryParameter(CheckBoxParameter.class, queryPanel.qDescriptionCheckBox, "is_description");
        registerQueryParameter(CheckBoxParameter.class, queryPanel.qCommentsCheckBox, "is_comments");

        setListeners();
        postPopulate();
    }

    private void setListeners() {
        queryPanel.filterComboBox.addItemListener(this);
        queryPanel.searchButton.addActionListener(this);
        queryPanel.refreshCheckBox.addActionListener(this);
        queryPanel.saveChangesButton.addActionListener(this);
        queryPanel.cancelChangesButton.addActionListener(this);
        queryPanel.gotoIssueButton.addActionListener(this);
        queryPanel.webButton.addActionListener(this);
        queryPanel.saveButton.addActionListener(this);
        queryPanel.refreshButton.addActionListener(this);
        queryPanel.modifyButton.addActionListener(this);
        queryPanel.seenButton.addActionListener(this);
        queryPanel.removeButton.addActionListener(this);
        queryPanel.refreshConfigurationButton.addActionListener(this);
        queryPanel.findIssuesButton.addActionListener(this);
        queryPanel.cloneQueryButton.addActionListener(this);

        queryPanel.issueIdTextField.addActionListener(this);

        queryPanel.categoryList.addKeyListener(this);
        queryPanel.versionList.addKeyListener(this);
        queryPanel.statusList.addKeyListener(this);
        queryPanel.resolutionList.addKeyListener(this);
        queryPanel.severityList.addKeyListener(this);
        queryPanel.priorityList.addKeyListener(this);
        queryPanel.assigneeList.addKeyListener(this);

        queryPanel.queryTextField.addActionListener(this);
    }

    /*   @Override
     public JComponent getComponent() {
     return queryPanel;
     }*/
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == queryPanel.filterComboBox) {
            onFilterChange((Filter) e.getItem());
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
//      if (e.getSource() == queryPanel.productList) {
//         onProductChanged(e);
//      }
    }

    @Override
    public void focusGained(FocusEvent e) {
//      if (panel.changedFromTextField.getText().equals("")) {                   // NOI18N
//         String lastChangeFrom = BugzillaConfig.getInstance().getLastChangeFrom();
//         panel.changedFromTextField.setText(lastChangeFrom);
//         panel.changedFromTextField.setSelectionStart(0);
//         panel.changedFromTextField.setSelectionEnd(lastChangeFrom.length());
//      }
    }

    @Override
    public void focusLost(FocusEvent e) {
        // do nothing
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == queryPanel.searchButton) {
                onRefresh();
            } else if (e.getSource() == queryPanel.gotoIssueButton) {
                onGotoIssue();
            } else if (e.getSource() == queryPanel.saveChangesButton) {
                onSave(true); // refresh
            } else if (e.getSource() == queryPanel.cancelChangesButton) {
                onCancelChanges();
            } else if (e.getSource() == queryPanel.webButton) {
                onWeb();
            } else if (e.getSource() == queryPanel.saveButton) {
                onSave(false); // do not refresh
            } else if (e.getSource() == queryPanel.refreshButton) {
                onRefresh();
            } else if (e.getSource() == queryPanel.modifyButton) {
                onModify();
            } else if (e.getSource() == queryPanel.seenButton) {
                onMarkSeen();
            } else if (e.getSource() == queryPanel.removeButton) {
                onRemove();
            } else if (e.getSource() == queryPanel.refreshCheckBox) {
                onAutoRefresh();
            } else if (e.getSource() == queryPanel.refreshConfigurationButton) {
                onRefreshConfiguration();
            } else if (e.getSource() == queryPanel.findIssuesButton) {
                onFindIssues();
            } else if (e.getSource() == queryPanel.cloneQueryButton) {
                onCloneQuery();
            } else if (e.getSource() == queryPanel.issueIdTextField) {
                if (!queryPanel.issueIdTextField.getText().trim().equals("")) {                // NOI18N
                    onGotoIssue();
                }
            } else if (e.getSource() == queryPanel.issueIdTextField
                    || e.getSource() == queryPanel.queryTextField) {
                onRefresh();
            }
        } catch (RedmineException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // do nothing
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // do nothing
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() != KeyEvent.VK_ENTER) {
            return;
        }
        if (e.getSource() == queryPanel.categoryList
                || e.getSource() == queryPanel.versionList
                || e.getSource() == queryPanel.statusList
                || e.getSource() == queryPanel.resolutionList
                || e.getSource() == queryPanel.priorityList
                || e.getSource() == queryPanel.assigneeList) {
            onRefresh();
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    private void onFilterChange(Filter filter) {
        selectFilter(filter);
    }

    private void onSave(final boolean refresh) throws RedmineException {
        Redmine.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                Redmine.LOG.fine("on save start");
                String name = query.getDisplayName();
                if (!query.isSaved()) {
                    name = getSaveName();
                    if (name == null) {
                        return;
                    }
                }
                assert name != null;
                save(name);
                Redmine.LOG.fine("on save finnish");

                if (refresh) {
                    onRefresh();
                }
            }
        });
    }

    /**
     * Saves the query under the given name
     *
     * @param name
     */
    private void save(String name) {
        Redmine.LOG.log(Level.FINE, "saving query '{0}'", new Object[]{name});
        query.setName(name);
        repository.saveQuery(query);
        query.setSaved(true); // XXX
        setAsSaved();
        if (!query.wasRun()) {
            Redmine.LOG.log(Level.FINE, "refreshing query '{0}' after save", new Object[]{name});
            onRefresh();
        }
        Redmine.LOG.log(Level.FINE, "query '{0}' saved", new Object[]{name});
    }

    private String getSaveName() {
        SaveQueryPanel.QueryNameValidator v = new SaveQueryPanel.QueryNameValidator() {
            @Override
            public String isValid(String name) {
                for (RedmineQuery q : repository.getQueries()) {
                    if (q.getDisplayName().equals(name)) {
                        return Bundle.MSG_SameName();
                    }
                }
                return null;
            }
        };
        return SaveQueryPanel.show(v, new HelpCtx("com.kenai.redminenb.query.savePanel"));
    }

    private void onCancelChanges() {
//        if(query.getDisplayName() != null) { // XXX need a better semantic - isSaved?
//            String urlParameters = RedmineConfig.getInstance().getUrlParams(repository, query.getDisplayName());
//            if(urlParameters != null) {
//                setParameters(urlParameters);
//            }
//        }
        setAsSaved();
    }

    public void selectFilter(final Filter filter) {
        if (filter != null) {
            // XXX this part should be handled in the issues table - move the filtercombo and the label over
            int c = 0;
            for (RedmineIssue issue : query.getIssues()) {
                if (filter.accept(issue.getNode())) {
                    c++;
                }
            }
            final int issueCount = c;

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    queryPanel.filterComboBox.setSelectedItem(filter);
                    setIssueCount(issueCount);
                }
            };
            if (EventQueue.isDispatchThread()) {
                r.run();
            } else {
                EventQueue.invokeLater(r);
            }
        }
        issueTable.setFilter(filter);
    }

    private void setAsSaved() {
        queryPanel.setSaved(query.getDisplayName(), getLastRefresh());
        queryPanel.setModifyVisible(false);
        queryPanel.refreshCheckBox.setVisible(true);
    }

    private String getLastRefresh() throws MissingResourceException {
        long l = query.getLastRefresh();
        return l > 0
                ? dateFormat.format(new Date(l))
                : Bundle.LBL_Never();
    }

    private void onGotoIssue() throws RedmineException {
        final Long issueId = (Long) queryPanel.issueIdTextField.getValue();
        if (issueId == null) {
            return;
        }

        final RequestProcessor.Task[] t = new RequestProcessor.Task[1];
        Cancellable c = new Cancellable() {
            @Override
            public boolean cancel() {
                if (t[0] != null) {
                    return t[0].cancel();
                }
                return true;
            }
        };
        final ProgressHandle handle = ProgressHandleFactory.createHandle(Bundle.MSG_Opening(issueId), c); // NOI18N
        t[0] = Redmine.getInstance().getRequestProcessor().create(new Runnable() {
            @Override
            public void run() {
                handle.start();
                try {
                    openIssue(repository.getIssue(String.valueOf(issueId)));
                } finally {
                    handle.finish();
                }
            }
        });
        t[0].schedule(0);
    }

    protected void openIssue(RedmineIssue issue) {
        if (issue != null) {
            RedmineUtil.openIssue(issue);
        } else {
            // XXX nice message?
        }
    }

    private void onWeb() throws RedmineException {
        String params = null; //query.getUrlParameters();
        String repoURL = repository.getUrl();
        final String urlString = repoURL + (StringUtils.isNotBlank(params) ? params : ""); // NOI18N

        Redmine.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                URL url;
                try {
                    url = new URL(urlString);
                } catch (MalformedURLException ex) {
                    Redmine.LOG.log(Level.SEVERE, null, ex);
                    return;
                }
                HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault();
                if (displayer != null) {
                    displayer.showURL(url);
                } else {
                    // XXX nice error message?
                    Redmine.LOG.warning("No URLDisplayer found.");             // NOI18N
                }
            }
        });
    }

    private void onCloneQuery() {
        RedmineQuery q = new RedmineQuery(null, repository, null, false, false, true);
        RedmineUtil.openQuery(q);
    }

    public void autoRefresh() {
        refresh(true, false);
    }

    public void refresh(boolean synchronously) {
        refresh(false, synchronously);
    }

    private void onAutoRefresh() {
        final boolean autoRefresh = queryPanel.refreshCheckBox.isSelected();
        RedmineConfig.getInstance().setQueryAutoRefresh(query.getDisplayName(), autoRefresh);
        logAutoRefreshEvent(autoRefresh);
        if (autoRefresh) {
            scheduleForRefresh();
        } else {
            repository.stopRefreshing(query);
        }
    }

    public void onRefresh() {
        refresh(false, false);
    }

    private void refresh(final boolean auto, boolean synchronously) {
        RequestProcessor.Task t;
        synchronized (REFRESH_LOCK) {
            if (refreshTask == null) {
                refreshTask = new QueryTask();
            } else {
                refreshTask.cancel();
            }
            t = refreshTask.post(auto);
        }
        if (synchronously) {
            t.waitFinished();
        }
    }

    private void onModify() {
        queryPanel.setModifyVisible(true);
    }

    private void onMarkSeen() throws RedmineException {
        Redmine.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                /*for (RedmineIssue issue : query.getIssues()) {
                 try {
                 issue.setSeen(true);
                 } catch (IOException ex) {
                 Redmine.LOG.log(Level.SEVERE, null, ex);
                 }
                 }*/
            }
        });
    }

    private void onRemove() throws RedmineException {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(Bundle.MSG_RemoveQuery(query.getDisplayName()),
                Bundle.CTL_RemoveQuery(),
                NotifyDescriptor.OK_CANCEL_OPTION);
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            Redmine.getInstance().getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    remove();
                }
            });
        }
    }

    private void onFindIssues() {
        Util.createNewQuery(RedmineUtil.getRepository(repository));
    }

    protected void scheduleForRefresh() {
        if (query.isSaved()) {
            repository.scheduleForRefresh(query);
        }
    }

    protected void logAutoRefreshEvent(boolean autoRefresh) {
        LogUtils.logAutoRefreshEvent(RedmineConnector.getConnectorName(),
                query.getDisplayName(),
                false,
                autoRefresh);
    }

    private void onRefreshConfiguration() {
//      postPopulate(query.getUrlParameters(), true);
        postPopulate();
    }

    protected final void postPopulate() {

        final RequestProcessor.Task[] t = new RequestProcessor.Task[1];
        Cancellable c = new Cancellable() {
            @Override
            public boolean cancel() {
                if (t[0] != null) {
                    return t[0].cancel();
                }
                return true;
            }
        };

        final String msgPopulating = Bundle.MSG_Populating(repository.getDisplayName());
        final ProgressHandle handle = ProgressHandleFactory.createHandle(msgPopulating, c);

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                enableFields(false);
                queryPanel.showRetrievingProgress(true, msgPopulating, !query.isSaved());
                handle.start();
            }
        });

        t[0] = rp.post(new Runnable() {
            @Override
            public void run() {
                try {
                    populate();

                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            enableFields(true);
                            handle.finish();
                            queryPanel.showRetrievingProgress(false, null, !query.isSaved());
                        }
                    });
                }
            }
        });
    }

    protected void populate() {
        if (Redmine.LOG.isLoggable(Level.FINE)) {
            Redmine.LOG.log(Level.FINE, "Starting populate query controller {0}", (query.isSaved() ? " - " + query.getDisplayName() : "")); // NOI18N
        }
        try {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    populateProjectDetails();

                    if (query.isSaved()) {
                        boolean autoRefresh = RedmineConfig.getInstance().getQueryAutoRefresh(query.getDisplayName());
                        queryPanel.refreshCheckBox.setSelected(autoRefresh);
                    }
                }
            });
        } finally {
            if (Redmine.LOG.isLoggable(Level.FINE)) {
                Redmine.LOG.log(Level.FINE, "Finnished populate query controller{0}", (query.isSaved() ? " - " + query.getDisplayName() : "")); // NOI18N
            }
        }
    }

    private void populateProjectDetails() {
        List<ParameterValue> pvList;

        // Tracker
        pvList = new ArrayList<ParameterValue>();
        for (Tracker t : repository.getTrackers()) {
            pvList.add(new ParameterValue(t.getName(), t.getId()));
        }
        trackerParameter.setParameterValues(pvList);

        // Status
        pvList = new ArrayList<ParameterValue>();
//      pvList.add(new ParameterValue("open"));
//      pvList.add(new ParameterValue("closed"));
//      pvList.add(null);
        for (IssueStatus s : repository.getStatuses()) {
            pvList.add(new ParameterValue(s.getName(), s.getId()));
        }
        statusParameter.setParameterValues(pvList);

        // Issue Priority
        pvList = new ArrayList<ParameterValue>();
        for (IssuePriority ip : repository.getIssuePriorities()) {
            pvList.add(new ParameterValue(ip.getName(), ip.getId()));
        }
        priorityParameter.setParameterValues(pvList);

        // Assignee (assigned to)
        pvList = new ArrayList<ParameterValue>();
        pvList.add(ParameterValue.NONE_PARAMETERVALUE);
        for (RedmineUser redmineUser : repository.getUsers()) {
            pvList.add(new ParameterValue(redmineUser.getFullName(), redmineUser.getId(), redmineUser));
        }
        assigneeParameter.setParameterValues(pvList);

        // Category
        pvList = new ArrayList<ParameterValue>();
        pvList.add(ParameterValue.NONE_PARAMETERVALUE);
        for (IssueCategory c : repository.getIssueCategories()) {
            pvList.add(new ParameterValue(c.getName(), c.getId()));
        }
        categoryParameter.setParameterValues(pvList);

        // Target Version
        pvList = new ArrayList<ParameterValue>();
        pvList.add(ParameterValue.NONE_PARAMETERVALUE);
        for (Version v : repository.getVersions()) {
            pvList.add(new ParameterValue(v.getName(), v.getId()));
        }
        versionParameter.setParameterValues(pvList);
    }

    private <T extends RedmineQueryParameter> T registerQueryParameter(Class<T> clazz, Component c, String parameterName) {
        try {
            Constructor<T> constructor = clazz.getConstructor(c.getClass(), String.class);
            T t = constructor.newInstance(c, parameterName);
            parameters.put(parameterName, t);
            return t;
        } catch (Exception ex) {
            Redmine.LOG.log(Level.SEVERE, parameterName, ex);
        }
        return null;
    }

    public Map<String, RedmineQueryParameter> getSearchParameters() {
        return new HashMap<String, RedmineQueryParameter>(parameters);
    }

    protected void enableFields(boolean bl) {
        // set all non parameter fields
        queryPanel.enableFields(bl);
        // set the parameter fields
        for (Map.Entry<String, RedmineQueryParameter> e : parameters.entrySet()) {
            RedmineQueryParameter pv = parameters.get(e.getKey());
            pv.setEnabled(bl && !pv.isEmpty());
        }
    }

    private void remove() {
        if (refreshTask != null) {
            refreshTask.cancel();
        }
        query.remove();
    }

    private void setIssueCount(final int count) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                queryPanel.tableSummaryLabel.setText(Bundle.LBL_MatchingIssues(count));
            }
        });
    }

    void switchToDeterminateProgress(long issuesCount) {
        if (refreshTask != null) {
            refreshTask.switchToDeterminateProgress(issuesCount);
        }
    }

    void addProgressUnit(String issueDesc) {
        if (refreshTask != null) {
            refreshTask.addProgressUnit(issueDesc);
        }
    }

    /* @Override
     public void setMode(QueryMode mode) {
     Filter filter;
     switch (mode) {
     case SHOW_ALL:
     filter = issueTable.getAllFilter();
     break;
     case SHOW_NEW_OR_CHANGED:
     filter = issueTable.getNewOrChangedFilter();
     break;
     default:
     throw new IllegalStateException("Unsupported mode " + mode);
     }
     selectFilter(filter);
     }*/
    @Override
    public boolean providesMode(QueryMode qm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JComponent getComponent(QueryMode qm) {
        return queryPanel;
    }

    @Override
    public void opened() {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void closed() {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean saveChanges(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean discardUnsavedChanges() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isChanged() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pl) {
        support.addPropertyChangeListener(pl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pl) {
        support.removePropertyChangeListener(pl);
    }
    private QueryProvider.IssueContainer<RedmineIssue> delegateContainer;

    void setIssueContainer(QueryProvider.IssueContainer<RedmineIssue> ic) {
        delegateContainer = ic;
    }

    private class QueryTask implements Runnable, Cancellable, QueryNotifyListener {

        private ProgressHandle handle;
        private RequestProcessor.Task task;
        private int counter;
        private boolean autoRefresh;
        private long progressMaxWorkunits;
        private int progressWorkunits;

        public QueryTask() {
            query.addNotifyListener(this);
        }

        private void startQuery() {
            handle = ProgressHandleFactory.createHandle(
                    Bundle.MSG_SearchingQuery(query.getDisplayName() != null
                            ? query.getDisplayName()
                            : repository.getDisplayName()),
                    this);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    enableFields(false);
                    queryPanel.showSearchingProgress(true, Bundle.MSG_Searching());
                }
            });
            handle.start();
        }

        private synchronized void finnishQuery() {
            task = null;
            if (handle != null) {
                handle.finish();
                handle = null;
            }
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    queryPanel.setQueryRunning(false);
                    queryPanel.setLastRefresh(getLastRefresh());
                    queryPanel.showNoContentPanel(false);
                    enableFields(true);
                }
            });
        }

        synchronized void switchToDeterminateProgress(long progressMaxWorkunits) {
            if (handle != null) {
                handle.switchToDeterminate((int) progressMaxWorkunits);
                this.progressMaxWorkunits = progressMaxWorkunits;
                this.progressWorkunits = 0;
            }
        }

        synchronized void addProgressUnit(String issueDesc) {
            if (handle != null && progressWorkunits < progressMaxWorkunits) {
                handle.progress(Bundle.LBL_RetrievingIssue(issueDesc), ++progressWorkunits);
            }
        }

        public void executeQuery() {
            setQueryRunning(true);
            try {
                query.refresh(autoRefresh);
            } finally {
                setQueryRunning(false); // XXX do we need this? its called in finishQuery anyway
                task = null;
            }

        }

        private void setQueryRunning(final boolean running) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    queryPanel.setQueryRunning(running);
                }
            });
        }

        @Override
        public void run() {
            startQuery();
            try {
                executeQuery();
            } finally {
                finnishQuery();
            }
        }

        RequestProcessor.Task post(boolean autoRefresh) {
            if (task != null) {
                task.cancel();
            }
            task = rp.create(this);
            this.autoRefresh = autoRefresh;
            task.schedule(0);
            return task;
        }

        @Override
        public boolean cancel() {
            if (task != null) {
                task.cancel();
                finnishQuery();
            }
            return true;
        }

        @Override
        public void notifyData(RedmineIssue issue) {
            issueTable.addNode(issue.getNode());
            if (!query.contains(issue.getID())) {
                // XXX this is quite ugly - the query notifies an archoived issue
                // but it doesn't "contain" it!
                return;
            }
            setIssueCount(++counter);
            if (counter == 1) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        queryPanel.showNoContentPanel(false);;
                    }
                });
            }
        }

        @Override
        public void started() {
            counter = 0;
            setIssueCount(counter);
        }

        @Override
        public void finished() {
        }
    }
}
