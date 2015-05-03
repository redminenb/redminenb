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
public class RedmineRepositoryPanel extends javax.swing.JPanel implements ActionListener {

    private boolean fieldsEnabled = true;
    private final RedmineRepositoryController controller;

    public RedmineRepositoryPanel(RedmineRepositoryController controller) {
        this.controller = controller;
        initComponents();
        
        rbAccessKey.addActionListener(this);
        rbCredentials.addActionListener(this);
        httpAuthEnabled.addActionListener(this);
        
        updateFieldState();
    }

    private void updateFieldState() {
        nameLabel.setEnabled(fieldsEnabled);
        nameTextField.setEnabled(fieldsEnabled);
        hostLabel.setEnabled(fieldsEnabled);
        urlTextField.setEnabled(fieldsEnabled);
        rbAccessKey.setEnabled(fieldsEnabled);
        accessKeyTextField.setEnabled(fieldsEnabled);
        authLabel.setEnabled(fieldsEnabled);
        rbCredentials.setEnabled(fieldsEnabled);
        userField.setEnabled(fieldsEnabled);
        pwdField.setEnabled(fieldsEnabled);
        projectLabel.setEnabled(fieldsEnabled);
        httpAuthEnabled.setEnabled(fieldsEnabled);
        
        accessKeyTextField.setEnabled(rbAccessKey.isSelected());
        httpAuthEnabled.setEnabled(rbAccessKey.isSelected());
        httpUserField.setEnabled(httpAuthEnabled.isEnabled() && httpAuthEnabled.isSelected());
        httpPwdField.setEnabled(httpAuthEnabled.isEnabled() && httpAuthEnabled.isSelected());
        
        userField.setEnabled(rbCredentials.isSelected());
        pwdField.setEnabled(rbCredentials.isSelected());
        
        featureWatchers.setEnabled(fieldsEnabled);
        featuresLabel.setEnabled(fieldsEnabled);
        
        connectButton.setEnabled(fieldsEnabled);
        projectComboBox.setEnabled(fieldsEnabled);
        createNewProjectButton.setEnabled(fieldsEnabled);
    }
    
    public void setFieldsEnabled(boolean enabled) {
        boolean oldState = this.fieldsEnabled;
        this.fieldsEnabled = enabled;
        if(oldState != enabled) {
            firePropertyChange("fieldsEnabled", oldState, this.fieldsEnabled);
        }
        updateFieldState();
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
        projectComboBox = new JComboBox<ProjectId>();
        createNewProjectButton = new LinkButton();
        featuresLabel = new JLabel();
        featureWatchers = new JCheckBox();
        httpAuthEnabled = new JCheckBox();
        userLabel = new JLabel();
        pwdLabel = new JLabel();
        httpUserLabel = new JLabel();
        httpPasswordLabel = new JLabel();
        httpUserField = new JTextField();
        httpPwdField = new JPasswordField();

        setNextFocusableComponent(nameTextField);
        setLayout(new GridBagLayout());

        nameLabel.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.nameLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(nameLabel, gridBagConstraints);

        hostLabel.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.hostLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(hostLabel, gridBagConstraints);

        projectLabel.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.projectLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(projectLabel, gridBagConstraints);

        nameTextField.setColumns(25);
        nameTextField.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.nameTextField.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(nameTextField, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(urlTextField, gridBagConstraints);

        accessKeyTextField.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.accessKeyTextField.text")); // NOI18N
        accessKeyTextField.setToolTipText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.accessKeyTextField.toolTipText")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(5, 5, 2, 5);
        add(accessKeyTextField, gridBagConstraints);

        connectButton.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.connectButton.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
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
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(progressPanel, gridBagConstraints);

        userField.setColumns(12);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(userField, gridBagConstraints);

        pwdField.setColumns(12);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(pwdField, gridBagConstraints);

        buttonGroup1.add(rbAccessKey);
        rbAccessKey.setSelected(true);
        rbAccessKey.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.rbAccessKey.text")); // NOI18N
        rbAccessKey.setActionCommand(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.rbAccessKey.actionCommand")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 2, 5);
        add(rbAccessKey, gridBagConstraints);

        authLabel.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.authLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(authLabel, gridBagConstraints);

        buttonGroup1.add(rbCredentials);
        rbCredentials.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.rbCredentials.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(rbCredentials, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(projectComboBox, gridBagConstraints);

        createNewProjectButton.setIcon(new ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/add.png"))); // NOI18N
        createNewProjectButton.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.createNewProjectButton.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(createNewProjectButton, gridBagConstraints);

        featuresLabel.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.featuresLabel.text_1")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(featuresLabel, gridBagConstraints);

        featureWatchers.setSelected(true);
        featureWatchers.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.featureWatchers.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(featureWatchers, gridBagConstraints);

        httpAuthEnabled.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.httpAuthEnabled.text")); // NOI18N
        httpAuthEnabled.setToolTipText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.httpAuthEnabled.toolTipText")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(2, 5, 2, 5);
        add(httpAuthEnabled, gridBagConstraints);

        userLabel.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.userLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(userLabel, gridBagConstraints);

        pwdLabel.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.pwdLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        add(pwdLabel, gridBagConstraints);

        httpUserLabel.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.httpUserLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(2, 5, 5, 5);
        add(httpUserLabel, gridBagConstraints);

        httpPasswordLabel.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.httpPasswordLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(2, 5, 5, 5);
        add(httpPasswordLabel, gridBagConstraints);

        httpUserField.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.httpUserField.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(2, 5, 5, 5);
        add(httpUserField, gridBagConstraints);

        httpPwdField.setText(NbBundle.getMessage(RedmineRepositoryPanel.class, "RedmineRepositoryPanel.httpPwdField.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(2, 5, 5, 5);
        add(httpPwdField, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    JTextField accessKeyTextField;
    JLabel authLabel;
    ButtonGroup buttonGroup1;
    JButton connectButton;
    protected LinkButton createNewProjectButton;
    JCheckBox featureWatchers;
    JLabel featuresLabel;
    JLabel hostLabel;
    JCheckBox httpAuthEnabled;
    JLabel httpPasswordLabel;
    JPasswordField httpPwdField;
    JTextField httpUserField;
    JLabel httpUserLabel;
    JLabel nameLabel;
    JTextField nameTextField;
    JLabel progressIcon;
    JPanel progressPanel;
    JScrollPane progressScrollPane;
    JTextPane progressTextPane;
    JComboBox<ProjectId> projectComboBox;
    JLabel projectLabel;
    final JPasswordField pwdField = new JPasswordField();
    JLabel pwdLabel;
    JRadioButton rbAccessKey;
    JRadioButton rbCredentials;
    JTextField urlTextField;
    final JTextField userField = new JTextField();
    JLabel userLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void actionPerformed(ActionEvent e) {
        updateFieldState();
    }
}
