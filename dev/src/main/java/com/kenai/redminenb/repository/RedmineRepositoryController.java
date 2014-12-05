/*
 * Copyright 2012 Anchialas and Mykolas.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kenai.redminenb.repository;

import com.kenai.redminenb.Redmine;
import com.kenai.redminenb.project.RedmineProjectPanel;
import com.kenai.redminenb.ui.Defaults;
import com.kenai.redminenb.util.ListComboBoxModel;
import com.kenai.redminenb.util.RedmineUtil;

import com.kenai.redminenb.api.AuthMode;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Project;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.openide.util.*;
import org.openide.util.NbBundle.Messages;

/**
 * Redmine repository parameter controller.
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
@Messages({
    "MSG_MissingName=Missing Name",
    "MSG_WrongUrl=Wrong URL format",
    "MSG_MissingUrl=Missing URL",
    "MSG_MissingUsername=Missing Usernname",
    "MSG_MissingPassword=Missing Password",
    "MSG_MissingAccessKey=Missing Access Key",
    "MSG_MissingProject=No Project is selected",
    "MSG_TrackerAlreadyExists=Issue Tracker with the same name already exists",
    "MSG_RepositoryAlreadyExists=The same Issue Tracker already exists",
    "MSG_AuthSuccessful=Successfully authenticated",
    "MSG_Unchanged=Unchanged"
})
public class RedmineRepositoryController implements RepositoryController, DocumentListener, ActionListener, ItemListener {
    
    private final RedmineRepository repository;
    private final RedmineRepositoryPanel panel;
    private String errorMessage;
    private final ChangeSupport support = new ChangeSupport(this);

    @SuppressWarnings("LeakingThisInConstructor")
    public RedmineRepositoryController(RedmineRepository repository) {
        this.repository = repository;

        panel = new RedmineRepositoryPanel(this);
        panel.nameTextField.getDocument().addDocumentListener(this);
        panel.urlTextField.getDocument().addDocumentListener(this);
        panel.accessKeyTextField.getDocument().addDocumentListener(this);
        panel.userField.getDocument().addDocumentListener(this);
        panel.pwdField.getDocument().addDocumentListener(this);

        panel.featureWatchers.addActionListener(this);
        panel.projectComboBox.addItemListener(this);
        panel.connectButton.addActionListener(this);
        panel.createNewProjectButton.addActionListener(this);

        panel.rbAccessKey.addActionListener(this);
        panel.rbCredentials.addActionListener(this);
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    private String getUrl() {
        String url = panel.urlTextField.getText().trim();
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url; // NOI18N
    }

    private String getName() {
        return panel.nameTextField.getText().trim();
    }

    private String getUser() {
        return panel.userField.getText();
    }

    private char[] getPassword() {
        return panel.pwdField.getPassword();
    }

    public AuthMode getAuthMode() {
        return panel.getAuthMode();
    }

    private String getAccessKey() {
        return panel.accessKeyTextField.getText().trim();
    }

    private ProjectId getProject() {
        return (ProjectId) panel.projectComboBox.getSelectedItem();
    }

    private boolean isFeatureWatchers() {
        return panel.featureWatchers.isSelected();
    }

    @Override
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public boolean isValid() {
        errorMessage = null;

        // check url
        String url = getUrl();
        if (getUrl().trim().isEmpty()) { // NOI18N
            errorMessage = Bundle.MSG_MissingUrl();
            return false;
        }

        try {
            new URL(url); // check this first even if URL is an URI
            new URI(url);
        } catch (MalformedURLException | URISyntaxException ex) {
            errorMessage = Bundle.MSG_WrongUrl();
            Redmine.LOG.log(Level.FINE, errorMessage, ex);
            return false;
        }

        // username and password required if not access key authentication
        if (getAuthMode() == AuthMode.Credentials) {
            if (StringUtils.isBlank(getUser())) {
                errorMessage = Bundle.MSG_MissingUsername();
                return false;
            } else if (ArrayUtils.isEmpty(getPassword())) {
                errorMessage = Bundle.MSG_MissingPassword();
                return false;
            }
        } else {
            if (StringUtils.isBlank(getAccessKey())) {
                errorMessage = Bundle.MSG_MissingAccessKey();
                return false;
            }
        }

        if (getName().trim().isEmpty()) {
            errorMessage = Bundle.MSG_MissingName();
            return false;
        }

        if (panel.projectComboBox.getSelectedIndex() == -1) {
            errorMessage = Bundle.MSG_MissingProject();
            return false;
        }

        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass().getName());
    }

    @Override
    public String getErrorMessage() {
        return errorMessage != null ? "<html>" + errorMessage + "</html>" : errorMessage;
    }

    @Override
    public void applyChanges() {
        repository.setInfoValues(getName(),
                getUrl(),
                getUser(),
                getPassword(),
                getAccessKey(),
                getAuthMode(),
                getProject() == null ? null : getProject().getId(),
                isFeatureWatchers());
    }

    @Override
    public final void populate() {
        assert SwingUtilities.isEventDispatchThread();

        panel.progressIcon.setIcon(null);
        panel.progressTextPane.setText("");
        panel.progressPanel.setVisible(true);

        panel.nameTextField.setText(repository.getDisplayName());
        panel.urlTextField.setText(repository.getUrl());

        panel.setAuthMode(repository.getAuthMode());
        panel.accessKeyTextField.setText(repository.getAccessKey());
        panel.userField.setText(repository.getUsername());
        panel.pwdField.setText(repository.getPassword() == null ? "" : String.valueOf(repository.getPassword()));
        
        if(repository.getProjectID() != null) {
            ProjectId project = new ProjectId(repository.getProjectID(), Bundle.MSG_Unchanged());
            panel.projectComboBox.setModel(new ListComboBoxModel<>(Collections.singletonList(project)));
            panel.projectComboBox.setSelectedItem(project);
        } else {
            panel.projectComboBox.setModel(new ListComboBoxModel<>());
        }

        panel.featureWatchers.setSelected(repository.isFeatureWatchers());
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        fireChange();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == panel.connectButton) {
            onConnect();
        } else if (e.getSource() == panel.projectComboBox) {
            onProjectSelected();
        } else if (e.getSource() == panel.createNewProjectButton) {
            onCreateNewProject();
        }
    }

    private void onConnect() {
        panel.enableFields(false);
        
        new SwingWorker<List<ProjectId>,Object>() {

            @Override
            protected List<ProjectId> doInBackground() throws Exception {
                RedmineManager manager = RedmineRepositoryController.this.getManager();
                List<ProjectId> projects = new ArrayList<>();
                for(Project p: manager.getProjects()) {
                    projects.add(new ProjectId(p.getId(), p.getName()));
                }
                Collections.sort(projects);
                return projects;
            }

            @Override
            protected void done() {
                panel.progressIcon.setIcon(null);
                panel.progressTextPane.setText("");
                panel.progressPanel.setVisible(true);
                try {
                    List<ProjectId> projects = get();
                    panel.progressIcon.setIcon(Defaults.getIcon("info.png"));
                    panel.progressTextPane.setText(Bundle.MSG_AuthSuccessful());
                    Object item = panel.projectComboBox.getSelectedItem();
                    panel.projectComboBox.setModel(new ListComboBoxModel<>(projects));
                    panel.projectComboBox.setSelectedItem(item);
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    String errorMessage = null;
                    if (cause instanceof RedmineException) {
                        errorMessage = Redmine.getMessage("MSG_REDMINE_ERROR",
                                Jsoup.parse(ex.getLocalizedMessage()).text());
                        Redmine.LOG.log(Level.INFO, errorMessage, ex);
                    } else if (cause instanceof Exception) {
                        errorMessage = Redmine.getMessage("MSG_CONNECTION_ERROR",
                                ex.getLocalizedMessage());
                        Redmine.LOG.log(Level.WARNING, errorMessage, ex);
                    }
                    if (errorMessage != null) {
                        panel.progressIcon.setIcon(Defaults.getIcon("warning.png"));
                        panel.progressTextPane.setText(errorMessage);
                    }
                } catch (InterruptedException ex) {
                }
                panel.enableFields(true);
            }
        }.execute();
    }

    private void onProjectSelected() {
        ProjectId project = getProject();
        if (project != null && StringUtils.isEmpty(getName())) {
            panel.nameTextField.setText(project.getName());
        }
        fireChange();
    }

    private void onCreateNewProject() {
        Object selectedProject = panel.projectComboBox.getSelectedItem();

        RedmineProjectPanel projectPanel = new RedmineProjectPanel(repository);

        if (RedmineUtil.show(projectPanel, "New Redmine project", "OK")) {
            try {
                List<Project> projects = repository.getManager().getProjects();
                Collections.sort(projects, RedmineUtil.ProjectComparator.SINGLETON);

                panel.projectComboBox.setModel(new ListComboBoxModel<>(projects));
                for (Project p : projects) {
                    if (p.getIdentifier().equals(projectPanel.getIdentifier())) {
                        selectedProject = p;
                        break;
                    }
                }
                panel.projectComboBox.setSelectedItem(selectedProject);

            } catch (RedmineException ex) {
                errorMessage = NbBundle.getMessage(Redmine.class,
                        "MSG_REDMINE_ERROR",
                        Jsoup.parse(ex.getLocalizedMessage()).text());
                Redmine.LOG.log(Level.INFO, errorMessage, ex);
            }
        }
        fireChange();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        support.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        support.removeChangeListener(l);
    }

    protected void fireChange() {
        support.fireChange();
    }

    @Override
    public void cancelChanges() {
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        fireChange();
    }

    private RedmineManager getManager() {
        RedmineManager manager;
        if (getAuthMode() == AuthMode.AccessKey) {
            manager = new RedmineManager(getUrl(), getAccessKey());
        } else {
            manager = new RedmineManager(getUrl());
            manager.setLogin(getUser());
            manager.setPassword(new String(getPassword()));
        }
        return manager;
    }
}
