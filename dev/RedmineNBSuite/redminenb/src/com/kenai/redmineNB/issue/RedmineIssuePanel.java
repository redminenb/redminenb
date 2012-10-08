package com.kenai.redmineNB.issue;

import com.kenai.redmineNB.Redmine;
import com.kenai.redmineNB.ui.Defaults;
import com.kenai.redmineNB.user.RedmineUser;
import com.kenai.redmineNB.util.RedmineUtil;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.logging.Level;
import javax.swing.*;
import org.apache.commons.lang.StringUtils;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;
import org.netbeans.modules.bugtracking.ui.issue.IssueTopComponent;
import org.netbeans.modules.bugtracking.util.IssueFinderUtils;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.DropDownButtonFactory;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;
import org.redmine.ta.beans.IssueCategory;
import org.redmine.ta.beans.IssueStatus;
import org.redmine.ta.beans.Tracker;
import org.redmine.ta.beans.Version;


/**
 * Panel showing a Redmine Issue.
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
public class RedmineIssuePanel extends javax.swing.JPanel {

   private static final long serialVersionUID = 9011030935877495476L;
   static final RequestProcessor RP = new RequestProcessor("Redmine Issue Panel", 5, false); // NOI18N
   //
   private RedmineIssue redmineIssue;
   //
   private JButton toolbarPopupButton;
   private JPopupMenu toolbarPopup;

   public RedmineIssuePanel(RedmineIssue redmineIssue) {
      this.redmineIssue = redmineIssue;

      initComponents();
      initValues();
      init();
      initIssue();
   }

   private void init() {
      headerLabel.setFont(headerLabel.getFont().deriveFont(headerLabel.getFont().getSize() * 1.5f));
      parentHeaderPanel.setOpaque(false);
      buttonPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));

      ((JLabel)doneComboBox.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);

      toolbar.add(Box.createHorizontalGlue());
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
      //toolbarPopupButton.setAction(a);
      toolbarPopupButton.addActionListener(a);
   }

   private void initIssue() {
      projectNameButton.setText(redmineIssue.getRepository().getProject().getName());

      org.redmine.ta.beans.Issue issue = this.redmineIssue.getIssue();
      if (issue != null) {
         parentHeaderPanel.setVisible(!redmineIssue.isNew());
         headerLabel.setVisible(!redmineIssue.isNew());

         createButton.setVisible(redmineIssue.isNew());
         updateButton.setVisible(!redmineIssue.isNew());
         resetButton.setVisible(!redmineIssue.isNew());
         toolbar.setVisible(!redmineIssue.isNew());

         if (!redmineIssue.isNew()) {
            Dimension dim = headerLabel.getPreferredSize();
            headerLabel.setMinimumSize(new Dimension(0, dim.height));
            headerLabel.setPreferredSize(new Dimension(0, dim.height));
            headerLabel.setText(redmineIssue.getDisplayName());
         }

         trackerComboBox.setSelectedItem(issue.getTracker());
         statusComboBox.setSelectedItem(redmineIssue.getRepository().getStatus(issue.getStatusId()));
         priorityComboBox.setSelectedItem(issue.getPriorityText());
         categoryComboBox.setSelectedItem(issue.getCategory());
         // TODO fix problem with IssueCategory's project and assignee

         subjectTextField.setText(issue.getSubject());
         parentTaskTextField.setValue(issue.getParentId());
         descriptionTextArea.setText(issue.getDescription());
         if (priorityComboBox.getSelectedIndex() < 0) {
            priorityComboBox.addItem(issue.getPriorityText());
            priorityComboBox.setSelectedItem(issue.getPriorityText());
            priorityComboBox.setSelectedItem(issue.getPriorityText());
         }
         assigneeComboBox.setSelectedUser(issue.getAssignee());
         targetVersionComboBox.setSelectedItem(issue.getTargetVersion());
         startDateChooser.setDate(issue.getStartDate());
         dueDateChooser.setDate(issue.getDueDate());
         estimateTimeTextField.setValue(issue.getEstimatedHours());
         doneComboBox.setSelectedIndex(Math.round(issue.getDoneRatio() / 10));

         Integer parentId = issue.getParentId();
         boolean hasParent = parentId != null;
         if (hasParent) {
            final String parentKey = String.valueOf(parentId);
            RP.post(new Runnable() {
               @Override
               public void run() {
                  RedmineIssue parentIssue = (RedmineIssue)redmineIssue.getRepository().getIssueCache().getIssue(parentKey);
                  if (parentIssue == null) {
                     parentIssue = redmineIssue.getRepository().getIssue(parentKey);
                  }
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
                        headerLabel.setIcon(ImageUtilities.loadImageIcon("com/kenai/redmineNB/resources/subtask.png", true)); // NOI18N
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
                                layout.createSequentialGroup().addComponent(parentButton).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(parentLabel));
                        layout.setVerticalGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(parentButton).addComponent(parentLabel));
                        parentHeaderPanel.setLayout(layout);
                        parentHeaderPanel.setMinimumSize(parentHeaderPanel.getPreferredSize());
                     }

                  });
               }

            });
         } else {
            // no parent issue
            //parentHeaderPanel.setVisible(false);
            parentHeaderPanel.setVisible(true);
            parentHeaderPanel.removeAll();
            headerLabel.setIcon(null);
         }

      } else {
         headerLabel.setText(null);
         parentHeaderPanel.setVisible(false);
         headerLabel.setVisible(false);

         createButton.setVisible(true);
         updateButton.setVisible(false);
         resetButton.setVisible(false);
         toolbar.setVisible(false);

         trackerComboBox.setSelectedItem(0);
         statusComboBox.setSelectedIndex(0);
         priorityComboBox.setSelectedItem(null);
         categoryComboBox.setSelectedItem(null);

         subjectTextField.setText(null);
         parentTaskTextField.setValue(null);
         descriptionTextArea.setText(null);
         assigneeComboBox.setSelectedItem(null);
         targetVersionComboBox.setSelectedItem(null);
         startDateChooser.setDate(null);
         dueDateChooser.setDate(null);
         estimateTimeTextField.setValue(null);
         doneComboBox.setSelectedIndex(0);
      }
      setInfoMessage(null);
   }

   private synchronized void setInfoMessage(String msg) {
      infoLabel.setVisible(msg != null);
      infoLabel.setText(msg);
      if (msg != null) {
         infoLabel.setIcon(Defaults.getIcon("info.png"));
         autoHide();
      }
   }

   private void setErrorMessage(String msg) {
      infoLabel.setVisible(msg != null);
      infoLabel.setText(msg);
      if (msg != null) {
         infoLabel.setIcon(Defaults.getIcon("error.png"));
         autoHide();
      }
   }

   private void autoHide() {
      RP.post(new Runnable() {
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

   private void setIssueData(org.redmine.ta.beans.Issue issue) {
      issue.setTracker((Tracker)trackerComboBox.getSelectedItem());
      issue.setStatusId(((IssueStatus)statusComboBox.getSelectedItem()).getId());

      issue.setSubject(subjectTextField.getText());
      issue.setParentId(getParentTaskId());
      issue.setDescription(descriptionTextArea.getText());
      //issue.setPriorityId(priorityComboBox.getSelectedItem());
      issue.setAssignee(assigneeComboBox.getSelectedUser() == null ? null : ((RedmineUser)assigneeComboBox.getSelectedUser()).getUser());
      issue.setCategory((IssueCategory)categoryComboBox.getSelectedItem());
      issue.setTargetVersion(targetVersionComboBox.getSelectedItem() == null ? null : (Version)targetVersionComboBox.getSelectedItem());
      issue.setStartDate(startDateChooser.getDate());
      issue.setDueDate(dueDateChooser.getDate());
      issue.setEstimatedHours(getEstimateTime());
      issue.setDoneRatio(doneComboBox.getSelectedIndex() * 10);

      //List<CustomField> customFields = null;
      //...
      //issue.setCustomFields(customFields);
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

   private boolean createIssue(org.redmine.ta.beans.Issue issue) {
      try {
         setIssueData(issue);
         this.redmineIssue.setIssue(this.redmineIssue.getRepository().getManager().createIssue(
                 this.redmineIssue.getRepository().getProject().getIdentifier(), issue));

         initIssue();
         setInfoMessage("Issue successfully created.");
         return true;

//      } catch (IOException ex) {
//         JOptionPane.showMessageDialog(this, Redmine.getMessage("MSG_CONNECTION_ERROR",
//                                                                ex.getLocalizedMessage()));
//         Redmine.LOG.log(Level.SEVERE, "Can't create Redmine issue", ex);
//      } catch (AuthenticationException ex) {
//         JOptionPane.showMessageDialog(this, Redmine.getMessage("MSG_AUTHENTICATION_ERROR",
//                                                                ex.getLocalizedMessage()));
//         Redmine.LOG.log(Level.SEVERE, "Can't create Redmine issue", ex);
//      } catch (NotFoundException ex) {
//         JOptionPane.showMessageDialog(this, Redmine.getMessage("ISSUE_NOT_FOUND_ERROR",
//                                                                ex.getLocalizedMessage()));
//         Redmine.LOG.log(Level.SEVERE, "Can't create Redmine issue", ex);
//      } catch (RedmineException ex) {
//         JOptionPane.showMessageDialog(this, Redmine.getMessage("MSG_REDMINE_ERROR",
//                                                                ex.getLocalizedMessage()));
//         Redmine.LOG.log(Level.SEVERE, "Can't create Redmine issue", ex);
//      }
      } catch (Exception ex) {
         Redmine.LOG.log(Level.SEVERE, "Can't create Redmine issue", ex);
         setErrorMessage("Creating the Issue failed!");
      }
      return false;
   }

   private boolean saveIssue() {
      try {
         org.redmine.ta.beans.Issue issue = this.redmineIssue.getIssue();
         setIssueData(issue);
         redmineIssue.getRepository().getManager().update(issue);
         redmineIssue.refresh();

         initIssue();
         setInfoMessage("Issue successfully saved.");
         return true;

//      } catch (IOException ex) {
//         JOptionPane.showMessageDialog(this, Redmine.getMessage("MSG_CONNECTION_ERROR",
//                                                                ex.getLocalizedMessage()));
//         Redmine.LOG.log(Level.SEVERE, "Can't update Redmine issue", ex);
//      } catch (AuthenticationException ex) {
//         JOptionPane.showMessageDialog(this, Redmine.getMessage("MSG_AUTHENTICATION_ERROR",
//                                                                ex.getLocalizedMessage()));
//         Redmine.LOG.log(Level.SEVERE, "Can't update Redmine issue", ex);
//      } catch (NotFoundException ex) {
//         JOptionPane.showMessageDialog(this, Redmine.getMessage("ISSUE_NOT_FOUND_ERROR",
//                                                                ex.getLocalizedMessage()));
//         Redmine.LOG.log(Level.SEVERE, "Can't update Redmine issue", ex);
//      } catch (RedmineException ex) {
//         JOptionPane.showMessageDialog(this, Redmine.getMessage("MSG_REDMINE_ERROR",
//                                                                ex.getLocalizedMessage()));
//         Redmine.LOG.log(Level.SEVERE, "Can't update Redmine issue", ex);
//      }
      } catch (Exception ex) {
         Redmine.LOG.log(Level.SEVERE, "Can't save Redmine issue", ex);
         setErrorMessage("Saving the Issue failed!");
      }
      return false;
   }

   private void initValues() {
      trackerComboBox.setRenderer(new Defaults.TrackerLCR());
      trackerComboBox.setModel(new DefaultComboBoxModel(redmineIssue.getRepository().getTrackers().toArray()));

      statusComboBox.setRenderer(new Defaults.IssueStatusLCR());
      statusComboBox.setModel(new DefaultComboBoxModel(redmineIssue.getRepository().getStatuses().toArray()));

      priorityComboBox.setRenderer(new Defaults.PriorityLCR());
      priorityComboBox.setModel(new DefaultComboBoxModel(redmineIssue.getRepository().getIssuePriorities().toArray()));

      assigneeComboBox.setRenderer(new Defaults.RepositoryUserLCR());
      assigneeComboBox.setUsers(redmineIssue.getRepository().getUsers());
      ((DefaultComboBoxModel)assigneeComboBox.getModel()).insertElementAt(null, 0);

      categoryComboBox.setRenderer(new Defaults.IssueCategoryLCR());
      categoryComboBox.setModel(new DefaultComboBoxModel(redmineIssue.getRepository().getIssueCategories().toArray()));
      ((DefaultComboBoxModel)categoryComboBox.getModel()).insertElementAt(null, 0);

      targetVersionComboBox.setRenderer(new Defaults.VersionLCR());
      targetVersionComboBox.setModel(new DefaultComboBoxModel(redmineIssue.getRepository().getVersions().toArray()));
      ((DefaultComboBoxModel)targetVersionComboBox.getModel()).insertElementAt(null, 0);
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

      headPane = new javax.swing.JPanel();
      headerLabel = new javax.swing.JLabel();
      parentHeaderPanel = new javax.swing.JPanel();
      buttonPane = new javax.swing.JPanel();
      updateButton = new javax.swing.JButton();
      createButton = new javax.swing.JButton();
      resetButton = new javax.swing.JButton();
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
      dueDateChooser = new com.toedter.calendar.JDateChooser();
      trackerComboBox = new javax.swing.JComboBox();
      estimateTimeTextField = new JFormattedTextField(NumberFormat.getNumberInstance());
      startDateLabel = new javax.swing.JLabel();
      dueDateLabel = new javax.swing.JLabel();
      statusComboBox = new javax.swing.JComboBox();
      priorityComboBox = new javax.swing.JComboBox();
      doneComboBox = new javax.swing.JComboBox();
      subjectTextField = new javax.swing.JTextField();
      jScrollPane1 = new javax.swing.JScrollPane();
      descriptionTextArea = new javax.swing.JTextArea();
      categoryLabel = new javax.swing.JLabel();
      statusLabel = new javax.swing.JLabel();
      parentTaskTextField = new JFormattedTextField(NumberFormat.getIntegerInstance());
      targetVersionComboBox = new javax.swing.JComboBox();
      targetVersionLabel = new javax.swing.JLabel();
      assigneeLabel = new javax.swing.JLabel();
      assigneeComboBox = new com.kenai.redmineNB.user.UserComboBox();
      startDateChooser = new com.toedter.calendar.JDateChooser();
      subjectLabel2 = new javax.swing.JLabel();

      setOpaque(false);

      headPane.setBackground(new java.awt.Color(255, 255, 255));

      headerLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.headerLabel.text")); // NOI18N

      javax.swing.GroupLayout headPaneLayout = new javax.swing.GroupLayout(headPane);
      headPane.setLayout(headPaneLayout);
      headPaneLayout.setHorizontalGroup(
         headPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(headPaneLayout.createSequentialGroup()
            .addGap(12, 12, 12)
            .addGroup(headPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(headPaneLayout.createSequentialGroup()
                  .addGap(10, 10, 10)
                  .addComponent(headerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
               .addComponent(parentHeaderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 687, Short.MAX_VALUE))
            .addContainerGap())
      );
      headPaneLayout.setVerticalGroup(
         headPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(headPaneLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(parentHeaderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(1, 1, 1)
            .addComponent(headerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(19, Short.MAX_VALUE))
      );

      buttonPane.setOpaque(false);

      updateButton.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.updateButton.text")); // NOI18N
      updateButton.setSelected(true);
      updateButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            updateButtonActionPerformed(evt);
         }
      });

      createButton.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.createButton.text")); // NOI18N
      createButton.setSelected(true);
      createButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            createButtonActionPerformed(evt);
         }
      });

      resetButton.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.resetButton.text")); // NOI18N
      resetButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            resetButtonActionPerformed(evt);
         }
      });

      infoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kenai/redmineNB/resources/info.png"))); // NOI18N

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
            .addComponent(resetButton)
            .addGap(18, 18, 18)
            .addComponent(infoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addContainerGap())
      );
      buttonPaneLayout.setVerticalGroup(
         buttonPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(buttonPaneLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(buttonPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
               .addComponent(infoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addGroup(buttonPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                  .addComponent(updateButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(resetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
               .addComponent(createButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

      issuePane.setOpaque(false);

      descriptionLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.descriptionLabel.text")); // NOI18N

      priorityLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.priorityLabel.text")); // NOI18N

      estimateTimeLabel1.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.estimateTimeLabel1.text")); // NOI18N

      subjectLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.subjectLabel.text")); // NOI18N

      estimateTimeLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.estimateTimeLabel.text")); // NOI18N

      subjectLabel1.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.subjectLabel1.text")); // NOI18N

      parentIdLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.parentIdLabel.text")); // NOI18N

      doneLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.doneLabel.text")); // NOI18N

      dueDateChooser.setOpaque(false);

      trackerComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Bug", "Feature", "Support" }));

      startDateLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.startDateLabel.text")); // NOI18N

      dueDateLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.dueDateLabel.text")); // NOI18N

      statusComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "New" }));

      priorityComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Low", "Normal", "High", "Urgent", "Immediate" }));
      priorityComboBox.setSelectedIndex(1);

      doneComboBox.setMaximumRowCount(11);
      doneComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0 %", "10 %", "20 %", "30 %", "40 %", "50 %", "60 %", "70 %", "80 %", "90 %", "100 %" }));

      subjectTextField.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.subjectTextField.text")); // NOI18N

      descriptionTextArea.setColumns(20);
      descriptionTextArea.setRows(5);
      jScrollPane1.setViewportView(descriptionTextArea);

      seenButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kenai/redmineNB/resources/help.png"))); // NOI18N
      seenButton.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.seenButton.text")); // NOI18N
      seenButton.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
      seenButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            seenButtonActionPerformed(evt);
         }
      });

      categoryLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.categoryLabel.text")); // NOI18N

      statusLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.statusLabel.text")); // NOI18N

      categoryAddButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kenai/redmineNB/resources/add.png"))); // NOI18N
      categoryAddButton.setToolTipText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.categoryAddButton.toolTipText")); // NOI18N
      categoryAddButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            categoryAddButtonActionPerformed(evt);
         }
      });

      targetVersionLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.targetVersionLabel.text")); // NOI18N

      versionAddButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kenai/redmineNB/resources/add.png"))); // NOI18N
      versionAddButton.setToolTipText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.versionAddButton.toolTipText")); // NOI18N
      versionAddButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            versionAddButtonActionPerformed(evt);
         }
      });

      assigneeLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.assigneeLabel.text")); // NOI18N

      startDateChooser.setOpaque(false);

      subjectLabel2.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.subjectLabel2.text")); // NOI18N

      projectNameButton.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.projectNameButton.text")); // NOI18N
      projectNameButton.setToolTipText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.projectNameButton.toolTipText")); // NOI18N
      projectNameButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            projectNameButtonActionPerformed(evt);
         }
      });

      javax.swing.GroupLayout issuePaneLayout = new javax.swing.GroupLayout(issuePane);
      issuePane.setLayout(issuePaneLayout);
      issuePaneLayout.setHorizontalGroup(
         issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(issuePaneLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(issuePaneLayout.createSequentialGroup()
                  .addComponent(targetVersionLabel)
                  .addGap(0, 0, Short.MAX_VALUE))
               .addGroup(issuePaneLayout.createSequentialGroup()
                  .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(descriptionLabel)
                     .addComponent(statusLabel)
                     .addComponent(priorityLabel)
                     .addComponent(assigneeLabel)
                     .addComponent(subjectLabel)
                     .addComponent(subjectLabel1)
                     .addComponent(seenButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(categoryLabel))
                  .addGap(12, 12, 12)
                  .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(subjectTextField)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, issuePaneLayout.createSequentialGroup()
                        .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                           .addComponent(trackerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                           .addGroup(issuePaneLayout.createSequentialGroup()
                              .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                 .addComponent(targetVersionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(statusComboBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(priorityComboBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(assigneeComboBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)))
                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(versionAddButton, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                           .addGroup(issuePaneLayout.createSequentialGroup()
                              .addComponent(categoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(categoryAddButton, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 105, Short.MAX_VALUE)
                        .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                           .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, issuePaneLayout.createSequentialGroup()
                              .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(dueDateLabel)
                                 .addComponent(startDateLabel)
                                 .addComponent(parentIdLabel)
                                 .addComponent(subjectLabel2)
                                 .addComponent(doneLabel))
                              .addGap(24, 24, 24))
                           .addGroup(issuePaneLayout.createSequentialGroup()
                              .addComponent(estimateTimeLabel)
                              .addGap(15, 15, 15)))
                        .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                           .addGroup(issuePaneLayout.createSequentialGroup()
                              .addComponent(estimateTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(estimateTimeLabel1))
                           .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                              .addComponent(doneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                              .addComponent(startDateChooser, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                              .addComponent(dueDateChooser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                              .addComponent(parentTaskTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                              .addComponent(projectNameButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
            .addContainerGap())
      );
      issuePaneLayout.setVerticalGroup(
         issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(issuePaneLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(subjectLabel1)
               .addComponent(trackerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(subjectLabel2)
               .addComponent(projectNameButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(subjectTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(subjectLabel))
            .addGap(18, 18, 18)
            .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(issuePaneLayout.createSequentialGroup()
                  .addComponent(descriptionLabel)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(seenButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(16, 16, 16)
            .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(statusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(statusLabel)
               .addComponent(parentIdLabel)
               .addComponent(parentTaskTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(7, 7, 7)
            .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
               .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                  .addComponent(priorityLabel)
                  .addComponent(priorityComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(startDateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(startDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(8, 8, 8)
            .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                  .addComponent(assigneeLabel)
                  .addComponent(assigneeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(dueDateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addComponent(dueDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(categoryAddButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                  .addComponent(categoryLabel)
                  .addComponent(categoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(estimateTimeLabel)
                  .addComponent(estimateTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(estimateTimeLabel1)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(versionAddButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                  .addComponent(targetVersionLabel)
                  .addComponent(targetVersionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(doneLabel)
                  .addComponent(doneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(21, 21, 21))
      );

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(headPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addGap(6, 6, 6)
                  .addComponent(issuePane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
               .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                  .addContainerGap()
                  .addComponent(buttonPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addComponent(headPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(buttonPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(issuePane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );
   }// </editor-fold>//GEN-END:initComponents

    private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
       if (isIssueValid()) {
          org.redmine.ta.beans.Issue issue = this.redmineIssue.getIssue();

          if (issue == null) {
             issue = new org.redmine.ta.beans.Issue();
             this.redmineIssue.setIssue(issue);
          }

          if (createIssue(issue)) {
             createButton.setVisible(false);
             updateButton.setVisible(true);
          }
       }
    }//GEN-LAST:event_createButtonActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
       if (isIssueValid()) {
          saveIssue();
       }
    }//GEN-LAST:event_updateButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
       redmineIssue.refresh();
       initIssue();
       setInfoMessage("Issue successfully reloaded.");
    }//GEN-LAST:event_resetButtonActionPerformed

   private void seenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seenButtonActionPerformed
      try {
         HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(
                 redmineIssue.getRepository().getUrl() + "/help/wiki_syntax.html"));
      } catch (MalformedURLException ex) {
         Exceptions.printStackTrace(ex);
      }
   }//GEN-LAST:event_seenButtonActionPerformed

   private void categoryAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_categoryAddButtonActionPerformed
      NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine("New Category label", "Add a new Category");
      if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION
              && StringUtils.isNotBlank(d.getInputText())) {
         IssueCategory ic = new IssueCategory(redmineIssue.getRepository().getProject(), d.getInputText());
         try {
            redmineIssue.getRepository().getManager().createCategory(ic);
            Collection<? extends IssueCategory> c = redmineIssue.getRepository().reloadIssueCategories();
            for (IssueCategory issueCategory : c) {
               if (ic.getName().equals(issueCategory.getName())) {
                  ic = issueCategory;
                  break;
               }
            }
            categoryComboBox.setModel(new DefaultComboBoxModel(c.toArray()));
            categoryComboBox.setSelectedItem(ic);

         } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
         }
      }
   }//GEN-LAST:event_categoryAddButtonActionPerformed

   private void versionAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_versionAddButtonActionPerformed
      NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine("New Version Name", "Add a new Version");
      if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION
              && StringUtils.isNotBlank(d.getInputText())) {
         Version v = new Version(redmineIssue.getRepository().getProject(), d.getInputText());
         try {
            redmineIssue.getRepository().getManager().createVersion(v);
            Collection<? extends Version> c = redmineIssue.getRepository().reloadVersions();
            for (Version version : c) {
               if (v.getName().equals(version.getName())) {
                  v = version;
                  break;
               }
            }
            targetVersionComboBox.setModel(new DefaultComboBoxModel(c.toArray()));
            targetVersionComboBox.setSelectedItem(v);

         } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
         }
      }
   }//GEN-LAST:event_versionAddButtonActionPerformed

   private void projectNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectNameButtonActionPerformed
      try {
         URL url = new URL(redmineIssue.getRepository().getUrl() + "/projects/" + redmineIssue.getIssue().getProject().getId()); // NOI18N
         HtmlBrowser.URLDisplayer.getDefault().showURL(url);
      } catch (IOException ex) {
         Redmine.LOG.log(Level.INFO, "Unable to show the issue's project in the browser.", ex); // NOI18N
      }

   }//GEN-LAST:event_projectNameButtonActionPerformed

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private com.kenai.redmineNB.user.UserComboBox assigneeComboBox;
   private javax.swing.JLabel assigneeLabel;
   javax.swing.JPanel buttonPane;
   final org.netbeans.modules.bugtracking.util.LinkButton categoryAddButton = new org.netbeans.modules.bugtracking.util.LinkButton();
   private javax.swing.JComboBox categoryComboBox;
   private javax.swing.JLabel categoryLabel;
   private javax.swing.JButton createButton;
   private javax.swing.JLabel descriptionLabel;
   private javax.swing.JTextArea descriptionTextArea;
   private javax.swing.JComboBox doneComboBox;
   private javax.swing.JLabel doneLabel;
   private com.toedter.calendar.JDateChooser dueDateChooser;
   private javax.swing.JLabel dueDateLabel;
   private javax.swing.JLabel estimateTimeLabel;
   private javax.swing.JLabel estimateTimeLabel1;
   private javax.swing.JFormattedTextField estimateTimeTextField;
   private javax.swing.JPanel headPane;
   private javax.swing.JLabel headerLabel;
   final javax.swing.JLabel infoLabel = new javax.swing.JLabel();
   private javax.swing.JPanel issuePane;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JPanel parentHeaderPanel;
   private javax.swing.JLabel parentIdLabel;
   private javax.swing.JFormattedTextField parentTaskTextField;
   private javax.swing.JComboBox priorityComboBox;
   private javax.swing.JLabel priorityLabel;
   final org.netbeans.modules.bugtracking.util.LinkButton projectNameButton = new org.netbeans.modules.bugtracking.util.LinkButton();
   private javax.swing.JButton resetButton;
   final org.netbeans.modules.bugtracking.util.LinkButton seenButton = new org.netbeans.modules.bugtracking.util.LinkButton();
   private com.toedter.calendar.JDateChooser startDateChooser;
   private javax.swing.JLabel startDateLabel;
   private javax.swing.JComboBox statusComboBox;
   private javax.swing.JLabel statusLabel;
   private javax.swing.JLabel subjectLabel;
   private javax.swing.JLabel subjectLabel1;
   private javax.swing.JLabel subjectLabel2;
   private javax.swing.JTextField subjectTextField;
   private javax.swing.JComboBox targetVersionComboBox;
   private javax.swing.JLabel targetVersionLabel;
   private org.openide.awt.Toolbar toolbar;
   private javax.swing.JComboBox trackerComboBox;
   private javax.swing.JButton updateButton;
   final org.netbeans.modules.bugtracking.util.LinkButton versionAddButton = new org.netbeans.modules.bugtracking.util.LinkButton();
   // End of variables declaration//GEN-END:variables
}
