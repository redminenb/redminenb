package com.kenai.redminenb.project;

import com.kenai.redminenb.Redmine;
import com.kenai.redminenb.repository.RedmineRepository;
import com.kenai.redminenb.util.ActionListenerPanel;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;
import com.taskadapter.redmineapi.NotFoundException;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.Project;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;

/**
 *
 * @author Mykolas
 */
public class RedmineProjectPanel extends ActionListenerPanel implements DocumentListener {

   private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[a-z0-9_]{1,100}");
   //
   private RedmineRepository repository;
   private String projectName;
   private String description;
   private String identifier;
   private String errorMessage;

   /**
    * Creates new form RedmineProjectPanel
    */
   public RedmineProjectPanel(RedmineRepository repository) {
      this.repository = repository;

      initComponents();

      nameTextField.getDocument().addDocumentListener(this);
      descriptionTextArea.getDocument().addDocumentListener(this);
      identifierTextField.getDocument().addDocumentListener(this);

      errorLabel.setVisible(false);
   }

   public String getProjectName() {
      return projectName;
   }

   public String getDescription() {
      return description;
   }

   public String getIdentifier() {
      return identifier;
   }

   private boolean isProjectError() {
      projectName = nameTextField.getText().trim();

      if (projectName.isEmpty()) {
         errorMessage = "The name is invalid";
         return true;
      }

      description = descriptionTextArea.getText().trim();
      identifier = identifierTextField.getText().trim();

      if (!IDENTIFIER_PATTERN.matcher(identifier).matches()) {
         errorMessage = "<html>Identifier length is between 1 and 100 characters. <br/>"
                 + "Only lower case letters (a-z), numbers and dashes are allowed.</html>";
         return true;
      }
      return false;
   }

   private boolean isProjectValid() {
      if (!isProjectError()) {
         errorLabel.setText(null);
         errorLabel.setVisible(false);

         return true;
      }

      errorLabel.setText(errorMessage);
      errorLabel.setVisible(true);

      return false;
   }

   private boolean createNewProject() {
      final Project project = new Project();

      project.setName(projectName);
      project.setDescription(description);
      project.setIdentifier(identifier);

      try {
         repository.getManager().createProject(project);
         return true;
      } catch (NotFoundException ex) {
         errorMessage = ex.getLocalizedMessage();
         Redmine.LOG.log(Level.INFO, errorMessage, ex);
      } catch (RedmineException ex) {
         errorMessage = NbBundle.getMessage(Redmine.class, "MSG_REDMINE_ERROR", ex.getLocalizedMessage());
         Redmine.LOG.log(Level.INFO, errorMessage, ex);
      }

      errorLabel.setText(errorMessage);
      errorLabel.setVisible(true);

      return false;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      if (e.getSource() == okButton) {
         if (isProjectValid()) {
            if (createNewProject()) {
               dialogDescribtor.setClosingOptions(new Object[]{okButton, cancelButton});
            }
         } else {
            dialogDescribtor.setClosingOptions(new Object[]{cancelButton});
         }
      }
   }

   @Override
   public void insertUpdate(DocumentEvent e) {
      isProjectValid();
   }

   @Override
   public void removeUpdate(DocumentEvent e) {
      isProjectValid();
   }

   @Override
   public void changedUpdate(DocumentEvent e) {
      isProjectValid();
   }

   @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new JLabel();
        identifierLabel = new JLabel();
        descriptionLabel = new JLabel();
        nameTextField = new JTextField();
        identifierTextField = new JTextField();
        jScrollPane1 = new JScrollPane();
        descriptionTextArea = new JTextArea();
        errorLabel = new JLabel();

        nameLabel.setText(NbBundle.getMessage(RedmineProjectPanel.class, "RedmineProjectPanel.nameLabel.text")); // NOI18N

        identifierLabel.setText(NbBundle.getMessage(RedmineProjectPanel.class, "RedmineProjectPanel.identifierLabel.text")); // NOI18N

        descriptionLabel.setText(NbBundle.getMessage(RedmineProjectPanel.class, "RedmineProjectPanel.descriptionLabel.text")); // NOI18N

        nameTextField.setText(NbBundle.getMessage(RedmineProjectPanel.class, "RedmineProjectPanel.nameTextField.text")); // NOI18N

        identifierTextField.setText(NbBundle.getMessage(RedmineProjectPanel.class, "RedmineProjectPanel.identifierTextField.text")); // NOI18N

        descriptionTextArea.setColumns(20);
        descriptionTextArea.setRows(5);
        jScrollPane1.setViewportView(descriptionTextArea);

        errorLabel.setForeground(new Color(153, 0, 0));
        errorLabel.setIcon(new ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/error.png"))); // NOI18N
        errorLabel.setText(NbBundle.getMessage(RedmineProjectPanel.class, "RedmineProjectPanel.errorLabel.text")); // NOI18N
        errorLabel.setMaximumSize(new Dimension(407, 16));

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(errorLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(nameLabel)
                            .addComponent(descriptionLabel)
                            .addComponent(identifierLabel))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(identifierTextField, GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)
                            .addComponent(nameTextField, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel)
                    .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(identifierTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(identifierLabel))
                .addGap(18, 18, 18)
                .addComponent(errorLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel descriptionLabel;
    private JTextArea descriptionTextArea;
    private JLabel errorLabel;
    private JLabel identifierLabel;
    private JTextField identifierTextField;
    private JScrollPane jScrollPane1;
    private JLabel nameLabel;
    private JTextField nameTextField;
    // End of variables declaration//GEN-END:variables
}
