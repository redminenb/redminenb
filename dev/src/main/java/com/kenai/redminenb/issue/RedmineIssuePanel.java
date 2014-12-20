package com.kenai.redminenb.issue;

import com.kenai.redminenb.Redmine;
import com.kenai.redminenb.ui.Defaults;
import com.kenai.redminenb.user.RedmineUser;
import com.kenai.redminenb.util.ListComboBoxModel;
import com.kenai.redminenb.util.RedmineUtil;

import com.kenai.redminenb.repository.RedmineRepository;
import com.kenai.redminenb.util.AttachmentDisplay;
import com.kenai.redminenb.util.BusyPanel;
import com.kenai.redminenb.util.ExpandablePanel;
import com.kenai.redminenb.util.FullSizeLayout;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssuePriority;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.Version;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle;
import org.apache.commons.lang.StringUtils;
import com.kenai.redminenb.util.LinkButton;
import com.kenai.redminenb.util.NestedProject;
import com.kenai.redminenb.util.VerticalScrollPane;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueCategoryFactory;
import com.taskadapter.redmineapi.bean.Journal;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.ProjectFactory;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntryActivity;
import com.taskadapter.redmineapi.bean.TimeEntryFactory;
import com.taskadapter.redmineapi.bean.TrackerFactory;
import com.taskadapter.redmineapi.bean.UserFactory;
import com.taskadapter.redmineapi.bean.VersionFactory;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.swing.Box.Filler;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.DropDownButtonFactory;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Panel showing a Redmine Issue.
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
@NbBundle.Messages({
    "BTN_AddAttachment=Add attachment"
})
public class RedmineIssuePanel extends JPanel {
   private static final Logger LOG = Logger.getLogger(RedmineIssuePanel.class.getName());
   private static final long serialVersionUID = 9011030935877495476L;
   private static File lastDirectory;
   //
   private final RedmineIssue redmineIssue;
   //
   private JButton toolbarPopupButton;
   private JPopupMenu toolbarPopup;
   private final ExpandablePanel commentPanel;
   private final ExpandablePanel logtimePanel;
   JComponent waitPanel = new BusyPanel();
   
   public RedmineIssuePanel(RedmineIssue redmineIssue) {
      this.redmineIssue = redmineIssue;
      initComponents();
      projectComboBox.addItemListener(new ItemListener() {
          @Override
          public void itemStateChanged(ItemEvent e) {
              initProjectData();
          }
      });
      updateCommentTabPanel.setVisible(false);
      commentPanel = new ExpandablePanel(updateCommentLabel, updateCommentTabPanel);
      logtimeInputPanel.setVisible(false);
      logtimePanel = new ExpandablePanel(logtimeLabel, logtimeInputPanel);
      initValues();
      init();
      initIssue();
      layerPane.setLayout(new FullSizeLayout());
      layerPane.add(waitPanel, JLayeredPane.MODAL_LAYER);
    }
 
