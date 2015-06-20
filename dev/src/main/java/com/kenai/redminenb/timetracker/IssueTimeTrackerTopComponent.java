/*
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
package com.kenai.redminenb.timetracker;

import com.kenai.redminenb.Redmine;
import com.kenai.redminenb.RedmineConnector;
import com.kenai.redminenb.issue.RedmineIssue;
import com.kenai.redminenb.repository.RedmineRepository;
import com.kenai.redminenb.util.TimeUtil;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntryActivity;
import com.taskadapter.redmineapi.bean.TimeEntryFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.kenai.redminenb//IssueTimeTracker//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = IssueTimeTrackerTopComponent.PREFERRED_ID,
        iconBase = "com/kenai/redminenb/resources/redmine.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "properties", openAtStartup = false)
@ActionID(category = "Window", id = "com.kenai.redminenb.timetracker.IssueTimeTrackerTopComponent")
@ActionReference(path = "Menu/Window/Tools", position = 1041, separatorBefore = 1040)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_IssueTimeTrackerAction",
        preferredID = "IssueTimeTrackerTopComponent"
)
@Messages({
    "CTL_IssueTimeTrackerAction=Redmine Issue TimeTracker",
    "CTL_IssueTimeTrackerTopComponent=Redmine Issue TimeTracker",
    "MSG_Time={0} hours",
    "MSG_Time_Running={0} hours (running)",
    "LBL_Hours=hours",
    "MSG_NoIssue=No issue selected",
    "MSG_Issue={0} - {1}",
    "MSG_Change_Running_Issue=Time tracking for ''{0}'' is running!",
    "TTL_Change_Running_Issue=Set issue for time tracking",
    "BTN_Change_Running_Issue_Cancel=Cancel",
    "BTN_Change_Running_Issue_Save=Save time",
    "BTN_Change_Running_Issue_Reset=Reset timer",
    "LBL_TimeTrackingActivity=Activity",
    "LBL_TimeTrackingComment=Comment",
    "LBL_Repository=Repository",
    "LBL_Issue=Issue",
    "LBL_Time=Time",
    "LBL_LogTime=Log time",
    "LBL_SaveFailed=Saving time entry failed",
    "BTN_StartTracking=Start",
    "BTN_StopTracking=Stop",})
public final class IssueTimeTrackerTopComponent extends TopComponent {

    private static final Logger LOG = Logger.getLogger(IssueTimeTrackerTopComponent.class.getName());
    @SuppressFBWarnings(value = "CI_CONFUSED_INHERITANCE", justification = "Needed to be usable from annotation")
    protected static final String PREFERRED_ID = "IssueTimeTrackerTopComponent";
    public static final String PROP_ISSUE = "issue";
    public static final String PROP_RUNNING = "running";

    private final Timer refreshTimer = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            refreshDisplay();
        }
    });
    private boolean running;
    private Date start;
    private long savedTime;
    private RedmineIssue issue;

    public static IssueTimeTrackerTopComponent getInstance() {
        return (IssueTimeTrackerTopComponent) WindowManager.getDefault().findTopComponent(PREFERRED_ID);
    }

    public IssueTimeTrackerTopComponent() {
        initComponents();
        setName(Bundle.CTL_IssueTimeTrackerTopComponent());
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_DND_COPY_DISABLED, Boolean.TRUE);
        refreshDisplay();
    }

    private long currentTime() {
        return savedTime + (start == null ? 0 : (new Date().getTime()
                - start.getTime()));
    }

    private void openIssue() {
        Redmine.getInstance().getSupport().openIssue(
                issue.getRepository(),
                issue);
    }

    private void refreshDisplay() {
        long timeInMS = currentTime();
        if (issue == null) {
            repositoryOutputLabel.setText(Bundle.MSG_NoIssue());
            issueOutputLabel.setText(Bundle.MSG_NoIssue());
            issueOutputLabel.setEnabled(false);
            timeOutputLabel.setText(Bundle.MSG_Time(TimeUtil.millisecondsToDecimalHours(0l)));
            saveButton.setEnabled(false);
            resetButton.setEnabled(false);
            startButton.setEnabled(false);
        } else {
            repositoryOutputLabel.setText(issue.getRepository().getDisplayName());
            issueOutputLabel.setText(Bundle.MSG_Issue(issue.getID(), issue.getSummary()));
            issueOutputLabel.setEnabled(true);
            startButton.setEnabled(true);
            if (running) {
                resetButton.setEnabled(false);
                saveButton.setEnabled(false);
                startButton.setText(Bundle.BTN_StopTracking());
                timeOutputLabel.setText(Bundle.MSG_Time_Running(TimeUtil.millisecondsToDecimalHours(timeInMS)));
            } else {
                startButton.setText(Bundle.BTN_StartTracking());
                if (savedTime > 0) {
                    saveButton.setEnabled(true);
                    resetButton.setEnabled(true);
                } else {
                    saveButton.setEnabled(false);
                    resetButton.setEnabled(false);
                }
                timeOutputLabel.setText(Bundle.MSG_Time(TimeUtil.millisecondsToDecimalHours(timeInMS)));
            }
        }
    }

    public boolean reset() {
        boolean result;
        if (!running) {
            savedTime = 0;
            result = true;
        } else {
            result = false;
        }
        refreshDisplay();
        return result;
    }

    public void save() {
        if (running) {
            return;
        }

        TimeEntryForm tef = new TimeEntryForm();
        tef.setIssue(issue);
        tef.setTime(savedTime);

        DialogDescriptor dd = new DialogDescriptor(tef, Bundle.LBL_LogTime());
        Object result = DialogDisplayer.getDefault().notify(dd);

        if (result == DialogDescriptor.OK_OPTION) {
            final TimeEntry te = TimeEntryFactory.create();
            TimeEntryActivity tea = tef.getTimeEntryActivity();
            te.setActivityId(tea.getId());
            te.setComment(tef.getComment());
            te.setHours(((float) tef.getTime()) / (60 * 60 * 1000));
            te.setIssueId(tef.getIssue().getIssue().getId());

            new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    issue.getRepository().getIssueManager().createTimeEntry(te);
                    issue.refresh();
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        savedTime = 0;
                    } catch (InterruptedException ex) {
                        Redmine.LOG.log(Level.SEVERE, Bundle.LBL_SaveFailed(), ex);
                    } catch (ExecutionException ex) {
                        Redmine.LOG.log(Level.SEVERE, Bundle.LBL_SaveFailed(), ex.getCause());
                    }
                }
            }.execute();
        }
    }

    public void setRunning(boolean running) {
        if (issue == null) {
            running = false;
        }
        boolean old = this.running;
        if (running != old) {
            this.running = running;
            if (running) {
                start = new Date();
            } else {
                savedTime = currentTime();
                start = null;
            }
            firePropertyChange(PROP_RUNNING, old, running);
            refreshDisplay();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public RedmineIssue getIssue() {
        return issue;
    }

    private boolean checkIssueChange(RedmineIssue oldIssue, RedmineIssue newIssue) {
        if (running || savedTime > 0) {
            String cancel = Bundle.BTN_Change_Running_Issue_Cancel();
            String save = Bundle.BTN_Change_Running_Issue_Save();
            String reset = Bundle.BTN_Change_Running_Issue_Reset();
            DialogDescriptor dd = new DialogDescriptor(
                    Bundle.MSG_Change_Running_Issue(oldIssue.getDisplayName()),
                    Bundle.TTL_Change_Running_Issue());
            dd.setOptions(new Object[]{cancel, reset, save});
            dd.setClosingOptions(new Object[]{cancel, reset, save});
            Object result = DialogDisplayer.getDefault().notify(dd);
            if (result.equals(reset) || result.equals(save)) {
                setRunning(false);
                if (result.equals(reset)) {
                    reset();
                } else {
                    save();
                }
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public void setIssue(RedmineIssue issue) {
        RedmineIssue old = this.issue;
        if (Objects.equals(old, issue)) {
            return;
        }
        if (checkIssueChange(old, issue)) {
            this.issue = issue;
            firePropertyChange(PROP_ISSUE, old, issue);
            refreshDisplay();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        issueLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        resetButton = new javax.swing.JButton();
        startButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        timeLabel = new javax.swing.JLabel();
        timeOutputLabel = new javax.swing.JLabel();
        repositoryLabel = new javax.swing.JLabel();
        repositoryOutputLabel = new javax.swing.JLabel();
        issueOutputLabel = new com.kenai.redminenb.util.LinkButton();

        setLayout(new java.awt.GridBagLayout());

        jPanel1.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        add(jPanel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(filler1, gridBagConstraints);

        issueLabel.setFont(issueLabel.getFont().deriveFont(issueLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(issueLabel, org.openide.util.NbBundle.getMessage(IssueTimeTrackerTopComponent.class, "IssueTimeTrackerTopComponent.issueLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        add(issueLabel, gridBagConstraints);

        jPanel2.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(resetButton, org.openide.util.NbBundle.getMessage(IssueTimeTrackerTopComponent.class, "IssueTimeTrackerTopComponent.resetButton.text")); // NOI18N
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });
        jPanel2.add(resetButton);

        org.openide.awt.Mnemonics.setLocalizedText(startButton, org.openide.util.NbBundle.getMessage(IssueTimeTrackerTopComponent.class, "IssueTimeTrackerTopComponent.startButton.text")); // NOI18N
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });
        jPanel2.add(startButton);

        org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(IssueTimeTrackerTopComponent.class, "IssueTimeTrackerTopComponent.saveButton.text")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        jPanel2.add(saveButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        add(jPanel2, gridBagConstraints);

        timeLabel.setFont(timeLabel.getFont().deriveFont(timeLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(timeLabel, org.openide.util.NbBundle.getMessage(IssueTimeTrackerTopComponent.class, "IssueTimeTrackerTopComponent.timeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        add(timeLabel, gridBagConstraints);

        timeOutputLabel.setFont(timeOutputLabel.getFont().deriveFont(timeOutputLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(timeOutputLabel, org.openide.util.NbBundle.getMessage(IssueTimeTrackerTopComponent.class, "IssueTimeTrackerTopComponent.timeOutputLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        add(timeOutputLabel, gridBagConstraints);

        repositoryLabel.setFont(repositoryLabel.getFont().deriveFont(repositoryLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(repositoryLabel, org.openide.util.NbBundle.getMessage(IssueTimeTrackerTopComponent.class, "IssueTimeTrackerTopComponent.repositoryLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        add(repositoryLabel, gridBagConstraints);

        repositoryOutputLabel.setFont(repositoryOutputLabel.getFont().deriveFont(repositoryOutputLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(repositoryOutputLabel, org.openide.util.NbBundle.getMessage(IssueTimeTrackerTopComponent.class, "IssueTimeTrackerTopComponent.repositoryOutputLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        add(repositoryOutputLabel, gridBagConstraints);

        issueOutputLabel.setBorder(null);
        org.openide.awt.Mnemonics.setLocalizedText(issueOutputLabel, org.openide.util.NbBundle.getMessage(IssueTimeTrackerTopComponent.class, "IssueTimeTrackerTopComponent.issueOutputLabel.text")); // NOI18N
        issueOutputLabel.setEnabled(false);
        issueOutputLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                issueOutputLabelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        add(issueOutputLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        this.setRunning(!this.isRunning());
    }//GEN-LAST:event_startButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        this.reset();
    }//GEN-LAST:event_resetButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        save();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void issueOutputLabelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_issueOutputLabelActionPerformed
        openIssue();
    }//GEN-LAST:event_issueOutputLabelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel issueLabel;
    private com.kenai.redminenb.util.LinkButton issueOutputLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel repositoryLabel;
    private javax.swing.JLabel repositoryOutputLabel;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton startButton;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JLabel timeOutputLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    protected void componentHidden() {
        refreshTimer.stop();
    }

    @Override
    protected void componentShowing() {
        refreshTimer.start();
    }

    void writeProperties(java.util.Properties p) {
        p.setProperty("version", "1.0");
        if (issue != null) {
            long currentTime = currentTime();
            p.setProperty("connector", RedmineConnector.ID);
            p.setProperty("repository", issue.getRepository().getID());
            p.setProperty("issue", issue.getID());
            if (currentTime > 0) {
                p.setProperty("time", Long.toString(currentTime));
            }
        }
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        if (!"1.0".equals(version)) {
            LOG.warning("Unknown version for TopComponent properties");
        }
        String connector = p.getProperty("connector");
        String repository = p.getProperty("repository");
        String issueId = p.getProperty("issue");
        if (connector != null && repository != null && issueId != null) {
            // Make sure repository is initialized via the issue API, no ...
            // this is a crude hack:
            // @todo: Find a better way to access "out" implementation
            RepositoryManager.getInstance().getRepository(connector, repository);
            RedmineRepository rr = RedmineRepository.getInstanceyById(repository);
            if (rr != null) {
                RedmineIssue ri = rr.getIssue(issueId);
                if (ri != null) {
                    this.setIssue(ri);
                }
                String time = p.getProperty("time", "0");
                try {
                    this.savedTime = Long.parseLong(time);
                } catch (NumberFormatException ex) {
                }
            }
        }
        refreshDisplay();
    }
}
