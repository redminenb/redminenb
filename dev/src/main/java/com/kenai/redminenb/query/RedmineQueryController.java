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
import com.kenai.redminenb.issue.RedmineIssue;
import com.kenai.redminenb.query.RedmineQueryParameter.CheckBoxParameter;
import com.kenai.redminenb.query.RedmineQueryParameter.ListParameter;
import com.kenai.redminenb.query.RedmineQueryParameter.TextFieldParameter;
import com.kenai.redminenb.repository.RedmineRepository;
import com.kenai.redminenb.timetracker.IssueTimeTrackerTopComponent;
import com.kenai.redminenb.user.RedmineUser;
import com.kenai.redminenb.util.RedmineUtil;
import com.kenai.redminenb.util.TableCellRendererCategory;
import com.kenai.redminenb.util.TableCellRendererPriority;
import com.kenai.redminenb.util.TableCellRendererTracker;
import com.kenai.redminenb.util.TableCellRendererUser;
import com.kenai.redminenb.util.TableCellRendererVersion;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssuePriority;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.Version;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import org.apache.commons.lang.StringUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.spi.QueryController;
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
    "LBL_SelectKeywords=Select or deselect keywords.",
    "MNU_OpenIssue=Open Issue",
    "MNU_OpenIssueForTimeTracking=Open Timetracker with Issue"
})
public class RedmineQueryController implements QueryController, ActionListener {
    private static final Logger LOG = Logger.getLogger(RedmineQueryController.class.getName());
    
    private RedmineQueryPanel queryPanel;
    private final QueryListModel queryListModel = new QueryListModel();
    private JTable issueTable;
    //
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // NOI18N
    private final RedmineRepository repository;
    //
    private final RedmineQuery query;
    //
    private ListParameter versionParameter;
    private ListParameter trackerParameter;
    private ListParameter statusParameter;
    private ListParameter categoryParameter;
    private ListParameter priorityParameter;
    private ListParameter assigneeParameter;
    private ListParameter watcherParameter;
    private Map<String, RedmineQueryParameter> parameters;
    //
    private final Object REFRESH_LOCK = new Object();
    private QueryTask refreshTask;

    public RedmineQueryController(RedmineRepository repository, RedmineQuery query) {
        this.repository = repository;
        this.query = query;
    }

    private void setListeners() {
        queryPanel.searchButton.addActionListener(this);
        queryPanel.refreshCheckBox.addActionListener(this);
        queryPanel.saveChangesButton.addActionListener(this);
        queryPanel.cancelChangesButton.addActionListener(this);
        queryPanel.gotoIssueButton.addActionListener(this);
        queryPanel.webButton.addActionListener(this);
        queryPanel.saveButton.addActionListener(this);
        queryPanel.refreshButton.addActionListener(this);
        queryPanel.modifyButton.addActionListener(this);
        queryPanel.removeButton.addActionListener(this);
        queryPanel.refreshConfigurationButton.addActionListener(this);
        queryPanel.issueIdTextField.addActionListener(this);
        queryPanel.queryTextField.addActionListener(this);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    private void modelToGUI() {
        for(Entry<String,ParameterValue[]> e: query.getParameters().entrySet()) {
            if(parameters.containsKey(e.getKey())) {
                parameters.get(e.getKey()).setValues(e.getValue());
            }
        }
        queryPanel.setTitle(query.getDisplayName());
        queryPanel.cancelChangesButton.setVisible(query.getDisplayName() != null);
        queryPanel.setLastRefresh(getLastRefresh());
    }
    
    private void guiToModel() {
        Map<String, ParameterValue[]> parameters = new HashMap<>();
        for(RedmineQueryParameter rqp: this.parameters.values()) {
            parameters.put(rqp.getParameter(), rqp.getValues());
        }
        query.setParameters(parameters);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == queryPanel.searchButton) {
            guiToModel();
            refresh();
        } else if (e.getSource() == queryPanel.gotoIssueButton) {
            onGotoIssue();
        } else if (e.getSource() == queryPanel.saveChangesButton) {
            guiToModel();
            onSave(true); // refresh
        } else if (e.getSource() == queryPanel.cancelChangesButton) {
            discardUnsavedChanges();
        } else if (e.getSource() == queryPanel.webButton) {
            onWeb();
        } else if (e.getSource() == queryPanel.saveButton) {
            guiToModel();
            onSave(false); // do not refresh
        } else if (e.getSource() == queryPanel.refreshButton) {
            refresh();
        } else if (e.getSource() == queryPanel.modifyButton) {
            onModify();
        } else if (e.getSource() == queryPanel.removeButton) {
            onRemove();
        } else if (e.getSource() == queryPanel.refreshCheckBox) {
            onAutoRefresh();
        } else if (e.getSource() == queryPanel.refreshConfigurationButton) {
            refreshConfiguration();
        } else if (e.getSource() == queryPanel.issueIdTextField) {
            if (!queryPanel.issueIdTextField.getText().trim().equals("")) {                // NOI18N
                onGotoIssue();
            }
        } else if (e.getSource() == queryPanel.issueIdTextField
                || e.getSource() == queryPanel.queryTextField) {
            refresh();
        }
    }

