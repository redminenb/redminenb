package com.kenai.redminenb.repository;

import com.kenai.redminenb.api.AuthMode;

/**
 *
 * @author Mykolas
 */
public class RedmineRepositoryPanel extends javax.swing.JPanel {

    private RedmineRepositoryController controller;

    public RedmineRepositoryPanel(RedmineRepositoryController controller) {
        this.controller = controller;

        initComponents();
    }

    void enableFields(boolean b) {
        nameLabel.setEnabled(b);
        nameTextField.setEnabled(b);
        hostLabel.setEnabled(b);
        urlTextField.setEnabled(b);
        rbAccessKey.setEnabled(b);
        accessKeyTextField.setEnabled(b);
        authLabel.setEnabled(b);
        rbCredentials.setEnabled(b);
        userField.setEnabled(b);
        pwdField.setEnabled(b);
        projectLabel.setEnabled(b);
    }

    AuthMode getAuthMode() {
        return rbAccessKey.isSelected() ? AuthMode.AccessKey : AuthMode.Credentials;
    }

    void setAuthMode(AuthMode authMode) {
        if (authMode == null || authMode == AuthMode.AccessKey) {
            rbAccessKey.setSelected(true);
        } else {
            rbCredentials.setSelected(true);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();

        progressPanel.setVisible(false);
        cancelButton.setVisible(false);
        nameTextField.requestFocus();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        controller.cancel();
    }

    @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      buttonGroup1 = new javax.swing.ButtonGroup();
      nameLabel = new javax.swing.JLabel();
      hostLabel = new javax.swing.JLabel();
      projectLabel = new javax.swing.JLabel();
      nameTextField = new javax.swing.JTextField();
      urlTextField = new javax.swing.JTextField();
      accessKeyTextField = new javax.swing.JTextField();
      connectButton = new javax.swing.JButton();
      cancelButton = new javax.swing.JButton();
      progressPanel = new javax.swing.JPanel();
      rbAccessKey = new javax.swing.JRadioButton();
      authLabel = new javax.swing.JLabel();
      rbCredentials = new javax.swing.JRadioButton();
      projectComboBox = new javax.swing.JComboBox();

      setNextFocusableComponent(nameTextField);

      nameLabel.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.nameLabel.text")); // NOI18N

      hostLabel.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.hostLabel.text")); // NOI18N

      projectLabel.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.projectLabel.text")); // NOI18N

      nameTextField.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.nameTextField.text")); // NOI18N

      accessKeyTextField.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.accessKeyTextField.text")); // NOI18N
      accessKeyTextField.setToolTipText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.accessKeyTextField.toolTipText")); // NOI18N

      connectButton.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.connectButton.text")); // NOI18N

      cancelButton.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.cancelButton.text")); // NOI18N

      progressPanel.setPreferredSize(new java.awt.Dimension(250, 25));
      progressPanel.setLayout(new java.awt.BorderLayout());

      userField.setColumns(15);

      pwdField.setColumns(15);

      buttonGroup1.add(rbAccessKey);
      rbAccessKey.setSelected(true);
      rbAccessKey.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.rbAccessKey.text")); // NOI18N
      rbAccessKey.setActionCommand(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.rbAccessKey.actionCommand")); // NOI18N
      rbAccessKey.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            rbAuthPerformed(evt);
         }
      });

      authLabel.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.authLabel.text")); // NOI18N

      buttonGroup1.add(rbCredentials);
      rbCredentials.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.rbCredentials.text")); // NOI18N
      rbCredentials.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            rbAuthPerformed(evt);
         }
      });

      createNewProjectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/add.png"))); // NOI18N
      createNewProjectButton.setToolTipText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.createNewProjectButton.toolTipText")); // NOI18N

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addComponent(authLabel)
                  .addGap(0, 0, Short.MAX_VALUE))
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(rbCredentials)
                     .addComponent(rbAccessKey)
                     .addComponent(hostLabel)
                     .addComponent(nameLabel)
                     .addComponent(projectLabel))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(urlTextField)
                     .addComponent(nameTextField)
                     .addComponent(accessKeyTextField)
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(progressPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cancelButton))
                     .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                           .addComponent(projectComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                           .addComponent(userField, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                           .addGroup(layout.createSequentialGroup()
                              .addComponent(createNewProjectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                              .addComponent(connectButton))
                           .addComponent(pwdField, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)))))))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(nameLabel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(urlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(hostLabel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(authLabel)
            .addGap(5, 5, 5)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(accessKeyTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(rbAccessKey))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(rbCredentials)
               .addComponent(pwdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(18, 18, 18)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
               .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                  .addComponent(projectLabel)
                  .addComponent(connectButton)
                  .addComponent(projectComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addComponent(createNewProjectButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(18, 18, 18)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(progressPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addComponent(cancelButton))
            .addContainerGap())
      );
   }// </editor-fold>//GEN-END:initComponents

    private void rbAuthPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbAuthPerformed
        accessKeyTextField.setEnabled(evt.getSource() == rbAccessKey);
        userField.setEnabled(evt.getSource() == rbCredentials);
        pwdField.setEnabled(evt.getSource() == rbCredentials);
    }//GEN-LAST:event_rbAuthPerformed

   // Variables declaration - do not modify//GEN-BEGIN:variables
   javax.swing.JTextField accessKeyTextField;
   private javax.swing.JLabel authLabel;
   private javax.swing.ButtonGroup buttonGroup1;
   javax.swing.JButton cancelButton;
   javax.swing.JButton connectButton;
   final org.netbeans.modules.bugtracking.util.LinkButton createNewProjectButton = new org.netbeans.modules.bugtracking.util.LinkButton();
   private javax.swing.JLabel hostLabel;
   private javax.swing.JLabel nameLabel;
   javax.swing.JTextField nameTextField;
   javax.swing.JPanel progressPanel;
   javax.swing.JComboBox projectComboBox;
   private javax.swing.JLabel projectLabel;
   final javax.swing.JPasswordField pwdField = new javax.swing.JPasswordField();
   javax.swing.JRadioButton rbAccessKey;
   javax.swing.JRadioButton rbCredentials;
   javax.swing.JTextField urlTextField;
   final javax.swing.JTextField userField = new javax.swing.JTextField();
   // End of variables declaration//GEN-END:variables
}
