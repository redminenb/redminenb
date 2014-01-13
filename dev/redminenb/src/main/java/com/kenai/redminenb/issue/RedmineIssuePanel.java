package com.kenai.redminenb.issue;

import com.kenai.redminenb.Redmine;
import com.kenai.redminenb.ui.Defaults;
import com.kenai.redminenb.user.RedmineUser;
import com.kenai.redminenb.util.ListComboBoxModel;
import com.kenai.redminenb.util.RedmineUtil;
import com.kenai.redminenb.util.markup.TextileUtil;

import com.kenai.redminenb.api.Helper;
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
import java.io.IOException;
import java.io.StringWriter;
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
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.netbeans.modules.bugtracking.team.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.DropDownButtonFactory;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.RequestProcessor;

/**
 * Panel showing a Redmine Issue.
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
public class RedmineIssuePanel extends JPanel {

   private static final long serialVersionUID = 9011030935877495476L;
   //
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

      privateCheckBox.setVisible(false);
   }

   void updateTextileOutput() {
      String text = descTextArea.getText();
      if (StringUtils.isNotBlank(text)) {
         //text = TextileUtil.getTextileMarkupParser().parseToHtml(text);
         //text = "<html>" + text.substring(text.indexOf("<body>"));
         StringWriter writer = new StringWriter();

         HtmlDocumentBuilder builder = new HtmlDocumentBuilder(writer);
         // avoid the <html> and <body> tags
         builder.setEmitAsDocument(false);

         MarkupParser parser = TextileUtil.getTextileMarkupParser();
         parser.setBuilder(builder);
         parser.parse(text);
         parser.setBuilder(null);
         text = "<html>" + writer.toString() + "</html>";
      }
      htmlOutputLabel.setText(text);
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

   final void initIssue() {
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

      projectNameButton.setText(redmineIssue.getRepository().getProject().getName());

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

         IssuePriority ip = Helper.getIssuePriority(issue);
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

         if (redmineIssue.hasParent()) {
            final String parentKey = String.valueOf(issue.getParentId());
            RP.post(new Runnable() {
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
         // new issue
         setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
         trackerComboBox.setSelectedItem(0);
         statusComboBox.setSelectedIndex(0);
         priorityComboBox.setSelectedItem(Helper.getDefaultIssuePriority());
         categoryComboBox.setSelectedItem(null);

         subjectTextField.setText(null);
         parentTaskTextField.setValue(null);
         descTextArea.setText(null);
         assigneeComboBox.setSelectedItem(null);
         targetVersionComboBox.setSelectedItem(null);
         startDateChooser.setDate(null);
         dueDateChooser.setDate(null);
         estimateTimeTextField.setValue(null);
         doneComboBox.setSelectedIndex(0);
      }
      setInfoMessage(null);
      updateTextileOutput();
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

   private void setIssueData(com.taskadapter.redmineapi.bean.Issue issue) {
      issue.setTracker((Tracker)trackerComboBox.getSelectedItem());
      issue.setStatusId(((IssueStatus)statusComboBox.getSelectedItem()).getId());

      issue.setSubject(subjectTextField.getText());
      issue.setParentId(getParentTaskId());
      issue.setDescription(descTextArea.getText());
      //issue.setPriorityId(priorityComboBox.getSelectedItem());
      issue.setAssignee(getSelectedAssignee());
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

   private boolean createIssue(com.taskadapter.redmineapi.bean.Issue issue) {
      try {
         setIssueData(issue);
         this.redmineIssue.setIssue(this.redmineIssue.getRepository().getManager().createIssue(
                 this.redmineIssue.getRepository().getProject().getIdentifier(), issue));

         initIssue();
         setInfoMessage("Issue successfully created.");
         return true;

      } catch (Exception ex) {
         Redmine.LOG.log(Level.SEVERE, "Can't create Redmine issue", ex);
         setErrorMessage("Can't create Redmine issue: " + ex.getMessage());
      }
      return false;
   }

   private boolean saveIssue() {
      try {
         com.taskadapter.redmineapi.bean.Issue issue = this.redmineIssue.getIssue();
         setIssueData(issue);
         redmineIssue.getRepository().getManager().update(issue);
         redmineIssue.refresh();

         initIssue();
         setInfoMessage("Issue successfully saved.");
         return true;

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
      ListComboBoxModel<RepositoryUser> model = new ListComboBoxModel<RepositoryUser>();
      model.add(null);
      model.addAll(redmineIssue.getRepository().getUsers());
      assigneeComboBox.setModel(model);

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
      dueDateChooser = new com.toedter.calendar.JDateChooser();
      trackerComboBox = new javax.swing.JComboBox();
      estimateTimeTextField = new JFormattedTextField(NumberFormat.getNumberInstance());
      startDateLabel = new javax.swing.JLabel();
      dueDateLabel = new javax.swing.JLabel();
      statusComboBox = new javax.swing.JComboBox();
      priorityComboBox = new javax.swing.JComboBox();
      doneComboBox = new javax.swing.JComboBox();
      subjectTextField = new javax.swing.JTextField();
      descScrollPane = new javax.swing.JScrollPane();
      descTextArea = new javax.swing.JTextArea();
      categoryLabel = new javax.swing.JLabel();
      statusLabel = new javax.swing.JLabel();
      parentTaskTextField = new JFormattedTextField(NumberFormat.getIntegerInstance());
      targetVersionComboBox = new javax.swing.JComboBox();
      targetVersionLabel = new javax.swing.JLabel();
      assigneeLabel = new javax.swing.JLabel();
      startDateChooser = new com.toedter.calendar.JDateChooser();
      subjectLabel2 = new javax.swing.JLabel();
      assigneeComboBox = new javax.swing.JComboBox();
      privateCheckBox = new javax.swing.JCheckBox();
      htmlOutputLabel = new javax.swing.JLabel();

      setOpaque(false);

      headPane.setBackground(new java.awt.Color(255, 255, 255));

      headerLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.headerLabel.text")); // NOI18N

      updatedLabel.setFont(updatedLabel.getFont().deriveFont(updatedLabel.getFont().getSize()-2f));
      updatedLabel.setForeground(new java.awt.Color(128, 128, 128));
      updatedLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.updatedLabel.text")); // NOI18N

      createdLabel.setFont(createdLabel.getFont().deriveFont(createdLabel.getFont().getSize()-2f));
      createdLabel.setForeground(new java.awt.Color(128, 128, 128));
      createdLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.createdLabel.text")); // NOI18N

      createdValueLabel.setFont(createdValueLabel.getFont().deriveFont(createdValueLabel.getFont().getSize()-2f));
      createdValueLabel.setForeground(new java.awt.Color(22, 75, 123));
      createdValueLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.createdValueLabel.text")); // NOI18N

      updatedValueLabel.setFont(updatedValueLabel.getFont().deriveFont(updatedValueLabel.getFont().getSize()-2f));
      updatedValueLabel.setForeground(new java.awt.Color(22, 75, 123));
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
                  .addComponent(updatedValueLabel))
               .addComponent(parentHeaderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addComponent(headerLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
            .addGap(8, 8, 8))
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
            .addComponent(infoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
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

      descTextArea.setColumns(20);
      descScrollPane.setViewportView(descTextArea);

      seenButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/help.png"))); // NOI18N
      seenButton.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.seenButton.text")); // NOI18N
      seenButton.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
      seenButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            seenButtonActionPerformed(evt);
         }
      });

      categoryLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.categoryLabel.text")); // NOI18N

      statusLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.statusLabel.text")); // NOI18N

      categoryAddButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/add.png"))); // NOI18N
      categoryAddButton.setToolTipText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.categoryAddButton.toolTipText")); // NOI18N
      categoryAddButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            categoryAddButtonActionPerformed(evt);
         }
      });

      targetVersionLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.targetVersionLabel.text")); // NOI18N

      versionAddButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/add.png"))); // NOI18N
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

      assignToMeButton.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.assignToMeButton.text")); // NOI18N
      assignToMeButton.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
      assignToMeButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            assignToMeButtonActionPerformed(evt);
         }
      });

      privateCheckBox.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.privateCheckBox.text")); // NOI18N

      htmlOutputLabel.setFont(htmlOutputLabel.getFont().deriveFont(htmlOutputLabel.getFont().getSize()-2f));
      htmlOutputLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
      htmlOutputLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

      javax.swing.GroupLayout issuePaneLayout = new javax.swing.GroupLayout(issuePane);
      issuePane.setLayout(issuePaneLayout);
      issuePaneLayout.setHorizontalGroup(
         issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, issuePaneLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(descriptionLabel)
               .addComponent(statusLabel)
               .addComponent(priorityLabel)
               .addComponent(assigneeLabel)
               .addComponent(subjectLabel)
               .addComponent(subjectLabel1)
               .addComponent(seenButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(categoryLabel)
               .addComponent(targetVersionLabel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(subjectTextField)
               .addGroup(issuePaneLayout.createSequentialGroup()
                  .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(descScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 613, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, issuePaneLayout.createSequentialGroup()
                        .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                           .addGroup(issuePaneLayout.createSequentialGroup()
                              .addComponent(trackerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                              .addGap(18, 18, 18)
                              .addComponent(privateCheckBox))
                           .addGroup(issuePaneLayout.createSequentialGroup()
                              .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(targetVersionComboBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, issuePaneLayout.createSequentialGroup()
                                    .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                       .addComponent(statusComboBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                       .addComponent(priorityComboBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(53, 53, 53))
                                 .addComponent(assigneeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE))
                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                              .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(versionAddButton, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(assignToMeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                           .addGroup(issuePaneLayout.createSequentialGroup()
                              .addComponent(categoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                              .addComponent(categoryAddButton, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(60, 60, 60)
                        .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                           .addComponent(dueDateLabel)
                           .addComponent(startDateLabel)
                           .addComponent(parentIdLabel)
                           .addComponent(subjectLabel2)
                           .addComponent(doneLabel)
                           .addComponent(estimateTimeLabel))
                        .addGap(8, 8, 8)
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
                              .addComponent(projectNameButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                  .addComponent(htmlOutputLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)))
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
               .addComponent(projectNameButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(privateCheckBox))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(subjectTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(subjectLabel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(issuePaneLayout.createSequentialGroup()
                  .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(issuePaneLayout.createSequentialGroup()
                        .addComponent(descriptionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(seenButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(descScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(statusComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(statusLabel)
                     .addComponent(parentIdLabel)
                     .addComponent(parentTaskTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                  .addGap(8, 8, 8)
                  .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(priorityLabel)
                        .addComponent(priorityComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(startDateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(startDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(assigneeLabel)
                        .addComponent(dueDateLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(assignToMeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(assigneeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                        .addComponent(doneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
               .addComponent(htmlOutputLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())
      );

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(headPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(buttonPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addComponent(issuePane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
            .addContainerGap(16, Short.MAX_VALUE))
      );
   }// </editor-fold>//GEN-END:initComponents

    private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
       if (isIssueValid()) {
          com.taskadapter.redmineapi.bean.Issue issue = this.redmineIssue.getIssue();

          if (issue == null) {
             issue = new com.taskadapter.redmineapi.bean.Issue();
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

   private void assignToMeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assignToMeButtonActionPerformed
      assigneeComboBox.setSelectedItem(redmineIssue.getRepository().getCurrentUser());
   }//GEN-LAST:event_assignToMeButtonActionPerformed

   private void projectNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectNameButtonActionPerformed
      try {
         URL url = new URL(redmineIssue.getRepository().getUrl() + "/projects/"
                 + redmineIssue.getRepository().getProject().getId()); // NOI18N
         HtmlBrowser.URLDisplayer.getDefault().showURL(url);
      } catch (IOException ex) {
         Redmine.LOG.log(Level.INFO, "Unable to show the issue's project in the browser.", ex); // NOI18N
      }
   }//GEN-LAST:event_projectNameButtonActionPerformed

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

   private void seenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seenButtonActionPerformed
      try {
         HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(
                 redmineIssue.getRepository().getUrl() + "/help/wiki_syntax.html"));
      } catch (MalformedURLException ex) {
         Exceptions.printStackTrace(ex);
      }
   }//GEN-LAST:event_seenButtonActionPerformed
   // Variables declaration - do not modify//GEN-BEGIN:variables
   final org.netbeans.modules.bugtracking.util.LinkButton assignToMeButton = new org.netbeans.modules.bugtracking.util.LinkButton();
   private javax.swing.JComboBox assigneeComboBox;
   private javax.swing.JLabel assigneeLabel;
   javax.swing.JPanel buttonPane;
   final org.netbeans.modules.bugtracking.util.LinkButton categoryAddButton = new org.netbeans.modules.bugtracking.util.LinkButton();
   private javax.swing.JComboBox categoryComboBox;
   private javax.swing.JLabel categoryLabel;
   private javax.swing.JButton createButton;
   private javax.swing.JLabel createdLabel;
   private javax.swing.JLabel createdValueLabel;
   private javax.swing.JScrollPane descScrollPane;
   javax.swing.JTextArea descTextArea;
   private javax.swing.JLabel descriptionLabel;
   private javax.swing.JComboBox doneComboBox;
   private javax.swing.JLabel doneLabel;
   private com.toedter.calendar.JDateChooser dueDateChooser;
   private javax.swing.JLabel dueDateLabel;
   private javax.swing.JLabel estimateTimeLabel;
   private javax.swing.JLabel estimateTimeLabel1;
   private javax.swing.JFormattedTextField estimateTimeTextField;
   private javax.swing.JPanel headPane;
   private javax.swing.JLabel headerLabel;
   private javax.swing.JLabel htmlOutputLabel;
   final javax.swing.JLabel infoLabel = new javax.swing.JLabel();
   private javax.swing.JPanel issuePane;
   private javax.swing.JPanel parentHeaderPanel;
   private javax.swing.JLabel parentIdLabel;
   private javax.swing.JFormattedTextField parentTaskTextField;
   private javax.swing.JComboBox priorityComboBox;
   private javax.swing.JLabel priorityLabel;
   private javax.swing.JCheckBox privateCheckBox;
   final org.netbeans.modules.bugtracking.util.LinkButton projectNameButton = new org.netbeans.modules.bugtracking.util.LinkButton();
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
   private javax.swing.JLabel updatedLabel;
   private javax.swing.JLabel updatedValueLabel;
   final org.netbeans.modules.bugtracking.util.LinkButton versionAddButton = new org.netbeans.modules.bugtracking.util.LinkButton();
   // End of variables declaration//GEN-END:variables
}
