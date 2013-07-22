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
package org.anchialas.nb.issuetracking.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import org.anchialas.nb.issuetracking.IssueTrackerData;
import org.anchialas.nb.issuetracking.IssueTrackingManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.ui.nodes.RepositoryNode;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 * Context-sensitive action added to the project's popup menu (of specific project types).
 *
 * @author Anchialas <anchialas@gmail.com>
 * @see http://wiki.netbeans.org/DevFaqActionAddProjectTypePopUp
 * @see org.netbeans.modules.team.ui.TeamPopupMenu
 */
@Messages({
   "LBL_ISSUE_TRACKING=Issue Tracking",
   "# {0} - connector name",
   "# {1} - issue tracker name",
   "LBL_CONNECTOR_ISSUE_TRACKER={0}: {1}"
})
public class IssueTrackingPopupMenuAction extends AbstractAction implements ContextAwareAction, AntProjectListener {

   private Map<Project, Data> menuProviders = Collections.synchronizedMap(new WeakHashMap<Project, Data>());
   private static IssueTrackingPopupMenuAction inst = null;

   private IssueTrackingPopupMenuAction() {
      putValue(NAME, Bundle.LBL_ISSUE_TRACKING());
   }

   @ActionID(id = "org.anchialas.nb.issuetracking.action.IssueTrackingPopupMenuAction", category = "Issue Tracking")
   @ActionRegistration(lazy = false, displayName = "#LBL_ISSUE_TRACKING")
   @ActionReference(path = "Projects/Actions", position = 157)
   public static synchronized IssueTrackingPopupMenuAction getDefault() {
      if (inst == null) {
         inst = new IssueTrackingPopupMenuAction();
         //TeamServerManager.getDefault().addPropertyChangeListener(WeakListeners.propertyChange(inst, TeamServerManager.getDefault()));
      }
      return inst;
   }

   @Override
   public Action createContextAwareInstance(Lookup actionContext) {
      return new PopupMenuPresenter(actionContext);
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      assert false;
   }

//   @Override
//   public void propertyChange(PropertyChangeEvent evt) {
//        if (TeamServerManager.PROP_INSTANCES.equals(evt.getPropertyName())) {
//            Utilities.getRequestProcessor().post(new Runnable() {
//                @Override
//                public void run() {
//                    menuProviders.clear();
//                }
//            });
//        }
//   }
   // AntProjectListener implementation //////////////////////////////////
   @Override
   public void configurationXmlChanged(AntProjectEvent ev) {
      // do nothing
   }

   @Override
   public void propertiesChanged(AntProjectEvent ev) {
      IssueTrackingManager.getRequestProcessor().post(new Runnable() {
         @Override
         public void run() {
            menuProviders.clear();
         }
      });
   }

   private final class PopupMenuPresenter extends AbstractAction implements Presenter.Popup {

      private final Project proj;

      private PopupMenuPresenter(Lookup actionContext) {
         Collection<? extends Project> projects = actionContext.lookupAll(Project.class);
         proj = projects.size() == 1 ? projects.iterator().next() : null;
      }

      @Override
      @Messages("LBL_CHECKING=Checking for Issue Tracking support - wait...")
      public JMenuItem getPopupPresenter() {
         final JMenu menu;
         if (proj == null) {
            menu = new JMenu(); //NOI18N
            menu.setVisible(false);
         } else {
            PopupMenuProvider provider = getMenuProvider(proj);
            if (provider == null) {
               menu = new JMenu(Bundle.LBL_CHECKING());
               menu.setVisible(true);
               menu.setEnabled(false);

               IssueTrackingManager.getRequestProcessor().post(new Runnable() { // cache the results, update the popup menu
                  @Override
                  public void run() {
                     Data data = menuProviders.get(proj);
                     IssueTrackerData issueTrackerData = IssueTrackingManager.getData(proj);

                     String repoUrl = data == null ? null : data.repoUrl;
                     if (repoUrl == null) {
                        repoUrl = issueTrackerData == null ? null : issueTrackerData.getUrl();
                        // repoUrl = VersioningQuery.getRemoteLocation(proj.getProjectDirectory().toURI());
                     }

                     PopupMenuProvider popupProvider = null;
                     if (repoUrl != null) {
                        RepositoryImpl repo = IssueTrackingManager.getRepository(issueTrackerData);
                        if (repo != null) {
                           popupProvider = new RepositoryPopupMenuProvider(repo);
                        }
                        //repo.getProvider().
                        //Collection<IssueImpl> recentIssues = BugtrackingUtil.getRecentIssues(repo);

                        if (popupProvider != null) {
                           issueTrackerData.getHelper().addAntProjectListener(
                                   WeakListeners.create(AntProjectListener.class, IssueTrackingPopupMenuAction.this, issueTrackerData.getHelper()));

                           menuProviders.put(proj, new Data(repoUrl, popupProvider));

                           final JMenu tmp = constructMenu();
                           final Component[] c = tmp.getMenuComponents();
                           SwingUtilities.invokeLater(new Runnable() {
                              @Override
                              public void run() {
                                 tmp.revalidate();
                                 menu.setText(tmp.getText());
                                 menu.setIcon(tmp.getIcon());
                                 menu.setEnabled(c.length > 0);
                                 for (int i = 0; i < c.length; i++) {
                                    Component item = c[i];
                                    menu.add(item);
                                 }
                                 menu.revalidate();
                                 menu.getParent().validate();
                              }
                           });
                        }
                     }
                     if (popupProvider == null) {
                        popupProvider = DummyProvider.getDefault();
                        menu.setVisible(false);
                        menuProviders.put(proj, new Data(repoUrl == null ? "" : repoUrl, popupProvider)); //NOI18N null cannot be used - project with no repo is null, "" is to indicate I already checked this one...
                     }
                  }
               });

            } else { // show for Kenai projects
               menu = constructMenu();
            }
         }
         return menu;
      }

