package com.kenai.redminenb.issue;

import com.kenai.redminenb.Redmine;
import com.kenai.redminenb.timetracker.IssueTimeTrackerTopComponent;
import com.kenai.redminenb.ui.Defaults;
import com.kenai.redminenb.util.RedmineUtil;

import com.taskadapter.redmineapi.bean.Issue;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.openide.awt.HtmlBrowser;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
public class RedmineIssueController implements IssueController {

    private final RedmineIssue redmineIssue;
    private final JComponent component;
    private final RedmineIssuePanel issuePanel;
    private Action viewWatchers;

    public RedmineIssueController(RedmineIssue issue) {
        this.redmineIssue = issue;
        issuePanel = new RedmineIssuePanel(issue);

        initActions();
        initListeners();

        JScrollPane scrollPane = new JScrollPane(issuePanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        JPanel pane = new JPanel(new BorderLayout());
        pane.setBorder(null);
        pane.setBackground(UIManager.getDefaults().getColor("EditorPane.background"));

        pane.add(scrollPane, BorderLayout.CENTER);        
        component = pane;
        
        redmineIssue.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if("busy".equals(evt.getPropertyName())) {
                    issuePanel.enableFields(! ((boolean)evt.getNewValue()));
                } else if (org.netbeans.modules.bugtracking.api.Issue.EVENT_ISSUE_DATA_CHANGED.equals(evt.getPropertyName())) {
                    issuePanel.customFieldValueBackingStore.clear();
                }
            }
        });
    }

    @Override
    public JComponent getComponent() {
        return component;
    }

    @Override
    public void opened() {
        if (redmineIssue != null) {
            issuePanel.opened();
            redmineIssue.opened();

            redmineIssue.getRepository().getRequestProcessor().execute(new Runnable() {

                @Override
                public void run() {
                    redmineIssue.refresh();
                    issuePanel.initIssue(null);
                }
            });
            viewWatchers.setEnabled(redmineIssue.getRepository().isFeatureWatchers());
        }
    }

    @Override
    public void closed() {
        if (redmineIssue != null) {
            redmineIssue.closed();
            issuePanel.closed();
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass().getName());
    }

    private RedmineIssue createSubTask() {
        Issue issue = redmineIssue.getIssue();
        // TODO: check if issue is available (id != 0)

        Issue subIssue = new Issue();
        subIssue.setParentId(issue.getId());
        subIssue.setAssignee(issue.getAssignee());
        //subIssue.setAuthor(issue.getAuthor());
        subIssue.setAuthor(redmineIssue.getRepository().getCurrentUser().getUser());
        subIssue.setCategory(issue.getCategory());
        subIssue.setPriorityId(issue.getPriorityId());

        subIssue.setTargetVersion(issue.getTargetVersion());
        subIssue.setTracker(issue.getTracker());
        subIssue.setStatusId(issue.getStatusId());
        subIssue.setDoneRatio(0);
        subIssue.setSubject("New Subtask");
        return new RedmineIssue(redmineIssue.getRepository(), subIssue);
    }

    private void initListeners() {
        abstract class DelegateChangeHandler implements DocumentListener {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }            
        };
        
        issuePanel.descTextArea.getDocument().addDocumentListener(new DelegateChangeHandler() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                issuePanel.updateTextileOutput();
            }
        });
        
        issuePanel.updateCommentTextArea.getDocument().addDocumentListener(new DelegateChangeHandler() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                issuePanel.updateCommentTextileOutput();
            }
        });
    }

    @NbBundle.Messages({
        "CTL_RefreshAction=Refresh",
        "CTL_ShowInBrowserAction=Show in Browser",
        "CTL_CreateSubTaskAction=Create Subtask",
        "CTL_ActionListAction.add=Add to Action Items",
        "CTL_ActionListAction.remove=Remove from Action Items",
        "CTL_OpenIssueForTimeTracking=Open Timetracker with Issue",
        "CTL_OpenWatchersList=Open Watchers List"
    })
    private void initActions() {
        Action timeTrackingAction = new AbstractAction(Bundle.CTL_OpenIssueForTimeTracking(),
                Defaults.getIcon("appointment-new.png")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                IssueTimeTrackerTopComponent.getInstance().open();
                IssueTimeTrackerTopComponent.getInstance().setIssue(redmineIssue);
            }
        };
        
        Action refreshAction = new AbstractAction(Bundle.CTL_RefreshAction(),
                Defaults.getIcon("refresh.png")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new SwingWorker() {

                            @Override
                            protected Object doInBackground() throws Exception {
                                redmineIssue.refresh();
                                issuePanel.initIssue(null);
                                return null;
                            }

                            @Override
                            protected void done() {
                                issuePanel.setInfoMessage("Issue successfully reloaded.");
                            }

                        }.execute();
                    }
                };

        Action showInBrowserAction = new AbstractAction(Bundle.CTL_ShowInBrowserAction(),
                Defaults.getIcon("redmine.png")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            URL url = new URL(redmineIssue.getRepository().getUrl() + "/issues/" + redmineIssue.getID()); // NOI18N
                            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                        } catch (IOException ex) {
                            Redmine.LOG.log(Level.INFO, "Unable to show the issue in the browser.", ex); // NOI18N
                        }
                    }
                };

        Action createSubTaskAction = new AbstractAction(Bundle.CTL_CreateSubTaskAction(),
                Defaults.getIcon("subtask.png")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        RedmineIssue subTask = createSubTask();
                        RedmineUtil.openIssue(subTask);
                    }
                };
        viewWatchers = new AbstractAction(Bundle.CTL_OpenWatchersList(),
                Defaults.getIcon("face-glasses.png")) {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        WatcherEditor we = new WatcherEditor(redmineIssue);
                        we.run();
                    }
                };

        viewWatchers.setEnabled(redmineIssue.getRepository().isFeatureWatchers());
        issuePanel.addToolbarAction(viewWatchers, false);
        issuePanel.addToolbarAction(timeTrackingAction, false);
        issuePanel.addToolbarAction(showInBrowserAction, false);
        issuePanel.addToolbarAction(createSubTaskAction, false);
        issuePanel.addToolbarAction(refreshAction, false);
    }
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    @Override
    public boolean saveChanges() {
        // @todo: Clean this up - the logic of issue handling has to be moved
        // from the panel into the controller...
        Mutex.EVENT.writeAccess(new Runnable() {
            @Override
            public void run() {
                issuePanel.saveIssue();
            }
        });
        return true;
    }

    @Override
    public boolean discardUnsavedChanges() {
        if(issuePanel != null) {
            redmineIssue.getRepository().getRequestProcessor().execute(new Runnable() {
                public void run() {
                    issuePanel.initIssue(null);
                }
            });
        }
        return true;
    }

    @Override
    public boolean isChanged() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pl) {
        support.addPropertyChangeListener(pl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pl) {
        support.removePropertyChangeListener(pl);
    }
}
