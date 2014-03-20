package com.kenai.redminenb.issue;

import com.kenai.redminenb.Redmine;
import com.kenai.redminenb.ui.Defaults;
import com.kenai.redminenb.util.RedmineUtil;

import com.taskadapter.redmineapi.bean.Issue;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
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
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.openide.awt.HtmlBrowser;
import org.openide.util.HelpCtx;
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

    public RedmineIssueController(RedmineIssue issue) {
        this.redmineIssue = issue;
        issuePanel = new RedmineIssuePanel(issue);
        //issuePane.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        initActions();
        initListeners();

        JScrollPane scrollPane = new JScrollPane(issuePanel);
        scrollPane.setBorder(null);
        //UIUtils.keepFocusedComponentVisible(scrollPane);

        JPanel pane = new JPanel(new BorderLayout());
        pane.setBorder(null);
        pane.setBackground(UIManager.getDefaults().getColor("EditorPane.background"));
//         if (!issue.isNew()) {
//            pane.add(issuePane.headPanel, BorderLayout.NORTH);
//         }
        pane.add(scrollPane, BorderLayout.CENTER);
        component = pane;
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

    /*@Override
     public boolean isValid() {
     return true; // PENDING
     }

     @Override
     public void applyChanges() {
     System.out.println("applyChanges " + this);
     }*/
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
        issuePanel.descTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                issuePanel.updateTextileOutput();
            }
        });
    }

    @NbBundle.Messages({
        "CTL_RefreshAction=Refresh",
        "CTL_ShowInBrowserAction=Show in Browser",
        "CTL_CreateSubTaskAction=Create Subtask",
        "CTL_ActionListAction.add=Add to Action Items",
        "CTL_ActionListAction.remove=Remove from Action Items"
    })
    private void initActions() {

        Action refreshAction = new AbstractAction(Bundle.CTL_RefreshAction(),
                Defaults.getIcon("refresh.png")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        redmineIssue.refresh();
                        issuePanel.initIssue();
                        issuePanel.setInfoMessage("Issue successfully reloaded.");
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

        //issuePanel.setDefaultPopupAction(showInBrowserAction);
        issuePanel.addToolbarAction(showInBrowserAction, false);
        issuePanel.addToolbarAction(createSubTaskAction, false);
        issuePanel.addToolbarAction(new ActionItemAction(), false);
        issuePanel.addToolbarAction(refreshAction, false);
    }
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    @Override
    public boolean saveChanges() {
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

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pl) {
        support.addPropertyChangeListener(pl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pl) {
        support.removePropertyChangeListener(pl);
    }

    private class ActionItemAction extends AbstractAction {

        private PropertyChangeListener actionItemListener;

        public ActionItemAction() {
            super("Action Item", Defaults.getIcon("hint.png"));
            putValue(Defaults.TOGGLE_BUTTON_KEY, Boolean.TRUE);
            updateTasklistButton();
        }

        private void setText(String txt) {
            putValue(Action.NAME, txt);
            putValue(Action.SHORT_DESCRIPTION, txt);
        }

        private void setSelected(boolean b) {
            putValue(Action.SELECTED_KEY, b);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            /*
             setEnabled(false);
             TaskListProvider
             RedmineTaskListProvider provider = RedmineTaskListProvider.getInstance();
             if (provider.isAdded(redmineIssue)) {
             provider.remove(redmineIssue);
             } else {
             attachTasklistListener(provider);
             provider.add(redmineIssue, true);
             }
             updateTasklistButton();
             */
        }
        /*
         private void attachTasklistListener(RedmineTaskListProvider provider) {
         if (actionItemListener == null) { // is not attached yet
         // listens on events comming from the tasklist, like when an issue is removed, etc.
         // needed to correctly update tasklistButton label and status
         actionItemListener = new PropertyChangeListener() {
         @Override
         public void propertyChange(PropertyChangeEvent evt) {
         if (RedmineTaskListProvider.PROPERTY_ISSUE_REMOVED.equals(evt.getPropertyName()) && redmineIssue.equals(evt.getOldValue())) {
         Runnable inAWT = new Runnable() {
         @Override
         public void run() {
         updateTasklistButton();
         }
         };
         if (EventQueue.isDispatchThread()) {
         inAWT.run();
         } else {
         EventQueue.invokeLater(inAWT);
         }
         }
         }
         };
         provider.addPropertyChangeListener(org.openide.util.WeakListeners.propertyChange(actionItemListener, provider));
         }
         }
         */

        private void updateTasklistButton() {
            /*
             setEnabled(false);
             RedmineIssuePanel.RP.post(new Runnable() {
             @Override
             public void run() {
             RedmineTaskListProvider provider = RedmineTaskListProvider.getInstance();
             if (provider == null || redmineIssue.isNew()) { // do not enable button for new issues
             return;
             }
             final boolean isInTasklist = provider.isAdded(redmineIssue);
             if (isInTasklist) {
             attachTasklistListener(provider);
             }
             EventQueue.invokeLater(new Runnable() {
             @Override
             public void run() {
             ActionItemAction.this.setText(isInTasklist
             ? Bundle.CTL_ActionListAction_remove()
             : Bundle.CTL_ActionListAction_add());
             setSelected(isInTasklist);
             setEnabled(true);
             }
             });
             }
             });
             */
        }
    }
}
