package com.kenai.redmineNB.project;

import com.kenai.redmineNB.Redmine;
import com.kenai.redmineNB.repository.RedmineRepository;
import com.kenai.redmineNB.util.ActionListenerPanel;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;
import com.taskadapter.redmineapi.NotFoundException;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.Project;


/**
 *
 * @author Mykolas
 */
public class RedmineProjectPanel extends ActionListenerPanel implements DocumentListener {

   private RedmineRepository repository;
   private String projectName;
   private String description;
   private String identifier;
   private String errorMessage;
   private Pattern identifierPattern = Pattern.compile("[a-z0-9_]{1,100}");


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

      if (!identifierPattern.matcher(identifier).matches()) {
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

        nameLabel = new javax.swing.JLabel();
        identifierLabel = new javax.swing.JLabel();
        descriptionLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        identifierTextField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JTextArea();
        errorLabel = new javax.swing.JLabel();

        nameLabel.setText(org.openide.util.NbBundle.getMessage(RedmineProjectPanel.class, "RedmineProjectPanel.nameLabel.text")); // NOI18N

        identifierLabel.setText(org.openide.util.NbBundle.getMessage(RedmineProjectPanel.class, "RedmineProjectPanel.identifierLabel.text")); // NOI18N

        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(RedmineProjectPanel.class, "RedmineProjectPanel.descriptionLabel.text")); // NOI18N

        nameTextField.setText(org.openide.util.NbBundle.getMessage(RedmineProjectPanel.class, "RedmineProjectPanel.nameTextField.text")); // NOI18N

        identifierTextField.setText(org.openide.util.NbBundle.getMessage(RedmineProjectPanel.class, "RedmineProjectPanel.identifierTextField.text")); // NOI18N

        descriptionTextArea.setColumns(20);
        descriptionTextArea.setRows(5);
        jScrollPane1.setViewportView(descriptionTextArea);

        errorLabel.setForeground(new java.awt.Color(153, 0, 0));
        errorLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kenai/redmineNB/resources/error.png"))); // NOI18N
        errorLabel.setText(org.openide.util.NbBundle.getMessage(RedmineProjectPanel.class, "RedmineProjectPanel.errorLabel.text")); // NOI18N
        errorLabel.setMaximumSize(new java.awt.Dimension(407, 16));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(errorLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nameLabel)
                            .addComponent(descriptionLabel)
                            .addComponent(identifierLabel))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(identifierTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)
                            .addComponent(nameTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(identifierTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(identifierLabel))
                .addGap(18, 18, 18)
                .addComponent(errorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel identifierLabel;
    private javax.swing.JTextField identifierTextField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    // End of variables declaration//GEN-END:variables
}
