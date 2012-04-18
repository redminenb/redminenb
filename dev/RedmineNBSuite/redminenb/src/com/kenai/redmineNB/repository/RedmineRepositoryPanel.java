package com.kenai.redmineNB.repository;

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
        hostTextField.setEnabled(b);
        rbAccessKey.setEnabled(b);
        accessKeyTextField.setEnabled(b);
        authLabel.setEnabled(b);
        rbCredentials.setEnabled(b);
        userField.setEnabled(b);
        pwdLabel.setEnabled(b); 
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
        hostTextField = new javax.swing.JTextField();
        accessKeyTextField = new javax.swing.JTextField();
        connectButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        progressPanel = new javax.swing.JPanel();
        projectComboBox = new com.kenai.redmineNB.project.ProjectComboBox();
        createNewProjectButton = new javax.swing.JButton();
        pwdLabel = new javax.swing.JLabel();
        rbAccessKey = new javax.swing.JRadioButton();
        authLabel = new javax.swing.JLabel();
        rbCredentials = new javax.swing.JRadioButton();
        userLabel = new javax.swing.JLabel();

        setNextFocusableComponent(nameTextField);

        nameLabel.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.nameLabel.text")); // NOI18N

        hostLabel.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.hostLabel.text")); // NOI18N

        projectLabel.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.projectLabel.text")); // NOI18N

        nameTextField.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.nameTextField.text")); // NOI18N

        hostTextField.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.hostTextField.text")); // NOI18N

        accessKeyTextField.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.accessKeyTextField.text")); // NOI18N
        accessKeyTextField.setToolTipText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.accessKeyTextField.toolTipText")); // NOI18N

        connectButton.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.connectButton.text")); // NOI18N

        cancelButton.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.cancelButton.text")); // NOI18N

        progressPanel.setPreferredSize(new java.awt.Dimension(250, 25));
        progressPanel.setLayout(new java.awt.BorderLayout());

        createNewProjectButton.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.createNewProjectButton.text")); // NOI18N

        pwdLabel.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.pwdLabel.text")); // NOI18N

        userField.setColumns(15);

        pwdField.setColumns(15);

        buttonGroup1.add(rbAccessKey);
        rbAccessKey.setSelected(true);
        rbAccessKey.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.rbAccessKey.text")); // NOI18N
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

        userLabel.setText(org.openide.util.NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.userLabel.text_1")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(authLabel)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hostLabel)
                    .addComponent(nameLabel)
                    .addComponent(projectLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(rbCredentials)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(userLabel))
                            .addComponent(rbAccessKey)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(pwdLabel)))))
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(hostTextField)
                    .addComponent(nameTextField)
                    .addComponent(accessKeyTextField)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(projectComboBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(userField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pwdField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(connectButton)
                            .addComponent(createNewProjectButton)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(progressPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cancelButton))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hostTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(userLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(pwdLabel)
                            .addComponent(pwdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(projectComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(projectLabel)
                            .addComponent(createNewProjectButton))
                        .addGap(18, 18, 18)
                        .addComponent(progressPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(connectButton)
                        .addGap(59, 59, 59)
                        .addComponent(cancelButton)
                        .addContainerGap())))
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
    javax.swing.JButton createNewProjectButton;
    private javax.swing.JLabel hostLabel;
    javax.swing.JTextField hostTextField;
    private javax.swing.JLabel nameLabel;
    javax.swing.JTextField nameTextField;
    javax.swing.JPanel progressPanel;
    com.kenai.redmineNB.project.ProjectComboBox projectComboBox;
    private javax.swing.JLabel projectLabel;
    final javax.swing.JPasswordField pwdField = new javax.swing.JPasswordField();
    private javax.swing.JLabel pwdLabel;
    javax.swing.JRadioButton rbAccessKey;
    javax.swing.JRadioButton rbCredentials;
    final javax.swing.JTextField userField = new javax.swing.JTextField();
    private javax.swing.JLabel userLabel;
    // End of variables declaration//GEN-END:variables
}
