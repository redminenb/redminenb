package com.kenai.redminenb.repository;

import com.kenai.redminenb.api.AuthMode;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import com.kenai.redminenb.util.LinkButton;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
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
        nameTextField.requestFocus();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        buttonGroup1 = new ButtonGroup();
        nameLabel = new JLabel();
        hostLabel = new JLabel();
        projectLabel = new JLabel();
        nameTextField = new JTextField();
        urlTextField = new JTextField();
        accessKeyTextField = new JTextField();
        connectButton = new JButton();
        progressPanel = new JPanel();
        progressIcon = new JLabel();
        progressScrollPane = new JScrollPane();
        progressTextPane = new JTextPane();
        rbAccessKey = new JRadioButton();
        authLabel = new JLabel();
        rbCredentials = new JRadioButton();
        projectComboBox = new JComboBox();
        createNewProjectButton = new LinkButton();
        featuresLabel = new JLabel();
        featureWatchers = new JCheckBox();

        setNextFocusableComponent(nameTextField);
        setLayout(new GridBagLayout());

        nameLabel.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.nameLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(nameLabel, gridBagConstraints);

        hostLabel.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.hostLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(hostLabel, gridBagConstraints);

        projectLabel.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.projectLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(projectLabel, gridBagConstraints);

        nameTextField.setColumns(25);
        nameTextField.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.nameTextField.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(nameTextField, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(urlTextField, gridBagConstraints);

        accessKeyTextField.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.accessKeyTextField.text")); // NOI18N
        accessKeyTextField.setToolTipText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.accessKeyTextField.toolTipText")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(1, 5, 1, 5);
        add(accessKeyTextField, gridBagConstraints);

        connectButton.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.connectButton.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(connectButton, gridBagConstraints);

        progressPanel.setLayout(new BorderLayout());

        progressIcon.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.progressIcon.text_1")); // NOI18N
        progressPanel.add(progressIcon, BorderLayout.LINE_START);

        progressScrollPane.setMinimumSize(new Dimension(22, 75));
        progressScrollPane.setOpaque(false);
        progressScrollPane.setPreferredSize(new Dimension(9, 75));

        progressTextPane.setEditable(false);
        progressTextPane.setOpaque(false);
        progressScrollPane.setViewportView(progressTextPane);

        progressPanel.add(progressScrollPane, BorderLayout.CENTER);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(progressPanel, gridBagConstraints);

        userField.setColumns(12);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(1, 5, 5, 5);
        add(userField, gridBagConstraints);

        pwdField.setColumns(12);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(1, 5, 5, 5);
        add(pwdField, gridBagConstraints);

        buttonGroup1.add(rbAccessKey);
        rbAccessKey.setSelected(true);
        rbAccessKey.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.rbAccessKey.text")); // NOI18N
        rbAccessKey.setActionCommand(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.rbAccessKey.actionCommand")); // NOI18N
        rbAccessKey.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                rbAuthPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(1, 5, 1, 5);
        add(rbAccessKey, gridBagConstraints);

        authLabel.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.authLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 1, 5);
        add(authLabel, gridBagConstraints);

        buttonGroup1.add(rbCredentials);
        rbCredentials.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.rbCredentials.text")); // NOI18N
        rbCredentials.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                rbAuthPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(1, 5, 5, 5);
        add(rbCredentials, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(projectComboBox, gridBagConstraints);

        createNewProjectButton.setIcon(new ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/add.png"))); // NOI18N
        createNewProjectButton.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.createNewProjectButton.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(createNewProjectButton, gridBagConstraints);

        featuresLabel.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.featuresLabel.text_1")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(featuresLabel, gridBagConstraints);

        featureWatchers.setSelected(true);
        featureWatchers.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.featureWatchers.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(featureWatchers, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void rbAuthPerformed(ActionEvent evt) {//GEN-FIRST:event_rbAuthPerformed
        accessKeyTextField.setEnabled(evt.getSource() == rbAccessKey);
        userField.setEnabled(evt.getSource() == rbCredentials);
        pwdField.setEnabled(evt.getSource() == rbCredentials);
    }//GEN-LAST:event_rbAuthPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    JTextField accessKeyTextField;
    JLabel authLabel;
    ButtonGroup buttonGroup1;
    JButton connectButton;
    protected LinkButton createNewProjectButton;
    JCheckBox featureWatchers;
    JLabel featuresLabel;
    JLabel hostLabel;
    JLabel nameLabel;
    JTextField nameTextField;
    JLabel progressIcon;
    JPanel progressPanel;
    JScrollPane progressScrollPane;
    JTextPane progressTextPane;
    JComboBox projectComboBox;
    JLabel projectLabel;
    final JPasswordField pwdField = new JPasswordField();
    JRadioButton rbAccessKey;
    JRadioButton rbCredentials;
    JTextField urlTextField;
    final JTextField userField = new JTextField();
    // End of variables declaration//GEN-END:variables
}
