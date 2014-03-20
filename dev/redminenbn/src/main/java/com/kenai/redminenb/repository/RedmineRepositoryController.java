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
import com.taskadapter.redmineapi.bean.Project;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.openide.util.*;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor.Task;

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
   "# {0} - the user name",
   "MSG_AuthSuccessful=Successfully authenticated as ''{0}''"
})
public class RedmineRepositoryController implements RepositoryController, DocumentListener, ActionListener {

   private RedmineRepository repository;
   //private RedmineRepository realRepository;
   private RedmineRepositoryPanel panel;
   private String errorMessage;
   private boolean connectError;
   private boolean populated = false;
   private boolean connected = false;
   private TaskRunner taskRunner;
   private RequestProcessor rp;
   private final ChangeSupport support = new ChangeSupport(this);

   public RedmineRepositoryController(RedmineRepository repository) {
      this.repository = repository;

      panel = new RedmineRepositoryPanel(this);
      panel.nameTextField.getDocument().addDocumentListener(this);
      panel.urlTextField.getDocument().addDocumentListener(this);
      panel.accessKeyTextField.getDocument().addDocumentListener(this);
      panel.userField.getDocument().addDocumentListener(this);
      panel.pwdField.getDocument().addDocumentListener(this);

      panel.projectComboBox.addActionListener(this);
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

   private Project getProject() {
      return (Project)panel.projectComboBox.getSelectedItem();
   }

   @Override
   public boolean isValid() {
      return validate();
   }

   private boolean validate() {
      if (connectError) {
         panel.connectButton.setEnabled(true);
         return false;
      }

      if (!populated) {
         return false;
      }
      errorMessage = null;

      panel.connectButton.setEnabled(false);
      panel.createNewProjectButton.setEnabled(false);

      // check url
      String url = getUrl();
      if (url.equals("")) { // NOI18N
         errorMessage = Bundle.MSG_MissingUrl();
         return false;
      }

      try {
         new URL(url); // check this first even if URL is an URI
         new URI(url);
      } catch (Exception ex) {
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

      panel.connectButton.setEnabled(true);
      panel.createNewProjectButton.setEnabled(connected);


      // check name
      String name = panel.nameTextField.getText().trim();

      if (name.equals("")) { // NOI18N
         errorMessage = Bundle.MSG_MissingName();
         return false;
      }

      // is name unique?
//      if ((repository.isFresh() && Redmine.getInstance().isRepositoryNameExists(name))
//               || (!repository.isFresh() && !name.equals(repository.getName())
//               && Redmine.getInstance().isRepositoryNameExists(name))) {
//      if (Redmine.getInstance().isRepositoryNameExists(name)) {
//         errorMessage = Bundle.MSG_TrackerAlreadyExists();
//         return false;
//      }

      // is repository unique?
//      RedmineRepository confRepository = Redmine.getInstance().repositoryExists(repository);
//
//      if ((repository.isFresh() && Redmine.getInstance().isRepositoryExists(repository))
//              || (!repository.isFresh() && confRepository != null
//              && !confRepository.getID().equals(repository.getID()))) {
//         errorMessage = Bundle.MSG_RepositoryAlreadyExists();
//         return false;
//      }

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
                               getProject());
   }

   @Override
   public final void populate() {
      taskRunner = new TaskRunner(NbBundle.getMessage(RedmineRepositoryPanel.class, "LBL_ReadingRepoData")) {  // NOI18N

         @Override
         protected void postRunSwing() {
            super.postRunSwing();
            if (populated) {
               panel.progressPanel.setVisible(false);
            }
         }

         @Override
         void execute() {
            SwingUtilities.invokeLater(new Runnable() {
               @Override
               public void run() {
                  RepositoryInfo info = repository.getInfo();
                  if (info != null) {
                     connected = false;

                     panel.nameTextField.setText(info.getDisplayName());
                     panel.urlTextField.setText(info.getUrl());

                     panel.setAuthMode(repository.getAuthMode());
                     panel.accessKeyTextField.setText(repository.getAccessKey());
                     panel.userField.setText(repository.getUsername());
                     panel.pwdField.setText(repository.getPassword() == null ? "" : String.valueOf(repository.getPassword()));

                     panel.projectComboBox.setModel(new ListComboBoxModel<Project>(Collections.singletonList(repository.getProject())));
                     panel.projectComboBox.setSelectedItem(repository.getProject());
                     panel.projectComboBox.setEnabled(false);
                  }
                  populated = true;
                  fireChange();
               }
            });
         }
      };
      taskRunner.startTask();
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
      if (populated) {
         validateErrorOff(e);
         fireChange();
      }
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      if (e.getSource() == panel.connectButton) {
         onConnect();
      } else if (e.getSource() == panel.projectComboBox) {
         onProjectSelected();
      } else if (e.getSource() == panel.createNewProjectButton) {
         onCreateNewProject();
      } else {
         SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               validate();
               fireChange();
            }
         });
      }
   }

   private void onConnect() {
//      if (taskRunner == null) {
      taskRunner = new TaskRunner(NbBundle.getMessage(RedmineRepositoryPanel.class,
                                                      "LBL_Connecting")) {  // NOI18N
         private List<Project> projects;

         @Override
         void execute() {
            connectError = true;
            connected = false;

            repository.setInfoValues(getName(),
                                     getUrl(),
                                     getUser(),
                                     getPassword(),
                                     getAccessKey(),
                                     getAuthMode(),
                                     getProject());

            try {
//                  InetAddress inetAddr = InetAddress.getByName(repository.getUrl());
//                  if (inetAddr.isReachable(500)) {
//                     
//                  }

               projects = repository.getManager().getProjects();
               Collections.sort(projects, RedmineUtil.ProjectComparator.SINGLETON);

               panel.progressPanel.removeAll();
               panel.progressPanel.add(new JLabel(Bundle.MSG_AuthSuccessful(repository.getCurrentUser().getFullName()),
                                                  Defaults.getIcon("info.png"),
                                                  SwingUtilities.LEADING), BorderLayout.NORTH);
               panel.progressPanel.setVisible(true);

               connectError = false;
               connected = true;

               SwingUtilities.invokeLater(new Runnable() {
                  @Override
                  public void run() {
                     Object item = panel.projectComboBox.getSelectedItem();
                     panel.projectComboBox.setModel(new ListComboBoxModel<Project>(projects));
                     panel.projectComboBox.setSelectedItem(item);
                     panel.projectComboBox.setEnabled(true);
                     onProjectSelected();
                  }
               });

            } catch (RedmineException ex) {
               errorMessage = Redmine.getMessage("MSG_REDMINE_ERROR",
                                                 ex.getLocalizedMessage());
               Redmine.LOG.log(Level.INFO, errorMessage, ex);
            } catch (Exception ex) {
               errorMessage = Redmine.getMessage("MSG_CONNECTION_ERROR",
                                                 ex.getLocalizedMessage());
               Redmine.LOG.log(Level.WARNING, errorMessage, ex);
            }

            fireChange();
         }
      };

//      }
      taskRunner.startTask();
   }

   private void onProjectSelected() {
      Project project = getProject();
      //repository.setProject(project);
      // auto-set name
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

            panel.projectComboBox.setModel(new ListComboBoxModel<Project>(projects));
            for (Project p : projects) {
               if (p.getIdentifier().equals(projectPanel.getIdentifier())) {
                  selectedProject = p;
                  break;
               }
            }
            panel.projectComboBox.setSelectedItem(selectedProject);

         } catch (RedmineException ex) {
            errorMessage = NbBundle.getMessage(Redmine.class,
                                               "MSG_REDMINE_ERROR", ex.getLocalizedMessage());
            Redmine.LOG.log(Level.INFO, errorMessage, ex);
         }
      }
      fireChange();
   }

   private void validateErrorOff(DocumentEvent e) {
      if (e.getDocument() == panel.accessKeyTextField.getDocument()
              || e.getDocument() == panel.urlTextField.getDocument()
              || e.getDocument() == panel.userField.getDocument()
              || e.getDocument() == panel.pwdField.getDocument()) {
         connectError = false;
         panel.projectComboBox.setModel(new ListComboBoxModel<Project>());
         panel.projectComboBox.setEnabled(false);
      }
   }

   void cancel() {
      if (taskRunner != null) {
         taskRunner.cancel();
      }
   }

   private RequestProcessor getRequestProcessor() {
      if (rp == null) {
         rp = new RequestProcessor("Redmine Repository tasks", 1, true); // NOI18N
      }
      return rp;
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
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   //
   // inner classes
   //
   private abstract class TaskRunner implements Runnable, Cancellable, ActionListener {

      private Task task;
      private ProgressHandle handle;
      private String labelText;

      public TaskRunner(String labelText) {
         this.labelText = labelText;
      }

      final void startTask() {
         //cancel();
         task = getRequestProcessor().create(this);
         task.schedule(0);
      }

      @Override
      final public void run() {
         preRun();
         try {
            execute();
         } finally {
            postRun();
         }
      }

      abstract void execute();

      protected void preRun() {
         handle = ProgressHandleFactory.createHandle(labelText, this);

         panel.progressPanel.removeAll();
         panel.progressPanel.add(ProgressHandleFactory.createProgressComponent(handle), BorderLayout.NORTH);
         panel.progressPanel.add(ProgressHandleFactory.createMainLabelComponent(handle), BorderLayout.CENTER);
         panel.cancelButton.addActionListener(this);

         handle.start();

         SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               preRunSwing();
            }
         });
      }

      /**
       * Pre-run stuff invoked on AWT Event Dispatching Thread.
       */
      protected void preRunSwing() {
         panel.progressPanel.setVisible(true);
         panel.cancelButton.setVisible(true);
         panel.connectButton.setEnabled(false);
         panel.createNewProjectButton.setEnabled(false);
         panel.enableFields(false);
         panel.projectComboBox.setEnabled(false);
      }

      protected void postRun() {
         if (handle != null) {
            handle.finish();
         }
         panel.cancelButton.removeActionListener(this);

         SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               postRunSwing();
            }
         });
      }

      /**
       * Post-run stuff invoked on AWT Event Dispatching Thread.
       */
      protected void postRunSwing() {
         if (errorMessage != null
                 && !connected) {
            panel.progressPanel.setVisible(false);
         }
         panel.connectButton.setEnabled(true);
         panel.cancelButton.setVisible(false);
         panel.enableFields(true);

         if (panel.projectComboBox.getItemCount() > 0) {
            panel.projectComboBox.setEnabled(true);
         }
         if (connected) {
            panel.createNewProjectButton.setEnabled(true);
         }
         validate();
      }

      @Override
      // TODO: implement correct task interruption
      public boolean cancel() {
         boolean ret = true;

         postRun();

         if (task != null) {
            // return true if the task has been removed from the queue,
            // false it the task has already been processed
            ret = task.cancel();
         }

         errorMessage = null;
         return ret;
      }

      @Override
      public void actionPerformed(ActionEvent e) {
         if (e.getSource() == panel.cancelButton) {
            cancel();
         }
      }
   }
}