      private JMenu constructMenu() {
         final JMenu teamPopup;
         Data data = menuProviders.get(proj);
         RepositoryImpl repo = data == null ? null : data.provider.getOriginator();
         if (repo == null) {
            teamPopup = new JMenu(Bundle.LBL_ISSUE_TRACKING());
         } else {
            teamPopup = new JMenu(Bundle.LBL_CONNECTOR_ISSUE_TRACKER(IssueTrackingManager.getConnector(repo.getConnectorId()).getDisplayName(),
                                                                     repo.getDisplayName()));
            teamPopup.setIcon(ImageUtilities.image2Icon(repo.getIcon()));
         }
         teamPopup.setVisible(false);
         if (data != null) {
            Action[] actions = data.provider.getPopupMenuActions();
            if (actions.length > 0) {
               teamPopup.setVisible(true);
               for (Action a : actions) {
                  if (a == null) {
                     teamPopup.addSeparator();
                  } else {
                     teamPopup.add(createmenuItem(a));
                  }
               }
            }
         }
         return teamPopup;
      }

      public JSeparator createJSeparator() {
         JMenu menu = new JMenu();
         menu.addSeparator();
         return (JSeparator)menu.getPopupMenu().getComponent(0);
      }

      PopupMenuProvider getMenuProvider(Project proj) {
         assert proj != null;
         Data data = menuProviders.get(proj);
         if (data == null) { // repo is not cached - has to be cached on the background before
            return null;
         }
         return data.provider;
      }

      @Override
      public void actionPerformed(ActionEvent e) {
      }
   }

   private JMenuItem createmenuItem(Action action) {
      JMenuItem item;
      if (action instanceof Presenter.Menu) {
         item = ((Presenter.Menu)action).getMenuPresenter();
      } else {
         item = new JMenuItem();
         Actions.connect(item, action, true);
      }
      return item;
   }

   public static interface PopupMenuProvider<T> {

      public Action[] getPopupMenuActions();

      public T getOriginator();
   }

   private static class RepositoryPopupMenuProvider implements PopupMenuProvider<RepositoryImpl> {

      private final RepositoryImpl repo;

      public RepositoryPopupMenuProvider(RepositoryImpl repo) {
         this.repo = repo;
      }

      @Override
      public Action[] getPopupMenuActions() {
         RepositoryNode node = new RepositoryNode(repo);
         return node.getActions(true);
      }

      @Override
      public RepositoryImpl getOriginator() {
         return repo;
      }
   }

   private static class DummyProvider implements PopupMenuProvider {

      private static DummyProvider instance;

      private DummyProvider() {
      }

      public static synchronized DummyProvider getDefault() {
         if (instance == null) {
            instance = new DummyProvider();
         }
         return instance;
      }

      @Override
      public Action[] getPopupMenuActions() {
         return new Action[0];
      }

      @Override
      public Object getOriginator() {
         return null;
      }
   }

   private static class Data {

      private final String repoUrl;
      private final PopupMenuProvider<RepositoryImpl> provider;

      public Data(String repoUrl, PopupMenuProvider provider) {
         this.repoUrl = repoUrl;
         this.provider = provider;
      }
   }
}