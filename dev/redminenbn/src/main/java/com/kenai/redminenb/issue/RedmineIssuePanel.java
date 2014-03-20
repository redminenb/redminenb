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
import com.toedter.calendar.JDateChooser;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import org.apache.commons.lang.StringUtils;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.netbeans.modules.team.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.commons.LinkButton;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.DropDownButtonFactory;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Toolbar;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
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

        headPane = new JPanel();
        headerLabel = new JLabel();
        parentHeaderPanel = new JPanel();
        updatedLabel = new JLabel();
        createdLabel = new JLabel();
        createdValueLabel = new JLabel();
        updatedValueLabel = new JLabel();
        buttonPane = new JPanel();
        updateButton = new JButton();
        createButton = new JButton();
        toolbar = new Toolbar();
        issuePane = new JPanel();
        descriptionLabel = new JLabel();
        categoryComboBox = new JComboBox();
        priorityLabel = new JLabel();
        estimateTimeLabel1 = new JLabel();
        subjectLabel = new JLabel();
        estimateTimeLabel = new JLabel();
        subjectLabel1 = new JLabel();
        parentIdLabel = new JLabel();
        doneLabel = new JLabel();
        dueDateChooser = new JDateChooser();
        trackerComboBox = new JComboBox();
        estimateTimeTextField = new JFormattedTextField(NumberFormat.getNumberInstance());
        startDateLabel = new JLabel();
        dueDateLabel = new JLabel();
        statusComboBox = new JComboBox();
        priorityComboBox = new JComboBox();
        doneComboBox = new JComboBox();
        subjectTextField = new JTextField();
        descScrollPane = new JScrollPane();
        descTextArea = new JTextArea();
        categoryLabel = new JLabel();
        statusLabel = new JLabel();
        parentTaskTextField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        targetVersionComboBox = new JComboBox();
        targetVersionLabel = new JLabel();
        assigneeLabel = new JLabel();
        startDateChooser = new JDateChooser();
        subjectLabel2 = new JLabel();
        assigneeComboBox = new JComboBox();
        privateCheckBox = new JCheckBox();
        htmlOutputLabel = new JLabel();

        setOpaque(false);

        headPane.setBackground(new Color(255, 255, 255));

        headerLabel.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.headerLabel.text")); // NOI18N

        updatedLabel.setFont(updatedLabel.getFont().deriveFont(updatedLabel.getFont().getSize()-2f));
        updatedLabel.setForeground(new Color(128, 128, 128));
        updatedLabel.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.updatedLabel.text")); // NOI18N

        createdLabel.setFont(createdLabel.getFont().deriveFont(createdLabel.getFont().getSize()-2f));
        createdLabel.setForeground(new Color(128, 128, 128));
        createdLabel.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.createdLabel.text")); // NOI18N

        createdValueLabel.setFont(createdValueLabel.getFont().deriveFont(createdValueLabel.getFont().getSize()-2f));
        createdValueLabel.setForeground(new Color(22, 75, 123));
        createdValueLabel.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.createdValueLabel.text")); // NOI18N

        updatedValueLabel.setFont(updatedValueLabel.getFont().deriveFont(updatedValueLabel.getFont().getSize()-2f));
        updatedValueLabel.setForeground(new Color(22, 75, 123));
        updatedValueLabel.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.updatedValueLabel.text")); // NOI18N

        GroupLayout headPaneLayout = new GroupLayout(headPane);
        headPane.setLayout(headPaneLayout);
        headPaneLayout.setHorizontalGroup(
            headPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(headPaneLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(headPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(headPaneLayout.createSequentialGroup()
                        .addComponent(createdLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(createdValueLabel)
                        .addGap(18, 18, 18)
                        .addComponent(updatedLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(updatedValueLabel))
                    .addComponent(parentHeaderPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(headerLabel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        headPaneLayout.setVerticalGroup(
            headPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(headPaneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(parentHeaderPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(headerLabel, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addGroup(headPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(createdLabel)
                    .addComponent(updatedLabel)
                    .addComponent(createdValueLabel)
                    .addComponent(updatedValueLabel))
                .addGap(8, 8, 8))
        );

        buttonPane.setOpaque(false);

        updateButton.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.updateButton.text")); // NOI18N
        updateButton.setToolTipText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.updateButton.toolTipText")); // NOI18N
        updateButton.setSelected(true);
        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        createButton.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.createButton.text")); // NOI18N
        createButton.setToolTipText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.createButton.toolTipText")); // NOI18N
        createButton.setSelected(true);
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                createButtonActionPerformed(evt);
            }
        });

        infoLabel.setIcon(new ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/info.png"))); // NOI18N

        toolbar.setRollover(true);
        toolbar.setBorderPainted(false);
        toolbar.setOpaque(false);

        GroupLayout buttonPaneLayout = new GroupLayout(buttonPane);
        buttonPane.setLayout(buttonPaneLayout);
        buttonPaneLayout.setHorizontalGroup(
            buttonPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(buttonPaneLayout.createSequentialGroup()
                .addComponent(createButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(updateButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(infoLabel, GroupLayout.PREFERRED_SIZE, 360, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(toolbar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        buttonPaneLayout.setVerticalGroup(
            buttonPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(buttonPaneLayout.createSequentialGroup()
                .addGroup(buttonPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(buttonPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(createButton)
                        .addComponent(updateButton))
                    .addComponent(infoLabel, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
                    .addComponent(toolbar, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        issuePane.setOpaque(false);

        descriptionLabel.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.descriptionLabel.text")); // NOI18N

        priorityLabel.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.priorityLabel.text")); // NOI18N

        estimateTimeLabel1.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.estimateTimeLabel1.text")); // NOI18N

        subjectLabel.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.subjectLabel.text")); // NOI18N

        estimateTimeLabel.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.estimateTimeLabel.text")); // NOI18N

        subjectLabel1.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.subjectLabel1.text")); // NOI18N

        parentIdLabel.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.parentIdLabel.text")); // NOI18N

        doneLabel.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.doneLabel.text")); // NOI18N

        dueDateChooser.setOpaque(false);

        trackerComboBox.setModel(new DefaultComboBoxModel(new String[] { "Bug", "Feature", "Support" }));

        startDateLabel.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.startDateLabel.text")); // NOI18N

        dueDateLabel.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.dueDateLabel.text")); // NOI18N

        statusComboBox.setModel(new DefaultComboBoxModel(new String[] { "New" }));

        priorityComboBox.setModel(new DefaultComboBoxModel(new String[] { "Low", "Normal", "High", "Urgent", "Immediate" }));
        priorityComboBox.setSelectedIndex(1);

        doneComboBox.setMaximumRowCount(11);
        doneComboBox.setModel(new DefaultComboBoxModel(new String[] { "0 %", "10 %", "20 %", "30 %", "40 %", "50 %", "60 %", "70 %", "80 %", "90 %", "100 %" }));

        subjectTextField.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.subjectTextField.text")); // NOI18N

        descTextArea.setColumns(20);
        descScrollPane.setViewportView(descTextArea);

        seenButton.setIcon(new ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/help.png"))); // NOI18N
        seenButton.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.seenButton.text")); // NOI18N
        seenButton.setFont(new Font("Lucida Grande", 0, 11)); // NOI18N
        seenButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                seenButtonActionPerformed(evt);
            }
        });

        categoryLabel.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.categoryLabel.text")); // NOI18N

        statusLabel.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.statusLabel.text")); // NOI18N

        categoryAddButton.setIcon(new ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/add.png"))); // NOI18N
        categoryAddButton.setToolTipText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.categoryAddButton.toolTipText")); // NOI18N
        categoryAddButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                categoryAddButtonActionPerformed(evt);
            }
        });

        targetVersionLabel.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.targetVersionLabel.text")); // NOI18N

        versionAddButton.setIcon(new ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/add.png"))); // NOI18N
        versionAddButton.setToolTipText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.versionAddButton.toolTipText")); // NOI18N
        versionAddButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                versionAddButtonActionPerformed(evt);
            }
        });

        assigneeLabel.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.assigneeLabel.text")); // NOI18N

        startDateChooser.setOpaque(false);

        subjectLabel2.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.subjectLabel2.text")); // NOI18N

        projectNameButton.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.projectNameButton.text")); // NOI18N
        projectNameButton.setToolTipText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.projectNameButton.toolTipText")); // NOI18N
        projectNameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                projectNameButtonActionPerformed(evt);
            }
        });

        assignToMeButton.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.assignToMeButton.text")); // NOI18N
        assignToMeButton.setFont(new Font("Lucida Grande", 0, 11)); // NOI18N
        assignToMeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                assignToMeButtonActionPerformed(evt);
            }
        });

        privateCheckBox.setText(NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.privateCheckBox.text")); // NOI18N

        htmlOutputLabel.setFont(htmlOutputLabel.getFont().deriveFont(htmlOutputLabel.getFont().getSize()-2f));
        htmlOutputLabel.setVerticalAlignment(SwingConstants.TOP);
        htmlOutputLabel.setVerticalTextPosition(SwingConstants.TOP);

        GroupLayout issuePaneLayout = new GroupLayout(issuePane);
        issuePane.setLayout(issuePaneLayout);
        issuePaneLayout.setHorizontalGroup(
            issuePaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, issuePaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel)
                    .addComponent(statusLabel)
                    .addComponent(priorityLabel)
                    .addComponent(assigneeLabel)
                    .addComponent(subjectLabel)
                    .addComponent(subjectLabel1)
                    .addComponent(seenButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(categoryLabel)
                    .addComponent(targetVersionLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(subjectTextField)
                    .addGroup(issuePaneLayout.createSequentialGroup()
                        .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(descScrollPane, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 613, GroupLayout.PREFERRED_SIZE)
                            .addGroup(GroupLayout.Alignment.LEADING, issuePaneLayout.createSequentialGroup()
                                .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addGroup(issuePaneLayout.createSequentialGroup()
                                        .addComponent(trackerComboBox, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(privateCheckBox))
                                    .addGroup(issuePaneLayout.createSequentialGroup()
                                        .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addComponent(targetVersionComboBox, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 233, GroupLayout.PREFERRED_SIZE)
                                            .addGroup(GroupLayout.Alignment.TRAILING, issuePaneLayout.createSequentialGroup()
                                                .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                    .addComponent(statusComboBox, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(priorityComboBox, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE))
                                                .addGap(53, 53, 53))
                                            .addComponent(assigneeComboBox, GroupLayout.PREFERRED_SIZE, 233, GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addComponent(versionAddButton, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
                                            .addComponent(assignToMeButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(issuePaneLayout.createSequentialGroup()
                                        .addComponent(categoryComboBox, GroupLayout.PREFERRED_SIZE, 233, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(categoryAddButton, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)))
                                .addGap(60, 60, 60)
                                .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(dueDateLabel)
                                    .addComponent(startDateLabel)
                                    .addComponent(parentIdLabel)
                                    .addComponent(subjectLabel2)
                                    .addComponent(doneLabel)
                                    .addComponent(estimateTimeLabel))
                                .addGap(8, 8, 8)
                                .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addGroup(issuePaneLayout.createSequentialGroup()
                                        .addComponent(estimateTimeTextField, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(estimateTimeLabel1))
                                    .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(doneComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(startDateChooser, GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                                        .addComponent(dueDateChooser, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(parentTaskTextField, GroupLayout.PREFERRED_SIZE, 74, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(projectNameButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(htmlOutputLabel, GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)))
                .addContainerGap())
        );
        issuePaneLayout.setVerticalGroup(
            issuePaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(issuePaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(subjectLabel1)
                    .addComponent(trackerComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(subjectLabel2)
                    .addComponent(projectNameButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(privateCheckBox))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(subjectTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(subjectLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(issuePaneLayout.createSequentialGroup()
                        .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(issuePaneLayout.createSequentialGroup()
                                .addComponent(descriptionLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(seenButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addComponent(descScrollPane, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(statusComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(statusLabel)
                            .addComponent(parentIdLabel)
                            .addComponent(parentTaskTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(8, 8, 8)
                        .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(priorityLabel)
                                .addComponent(priorityComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(startDateLabel, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
                            .addComponent(startDateChooser, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(assigneeLabel)
                                .addComponent(dueDateLabel, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
                                .addComponent(assignToMeButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(assigneeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addComponent(dueDateChooser, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(categoryAddButton, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(categoryLabel)
                                .addComponent(categoryComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(estimateTimeLabel)
                                .addComponent(estimateTimeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(estimateTimeLabel1)))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(versionAddButton, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(issuePaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(targetVersionLabel)
                                .addComponent(targetVersionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(doneLabel)
                                .addComponent(doneComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
                    .addComponent(htmlOutputLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(headPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(buttonPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(issuePane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(headPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(issuePane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void createButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
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

    private void updateButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
       if (isIssueValid()) {
          saveIssue();
       }
    }//GEN-LAST:event_updateButtonActionPerformed

   private void assignToMeButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_assignToMeButtonActionPerformed
      assigneeComboBox.setSelectedItem(redmineIssue.getRepository().getCurrentUser());
   }//GEN-LAST:event_assignToMeButtonActionPerformed

   private void projectNameButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_projectNameButtonActionPerformed
      try {
         URL url = new URL(redmineIssue.getRepository().getUrl() + "/projects/"
                 + redmineIssue.getRepository().getProject().getId()); // NOI18N
         HtmlBrowser.URLDisplayer.getDefault().showURL(url);
      } catch (IOException ex) {
         Redmine.LOG.log(Level.INFO, "Unable to show the issue's project in the browser.", ex); // NOI18N
      }
   }//GEN-LAST:event_projectNameButtonActionPerformed

   private void versionAddButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_versionAddButtonActionPerformed
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

   private void categoryAddButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_categoryAddButtonActionPerformed
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

   private void seenButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_seenButtonActionPerformed
      try {
         HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(
                 redmineIssue.getRepository().getUrl() + "/help/wiki_syntax.html"));
      } catch (MalformedURLException ex) {
         Exceptions.printStackTrace(ex);
      }
   }//GEN-LAST:event_seenButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final LinkButton assignToMeButton = new LinkButton();
    private JComboBox assigneeComboBox;
    private JLabel assigneeLabel;
    JPanel buttonPane;
    final LinkButton categoryAddButton = new LinkButton();
    private JComboBox categoryComboBox;
    private JLabel categoryLabel;
    private JButton createButton;
    private JLabel createdLabel;
    private JLabel createdValueLabel;
    private JScrollPane descScrollPane;
    JTextArea descTextArea;
    private JLabel descriptionLabel;
    private JComboBox doneComboBox;
    private JLabel doneLabel;
    private JDateChooser dueDateChooser;
    private JLabel dueDateLabel;
    private JLabel estimateTimeLabel;
    private JLabel estimateTimeLabel1;
    private JFormattedTextField estimateTimeTextField;
    private JPanel headPane;
    private JLabel headerLabel;
    private JLabel htmlOutputLabel;
    final JLabel infoLabel = new JLabel();
    private JPanel issuePane;
    private JPanel parentHeaderPanel;
    private JLabel parentIdLabel;
    private JFormattedTextField parentTaskTextField;
    private JComboBox priorityComboBox;
    private JLabel priorityLabel;
    private JCheckBox privateCheckBox;
    final LinkButton projectNameButton = new LinkButton();
    final LinkButton seenButton = new LinkButton();
    private JDateChooser startDateChooser;
    private JLabel startDateLabel;
    private JComboBox statusComboBox;
    private JLabel statusLabel;
    private JLabel subjectLabel;
    private JLabel subjectLabel1;
    private JLabel subjectLabel2;
    private JTextField subjectTextField;
    private JComboBox targetVersionComboBox;
    private JLabel targetVersionLabel;
    private Toolbar toolbar;
    private JComboBox trackerComboBox;
    private JButton updateButton;
    private JLabel updatedLabel;
    private JLabel updatedValueLabel;
    final LinkButton versionAddButton = new LinkButton();
    // End of variables declaration//GEN-END:variables
}
