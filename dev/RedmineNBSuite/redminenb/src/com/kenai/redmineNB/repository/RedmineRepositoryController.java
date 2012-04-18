package com.kenai.redmineNB.repository;

import com.kenai.redmineNB.Redmine;
import com.kenai.redminenb.api.AuthMode;
import com.kenai.redmineNB.project.RedmineProjectPanel;
import com.kenai.redmineNB.ui.Defaults;
import com.kenai.redmineNB.util.RedmineUtil;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.openide.util.*;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor.Task;
import org.redmine.ta.AuthenticationException;
import org.redmine.ta.RedmineException;
import org.redmine.ta.beans.Project;
import org.redmine.ta.beans.User;


/**
 * Redmine repository parameters controller.
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
   "MSG_AuthSuccessful=Successfully authenticated as ''{0}''"
})
public class RedmineRepositoryController extends BugtrackingController implements DocumentListener,
                                                                                  ActionListener {

   private RedmineRepository repository;
   //private RedmineRepository realRepository;
   private RedmineRepositoryPanel panel;
   private String errorMessage;
   private boolean connectError;
//    private boolean populated = false;
   private boolean connected = false;
   private TaskRunner taskRunner;
   private RequestProcessor rp;


   public RedmineRepositoryController(RedmineRepository repository) {
      this.repository = repository;

      panel = new RedmineRepositoryPanel(this);
      populate();

      panel.nameTextField.getDocument().addDocumentListener(this);
      panel.hostTextField.getDocument().addDocumentListener(this);
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
      populate();
      return panel;
   }


   @Override
   public boolean isValid() {
      return validate();
   }


   private String getHost() {
      String url = panel.hostTextField.getText().trim();
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
      return (Project) panel.projectComboBox.getSelectedItem();
   }


   private boolean validate() {
      if (connectError) {
         panel.connectButton.setEnabled(true);
         return false;
      }
      errorMessage = null;

      panel.connectButton.setEnabled(false);
      panel.createNewProjectButton.setEnabled(false);

      // check name
      String name = panel.nameTextField.getText().trim();

      if (name.equals("")) { // NOI18N
         errorMessage = Bundle.MSG_MissingName();
         return false;
      }

      // is name unique?
      try {
         if ((repository.isFresh() && Redmine.getInstance().isRepositoryNameExists(name))
                 || (!repository.isFresh() && !name.equals(repository.getName())
                 && Redmine.getInstance().isRepositoryNameExists(name))) {
            errorMessage = Bundle.MSG_TrackerAlreadyExists();
            return false;
         }
      } catch (com.kenai.redmineNB.RedmineException ex) {
         JOptionPane.showMessageDialog(panel, ex.getLocalizedMessage());
      }

      // check url
      String url = getHost();
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

      // is repository unique?
      try {
         RedmineRepository confRepository = Redmine.getInstance().repositoryExists(repository);

         if ((repository.isFresh() && Redmine.getInstance().isRepositoryExists(repository))
                 || (!repository.isFresh() && confRepository != null
                 && !confRepository.getID().equals(repository.getID()))) {
            errorMessage = Bundle.MSG_RepositoryAlreadyExists();
            return false;
         }
      } catch (com.kenai.redmineNB.RedmineException ex) {
         JOptionPane.showMessageDialog(panel, ex.getLocalizedMessage());
      }

      if (panel.projectComboBox.getSelectedIndex() == -1) {
         errorMessage = Bundle.MSG_MissingProject();
         return false;
      }

      return true;
   }


   @Override
   public HelpCtx getHelpCtx() {
      return new HelpCtx(getClass());
   }


   @Override
   public String getErrorMessage() {
      return errorMessage != null ? "<html>" + errorMessage + "</html>" : errorMessage;
   }


   @Override
   public void applyChanges() {
      try {
         repository.setName(getName());
         repository.getNode().setName(getName());
         repository.setAuthMode(getAuthMode());

         if (repository.isFresh()) {
            Redmine.getInstance().addRepository(repository);
            repository.setFresh(false);
         } else {
//                realRepository.set(repository);
//                Redmine.getInstance().updateRepository(realRepository);
            Redmine.getInstance().updateRepository(repository);
         }
      } catch (com.kenai.redmineNB.RedmineException ex) {
         JOptionPane.showMessageDialog(panel, ex.getLocalizedMessage());
      }
   }


   final void populate() {
      connected = false;

      panel.nameTextField.setText(repository.getName());
      panel.hostTextField.setText(repository.getUrl());

      panel.setAuthMode(repository.getAuthMode());
      panel.accessKeyTextField.setText(repository.getAccessKey());
      panel.userField.setText(repository.getUsername());
      panel.pwdField.setText(repository.getPassword() == null ? "" : String.valueOf(repository.getPassword()));

      panel.projectComboBox.setProjects(Collections.singletonList(repository.getProject()));
      panel.projectComboBox.setSelectedItem(repository.getProject());
      panel.projectComboBox.setEnabled(false);
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
      validateErrorOff(e);
      fireDataChanged();
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
               fireDataChanged();
            }

         });
      }
   }


   private void onConnect() {
      if (taskRunner == null) {
         taskRunner = new TaskRunner(NbBundle.getMessage(RedmineRepositoryPanel.class,
                                                         "LBL_Connecting")) {  // NOI18N

            private List<Project> projects;


            @Override
            void execute() {
               connectError = true;
               connected = false;

               repository.setUrl(getHost());
               repository.setAccessKey(getAccessKey());
               repository.setUsername(getUser());
               repository.setPassword(getPassword());
               repository.setAuthMode(getAuthMode());

               try {
//                  InetAddress inetAddr = InetAddress.getByName(repository.getUrl());
//                  if (inetAddr.isReachable(500)) {
//                     
//                  }

                  projects = repository.getManager().getProjects();
                  // check authentication
                  User currentUser = repository.getManager().getCurrentUser();

                  panel.progressPanel.removeAll();
                  panel.progressPanel.add(new JLabel(Bundle.MSG_AuthSuccessful(currentUser.getFullName()),
                                                     Defaults.getIcon("info.png"),
                                                     SwingUtilities.LEADING));
                  panel.progressPanel.setVisible(true);

                  connectError = false;
                  connected = true;

                  SwingUtilities.invokeLater(new Runnable() {

                     @Override
                     public void run() {
                        Object item = panel.projectComboBox.getSelectedItem();
                        panel.projectComboBox.setProjects(projects);
                        panel.projectComboBox.setSelectedItem(item);
                        panel.projectComboBox.setEnabled(true);
                        onProjectSelected();
                     }

                  });

               } catch (AuthenticationException ex) {
                  //errorMessage = Redmine.getMessage("MSG_AUTHENTICATION_ERROR", ex.getLocalizedMessage());
                  errorMessage = ex.getLocalizedMessage();
                  Redmine.LOG.log(Level.INFO, errorMessage);
               } catch (RedmineException ex) {
                  errorMessage = Redmine.getMessage("MSG_REDMINE_ERROR",
                                                    ex.getLocalizedMessage());
                  Redmine.LOG.log(Level.INFO, errorMessage, ex);
               } catch (Exception ex) {
                  errorMessage = Redmine.getMessage("MSG_CONNECTION_ERROR",
                                                    ex.getLocalizedMessage());
                  Redmine.LOG.log(Level.WARNING, errorMessage, ex);
               }

               fireDataChanged();
            }

         };
      }
      taskRunner.startTask();
   }


   private void onProjectSelected() {
      repository.setProject(getProject());
      fireDataChanged();
   }


   private void onCreateNewProject() {
      RedmineProjectPanel projectPanel = new RedmineProjectPanel(repository);

      if (RedmineUtil.show(projectPanel, "New Redmine project", "OK")) {
         try {
            panel.projectComboBox.setProjects(repository.getManager().getProjects());
            panel.projectComboBox.setSelectedIndex(-1);
         } catch (IOException ex) {
            errorMessage = NbBundle.getMessage(Redmine.class,
                                               "MSG_CONNECTION_ERROR", ex.getLocalizedMessage());
            Redmine.LOG.log(Level.INFO, errorMessage, ex);
         } catch (AuthenticationException ex) {
            errorMessage = NbBundle.getMessage(Redmine.class,
                                               "MSG_AUTHENTICATION_ERROR", ex.getLocalizedMessage());
            Redmine.LOG.log(Level.INFO, errorMessage, ex);
         } catch (RedmineException ex) {
            errorMessage = NbBundle.getMessage(Redmine.class,
                                               "MSG_REDMINE_ERROR", ex.getLocalizedMessage());
            Redmine.LOG.log(Level.INFO, errorMessage, ex);
         }
      }

      fireDataChanged();
   }


   private void validateErrorOff(DocumentEvent e) {
      if (e.getDocument() == panel.accessKeyTextField.getDocument()
              || e.getDocument() == panel.hostTextField.getDocument()
              || e.getDocument() == panel.userField.getDocument()
              || e.getDocument() == panel.pwdField.getDocument()) {
         connectError = false;
         panel.projectComboBox.setProjects(null);
         panel.projectComboBox.setEnabled(false);
      }
   }


   void cancel() {
      if (taskRunner != null) {
         taskRunner.cancel();
      }
   }


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
         if (errorMessage != null) {
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


   private RequestProcessor getRequestProcessor() {
      if (rp == null) {
         rp = new RequestProcessor("Redmine Repository tasks", 1, true); // NOI18N
      }

      return rp;
   }

}