    /////////////////////////////////////////////////////////////////////////////

    private void onSave(final boolean refresh) {
        query.getRepository().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                Redmine.LOG.fine("on save start");
                String name = query.getDisplayName();
                if (query.getDisplayName() == null || query.getDisplayName().isEmpty()) {
                    name = getSaveName();
                    if (name == null) {
                        return;
                    }
                }
                assert name != null;
                saveChanges(name);
                Redmine.LOG.fine("on save finnish");

                if (refresh) {
                    refresh();
                }
            }
        });
    }

    private String getSaveName() {
        NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                "Name", "Save query");
        DialogDisplayer.getDefault().notify(nd);
        if(nd.getValue() == NotifyDescriptor.OK_OPTION) {
            return nd.getInputText();
        } else {
            return null;
        }
    }

    private void setAsSaved(boolean showModify) {
        queryPanel.setModifyVisible(showModify);
        queryPanel.cancelChangesButton.setVisible(! showModify);
        queryPanel.refreshCheckBox.setVisible(! showModify);
    }

    private String getLastRefresh() throws MissingResourceException {
        long l = query.getLastRefresh();
        return l > 0
                ? dateFormat.format(new Date(l))
                : Bundle.LBL_Never();
    }

    private void onGotoIssue() {
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
        t[0] = query.getRepository().getRequestProcessor().create(new Runnable() {
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

    private void onWeb() {
        String params = null; //query.getUrlParameters();
        String repoURL = repository.getUrl();
        final String urlString = repoURL + (StringUtils.isNotBlank(params) ? params : ""); // NOI18N

        query.getRepository().getRequestProcessor().post(new Runnable() {
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

    public void autoRefresh() {
        refresh(true);
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

    public void refresh() {
        refresh(false);
    }

    private void refresh(final boolean auto) {
        RequestProcessor.Task t;
        synchronized (REFRESH_LOCK) {
            if (refreshTask == null) {
                refreshTask = new QueryTask();
            } else {
                refreshTask.cancel();
            }
            t = refreshTask.post(auto);
        }
    }

    private void onModify() {
        queryPanel.setModifyVisible(true);
    }

    private void onRemove() {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(Bundle.MSG_RemoveQuery(query.getDisplayName()),
                Bundle.CTL_RemoveQuery(),
                NotifyDescriptor.OK_CANCEL_OPTION);
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            query.getRepository().getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    remove();
                }
            });
        }
    }

    protected void scheduleForRefresh() {
        if (query.isSaved()) {
            repository.scheduleForRefresh(query);
        }
    }

    protected void logAutoRefreshEvent(boolean autoRefresh) {
        LOG.fine(String.format("AutoRefresh '%s-%s', Autorefresh: %b",
                RedmineConnector.NAME,
                query.getDisplayName(),
                autoRefresh
                ));
    }

    private void refreshConfiguration() {
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

        t[0] = query.getRepository().getRequestProcessor().post(new Runnable() {
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
                            modelToGUI();
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
        pvList = new ArrayList<>();
        for (Tracker t : repository.getTrackers()) {
            pvList.add(new ParameterValue(t.getName(), t.getId()));
        }
        trackerParameter.setParameterValues(pvList);

        // Status
        pvList = new ArrayList<>();
        for (IssueStatus s : repository.getStatuses()) {
            pvList.add(new ParameterValue(s.getName(), s.getId()));
        }
        statusParameter.setParameterValues(pvList);

        // Issue Priority
        pvList = new ArrayList<>();
        for (IssuePriority ip : repository.getIssuePriorities()) {
            pvList.add(new ParameterValue(ip.getName(), ip.getId()));
        }
        priorityParameter.setParameterValues(pvList);

        // Assignee (assigned to)
        pvList = new ArrayList<>();
        pvList.add(ParameterValue.NONE_PARAMETERVALUE);
        for (RedmineUser redmineUser : repository.getUsers()) {
            pvList.add(new ParameterValue(redmineUser.getUser().getFullName(), redmineUser.getId()));
        }
        assigneeParameter.setParameterValues(pvList);

        // Category
        pvList = new ArrayList<>();
        pvList.add(ParameterValue.NONE_PARAMETERVALUE);
        for (IssueCategory c : repository.getIssueCategories()) {
            pvList.add(new ParameterValue(c.getName(), c.getId()));
        }
        categoryParameter.setParameterValues(pvList);

        // Target Version
        pvList = new ArrayList<>();
        pvList.add(ParameterValue.NONE_PARAMETERVALUE);
        for (Version v : repository.getVersions()) {
            pvList.add(new ParameterValue(v.getName(), v.getId()));
        }
        versionParameter.setParameterValues(pvList);
        
        // Watchers
        pvList = new ArrayList<>();
        for (RedmineUser redmineUser : repository.getUsers()) {
            pvList.add(new ParameterValue(redmineUser.getUser().getFullName(), redmineUser.getId()));
        }
        watcherParameter.setParameterValues(pvList);
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

    @Override
    public boolean providesMode(QueryMode qm) {
        return qm == QueryMode.EDIT || qm == QueryMode.VIEW;
    }

    @Override
    public JComponent getComponent(QueryMode qm) {
        if (queryPanel == null) {

            DefaultTableColumnModel tcm = new DefaultTableColumnModel();

            TableColumn tce;

            tce = new TableColumn(0);
            tce.setHeaderValue("ID");
            tce.setMinWidth(0);
            tce.setPreferredWidth(40);
            tce.setMaxWidth(40);
            tcm.addColumn(tce);

            tce = new TableColumn(1);
            tce.setHeaderValue("Summary");
            tce.setPreferredWidth(250);
            tcm.addColumn(tce);
            
            tce = new TableColumn(2);
            tce.setHeaderValue("Tracker");
            tce.setCellRenderer(new TableCellRendererTracker());
            tce.setMinWidth(0);
            tce.setPreferredWidth(80);
            tce.setMaxWidth(80);
            tcm.addColumn(tce);

            tce = new TableColumn(3);
            tce.setHeaderValue("Priority");
            tce.setCellRenderer(new TableCellRendererPriority());
            tce.setMinWidth(0);
            tce.setPreferredWidth(80);
            tce.setMaxWidth(80);
            tcm.addColumn(tce);

            tce = new TableColumn(4);
            tce.setHeaderValue("Status");
            tce.setMinWidth(0);
            tce.setPreferredWidth(80);
            tce.setMaxWidth(80);
            tcm.addColumn(tce);

            tce = new TableColumn(5);
            tce.setHeaderValue("Assigned to");
            tce.setCellRenderer(new TableCellRendererUser());
            tce.setMinWidth(0);
            tce.setPreferredWidth(80);
            tce.setMaxWidth(80);
            tcm.addColumn(tce);

            tce = new TableColumn(6);
            tce.setHeaderValue("Category");
            tce.setCellRenderer(new TableCellRendererCategory());
            tce.setMinWidth(0);
            tce.setPreferredWidth(80);
            tce.setMaxWidth(80);
            tcm.addColumn(tce);

            tce = new TableColumn(7);
            tce.setHeaderValue("Version");
            tce.setCellRenderer(new TableCellRendererVersion());
            tce.setMinWidth(0);
            tce.setPreferredWidth(80);
            tce.setMaxWidth(80);
            tcm.addColumn(tce);

            issueTable = new JTable();
            issueTable.setAutoCreateRowSorter(true);
            issueTable.setModel(queryListModel);
            issueTable.setColumnModel(tcm);
            issueTable.getRowSorter().setSortKeys(Collections.singletonList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
            issueTable.getTableHeader().setReorderingAllowed(false);
            issueTable.doLayout();
            issueTable.addMouseListener(issueTableIssueOpener);
            issueTable.addKeyListener(issueTableIssueOpener);

            queryPanel = new RedmineQueryPanel(new JScrollPane(issueTable), this);
            parameters = new LinkedHashMap<>();
            // set parameters
            trackerParameter = registerQueryParameter(ListParameter.class, queryPanel.trackerList, "tracker_id");
            categoryParameter = registerQueryParameter(ListParameter.class, queryPanel.categoryList, "category_id");
            versionParameter = registerQueryParameter(ListParameter.class, queryPanel.versionList, "fixed_version_id");
            statusParameter = registerQueryParameter(ListParameter.class, queryPanel.statusList, "status_id");
            priorityParameter = registerQueryParameter(ListParameter.class, queryPanel.priorityList, "priority_id");
            assigneeParameter = registerQueryParameter(ListParameter.class, queryPanel.assigneeList, "assigned_to_id");
            watcherParameter = registerQueryParameter(ListParameter.class, queryPanel.watcherList, "watcher_id");
            
            registerQueryParameter(TextFieldParameter.class, queryPanel.queryTextField, "query");
            registerQueryParameter(CheckBoxParameter.class, queryPanel.qSubjectCheckBox, "is_subject");
            registerQueryParameter(CheckBoxParameter.class, queryPanel.qDescriptionCheckBox, "is_description");

            setListeners();
            postPopulate();
        }
        if(qm == QueryMode.VIEW) {
            setAsSaved(false);
        } else {
            setAsSaved(true);
        }
        return queryPanel;
    }

    @Override
    public void opened() {
        modelToGUI();
    }

    @Override
    public void closed() {
    }

    @Override
    public boolean saveChanges(String name) {
        Redmine.LOG.log(Level.FINE, "saving query '{0}'", new Object[]{name});
        query.setName(name);
        repository.saveQuery(query);
        query.setSaved(true); // XXX
        setAsSaved(false);
        if (!query.wasRun()) {
            Redmine.LOG.log(Level.FINE, "refreshing query '{0}' after save", new Object[]{name});
            refresh();
        }
        Redmine.LOG.log(Level.FINE, "query '{0}' saved", new Object[]{name});
        return true;
    }

    @Override
    public boolean discardUnsavedChanges() {
        RedmineConfig.getInstance().reloadQuery(query);
        modelToGUI();
        setAsSaved(false);
        return true;
    }

    @Override
    public boolean isChanged() {
        return query.isSaved();
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

    private class QueryTask implements Runnable, Cancellable, QueryNotifyListener {
        private RequestProcessor.Task task;
        private int counter;
        private boolean autoRefresh;

        public QueryTask() {
            query.addNotifyListener(this);
        }

        private void startQuery() {
            if (queryPanel != null) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        enableFields(false);
                        queryPanel.showSearchingProgress(true, Bundle.MSG_Searching());
                    }
                });
            }
        }

        private synchronized void finnishQuery() {
            task = null;
            if (queryPanel != null) {
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
            if (queryPanel != null) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        queryPanel.setQueryRunning(running);
                    }
                });
            }
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
            task = query.getRepository().getRequestProcessor().create(this);
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
            counter++;
            if (queryPanel != null) {
                setIssueCount(counter);
                if (counter == 1) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            queryPanel.showNoContentPanel(false);
                        }
                    });
                }
            }
        }

        @Override
        public void started() {
            counter = 0;
            if (queryPanel != null) {
                setIssueCount(counter);
            }
        }

        @Override
        public void finished() {
            queryListModel.setIssues(query.getIssues());
        }
    }

    private class IssueTableIssueOpener implements MouseListener, KeyListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            int mouseRow = issueTable.rowAtPoint(e.getPoint());
            if((mouseRow != -1) && (! issueTable.isRowSelected(mouseRow))) {
                issueTable.setRowSelectionInterval(mouseRow, mouseRow);
            }
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                int viewRow = issueTable.getSelectedRow();
                if (viewRow == -1) {
                    return;
                }
                int modelRow = issueTable.convertRowIndexToModel(viewRow);
                RedmineIssue mi = queryListModel.getIssue(modelRow);
                Redmine.getInstance().getSupport().openIssue(
                        mi.getRepository(),
                        mi);
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                final RedmineIssue issue;
                int viewRow = issueTable.getSelectedRow();
                if (viewRow != -1) {
                    int modelRow = issueTable.convertRowIndexToModel(viewRow);
                    issue = queryListModel.getIssue(modelRow);
                } else {
                    issue = null;
                }
                
                JPopupMenu menu = new JPopupMenu();
                JMenuItem openItem = new JMenuItem(Bundle.MNU_OpenIssue());
                openItem.setEnabled(issue != null);
                openItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Redmine.getInstance().getSupport().openIssue(
                                issue.getRepository(),
                                issue);
                    }
                });
                JMenuItem trackItem = new JMenuItem(Bundle.MNU_OpenIssueForTimeTracking());
                trackItem.setEnabled(issue != null);
                trackItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        IssueTimeTrackerTopComponent.getInstance().open();
                        IssueTimeTrackerTopComponent.getInstance().setIssue(issue);
                    }
                });
                menu.add(openItem);
                menu.add(trackItem);
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                e.consume();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                int viewRow = issueTable.getSelectedRow();
                if (viewRow == -1) {
                    return;
                }
                int modelRow = issueTable.convertRowIndexToModel(viewRow);
                RedmineIssue mi = queryListModel.getIssue(modelRow);
                Redmine.getInstance().getSupport().openIssue(
                        mi.getRepository(),
                        mi);
                e.consume();
            }
        }
    }

    IssueTableIssueOpener issueTableIssueOpener = new IssueTableIssueOpener();
}
