/*
 * Copyright 2013 Anchialas <anchialas@gmail.com>.
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
package org.anchialas.nb.issuetracking.customizer;

import com.kenai.redmineNB.RedmineConnector;
import com.kenai.redmineNB.util.ListComboBoxModel;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.apache.commons.lang.StringUtils;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.ui.ApisupportAntUIUtils;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SingleModuleProperties;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.DelegatingConnector;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.jira.JiraUpdater;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.ui.selectors.SelectorPanel;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.RepositoryComboSupport;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 * Represents <em>Issue Tracking</em> panel in various NetBeans project customizer.
 *
 * @author Anchialas <anchialas@gmail.com>
 */
public class CustomizerIssueTracking extends JPanel implements
        /* ModuleProperties.LazyStorage, */ PropertyChangeListener, HelpCtx.Provider {

   public static final String KEY_ENABLED = "issuetracking.enabled"; // NOI18N
   public static final String KEY_NAME = "issuetracking.name"; // NOI18N
   public static final String KEY_URL = "issuetracking.url"; // NOI18N
   public static final String KEY_CONNECTOR = "issuetracking.connector"; // NOI18N
   /** Property whether this panel is valid. */
   static final String VALID_PROPERTY = "isPanelValid"; // NOI18N
   /** Property for error message of this panel. */
   static final String ERROR_MESSAGE_PROPERTY = "errorMessage"; // NOI18N
   //
   protected ModuleProperties props;
   protected final ProjectCustomizer.Category category;
   private RepositoryComboSupport repoComboSupport;

   //private final RepositoryComboSupport rs;
   //SingleModuleProperties
   //SuiteProperties
   public CustomizerIssueTracking(ModuleProperties props,
                                  ProjectCustomizer.Category category,
                                  @NonNull NbModuleProject p) {
      super();
      this.props = props;
      this.category = category;

      initComponents();
      init();
   }

   private void init() {
      List<DelegatingConnector> dcList = findConnectors();
      newButton.setVisible(!dcList.isEmpty());
      connectorComboBox.setModel(new ListComboBoxModel<DelegatingConnector>(dcList));

      final ListCellRenderer<? super DelegatingConnector> lcr = connectorComboBox.getRenderer();
      connectorComboBox.setRenderer(new ListCellRenderer<DelegatingConnector>() {
         @Override
         public Component getListCellRendererComponent(JList<? extends DelegatingConnector> list,
                                                       DelegatingConnector value,
                                                       int index, boolean isSelected, boolean cellHasFocus) {
            @SuppressWarnings("unchecked")
            Component c = lcr.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel) {
               JLabel label = (JLabel)c;
               if (value == null) {
                  label.setIcon(null);
                  label.setText(null);
               } else {
                  label.setIcon((Icon)value.getIcon());
                  label.setText(value.getDisplayName());
               }
            }
            return c;
         }
      });

      // issue tracking repos
      //repoComboBox.setSelectedIndex(-1);
      // the issue tracking repositories are loaded asynchronous
      // -> use PropertyChangeListener and ItemListener to track changes
      repoComboBox.addPropertyChangeListener(this);
      repoComboSupport = RepositoryComboSupport.setup(this, repoComboBox, true);

      // TODO: filter repos by connector

      nameTextField.setEditable(false);
      urlTextField.setEditable(false);

      refresh(true);
      attachListeners();
   }

   private void setIssueTrackingEnabled(boolean b) {
      enableCheckBox.setSelected(b);
      pane.setVisible(b);
      if (b && connectorComboBox.getSelectedIndex() == -1) {
         // set Redmine connector as default
         for (DelegatingConnector dc : findConnectors()) {
            if (RedmineConnector.ID.equals(dc.getID())) {
               connectorComboBox.setSelectedItem(dc);
               return;
            }
         }
         connectorComboBox.setSelectedIndex(0);
      }
   }

   private void attachListeners() {
      category.setOkButtonListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            store();
         }
      });

      props.addPropertyChangeListener(this);

      enableCheckBox.addItemListener(new ItemListener() {
         @Override
         public void itemStateChanged(ItemEvent e) {
            setIssueTrackingEnabled(enableCheckBox.isSelected());
         }
      });

      DocumentListener dl = new UIUtil.DocumentAdapter() {
         @Override
         public void insertUpdate(DocumentEvent e) {
            checkValidity();
         }
      };
      nameTextField.getDocument().addDocumentListener(dl);
      urlTextField.getDocument().addDocumentListener(dl);

      repoComboBox.addItemListener(new ItemListener() {
         @Override
         public void itemStateChanged(ItemEvent e) {
            Object item = repoComboBox.getSelectedItem();
            if (item instanceof Repository) {
               Repository repo = (Repository)item;
               nameTextField.setText(repo.getDisplayName());
               urlTextField.setText(repo.getUrl());
               checkValidity();
            }
         }
      });
   }

   private List<DelegatingConnector> findConnectors() {
      List<DelegatingConnector> list = new ArrayList<DelegatingConnector>(
              Arrays.asList(BugtrackingManager.getInstance().getConnectors()));
      // add JIRA proxy if needed
      if (!BugtrackingUtil.isJiraInstalled()) {
         list.add(JiraUpdater.getInstance().getConnector());
      }
      return list;
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getSource() == repoComboBox) {
         if ("model".equals(evt.getPropertyName())) {
            repoComboBox.getModel().addListDataListener(new ListDataListener() {
               @Override
               public void intervalAdded(ListDataEvent e) {
                  contentsChanged(e);
               }

               @Override
               public void intervalRemoved(ListDataEvent e) {
                  contentsChanged(e);
               }

               @Override
               public void contentsChanged(ListDataEvent e) {
                  //refreshRepo();
                  SwingUtilities.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                        refreshRepo();
                     }
                  });
               }
            });
         }
      } else if (ModuleProperties.PROPERTIES_REFRESHED.equals(evt.getPropertyName())) {
         refresh(false);
      }
   }

   private void refreshRepo() {
      String repoName = (String)repoComboBox.getClientProperty("initial");
      if (repoName != null) {
         for (int i = 0; i < repoComboBox.getItemCount(); i++) {
            Object item = repoComboBox.getItemAt(i);
            if (item instanceof Repository) {
               Repository repo = (Repository)item;
               if (repoName.equals(repo.getDisplayName())) {
                  repoComboBox.putClientProperty("initial", null);
                  repoComboBox.setSelectedIndex(i);
//                  SwingUtilities.invokeLater(new Runnable() {
//                     @Override
//                     public void run() {
//                        repoComboBox.setSelectedItem(repo);
//                     }
//                  });
                  return;
               }
            }
         }
         checkValidity();
      }
   }

   /**
    * This method is called whenever {@link ModuleProperties} are refreshed.
    */
   protected void refresh(boolean initial) {
      boolean enabled = Boolean.valueOf(props.getProperty(KEY_ENABLED));
      setIssueTrackingEnabled(enabled);

      // set Connector ComboBox
      connectorComboBox.setSelectedIndex(-1);
      String connectorID = props.getProperty(KEY_CONNECTOR);
      if (StringUtils.isNotBlank(connectorID)) {
         for (int i = 0; i < connectorComboBox.getItemCount(); i++) {
            DelegatingConnector dc = connectorComboBox.getItemAt(i);
            if (connectorID.equalsIgnoreCase(dc.getID())) {
               connectorComboBox.setSelectedIndex(i);
               break;
            }
         }
         if (connectorComboBox.getSelectedIndex() < 0) {
            // connector with given ID is not available
            DelegatingConnector dc = new DelegatingConnector(
                    null,
                    connectorID,
                    Bundle.MSG_ConnectorUnknown(connectorID),
                    null, null);
            connectorComboBox.addItem(dc);
            connectorComboBox.setSelectedItem(dc);
         }
      }

      // set Issue Tracker ComboBox (repository)
      String repoName = props.getProperty(KEY_NAME);
      if (initial) {
         repoComboBox.putClientProperty("initial", repoName);
      } else {
         repoComboBox.putClientProperty("initial", null);
      }
      //Object repository = repoComboBox.getSelectedItem();
      //connectorComboBox.setEnabled(repository == null);


      // set text fields
      ApisupportAntUIUtils.setText(nameTextField, repoName);
      ApisupportAntUIUtils.setText(urlTextField, props.getProperty(KEY_URL));


      checkValidity();
   }

   private String getRepoName() {
      return nameTextField.getText().trim();
   }

   private String getRepoUrl() {
      return urlTextField.getText().trim();
   }

   //@Override
   public void store() {
      props.setProperty(KEY_ENABLED, String.valueOf(enableCheckBox.isSelected()));
      props.setProperty(KEY_NAME, getRepoName());
      props.setProperty(KEY_URL, getRepoUrl());

      Object connector = connectorComboBox.getSelectedItem();
      if (connector instanceof DelegatingConnector) {
         props.setProperty(KEY_CONNECTOR, ((DelegatingConnector)connector).getID());
      } else {
         // TODO: remove property or leave existing value in this case?
         //props.removeProperty(KEY_CONNECTOR);
      }
      // TODO: can't use package private ModuleProperties.LazyStorage
      // -> call props.storeProperties() by reflection
      try {
         Method m = ModuleProperties.class.getDeclaredMethod("storeProperties");
         m.setAccessible(true);
         m.invoke(props);
      } catch (Exception ex) {
         Exceptions.printStackTrace(ex);
      }
   }

   @Messages({
      "# {0} - field name",
      "MSG_RequiredValue={0} value is required",
      "# {0} - Repository Name",
      "MSG_ConnectorUnknown=Unknown Issue Tracker ''{0}'': Please choose an existing or create a new Issue Tracker",
      "# {0} - connector ID",
      "MSG_RepositoryUnknown=<html><i>Connector ''{0}'' not installed</i>",
      
   })
   protected void checkValidity() {
      category.setValid(false);
      if (StringUtils.isBlank(nameTextField.getText())) {
         category.setErrorMessage(Bundle.MSG_RequiredValue(nameLabel.getText()));
      } else if (StringUtils.isBlank(urlTextField.getText())) {
         category.setErrorMessage(Bundle.MSG_RequiredValue(urlLabel.getText()));
      } else if (!(repoComboBox.getSelectedItem() instanceof Repository)) {
         category.setErrorMessage(Bundle.MSG_ConnectorUnknown(((Repository)repoComboBox.getSelectedItem()).getDisplayName()));
      } else {
         category.setErrorMessage(null);
         category.setValid(true);
      }

   }

   @Override
   public HelpCtx getHelpCtx() {
      return new HelpCtx("org.netbeans.modules.apisupport.project.ui.customizer.CustomizerIssueTracking");
   }

   /**
    * @see org.netbeans.modules.bugtracking.ui.selectors.RepositorySelector
    */
   private Repository createRepository() {
      DelegatingConnector dc = (DelegatingConnector)connectorComboBox.getSelectedItem();

//      RepositoryInfo repoInfo = new RepositoryInfo(getRepoName(),
//                                                   dc.getID(),
//                                                   getRepoUrl(),
//                                                   getRepoName(),
//                                                   null);
//      Repository repo = dc.createRepository(repoInfo);

      Repository repo = dc.createRepository();
      //repo.edit();
      if (BugtrackingUtil.editRepository(repo)) {
         return repo;
      }
      return null;
   }

   /**
    * This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      enableCheckBox = new javax.swing.JCheckBox();
      pane = new javax.swing.JPanel();
      repoComboBox = new javax.swing.JComboBox();
      connectorComboBox = new javax.swing.JComboBox<DelegatingConnector>();
      urlTextField = new javax.swing.JTextField();
      connectorLabel = new javax.swing.JLabel();
      urlLabel = new javax.swing.JLabel();
      nameTextField = new javax.swing.JTextField();
      repoLabel = new javax.swing.JLabel();
      newButton = new org.netbeans.modules.bugtracking.util.LinkButton();
      nameLabel = new javax.swing.JLabel();

      org.openide.awt.Mnemonics.setLocalizedText(enableCheckBox, org.openide.util.NbBundle.getMessage(CustomizerIssueTracking.class, "CustomizerIssueTracking.enableCheckBox.text")); // NOI18N

      urlTextField.setText(org.openide.util.NbBundle.getMessage(CustomizerIssueTracking.class, "CustomizerIssueTracking.urlTextField.text")); // NOI18N

      connectorLabel.setLabelFor(connectorComboBox);
      org.openide.awt.Mnemonics.setLocalizedText(connectorLabel, org.openide.util.NbBundle.getMessage(CustomizerIssueTracking.class, "CustomizerIssueTracking.connectorLabel.text")); // NOI18N

      urlLabel.setLabelFor(urlTextField);
      org.openide.awt.Mnemonics.setLocalizedText(urlLabel, org.openide.util.NbBundle.getMessage(CustomizerIssueTracking.class, "CustomizerIssueTracking.urlLabel.text")); // NOI18N

      nameTextField.setText(org.openide.util.NbBundle.getMessage(CustomizerIssueTracking.class, "CustomizerIssueTracking.nameTextField.text")); // NOI18N

      repoLabel.setLabelFor(repoComboBox);
      org.openide.awt.Mnemonics.setLocalizedText(repoLabel, org.openide.util.NbBundle.getMessage(CustomizerIssueTracking.class, "CustomizerIssueTracking.repoLabel.text")); // NOI18N
      repoLabel.setFocusCycleRoot(true);

      org.openide.awt.Mnemonics.setLocalizedText(newButton, org.openide.util.NbBundle.getMessage(CustomizerIssueTracking.class, "CustomizerIssueTracking.newButton.text")); // NOI18N
      newButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
            newButtonActionPerformed(evt);
         }
      });

      nameLabel.setLabelFor(nameTextField);
      org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(CustomizerIssueTracking.class, "CustomizerIssueTracking.nameLabel.text")); // NOI18N

      javax.swing.GroupLayout paneLayout = new javax.swing.GroupLayout(pane);
      pane.setLayout(paneLayout);
      paneLayout.setHorizontalGroup(
         paneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(paneLayout.createSequentialGroup()
            .addGroup(paneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(paneLayout.createSequentialGroup()
                  .addComponent(connectorLabel)
                  .addGap(37, 37, 37)
                  .addComponent(connectorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addGroup(paneLayout.createSequentialGroup()
                  .addComponent(repoLabel)
                  .addGap(18, 18, 18)
                  .addComponent(repoComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(newButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
               .addGroup(paneLayout.createSequentialGroup()
                  .addGroup(paneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(urlLabel)
                     .addComponent(nameLabel))
                  .addGap(66, 66, 66)
                  .addGroup(paneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(urlTextField))))
            .addContainerGap())
      );
      paneLayout.setVerticalGroup(
         paneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(paneLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(paneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(connectorLabel)
               .addComponent(connectorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(paneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(repoLabel)
               .addComponent(repoComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(newButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(paneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(nameLabel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(paneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(urlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(urlLabel))
            .addContainerGap())
      );

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(enableCheckBox)
            .addContainerGap(335, Short.MAX_VALUE))
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addGap(35, 35, 35)
            .addComponent(pane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(28, 28, 28))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(enableCheckBox)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(pane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(62, Short.MAX_VALUE))
      );
   }// </editor-fold>//GEN-END:initComponents

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed
       Repository repo = createRepository();
       if (repo != null) {
          //repoComboBox.addItem(repo);
          repoComboSupport.refreshRepositoryModel();
          repoComboBox.setSelectedItem(repo);
       }
    }//GEN-LAST:event_newButtonActionPerformed
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JComboBox<DelegatingConnector> connectorComboBox;
   private javax.swing.JLabel connectorLabel;
   private javax.swing.JCheckBox enableCheckBox;
   private javax.swing.JLabel nameLabel;
   javax.swing.JTextField nameTextField;
   private org.netbeans.modules.bugtracking.util.LinkButton newButton;
   private javax.swing.JPanel pane;
   private javax.swing.JComboBox repoComboBox;
   private javax.swing.JLabel repoLabel;
   private javax.swing.JLabel urlLabel;
   javax.swing.JTextField urlTextField;
   // End of variables declaration//GEN-END:variables
}
