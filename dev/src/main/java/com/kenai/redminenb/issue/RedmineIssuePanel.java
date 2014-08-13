package com.kenai.redminenb.issue;

import com.kenai.redminenb.Redmine;
import com.kenai.redminenb.ui.Defaults;
import com.kenai.redminenb.user.RedmineUser;
import com.kenai.redminenb.util.ListComboBoxModel;
import com.kenai.redminenb.util.RedmineUtil;
import com.kenai.redminenb.util.markup.TextileUtil;

import com.kenai.redminenb.api.Helper;
import com.kenai.redminenb.repository.RedmineRepository;
import com.kenai.redminenb.util.AttachmentDisplay;
import com.kenai.redminenb.util.ExpandablePanel;
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
import com.kenai.redminenb.util.LinkButton;
import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Journal;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntryActivity;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.Box.Filler;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.text.JTextComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.DropDownButtonFactory;
import org.openide.awt.HtmlBrowser;
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
   private static File lastDirectory;
   //
   static final RequestProcessor RP = new RequestProcessor("Redmine Issue Panel", 5, false); // NOI18N
   //
   private RedmineIssue redmineIssue;
   //
   private JButton toolbarPopupButton;
   private JPopupMenu toolbarPopup;
   private ExpandablePanel commentPanel;
   private ExpandablePanel logtimePanel;

   public RedmineIssuePanel(RedmineIssue redmineIssue) {
      this.redmineIssue = redmineIssue;

      initComponents();
      updateCommentTabPanel.setVisible(false);
      commentPanel = new ExpandablePanel(updateCommentLabel, updateCommentTabPanel);
      logtimeInputPanel.setVisible(false);
      logtimePanel = new ExpandablePanel(logtimeLabel, logtimeInputPanel);
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

   void updateCommentTextileOutput() {
      updateTextileRendering(updateCommentTextArea, updateCommentHtmlOutputLabel);
   }
   
   void updateTextileOutput() {
      updateTextileRendering(descTextArea, htmlOutputLabel);
   }
   
   private static void updateTextileRendering(JTextComponent input, JLabel output) {
      String text = input.getText();
      if (StringUtils.isNotBlank(text)) {
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
      output.setText(text);
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

         if(issue.getJournals() != null && issue.getJournals().size() > 0) {
             journalOuterPane.setVisible(true);
         } else {
             journalOuterPane.setVisible(false);
         }
         journalPane.removeAll();
         List<Journal> journalEntries = issue.getJournals();
         for(int i = 0; i < journalEntries.size(); i++) {
             JournalDisplay jdisplay = new JournalDisplay(journalEntries.get(i), i);
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
          lb.setText("Add");
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
                  int result = fileChooser.showOpenDialog(RedmineIssuePanel.this);
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
         journalOuterPane.setVisible(true);
         commentPanel.setVisible(true);
         logtimePanel.setVisible(true);
         descriptionPanel.setSelectedIndex(1);
         spentHoursLabel.setText(NbBundle.getMessage(getClass(), 
                 "RedmineIssuePanel.spentHoursReplacementLabel.text",
                 issue.getSpentHours()));
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
         journalOuterPane.setVisible(false);
         commentPanel.setVisible(false);
         logtimePanel.setVisible(false);
         descriptionPanel.setSelectedIndex(0);
         spentHoursLabel = new javax.swing.JLabel();
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
      issue.setPriorityId(((IssuePriority) priorityComboBox.getSelectedItem()).getId());
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
               String projektId = rr.getProject().getIdentifier();
               Issue issue =  rr.getManager().createIssue(projektId, inputIssue);
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
                redmineIssue.getRepository().getManager().update(issue);
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
      ListComboBoxModel<RedmineUser> model = new ListComboBoxModel<>();
      model.add(null);
      model.addAll(redmineIssue.getRepository().getUsers());
      assigneeComboBox.setModel(model);

      categoryComboBox.setRenderer(new Defaults.IssueCategoryLCR());
      categoryComboBox.setModel(new DefaultComboBoxModel(redmineIssue.getRepository().getIssueCategories().toArray()));
      ((DefaultComboBoxModel)categoryComboBox.getModel()).insertElementAt(null, 0);

      targetVersionComboBox.setRenderer(new Defaults.VersionLCR());
      targetVersionComboBox.setModel(new DefaultComboBoxModel(redmineIssue.getRepository().getVersions().toArray()));
      ((DefaultComboBoxModel)targetVersionComboBox.getModel()).insertElementAt(null, 0);
      
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
        descriptionPanel = new javax.swing.JTabbedPane();
        descScrollPane = new javax.swing.JScrollPane();
        descTextArea = new javax.swing.JTextArea();
        htmlOutputLabel = new javax.swing.JLabel();
        updateCommentLabel = new javax.swing.JLabel();
        updateCommentTabPanel = new javax.swing.JTabbedPane();
        updateCommentScrollPane1 = new javax.swing.JScrollPane();
        updateCommentTextArea = new javax.swing.JTextArea();
        jScrollPane1updateCommentScrollPane2 = new javax.swing.JScrollPane();
        updateCommentHtmlOutputLabel = new javax.swing.JLabel();
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
        journalOuterPane = new javax.swing.JPanel();
        journalPane = new javax.swing.JPanel();
        updateCommentOuterPanel = new javax.swing.JPanel();

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
                .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
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

        wikiSyntaxButton.setBorder(null);
        wikiSyntaxButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/help.png"))); // NOI18N
        wikiSyntaxButton.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.wikiSyntaxButton.text")); // NOI18N
        wikiSyntaxButton.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        wikiSyntaxButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wikiSyntaxButtonActionPerformed(evt);
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

        projectNameButton.setBorder(null);
        projectNameButton.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.projectNameButton.text")); // NOI18N
        projectNameButton.setToolTipText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.projectNameButton.toolTipText")); // NOI18N
        projectNameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectNameButtonActionPerformed(evt);
            }
        });

        assignToMeButton.setBorder(null);
        assignToMeButton.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.assignToMeButton.text")); // NOI18N
        assignToMeButton.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        assignToMeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assignToMeButtonActionPerformed(evt);
            }
        });

        privateCheckBox.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.privateCheckBox.text")); // NOI18N

        descriptionPanel.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        descScrollPane.setMinimumSize(new java.awt.Dimension(22, 120));
        descScrollPane.setPreferredSize(new java.awt.Dimension(223, 120));

        descTextArea.setColumns(20);
        descScrollPane.setViewportView(descTextArea);

        descriptionPanel.addTab(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.descScrollPane.TabConstraints.tabTitle"), descScrollPane); // NOI18N

        htmlOutputLabel.setBackground(new java.awt.Color(255, 255, 255));
        htmlOutputLabel.setFont(htmlOutputLabel.getFont().deriveFont(htmlOutputLabel.getFont().getSize()-2f));
        htmlOutputLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        htmlOutputLabel.setOpaque(true);
        htmlOutputLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        descriptionPanel.addTab(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.htmlOutputLabel.TabConstraints.tabTitle"), htmlOutputLabel); // NOI18N

        updateCommentLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.updateCommentLabel.text")); // NOI18N

        updateCommentTabPanel.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        updateCommentScrollPane1.setMinimumSize(new java.awt.Dimension(22, 80));
        updateCommentScrollPane1.setPreferredSize(new java.awt.Dimension(228, 80));

        updateCommentTextArea.setColumns(20);
        updateCommentScrollPane1.setViewportView(updateCommentTextArea);

        updateCommentTabPanel.addTab(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.updateCommentScrollPane1.TabConstraints.tabTitle"), updateCommentScrollPane1); // NOI18N

        jScrollPane1updateCommentScrollPane2.setMinimumSize(new java.awt.Dimension(22, 80));
        jScrollPane1updateCommentScrollPane2.setPreferredSize(new java.awt.Dimension(228, 120));

        updateCommentHtmlOutputLabel.setBackground(new java.awt.Color(255, 255, 255));
        updateCommentHtmlOutputLabel.setFont(updateCommentHtmlOutputLabel.getFont().deriveFont(updateCommentHtmlOutputLabel.getFont().getSize()-2f));
        updateCommentHtmlOutputLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        updateCommentHtmlOutputLabel.setOpaque(true);
        updateCommentHtmlOutputLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jScrollPane1updateCommentScrollPane2.setViewportView(updateCommentHtmlOutputLabel);

        updateCommentTabPanel.addTab(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.jScrollPane1updateCommentScrollPane2.TabConstraints.tabTitle"), jScrollPane1updateCommentScrollPane2); // NOI18N

        attachmentLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.attachmentLabel.text")); // NOI18N

        attachmentPanel.setOpaque(false);
        attachmentPanel.setLayout(new javax.swing.BoxLayout(attachmentPanel, javax.swing.BoxLayout.PAGE_AXIS));

        logtimeLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.logtimeLabel.text")); // NOI18N

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

        spentHoursLabel.setText(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.spentHoursLabel.text")); // NOI18N

        javax.swing.GroupLayout issuePaneLayout = new javax.swing.GroupLayout(issuePane);
        issuePane.setLayout(issuePaneLayout);
        issuePaneLayout.setHorizontalGroup(
            issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(issuePaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(logtimeInputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(issuePaneLayout.createSequentialGroup()
                        .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(issuePaneLayout.createSequentialGroup()
                                .addComponent(attachmentLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(attachmentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(issuePaneLayout.createSequentialGroup()
                                .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(updateCommentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 879, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(logtimeLabel))
                                .addGap(0, 31, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addComponent(updateCommentTabPanel)
                    .addGroup(issuePaneLayout.createSequentialGroup()
                        .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(descriptionLabel)
                            .addComponent(statusLabel)
                            .addComponent(priorityLabel)
                            .addComponent(assigneeLabel)
                            .addComponent(subjectLabel)
                            .addComponent(subjectLabel1)
                            .addComponent(wikiSyntaxButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(targetVersionLabel)
                            .addComponent(categoryLabel))
                        .addGap(0, 0, 0)
                        .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(subjectTextField)
                            .addGroup(issuePaneLayout.createSequentialGroup()
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
                                .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, issuePaneLayout.createSequentialGroup()
                                        .addComponent(estimateTimeTextField)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(estimateTimeLabel1))
                                    .addGroup(issuePaneLayout.createSequentialGroup()
                                        .addComponent(doneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(spentHoursLabel))
                                    .addComponent(startDateChooser, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                                    .addComponent(dueDateChooser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(parentTaskTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(projectNameButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 181, Short.MAX_VALUE))
                            .addComponent(descriptionPanel)))))
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
                        .addComponent(descriptionLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(wikiSyntaxButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(descriptionPanel))
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
                    .addComponent(versionAddButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(issuePaneLayout.createSequentialGroup()
                        .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(targetVersionLabel)
                            .addComponent(targetVersionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(doneLabel)
                            .addComponent(doneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spentHoursLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(issuePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(attachmentLabel)
                    .addComponent(attachmentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(updateCommentLabel)
                .addGap(0, 0, 0)
                .addComponent(updateCommentTabPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logtimeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logtimeInputPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        journalOuterPane.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(RedmineIssuePanel.class, "RedmineIssuePanel.journalOuterPane.border.title"))); // NOI18N
        journalOuterPane.setOpaque(false);
        journalOuterPane.setLayout(new java.awt.BorderLayout());

        journalPane.setOpaque(false);
        journalPane.setLayout(new javax.swing.BoxLayout(journalPane, javax.swing.BoxLayout.PAGE_AXIS));
        journalOuterPane.add(journalPane, java.awt.BorderLayout.PAGE_START);

        updateCommentOuterPanel.setOpaque(false);
        updateCommentOuterPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(headPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(buttonPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(502, 502, 502)
                        .addComponent(updateCommentOuterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(journalOuterPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(issuePane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(headPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPane, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(issuePane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(journalOuterPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(updateCommentOuterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
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

   private void wikiSyntaxButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_wikiSyntaxButtonActionPerformed
      try {
         HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(
                 redmineIssue.getRepository().getUrl() + "/help/wiki_syntax.html"));
      } catch (MalformedURLException ex) {
         Exceptions.printStackTrace(ex);
      }
   }//GEN-LAST:event_wikiSyntaxButtonActionPerformed

    private void logtimeSaveButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_logtimeSaveButtonActionPerformed
        String hoursString = logtimeSpentTextField.getText();
        float hours = 0;
        try {
            hours = NumberFormat.getNumberInstance().parse(logtimeSpentTextField.getText())
                    .floatValue();
        } catch (ParseException ex) {
            setErrorMessage("Failed to parse '" + hoursString + "' as a float value");
            return;
        }
        final TimeEntry te = new TimeEntry();
        TimeEntryActivity tea = (TimeEntryActivity) logtimeActivityComboBox.getSelectedItem();
        te.setActivityId(tea.getId());
        te.setComment(logtimeCommentTextField.getText());
        te.setHours(hours);
        te.setIssueId(getIssue().getIssue().getId());

        new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                redmineIssue.getRepository().getManager().createTimeEntry(te);
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
    com.toedter.calendar.JDateChooser dueDateChooser;
    javax.swing.JLabel dueDateLabel;
    javax.swing.JLabel estimateTimeLabel;
    javax.swing.JLabel estimateTimeLabel1;
    javax.swing.JFormattedTextField estimateTimeTextField;
    javax.swing.JPanel headPane;
    javax.swing.JLabel headerLabel;
    javax.swing.JLabel htmlOutputLabel;
    final javax.swing.JLabel infoLabel = new javax.swing.JLabel();
    javax.swing.JPanel issuePane;
    javax.swing.JScrollPane jScrollPane1updateCommentScrollPane2;
    javax.swing.JPanel journalOuterPane;
    javax.swing.JPanel journalPane;
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
    final com.kenai.redminenb.util.LinkButton projectNameButton = new com.kenai.redminenb.util.LinkButton();
    javax.swing.JLabel spentHoursLabel;
    com.toedter.calendar.JDateChooser startDateChooser;
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
    javax.swing.JLabel updateCommentHtmlOutputLabel;
    javax.swing.JLabel updateCommentLabel;
    javax.swing.JPanel updateCommentOuterPanel;
    javax.swing.JScrollPane updateCommentScrollPane1;
    javax.swing.JTabbedPane updateCommentTabPanel;
    javax.swing.JTextArea updateCommentTextArea;
    javax.swing.JLabel updatedLabel;
    javax.swing.JLabel updatedValueLabel;
    final com.kenai.redminenb.util.LinkButton versionAddButton = new com.kenai.redminenb.util.LinkButton();
    final com.kenai.redminenb.util.LinkButton wikiSyntaxButton = new com.kenai.redminenb.util.LinkButton();
    // End of variables declaration//GEN-END:variables
}