   private void init() {
      headerLabel.setFont(headerLabel.getFont().deriveFont(headerLabel.getFont().getSize() * 1.5f));
      parentHeaderPanel.setOpaque(false);
      buttonPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));

      ((JLabel)doneComboBox.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);

      toolbar.add(Box.createHorizontalGlue());

      privateCheckBox.setVisible(false);
   }

   void updateCommentTextileOutput() {
       updateCommentHtmlOutputLabel.setTextileText(updateCommentTextArea.getText());
   }
   
   void updateTextileOutput() {
        htmlOutputLabel.setTextileText(descTextArea.getText());
   }

   void addToolbarPopupButton() {
      toolbarPopup = new JPopupMenu();
      toolbarPopupButton = DropDownButtonFactory.createDropDownButton(Defaults.getIcon("redmine.png"), toolbarPopup);
      toolbar.add(toolbarPopupButton);
   }

   void addToolbarAction(Action a, boolean addToPopup) {
      if (a.getValue(Action.SHORT_DESCRIPTION) == null) {
         a.putValue(Action.SHORT_DESCRIPTION, a.getValue(Action.NAME));
      }
      if (addToPopup) {
         if (toolbarPopup == null) {
            addToolbarPopupButton();
         }
         toolbarPopup.add(a);
      } else {
         if (Boolean.TRUE.equals(a.getValue(Defaults.TOGGLE_BUTTON_KEY))) {
            JToggleButton toggleButton = new JToggleButton(a);
            toggleButton.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
            toolbar.add(toggleButton);
         } else {
            toolbar.add(a);
         }
      }
   }

   void setDefaultPopupAction(Action a) {
      if (toolbarPopupButton == null) {
         addToolbarPopupButton();
      }
      toolbarPopupButton.addActionListener(a);
   }

   final void initIssue() {
      assert SwingUtilities.isEventDispatchThread() : "Need to be on EDT when updating components";
      com.taskadapter.redmineapi.bean.Issue issue = this.redmineIssue.getIssue();

      headPane.setVisible(!redmineIssue.isNew() || issue != null);
      parentHeaderPanel.setVisible(!redmineIssue.isNew());
      //headerLabel.setVisible(!redmineIssue.isNew());
      headerLabel.setVisible(issue != null);
      createdLabel.setVisible(!redmineIssue.isNew());
      createdValueLabel.setVisible(!redmineIssue.isNew());
      updatedLabel.setVisible(!redmineIssue.isNew());
      updatedValueLabel.setVisible(!redmineIssue.isNew());

      createButton.setVisible(redmineIssue.isNew());
      updateButton.setVisible(!redmineIssue.isNew());
      toolbar.setVisible(!redmineIssue.isNew());

      if (issue != null) {
         Dimension dim = headerLabel.getPreferredSize();
         headerLabel.setMinimumSize(new Dimension(0, dim.height));
         headerLabel.setPreferredSize(new Dimension(0, dim.height));
         headerLabel.setText(redmineIssue.getDisplayName());

         if (issue.getCreatedOn() != null) {
            createdValueLabel.setText(RedmineIssue.DATETIME_FORMAT.format(issue.getCreatedOn()) + " by " + issue.getAuthor().getFullName());
         }
         if (issue.getUpdatedOn() == null) {
            updatedLabel.setVisible(false);
            updatedValueLabel.setVisible(false);
            updatedValueLabel.setText(null);
         } else {
            updatedLabel.setVisible(true);
            updatedValueLabel.setVisible(true);
            updatedValueLabel.setText(RedmineIssue.DATETIME_FORMAT.format(issue.getUpdatedOn()));
         }

         subjectTextField.setText(issue.getSubject());
         parentTaskTextField.setValue(issue.getParentId());
         descTextArea.setText(issue.getDescription());
         descTextArea.setCaretPosition(0);

         trackerComboBox.setSelectedItem(issue.getTracker());
         statusComboBox.setSelectedItem(redmineIssue.getRepository().getStatus(issue.getStatusId()));
         categoryComboBox.setSelectedItem(issue.getCategory());
         projectComboBox.setSelectedItem(new NestedProject(issue.getProject()));

         IssuePriority ip = redmineIssue.getRepository().getIssuePriority(issue.getPriorityId());
         priorityComboBox.setSelectedItem(ip);
         if (priorityComboBox.getSelectedIndex() < 0) {
            priorityComboBox.addItem(ip);
            priorityComboBox.setSelectedItem(ip);
         }
         assigneeComboBox.setSelectedItem(RedmineUser.fromIssue(issue));

         targetVersionComboBox.setSelectedItem(issue.getTargetVersion());
         startDateChooser.setDate(issue.getStartDate());
         dueDateChooser.setDate(issue.getDueDate());
         estimateTimeTextField.setValue(issue.getEstimatedHours());
         doneComboBox.setSelectedIndex(Math.round(issue.getDoneRatio() / 10));

         if(issue.getJournals() != null && issue.getJournals().size() > 0) {
             journalOuterPane.setVisible(true);
         } else {
             journalOuterPane.setVisible(false);
         }
         journalPane.removeAll();
         List<Journal> journalEntries = new ArrayList<>(issue.getJournals());
         for(int i = 0; i < journalEntries.size(); i++) {
             JournalDisplay jdisplay = new JournalDisplay(redmineIssue, journalEntries.get(i), i);
             journalPane.add(jdisplay);
         }
         journalPane.doLayout();
         journalPane.revalidate();
         
          attachmentPanel.removeAll();
          if (issue.getAttachments() != null) {
              for (Attachment ad : issue.getAttachments()) {
                  AttachmentDisplay adisplay = new AttachmentDisplay(redmineIssue, ad);
                  adisplay.setAlignmentX(Component.LEFT_ALIGNMENT);
                  attachmentPanel.add(adisplay);
              }
          }
          LinkButton lb = new LinkButton();
          lb.setBorder(null);
          lb.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/add.png")));
          lb.setToolTipText(Bundle.BTN_AddAttachment());
          lb.setActionCommand("addAttachment");
          lb.addActionListener(new ActionListener() {

             @Override
             public void actionPerformed(ActionEvent e) {
                  JPanel panel = new JPanel(new GridBagLayout());
                  final JLabel descLabel = new JLabel("Description: ");
                  final JLabel commandLabel = new JLabel("Comment: ");
                  final JTextArea comment = new JTextArea();
                  JScrollPane commenctScrollPane = new JScrollPane(comment);
                  final JTextField description = new JTextField();
                  Dimension min = description.getMinimumSize();
                  Dimension pref = description.getPreferredSize();
                  Dimension max = description.getMaximumSize();
                  min.setSize(250, min.getHeight());
                  pref.setSize(250, min.getHeight());
                  max.setSize(max.getWidth(), min.getHeight());
                  description.setMinimumSize(min);
                  description.setPreferredSize(pref);
                  description.setMaximumSize(max);
                  GridBagConstraints gbc = new GridBagConstraints();
                  gbc.anchor = GridBagConstraints.BASELINE_LEADING;
                  gbc.fill = GridBagConstraints.BOTH;
                  gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; gbc.weighty = 0;
                  panel.add(descLabel, gbc);
                  gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 1; gbc.weighty = 0;
                  panel.add(description, gbc);
                  gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.weighty = 0;
                  panel.add(commandLabel, gbc);
                  gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 1; gbc.weighty = 1;
                  panel.add(commenctScrollPane, gbc);
                  panel.add(new Filler(new Dimension(0,0),new Dimension(0,0),new Dimension(Short.MAX_VALUE, Short.MAX_VALUE)));
                  JFileChooser fileChooser = new JFileChooser(lastDirectory);
                  fileChooser.setAccessory(panel);
                  fileChooser.setDialogTitle("Add attachment");
                  int result = fileChooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
                  if (result == JFileChooser.APPROVE_OPTION) {
                      lastDirectory = fileChooser.getCurrentDirectory();
                      final File selectedFile = fileChooser.getSelectedFile();
                      new SwingWorker() {

                          @Override
                          protected Object doInBackground() throws Exception {
                              redmineIssue.attachFile(selectedFile,
                                      description.getText(),
                                      comment.getText(),
                                      false);
                              redmineIssue.refresh();
                              return null;
                          }

                          @Override
                          protected void done() {
                              initIssue();
                          }
                      }.execute();
                  }
              }
          });
          attachmentPanel.add(lb);
          
         if (redmineIssue.hasParent()) {
            final String parentKey = String.valueOf(issue.getParentId());
            redmineIssue.getRepository().getRequestProcessor().post(new Runnable() {
               @Override
               public void run() {
                  RedmineIssue parentIssue = RedmineUtil.getIssue(redmineIssue.getRepository(), parentKey);
                  if (parentIssue == null) {
                     // how could this be possible? parent removed?
                     Redmine.LOG.log(Level.INFO, "issue {0} is referencing a not available parent with id {1}",
                                     new Object[]{redmineIssue.getID(), parentKey}); // NOI18N
                     return;
                  }
                  final RedmineIssue parent = parentIssue;
                  EventQueue.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                        parentHeaderPanel.setVisible(true);
                        parentHeaderPanel.removeAll();
                        headerLabel.setIcon(ImageUtilities.loadImageIcon("com/kenai/redminenb/resources/subtask.png", true)); // NOI18N
                        GroupLayout layout = new GroupLayout(parentHeaderPanel);
                        JLabel parentLabel = new JLabel();
                        parentLabel.setText(parent.getSummary());
                        LinkButton parentButton = new LinkButton(new AbstractAction() {
                           @Override
                           public void actionPerformed(ActionEvent e) {
                              RedmineUtil.openIssue(parent);
                           }
                        });
                        parentButton.setText(parent.getIssue().getTracker().getName() + " #" + parent.getID() + ':');
                        layout.setHorizontalGroup(
                                layout.createSequentialGroup().addComponent(parentButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(parentLabel));
                        layout.setVerticalGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(parentButton).addComponent(parentLabel));
                        parentHeaderPanel.add(parentButton);
                        parentHeaderPanel.setLayout(layout);
                     }
                  });
               }
            });            
         } else {
            // no parent issue
            parentHeaderPanel.setVisible(false);
            parentHeaderPanel.removeAll();
            headerLabel.setIcon(null);
         }
         journalOuterPane.setVisible(true);
         commentPanel.setVisible(true);
         logtimePanel.setVisible(true);
         descriptionPanel.setSelectedIndex(1);
         attachmentLabel.setVisible(true);
         attachmentPanel.setVisible(true);
         spentHoursLabel.setText(NbBundle.getMessage(getClass(), 
                 "RedmineIssuePanel.spentHoursReplacementLabel.text",
                 issue.getSpentHours()));
      } else {
         // new issue
         setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
         trackerComboBox.setSelectedItem(0);
         statusComboBox.setSelectedIndex(0);
         priorityComboBox.setSelectedItem(redmineIssue.getRepository().getDefaultIssuePriority());
         categoryComboBox.setSelectedItem(null);
         projectComboBox.setSelectedItem(redmineIssue
                      .getRepository()
                      .getProjects()
                      .get(redmineIssue.getRepository().getProjectID()));

         subjectTextField.setText(null);
         parentTaskTextField.setValue(null);
         descTextArea.setText(null);
         assigneeComboBox.setSelectedItem(null);
         targetVersionComboBox.setSelectedItem(null);
         startDateChooser.setDate(null);
         dueDateChooser.setDate(null);
         estimateTimeTextField.setValue(null);
         doneComboBox.setSelectedIndex(0);
         journalOuterPane.setVisible(false);
         commentPanel.setVisible(false);
         logtimePanel.setVisible(false);
         descriptionPanel.setSelectedIndex(0);
         attachmentLabel.setVisible(false);
         attachmentPanel.setVisible(false);
         spentHoursLabel.setText("");
      }
      initProjectData();
      setInfoMessage(null);
      updateTextileOutput();
    }

    private void initProjectData() {
        ListComboBoxModel<RedmineUser> assigneeModel = new ListComboBoxModel<>();
        assigneeModel.add(null);
        ListComboBoxModel<IssueCategory> categoryModel = new ListComboBoxModel<>();
        categoryModel.add(null);
        ListComboBoxModel<Version> versionsModel = new ListComboBoxModel<>();
        versionsModel.add(null);
        
        NestedProject np = (NestedProject) projectComboBox.getSelectedItem();
        if (np != null) {
            Project p = np.getProject();
            if (p != null) {
                assigneeModel.addAll(redmineIssue.getRepository().getUsers(p));
                categoryModel.addAll(redmineIssue.getRepository().getIssueCategories(p));
                versionsModel.addAll(redmineIssue.getRepository().getVersions(p));
            }
        }
        
        assigneeModel.setSelectedItem(assigneeComboBox.getSelectedItem());
        categoryModel.setSelectedItem(categoryComboBox.getSelectedItem());
        versionsModel.setSelectedItem(targetVersionComboBox.getSelectedItem());
        
        if(! assigneeModel.getElements().contains(assigneeModel.getSelectedItem())) {
            assigneeModel.setSelectedItem(null);
        }
        if(! categoryModel.getElements().contains(categoryModel.getSelectedItem())) {
            categoryModel.setSelectedItem(null);
        }
        if(! versionsModel.getElements().contains(versionsModel.getSelectedItem())) {
            versionsModel.setSelectedItem(null);
        }
        
        categoryComboBox.setModel(categoryModel);
        assigneeComboBox.setModel(assigneeModel);
        targetVersionComboBox.setModel(versionsModel);
        
        if(assigneeModel.getElements().contains(redmineIssue.getRepository().getCurrentUser())) {
            assignToMeButton.setEnabled(true);
        } else {
            assignToMeButton.setEnabled(false);
        }
        versionAddButton.setEnabled(np != null);
        categoryAddButton.setEnabled(np != null);
    }
   
   synchronized void setInfoMessage(String msg) {
      infoLabel.setVisible(msg != null);
      infoLabel.setText(msg);
      if (msg != null) {
         infoLabel.setIcon(Defaults.getIcon("info.png"));
         autoHide();
      }
   }

   void setErrorMessage(String msg) {
      infoLabel.setVisible(msg != null);
      infoLabel.setText(msg);
      if (msg != null) {
         infoLabel.setIcon(Defaults.getIcon("error.png"));
         autoHide();
      }
   }

   private void autoHide() {
      redmineIssue.getRepository().getRequestProcessor().post(new Runnable() {
         @Override
         public void run() {
            EventQueue.invokeLater(new Runnable() {
               @Override
               public void run() {
                  setInfoMessage(null);
               }
            });
         }
      }, 5000);

   }

   private void setIssueData(com.taskadapter.redmineapi.bean.Issue issue) {
      issue.setTracker((Tracker)trackerComboBox.getSelectedItem());
      issue.setStatusId(((IssueStatus)statusComboBox.getSelectedItem()).getId());

      issue.setSubject(subjectTextField.getText());
      issue.setParentId(getParentTaskId());
      issue.setDescription(descTextArea.getText());
      issue.setPriorityId(((IssuePriority) priorityComboBox.getSelectedItem()).getId());
      issue.setAssignee(getSelectedAssignee());
      issue.setCategory((IssueCategory)categoryComboBox.getSelectedItem());
      issue.setTargetVersion(targetVersionComboBox.getSelectedItem() == null ? null : (Version)targetVersionComboBox.getSelectedItem());
      issue.setStartDate(startDateChooser.getDate());
      issue.setDueDate(dueDateChooser.getDate());
      issue.setEstimatedHours(getEstimateTime());
      issue.setDoneRatio(doneComboBox.getSelectedIndex() * 10);
      // Workaround for https://github.com/taskadapter/redmine-java-api/pull/163
      Project p = ((NestedProject)projectComboBox.getSelectedItem()).getProject();
      Project transfer = ProjectFactory.create(p.getId());
      transfer.setIdentifier(p.getId().toString());
      issue.setProject(transfer);

      //List<CustomField> customFields = null;
      //...
      //issue.setCustomFields(customFields);
   }

   private User getSelectedAssignee() {
      return assigneeComboBox.getSelectedItem() == null ? null : ((RedmineUser)assigneeComboBox.getSelectedItem()).getUser();
   }

   private Integer getParentTaskId() {
      Number n = (Number)parentTaskTextField.getValue();
      return n == null ? null : n.intValue();
   }

   private Float getEstimateTime() {
      Number n = (Number)estimateTimeTextField.getValue();
      return n == null ? null : n.floatValue();
   }

   RedmineIssue getIssue() {
      return redmineIssue;
   }

    private void createIssue() {
        com.taskadapter.redmineapi.bean.Issue issue = this.redmineIssue.getIssue();

        if (issue == null) {
            issue = new Issue();
        }
        
        setIssueData(issue);
        
        final Issue inputIssue = issue;
        
        new SwingWorker() {

           @Override
           protected Object doInBackground() throws Exception {
               RedmineRepository rr = redmineIssue.getRepository();
               String projektId = ((NestedProject) projectComboBox.getSelectedItem()).getProject().getIdentifier();
               Issue issue = rr.getIssueManager().createIssue(projektId, inputIssue);
               redmineIssue.setIssue(issue);
               redmineIssue.getRepository().getIssueCache().put(redmineIssue);
               return null;
           }

            @Override
            protected void done() {
                try {
                    get();
                    initIssue();
                    setInfoMessage("Issue successfully created.");
                    createButton.setVisible(false);
                    updateButton.setVisible(true);
                } catch (InterruptedException ex) {
                    Redmine.LOG.log(Level.SEVERE, "Can't create Redmine issue", ex);
                    setErrorMessage("Can't create Redmine issue: "
                            + ex.getMessage());
                } catch (ExecutionException ex) {
                    Redmine.LOG.log(Level.SEVERE, "Can't create Redmine issue", ex.getCause());
                    setErrorMessage("Can't create Redmine issue: "
                            + ex.getCause().getMessage());
                }
            }
       }.execute();
   }

   void saveIssue() {
        final Issue issue = this.redmineIssue.getIssue();
        setIssueData(issue);
        String comment = updateCommentTextArea.getText();
        if(! StringUtils.isBlank(comment)) {
            issue.setNotes(comment);
        }
        
        new SwingWorker() {

           @Override
           protected Object doInBackground() throws Exception {
                redmineIssue.getRepository().getIssueManager().update(issue);
                redmineIssue.refresh();
                return null;
           }

            @Override
            protected void done() {
                try {
                    get();
                    initIssue();
                    setInfoMessage("Issue successfully saved.");
                    updateCommentTextArea.setText("");
                    commentPanel.colapse();
                    logtimePanel.colapse();
                    createButton.setVisible(false);
                    updateButton.setVisible(true);
                } catch (InterruptedException ex) {
                    Redmine.LOG.log(Level.SEVERE, "Can't save Redmine issue", ex);
                    setErrorMessage("Saving the Issue failed: "
                            + ex.getMessage());
                } catch (ExecutionException ex) {
                    Redmine.LOG.log(Level.SEVERE, "Can't save Redmine issue", ex.getCause());
                    setErrorMessage("Saving the Issue failed: "
                            + ex.getCause().getMessage());
                }
            }
       }.execute();
   }

   private void initValues() {
      trackerComboBox.setRenderer(new Defaults.TrackerLCR());
      trackerComboBox.setModel(new DefaultComboBoxModel(redmineIssue.getRepository().getTrackers().toArray()));

      statusComboBox.setRenderer(new Defaults.IssueStatusLCR());
      statusComboBox.setModel(new DefaultComboBoxModel(redmineIssue.getRepository().getStatuses().toArray()));

      priorityComboBox.setRenderer(new Defaults.PriorityLCR());
      priorityComboBox.setModel(new DefaultComboBoxModel(redmineIssue.getRepository().getIssuePriorities().toArray()));

      assigneeComboBox.setRenderer(new Defaults.RepositoryUserLCR());
      User prototypeUser = UserFactory.create();
      prototypeUser.setFirstName("John, some more space,");
      prototypeUser.setLastName("Doe, and some...");
      assigneeComboBox.setPrototypeDisplayValue(new RedmineUser(prototypeUser));

      categoryComboBox.setRenderer(new Defaults.IssueCategoryLCR());

      targetVersionComboBox.setRenderer(new Defaults.VersionLCR());
      
      logtimeActivityComboBox.setRenderer(new Defaults.TimeEntryActivityLCR());
      DefaultComboBoxModel timeEntryActivityModel = new DefaultComboBoxModel(
              redmineIssue.getRepository().getTimeEntryActivities().toArray()
      );
      for(int i = 0; i < timeEntryActivityModel.getSize(); i++) {
          TimeEntryActivity tea = (TimeEntryActivity) timeEntryActivityModel.getElementAt(i);
          if(tea.isDefault()) {
              timeEntryActivityModel.setSelectedItem(tea);
              break;
          }
      }
      logtimeActivityComboBox.setModel(timeEntryActivityModel);
      
      List<NestedProject> projects = new ArrayList<>(redmineIssue.getRepository().getProjects().values());
      Collections.sort(projects);
      projectComboBox.setModel(new DefaultComboBoxModel(projects.toArray()));
   }

   private boolean isIssueValid() {
      return true;
   }

   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(Defaults.paintGradient((Graphics2D)g, getWidth(), getHeight()));
   }

   void opened() {
//      undoRedoSupport = Bugzilla.getInstance().getUndoRedoSupport(issue);
//      undoRedoSupport.register(addCommentArea);
//
//      // Hack - reset any previous modifications when the issue window is reopened
//      reloadForm(true);
   }

   void closed() {
      if (redmineIssue != null) {
//         commentsPanel.storeSettings();
//         if (undoRedoSupport != null) {
//            undoRedoSupport.unregisterAll();
//            undoRedoSupport = null;
//         }
      }
   }

   @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        layerPane = new javax.swing.JLayeredPane();
        innerPanel = new VerticalScrollPane();
        headPane = new javax.swing.JPanel();
        headerLabel = new javax.swing.JLabel();
        parentHeaderPanel = new javax.swing.JPanel();
        updatedLabel = new javax.swing.JLabel();
        createdLabel = new javax.swing.JLabel();
        createdValueLabel = new javax.swing.JLabel();
        updatedValueLabel = new javax.swing.JLabel();
        buttonPane = new javax.swing.JPanel();
        updateButton = new javax.swing.JButton();
        createButton = new javax.swing.JButton();
        toolbar = new org.openide.awt.Toolbar();
        issuePane = new javax.swing.JPanel();
        descriptionLabel = new javax.swing.JLabel();
        categoryComboBox = new javax.swing.JComboBox();
        priorityLabel = new javax.swing.JLabel();
        estimateTimeLabel1 = new javax.swing.JLabel();
        subjectLabel = new javax.swing.JLabel();
        estimateTimeLabel = new javax.swing.JLabel();
        subjectLabel1 = new javax.swing.JLabel();
        parentIdLabel = new javax.swing.JLabel();
        doneLabel = new javax.swing.JLabel();
        trackerComboBox = new javax.swing.JComboBox();
        estimateTimeTextField = new JFormattedTextField(NumberFormat.getNumberInstance());
        startDateLabel = new javax.swing.JLabel();
        dueDateLabel = new javax.swing.JLabel();
        statusComboBox = new javax.swing.JComboBox();
        priorityComboBox = new javax.swing.JComboBox();
        doneComboBox = new javax.swing.JComboBox();
        subjectTextField = new javax.swing.JTextField();
        categoryLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        parentTaskTextField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        targetVersionComboBox = new javax.swing.JComboBox();
        targetVersionLabel = new javax.swing.JLabel();
        assigneeLabel = new javax.swing.JLabel();
        subjectLabel2 = new javax.swing.JLabel();
        assigneeComboBox = new javax.swing.JComboBox();
        privateCheckBox = new javax.swing.JCheckBox();
        descriptionPanel = new javax.swing.JTabbedPane();
        descScrollPane = new javax.swing.JScrollPane();
        descTextArea = new javax.swing.JTextArea();
        htmlOutputLabel = new com.kenai.redminenb.util.markup.TextilePreview();
        updateCommentLabel = new javax.swing.JLabel();
        updateCommentTabPanel = new javax.swing.JTabbedPane();
        updateCommentScrollPane1 = new javax.swing.JScrollPane();
        updateCommentTextArea = new javax.swing.JTextArea();
        updateCommentHtmlOutputLabel = new com.kenai.redminenb.util.markup.TextilePreview();
        attachmentLabel = new javax.swing.JLabel();
        attachmentPanel = new com.kenai.redminenb.util.DelegatingBaseLineJPanel();
        logtimeLabel = new javax.swing.JLabel();
        logtimeInputPanel = new javax.swing.JPanel();
        logtimeSpentLabel = new javax.swing.JLabel();
        logtimeCommentLabel = new javax.swing.JLabel();
        logtimeActivityLabel = new javax.swing.JLabel();
        logtimeSpentTextField = new javax.swing.JTextField();
        logtimeCommentTextField = new javax.swing.JTextField();
        logtimeActivityComboBox = new javax.swing.JComboBox();
        logtimeSaveButton = new javax.swing.JButton();
        logtimeHoursLabel = new javax.swing.JLabel();
        spentHoursLabel = new javax.swing.JLabel();
        startDateChooser = new com.kenai.redminenb.util.DatePicker();
        dueDateChooser = new com.kenai.redminenb.util.DatePicker();
        projectComboBox = new javax.swing.JComboBox();
        journalOuterPane = new javax.swing.JPanel();
        journalPane = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        innerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        headerLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.headerLabel.text")); // NOI18N

        parentHeaderPanel.setRequestFocusEnabled(false);

        updatedLabel.setFont(updatedLabel.getFont().deriveFont(updatedLabel.getFont().getStyle() & ~java.awt.Font.BOLD, updatedLabel.getFont().getSize()-2));
        updatedLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("textInactiveText"));
        updatedLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.updatedLabel.text")); // NOI18N

        createdLabel.setFont(createdLabel.getFont().deriveFont(createdLabel.getFont().getStyle() & ~java.awt.Font.BOLD, createdLabel.getFont().getSize()-2));
        createdLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("textInactiveText"));
        createdLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.createdLabel.text")); // NOI18N

        createdValueLabel.setFont(createdValueLabel.getFont().deriveFont(createdValueLabel.getFont().getStyle() | java.awt.Font.BOLD, createdValueLabel.getFont().getSize()-2));
        createdValueLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("textInactiveText"));
        createdValueLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.createdValueLabel.text")); // NOI18N

        updatedValueLabel.setFont(updatedValueLabel.getFont().deriveFont(updatedValueLabel.getFont().getStyle() | java.awt.Font.BOLD, updatedValueLabel.getFont().getSize()-2));
        updatedValueLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("textInactiveText"));
        updatedValueLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.updatedValueLabel.text")); // NOI18N

        javax.swing.GroupLayout headPaneLayout = new javax.swing.GroupLayout(headPane);
        headPane.setLayout(headPaneLayout);
        headPaneLayout.setHorizontalGroup(
            headPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headPaneLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(headPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(headPaneLayout.createSequentialGroup()
                        .addComponent(createdLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(createdValueLabel)
                        .addGap(18, 18, 18)
                        .addComponent(updatedLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(updatedValueLabel)
                        .addGap(246, 246, 246))
                    .addComponent(parentHeaderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(headerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        headPaneLayout.setVerticalGroup(
            headPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(parentHeaderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(headerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addGroup(headPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createdLabel)
                    .addComponent(updatedLabel)
                    .addComponent(createdValueLabel)
                    .addComponent(updatedValueLabel))
                .addGap(0, 0, 0))
        );

        buttonPane.setOpaque(false);

        updateButton.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.updateButton.text")); // NOI18N
        updateButton.setToolTipText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.updateButton.toolTipText")); // NOI18N
        updateButton.setSelected(true);
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        createButton.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.createButton.text")); // NOI18N
        createButton.setToolTipText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.createButton.toolTipText")); // NOI18N
        createButton.setSelected(true);
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createButtonActionPerformed(evt);
            }
        });

        infoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/info.png"))); // NOI18N

        toolbar.setRollover(true);
        toolbar.setBorderPainted(false);
        toolbar.setOpaque(false);

        javax.swing.GroupLayout buttonPaneLayout = new javax.swing.GroupLayout(buttonPane);
        buttonPane.setLayout(buttonPaneLayout);
        buttonPaneLayout.setHorizontalGroup(
            buttonPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPaneLayout.createSequentialGroup()
                .addComponent(createButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(updateButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(infoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        buttonPaneLayout.setVerticalGroup(
            buttonPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPaneLayout.createSequentialGroup()
                .addGroup(buttonPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(buttonPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(createButton)
                        .addComponent(updateButton))
                    .addComponent(infoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        issuePane.setLayout(new java.awt.GridBagLayout());

        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.descriptionLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(descriptionLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(categoryComboBox, gridBagConstraints);

        priorityLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.priorityLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(priorityLabel, gridBagConstraints);

        estimateTimeLabel1.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.estimateTimeLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(estimateTimeLabel1, gridBagConstraints);

        subjectLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.subjectLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(subjectLabel, gridBagConstraints);

        estimateTimeLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.estimateTimeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(estimateTimeLabel, gridBagConstraints);

        subjectLabel1.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.subjectLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(subjectLabel1, gridBagConstraints);

        parentIdLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.parentIdLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(parentIdLabel, gridBagConstraints);

        doneLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.doneLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(doneLabel, gridBagConstraints);

        trackerComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Bug", "Feature", "Support" }));
        trackerComboBox.setPrototypeDisplayValue(TrackerFactory.create(-1, "A really long tracker prototype"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(trackerComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(estimateTimeTextField, gridBagConstraints);

        startDateLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.startDateLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(startDateLabel, gridBagConstraints);

        dueDateLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.dueDateLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(dueDateLabel, gridBagConstraints);

        statusComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "New" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(statusComboBox, gridBagConstraints);

        priorityComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Low", "Normal", "High", "Urgent", "Immediate" }));
        priorityComboBox.setSelectedIndex(1);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(priorityComboBox, gridBagConstraints);

        doneComboBox.setMaximumRowCount(11);
        doneComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0 %", "10 %", "20 %", "30 %", "40 %", "50 %", "60 %", "70 %", "80 %", "90 %", "100 %" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(doneComboBox, gridBagConstraints);

        subjectTextField.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.subjectTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(subjectTextField, gridBagConstraints);

        wikiSyntaxButton.setBorder(null);
        wikiSyntaxButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/help.png"))); // NOI18N
        wikiSyntaxButton.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.wikiSyntaxButton.text")); // NOI18N
        wikiSyntaxButton.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        wikiSyntaxButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wikiSyntaxButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(wikiSyntaxButton, gridBagConstraints);

        categoryLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.categoryLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(categoryLabel, gridBagConstraints);

        statusLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.statusLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(statusLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(parentTaskTextField, gridBagConstraints);

        categoryAddButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/add.png"))); // NOI18N
        categoryAddButton.setToolTipText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.categoryAddButton.toolTipText")); // NOI18N
        categoryAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                categoryAddButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(categoryAddButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(targetVersionComboBox, gridBagConstraints);

        targetVersionLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.targetVersionLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(targetVersionLabel, gridBagConstraints);

        versionAddButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/add.png"))); // NOI18N
        versionAddButton.setToolTipText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.versionAddButton.toolTipText")); // NOI18N
        versionAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                versionAddButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(versionAddButton, gridBagConstraints);

        assigneeLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.assigneeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(assigneeLabel, gridBagConstraints);

        subjectLabel2.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.subjectLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(subjectLabel2, gridBagConstraints);

        assignToMeButton.setBorder(null);
        assignToMeButton.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.assignToMeButton.text")); // NOI18N
        assignToMeButton.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        assignToMeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assignToMeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(assignToMeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(assigneeComboBox, gridBagConstraints);

        privateCheckBox.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.privateCheckBox.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(privateCheckBox, gridBagConstraints);

        descScrollPane.setMinimumSize(new java.awt.Dimension(22, 120));
        descScrollPane.setPreferredSize(new java.awt.Dimension(223, 120));

        descTextArea.setLineWrap(true);
        descScrollPane.setViewportView(descTextArea);

        descriptionPanel.addTab(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.descScrollPane.TabConstraints.tabTitle"), descScrollPane); // NOI18N
        descriptionPanel.addTab(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.htmlOutputLabel.TabConstraints.tabTitle"), htmlOutputLabel); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(descriptionPanel, gridBagConstraints);

        updateCommentLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.updateCommentLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(updateCommentLabel, gridBagConstraints);

        updateCommentScrollPane1.setMinimumSize(new java.awt.Dimension(22, 80));
        updateCommentScrollPane1.setPreferredSize(new java.awt.Dimension(228, 80));

        updateCommentTextArea.setLineWrap(true);
        updateCommentScrollPane1.setViewportView(updateCommentTextArea);

        updateCommentTabPanel.addTab(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.updateCommentScrollPane1.TabConstraints.tabTitle"), updateCommentScrollPane1); // NOI18N
        updateCommentTabPanel.addTab(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.updateCommentHtmlOutputLabel.TabConstraints.tabTitle"), updateCommentHtmlOutputLabel); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(updateCommentTabPanel, gridBagConstraints);

        attachmentLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.attachmentLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(attachmentLabel, gridBagConstraints);

        attachmentPanel.setOpaque(false);
        attachmentPanel.setLayout(new javax.swing.BoxLayout(attachmentPanel, javax.swing.BoxLayout.PAGE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(attachmentPanel, gridBagConstraints);

        logtimeLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.logtimeLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(logtimeLabel, gridBagConstraints);

        logtimeInputPanel.setOpaque(false);
        logtimeInputPanel.setLayout(new java.awt.GridBagLayout());

        logtimeSpentLabel.setFont(logtimeSpentLabel.getFont().deriveFont(logtimeSpentLabel.getFont().getStyle() | java.awt.Font.BOLD));
        logtimeSpentLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.logtimeSpentLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        logtimeInputPanel.add(logtimeSpentLabel, gridBagConstraints);

        logtimeCommentLabel.setFont(logtimeCommentLabel.getFont().deriveFont(logtimeCommentLabel.getFont().getStyle() | java.awt.Font.BOLD));
        logtimeCommentLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.logtimeCommentLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        logtimeInputPanel.add(logtimeCommentLabel, gridBagConstraints);

        logtimeActivityLabel.setFont(logtimeActivityLabel.getFont().deriveFont(logtimeActivityLabel.getFont().getStyle() | java.awt.Font.BOLD));
        logtimeActivityLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.logtimeActivityLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        logtimeInputPanel.add(logtimeActivityLabel, gridBagConstraints);

        logtimeSpentTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        logtimeSpentTextField.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.logtimeSpentTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        logtimeInputPanel.add(logtimeSpentTextField, gridBagConstraints);

        logtimeCommentTextField.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.logtimeCommentTextField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        logtimeInputPanel.add(logtimeCommentTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        logtimeInputPanel.add(logtimeActivityComboBox, gridBagConstraints);

        logtimeSaveButton.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.logtimeSaveButton.text")); // NOI18N
        logtimeSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logtimeSaveButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        logtimeInputPanel.add(logtimeSaveButton, gridBagConstraints);

        logtimeHoursLabel.setFont(logtimeHoursLabel.getFont().deriveFont(logtimeHoursLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        logtimeHoursLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.logtimeHoursLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 12);
        logtimeInputPanel.add(logtimeHoursLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(logtimeInputPanel, gridBagConstraints);

        spentHoursLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.spentHoursLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(spentHoursLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(startDateChooser, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(dueDateChooser, gridBagConstraints);

        projectComboBox.setRenderer(new com.kenai.redminenb.ui.Defaults.ProjectLCR());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        issuePane.add(projectComboBox, gridBagConstraints);

        journalOuterPane.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.journalOuterPane.border.title"))); // NOI18N
        journalOuterPane.setOpaque(false);
        journalOuterPane.setLayout(new java.awt.BorderLayout());

        journalPane.setOpaque(false);
        journalPane.setLayout(new javax.swing.BoxLayout(journalPane, javax.swing.BoxLayout.PAGE_AXIS));
        journalOuterPane.add(journalPane, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout innerPanelLayout = new javax.swing.GroupLayout(innerPanel);
        innerPanel.setLayout(innerPanelLayout);
        innerPanelLayout.setHorizontalGroup(
            innerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(headPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(innerPanelLayout.createSequentialGroup()
                .addGroup(innerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(innerPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(buttonPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(innerPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(journalOuterPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(innerPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(issuePane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        innerPanelLayout.setVerticalGroup(
            innerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(innerPanelLayout.createSequentialGroup()
                .addComponent(headPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(issuePane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(journalOuterPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        layerPane.add(innerPanel);
        innerPanel.setBounds(0, 0, 689, 784);

        add(layerPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void createButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
       if (isIssueValid()) {
           createIssue();
       }
    }//GEN-LAST:event_createButtonActionPerformed

    private void updateButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
       if (isIssueValid()) {
          saveIssue();
       }
    }//GEN-LAST:event_updateButtonActionPerformed

    private void logtimeSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logtimeSaveButtonActionPerformed
        String hoursString = logtimeSpentTextField.getText();
        float hours = 0;
        try {
            hours = NumberFormat.getNumberInstance().parse(logtimeSpentTextField.getText())
            .floatValue();
        } catch (ParseException ex) {
            setErrorMessage("Failed to parse '" + hoursString + "' as a float value");
            return;
        }
        final TimeEntry te = TimeEntryFactory.create();
        TimeEntryActivity tea = (TimeEntryActivity) logtimeActivityComboBox.getSelectedItem();
        te.setActivityId(tea.getId());
        te.setComment(logtimeCommentTextField.getText());
        te.setHours(hours);
        te.setIssueId(getIssue().getIssue().getId());

        new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                redmineIssue.getRepository().getIssueManager().createTimeEntry(te);
                redmineIssue.refresh();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    initIssue();
                    setInfoMessage("Time entry successfully saved.");
                    logtimeSpentTextField.setText("");
                    logtimeCommentTextField.setText("");
                    logtimePanel.colapse();
                    commentPanel.colapse();
                } catch (InterruptedException ex) {
                    Redmine.LOG.log(Level.SEVERE, "Saving time entry failed", ex);
                    setErrorMessage("Saving time entry failed: "
                        + ex.getMessage());
                } catch (ExecutionException ex) {
                    Redmine.LOG.log(Level.SEVERE, "Saving time entry failed", ex.getCause());
                    setErrorMessage("Saving time entry failed: "
                        + ex.getCause().getMessage());
                }
            }
        }.execute();
    }//GEN-LAST:event_logtimeSaveButtonActionPerformed

    private void assignToMeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assignToMeButtonActionPerformed
        assigneeComboBox.setSelectedItem(redmineIssue.getRepository().getCurrentUser());
    }//GEN-LAST:event_assignToMeButtonActionPerformed

    private void versionAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_versionAddButtonActionPerformed
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine("New Version Name", "Add a new Version");
        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION
            && StringUtils.isNotBlank(d.getInputText())) {
            NestedProject np = (NestedProject) projectComboBox.getSelectedItem();
            assert np != null;
            try {
                Version v = VersionFactory.create(np.getProject(), d.getInputText());
                redmineIssue.getRepository().getProjectManager().createVersion(v);
                Collection<? extends Version> c = redmineIssue.getRepository().reloadVersions(np.getProject());
                for (Version version : c) {
                    if (v.getName().equals(version.getName())) {
                        v = version;
                        break;
                    }
                }
                targetVersionComboBox.setModel(new DefaultComboBoxModel(c.toArray()));
                targetVersionComboBox.setSelectedItem(v);

            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to create Version", ex);
            }
        }
    }//GEN-LAST:event_versionAddButtonActionPerformed

    private void categoryAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_categoryAddButtonActionPerformed
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine("New Category label", "Add a new Category");
        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION
            && StringUtils.isNotBlank(d.getInputText())) {
            NestedProject np = (NestedProject) projectComboBox.getSelectedItem();
            assert np != null;
            try {
                IssueCategory ic = IssueCategoryFactory.create(np.getProject(), d.getInputText());
                redmineIssue.getRepository().getIssueManager().createCategory(ic);
                Collection<? extends IssueCategory> c = redmineIssue.getRepository().reloadIssueCategories(np.getProject());
                for (IssueCategory issueCategory : c) {
                    if (ic.getName().equals(issueCategory.getName())) {
                        ic = issueCategory;
                        break;
                    }
                }
                categoryComboBox.setModel(new DefaultComboBoxModel(c.toArray()));
                categoryComboBox.setSelectedItem(ic);

            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to create category", ex);
            }
        }
    }//GEN-LAST:event_categoryAddButtonActionPerformed

    private void wikiSyntaxButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wikiSyntaxButtonActionPerformed
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(
                redmineIssue.getRepository().getUrl() + "/help/wiki_syntax.html"));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_wikiSyntaxButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    final com.kenai.redminenb.util.LinkButton assignToMeButton = new com.kenai.redminenb.util.LinkButton();
    javax.swing.JComboBox assigneeComboBox;
    javax.swing.JLabel assigneeLabel;
    javax.swing.JLabel attachmentLabel;
    com.kenai.redminenb.util.DelegatingBaseLineJPanel attachmentPanel;
    javax.swing.JPanel buttonPane;
    final com.kenai.redminenb.util.LinkButton categoryAddButton = new com.kenai.redminenb.util.LinkButton();
    javax.swing.JComboBox categoryComboBox;
    javax.swing.JLabel categoryLabel;
    javax.swing.JButton createButton;
    javax.swing.JLabel createdLabel;
    javax.swing.JLabel createdValueLabel;
    javax.swing.JScrollPane descScrollPane;
    javax.swing.JTextArea descTextArea;
    javax.swing.JLabel descriptionLabel;
    javax.swing.JTabbedPane descriptionPanel;
    javax.swing.JComboBox doneComboBox;
    javax.swing.JLabel doneLabel;
    org.jdesktop.swingx.JXDatePicker dueDateChooser;
    javax.swing.JLabel dueDateLabel;
    javax.swing.JLabel estimateTimeLabel;
    javax.swing.JLabel estimateTimeLabel1;
    javax.swing.JFormattedTextField estimateTimeTextField;
    javax.swing.JPanel headPane;
    javax.swing.JLabel headerLabel;
    com.kenai.redminenb.util.markup.TextilePreview htmlOutputLabel;
    final javax.swing.JLabel infoLabel = new javax.swing.JLabel();
    javax.swing.JPanel innerPanel;
    javax.swing.JPanel issuePane;
    javax.swing.JPanel journalOuterPane;
    javax.swing.JPanel journalPane;
    javax.swing.JLayeredPane layerPane;
    javax.swing.JComboBox logtimeActivityComboBox;
    javax.swing.JLabel logtimeActivityLabel;
    javax.swing.JLabel logtimeCommentLabel;
    javax.swing.JTextField logtimeCommentTextField;
    javax.swing.JLabel logtimeHoursLabel;
    javax.swing.JPanel logtimeInputPanel;
    javax.swing.JLabel logtimeLabel;
    javax.swing.JButton logtimeSaveButton;
    javax.swing.JLabel logtimeSpentLabel;
    javax.swing.JTextField logtimeSpentTextField;
    javax.swing.JPanel parentHeaderPanel;
    javax.swing.JLabel parentIdLabel;
    javax.swing.JFormattedTextField parentTaskTextField;
    javax.swing.JComboBox priorityComboBox;
    javax.swing.JLabel priorityLabel;
    javax.swing.JCheckBox privateCheckBox;
    javax.swing.JComboBox projectComboBox;
    javax.swing.JLabel spentHoursLabel;
    org.jdesktop.swingx.JXDatePicker startDateChooser;
    javax.swing.JLabel startDateLabel;
    javax.swing.JComboBox statusComboBox;
    javax.swing.JLabel statusLabel;
    javax.swing.JLabel subjectLabel;
    javax.swing.JLabel subjectLabel1;
    javax.swing.JLabel subjectLabel2;
    javax.swing.JTextField subjectTextField;
    javax.swing.JComboBox targetVersionComboBox;
    javax.swing.JLabel targetVersionLabel;
    org.openide.awt.Toolbar toolbar;
    javax.swing.JComboBox trackerComboBox;
    javax.swing.JButton updateButton;
    com.kenai.redminenb.util.markup.TextilePreview updateCommentHtmlOutputLabel;
    javax.swing.JLabel updateCommentLabel;
    javax.swing.JScrollPane updateCommentScrollPane1;
    javax.swing.JTabbedPane updateCommentTabPanel;
    javax.swing.JTextArea updateCommentTextArea;
    javax.swing.JLabel updatedLabel;
    javax.swing.JLabel updatedValueLabel;
    final com.kenai.redminenb.util.LinkButton versionAddButton = new com.kenai.redminenb.util.LinkButton();
    final com.kenai.redminenb.util.LinkButton wikiSyntaxButton = new com.kenai.redminenb.util.LinkButton();
    // End of variables declaration//GEN-END:variables
}
