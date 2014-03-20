package com.kenai.redminenb.repository;

import com.kenai.redminenb.api.AuthMode;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import org.openide.util.NbBundle;

/**
 *
 * @author Mykolas
 */
public class RedmineRepositoryPanel extends javax.swing.JPanel {

    private final RedmineRepositoryController controller;

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

        buttonGroup1 = new ButtonGroup();
        nameLabel = new JLabel();
        hostLabel = new JLabel();
        projectLabel = new JLabel();
        nameTextField = new JTextField();
        urlTextField = new JTextField();
        accessKeyTextField = new JTextField();
        connectButton = new JButton();
        cancelButton = new JButton();
        progressPanel = new JPanel();
        rbAccessKey = new JRadioButton();
        authLabel = new JLabel();
        rbCredentials = new JRadioButton();
        projectComboBox = new JComboBox();
        createNewProjectButton = new JButton();

        setNextFocusableComponent(nameTextField);

        nameLabel.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.nameLabel.text")); // NOI18N

        hostLabel.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.hostLabel.text")); // NOI18N

        projectLabel.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.projectLabel.text")); // NOI18N

        nameTextField.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.nameTextField.text")); // NOI18N

        accessKeyTextField.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.accessKeyTextField.text")); // NOI18N
        accessKeyTextField.setToolTipText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.accessKeyTextField.toolTipText")); // NOI18N

        connectButton.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.connectButton.text")); // NOI18N

        cancelButton.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.cancelButton.text")); // NOI18N

        progressPanel.setPreferredSize(new Dimension(250, 25));
        progressPanel.setLayout(new BorderLayout());

        userField.setColumns(15);

        pwdField.setColumns(15);

        buttonGroup1.add(rbAccessKey);
        rbAccessKey.setSelected(true);
        rbAccessKey.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.rbAccessKey.text")); // NOI18N
        rbAccessKey.setActionCommand(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.rbAccessKey.actionCommand")); // NOI18N
        rbAccessKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                rbAuthPerformed(evt);
            }
        });

        authLabel.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.authLabel.text")); // NOI18N

        buttonGroup1.add(rbCredentials);
        rbCredentials.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.rbCredentials.text")); // NOI18N
        rbCredentials.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                rbAuthPerformed(evt);
            }
        });

        createNewProjectButton.setIcon(new ImageIcon("F:\\hudson\\hudson-slave\\workspace\\ee\\redminenb\\dev\\redminenbn\\src\\main\\resources\\com\\kenai\\redminenb\\resources\\add.png")); // NOI18N
        createNewProjectButton.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.createNewProjectButton.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(authLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(rbCredentials)
                            .addComponent(rbAccessKey)
                            .addComponent(hostLabel)
                            .addComponent(nameLabel)
                            .addComponent(projectLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(urlTextField)
                            .addComponent(nameTextField)
                            .addComponent(accessKeyTextField)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(progressPanel, GroupLayout.DEFAULT_SIZE, 0, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cancelButton))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                    .addComponent(projectComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(userField, GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(createNewProjectButton, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(connectButton))
                                    .addComponent(pwdField, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)))))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(urlTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(hostLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(authLabel)
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(accessKeyTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(rbAccessKey))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(rbCredentials)
                    .addComponent(pwdField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(userField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLabel)
                    .addComponent(connectButton)
                    .addComponent(projectComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(createNewProjectButton))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(progressPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cancelButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void rbAuthPerformed(ActionEvent evt) {//GEN-FIRST:event_rbAuthPerformed
        accessKeyTextField.setEnabled(evt.getSource() == rbAccessKey);
        userField.setEnabled(evt.getSource() == rbCredentials);
        pwdField.setEnabled(evt.getSource() == rbCredentials);
    }//GEN-LAST:event_rbAuthPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    JTextField accessKeyTextField;
    private JLabel authLabel;
    private ButtonGroup buttonGroup1;
    JButton cancelButton;
    JButton connectButton;
    protected JButton createNewProjectButton;
    private JLabel hostLabel;
    private JLabel nameLabel;
    JTextField nameTextField;
    JPanel progressPanel;
    JComboBox projectComboBox;
    private JLabel projectLabel;
    final JPasswordField pwdField = new JPasswordField();
    JRadioButton rbAccessKey;
    JRadioButton rbCredentials;
    JTextField urlTextField;
    final JTextField userField = new JTextField();
    // End of variables declaration//GEN-END:variables
}
