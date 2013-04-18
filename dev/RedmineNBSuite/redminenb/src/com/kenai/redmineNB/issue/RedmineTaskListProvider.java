/*
 * Copyright 2012 Anchialas.
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
package com.kenai.redmineNB.issue;

import com.kenai.redmineNB.Redmine;
import com.kenai.redmineNB.RedmineConfig;
import com.kenai.redmineNB.RedmineConnector;
import com.kenai.redmineNB.repository.RedmineRepository;
import com.kenai.redmineNB.util.RedmineUtil;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.spi.TaskListIssueProvider;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;


/**
 * {@link TaskListIssueProvider} for Redmine issues.
 *
 * @author Anchialas <anchialas@gmail.com>
 */
public final class RedmineTaskListProvider extends TaskListIssueProvider implements PropertyChangeListener {

   private static final Logger LOG = Logger.getLogger(RedmineTaskListProvider.class.getName());
   private static final String STORAGE_COMMON_VERSION = "1";                  //NOI18N
   public static final String PROPERTY_ISSUE_REMOVED = "issue-removed"; //NOI18N
   //
   private static RedmineTaskListProvider instance;
   //
   private final Object LOCK = new Object();
   private final RequestProcessor rp = new RequestProcessor("RedmineTaskListProvider", 1, false);
   private final PropertyChangeSupport support;
   //
   private boolean initialized;
   private HashMap<String, RedmineLazyIssue> watchedIssues = new HashMap<String, RedmineLazyIssue>(10);
   private Map<String, RedmineRepository> redmineRepositories = new HashMap<String, RedmineRepository>();

   public static synchronized RedmineTaskListProvider getInstance() {
      if (instance == null) {
         instance = new RedmineTaskListProvider();
      }
      return instance;
   }

   private RedmineTaskListProvider() {
      // initialization
      support = new PropertyChangeSupport(this);
      reloadAsync();
   }

   /**
    * Schedules the given issue to be added to the tasklist
    *
    * @param issue        issue to add to the tasklist
    * @param openTaskList if set to true, the tasklist will also be asked to open
    */
   public void add(RedmineIssue issue, boolean openTaskList) {
      URL url = getUrl(issue);
      RedmineLazyIssue lazyIssue;
      // local store
      synchronized (LOCK) {
         if (isAdded(url)) {
            return;
         }
         try {
            RedmineRepository bugzillaRepository = issue.getRepository();

            Repository repository = RedmineUtil.getRepository(bugzillaRepository);
            repository.removePropertyChangeListener(this);
            repository.addPropertyChangeListener(this);

            // create a representation of the real issue for tasklist
            lazyIssue = new RedmineLazyIssue(issue, this);
            watchedIssues.put(url.toString(), lazyIssue);

         } catch (MalformedURLException e) {
            return;
         }
      }
      saveIntern();

      // schedule the addition to tasklist
      super.add(openTaskList, lazyIssue);
   }

   /**
    * Schedule given issue to be removed from the tasklist
    *
    * @param issue
    */
   public void remove(RedmineIssue issue) {
      URL url = getUrl(issue);
      remove(url, true);
   }

   /**
    * Tests if given issue is added to the tasklist.
    *
    * @param issue
    * @return true if the given issue is already added.
    */
   public boolean isAdded(RedmineIssue issue) {
      URL url = getUrl(issue);
      return isAdded(url);
   }

   @Override
   public void removed(LazyIssue lazyIssue) {
      RedmineLazyIssue removedIssue;
      synchronized (LOCK) {
         if (!isAdded(lazyIssue.getUrl())) {
            return;
         }
         removedIssue = watchedIssues.remove(lazyIssue.getUrl().toString());
      }
      saveIntern();
      fireIssueRemoved(removedIssue);
   }

   /**
    * These properties are fired:
    * <ul>
    * <li>{@link #PROPERTY_ISSUE_REMOVED} when an issue is removed from the tasklist in other way that with {@link #remove(org.netbeans.modules.Redmine.issue.RedmineIssue),
    * e.g. with a Remove action from a popup menu in the tasklist.</li>
    * </ul>
    *
    * @param listener
    */
   public void addPropertyChangeListener(PropertyChangeListener listener) {
      support.addPropertyChangeListener(listener);
   }

   public void removePropertyChangeListener(PropertyChangeListener listener) {
      support.removePropertyChangeListener(listener);
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      if (Repository.EVENT_ATTRIBUTES_CHANGED.equals(evt.getPropertyName())) {
         if (evt.getOldValue() != null && evt.getOldValue() instanceof Map) {
            Object oldValue = ((Map)evt.getOldValue()).get(Repository.ATTRIBUTE_URL);
            if (oldValue != null && oldValue instanceof String) {
               String oldRepoUrl = (String)oldValue;
               LinkedList<RedmineLazyIssue> issuesToRefresh = new LinkedList<RedmineLazyIssue>();
               synchronized (LOCK) {
                  // lookup all issues with the same repository url as the changed value
                  for (Map.Entry<String, RedmineLazyIssue> e : watchedIssues.entrySet()) {
                     RedmineLazyIssue issue = e.getValue();
                     Object sourceRepository = evt.getSource();
                     if (sourceRepository != null 
                             && sourceRepository.equals(issue.getRepository())) {
                        URL oldUrl = getUrl(oldRepoUrl, issue.issueId);
                        if (issue.getUrl().toString().equals(oldUrl.toString())) {
                           LOG.log(Level.FINE, "propertyChange: Issue {0} with url {1} needs to be refreshed, repository's url {2} has changed", //NOI18N
                                   new String[]{issue.toString(), oldUrl.toString(), oldRepoUrl});
                           issuesToRefresh.add(issue);
                        }
                     }
                  }
               }
               // refresh issues
               for (RedmineLazyIssue issue : issuesToRefresh) {
                  remove(issue.getUrl(), false);
                  add(issue.getName(), issue.issueId, issue.getRepository());
               }
               // store new issues
               if (!issuesToRefresh.isEmpty()) {
                  saveIntern();
               }
            }
         }
      }
   }

   /**
    * Removes all issues from the ActionItem's which belong to the given repository.
    *
    * @param repository
    */
   public void removeAllFor(RedmineRepository repository) {
      LinkedList<RedmineLazyIssue> issuesToRemove = new LinkedList<RedmineLazyIssue>();
      synchronized (LOCK) {
         // lookup all issues with the same repository url as the changed value
         for (Map.Entry<String, RedmineLazyIssue> e : watchedIssues.entrySet()) {
            RedmineLazyIssue issue = e.getValue();
            if (repository == issue.getRepository()) {
               LOG.log(Level.FINE, "removeAllFor: issue {0} repository {1} has been removed", new String[]{issue.toString(), repository.toString()}); //NOI18N
               issuesToRemove.add(issue);
            }
         }
      }
      // remove issues
      for (RedmineLazyIssue issue : issuesToRemove) {
         remove(issue.getUrl(), false);
      }
      // store issues
      if (!issuesToRemove.isEmpty()) {
         saveIntern();
      }
   }

   /**
    * Call when an issue is loaded for the first time.
    *
    * @param issue cannot be null
    */
   public void notifyIssueCreated(RedmineIssue issue) {
      URL url = getUrl(issue);
      RedmineLazyIssue lazyIssue;
      synchronized (LOCK) {
         lazyIssue = watchedIssues.get(url.toString());
      }
      if (lazyIssue != null) {
         lazyIssue.setIssueReference(issue);
      }
   }

   public void notifyRepositoryCreated(RedmineRepository repository) {
      if (repository.getInfo() != null) {
         redmineRepositories.put(repository.getID(), repository);
      }
   }

   public void notifyRepositoryRemoved(RedmineRepository repository) {
      if (repository.getInfo() != null) {
         redmineRepositories.remove(repository.getID());
      }
   }

   // **** private methods ***** //
   private boolean isAdded(URL url) {
      initializeIssues();
      if (url == null) {
         return false;
      }
      synchronized (LOCK) {
         return watchedIssues.containsKey(url.toString());
      }
   }

   private static URL getUrl(RedmineIssue issue) {
      return getUrl(issue.getRepository().getUrl(), issue.getID());
   }

   private static URL getUrl(String repositoryUrl, String issueId) {
//      String url = Redmine.getInstance().getRepositoryConnector().getTaskUrl(repositoryUrl, issueId);
//      try {
//         return new URL(url);
//      } catch (MalformedURLException ex) {
//         LOG.log(Level.INFO, null, ex);
//      }
//      try {
//         return new URL(repositoryUrl + "#" + issueId);             //NOI18N
//      } catch (MalformedURLException ex) {
//         LOG.log(Level.INFO, null, ex);
//         return null;
//      }
      try {
         return new URL(repositoryUrl + "/" + issueId);
      } catch (MalformedURLException ex) {
         LOG.log(Level.INFO, null, ex);
      }
      return null;
   }

   private void reloadAsync() {
      rp.post(new Runnable() {
         @Override
         public void run() {
            initializeIssues();
         }

      });
   }

   private void saveIntern() {
      RedmineLazyIssue[] lazyIssues;
      synchronized (LOCK) {
         lazyIssues = watchedIssues.values().toArray(new RedmineLazyIssue[watchedIssues.size()]);
      }
      final RedmineLazyIssue[] lazyIssuesToSave = lazyIssues;
      rp.post(new Runnable() {
         @Override
         public void run() {
            initializeIssues();
            LOG.log(Level.FINE, "saveIntern: saving issues");       //NOI18N
            HashMap<String, List<String>> issues = new HashMap<String, List<String>>();
            for (RedmineLazyIssue issue : lazyIssuesToSave) {
               String repositoryIdent = issue.getRepositoryUrl();
               if (repositoryIdent != null) {
                  List<String> issueAttributes = issues.get(repositoryIdent);
                  if (issueAttributes == null) {
                     issueAttributes = new LinkedList<String>();
                     issueAttributes.add(STORAGE_COMMON_VERSION);
                  }
                  issueAttributes.add(issue.issueId);            // issue id
                  issueAttributes.add(issue.getName());          // description
                  if (LOG.isLoggable(Level.FINE)) {
                     LOG.log(Level.FINE, "saveIntern: saving {0} for repo: {1}", 
                             new Object[]{issueAttributes, repositoryIdent}); //NOI18N
                  }
                  issues.put(repositoryIdent, issueAttributes);
               }
            }
            // save permanently
            RedmineConfig.getInstance().setActionItemIssues(issues);
         }

      });
   }

   private void initializeIssues() {
      synchronized (LOCK) {
         if (initialized) {
            return;
         }
         initialized = true;
         LOG.finer("initializeIssues: reloading saved issues");      //NOI18N
         // load from storage
         Map<String, List<String>> repositoryIssues = RedmineConfig.getInstance().getActionItemIssues();
         if (repositoryIssues.isEmpty()) {
            LOG.fine("initializeIssues: no saved issues");          //NOI18N
            return;
         }
         addIssues(repositoryIssues);
      }
   }

   private String getNextAttribute(ListIterator<String> it) {
      String attr = null;
      if (it.hasNext()) {
         attr = it.next();
      }
      return attr;
   }

   private void addIssues(Map<String, List<String>> repositoryIssues) {
      Collection<Repository> repositories = RepositoryManager.getInstance().getRepositories(RedmineConnector.ID);
      for (Repository repository : repositories) {
         // all issues for this repository
         List<String> issueAttributes = repositoryIssues.get(repository.getUrl());
         if (issueAttributes != null && issueAttributes.size() > 1) {
            ListIterator<String> it = issueAttributes.listIterator();
            if (!STORAGE_COMMON_VERSION.equals(it.next())) {
               LOG.log(Level.WARNING, "Old unsupported storage version, expecting {0}", STORAGE_COMMON_VERSION); //NOI18N
               break;
            }
            for (; it.hasNext();) {
               String issueId = getNextAttribute(it);
               String issueName = getNextAttribute(it);
               if (issueId == null || issueName == null) {
                  LOG.log(Level.WARNING, "Corrupted issue attributes: {0} {1}", new String[]{issueId, issueName}); //NOI18N
                  break;
               }
               RedmineRepository redmineRepository = redmineRepositories.get(repository.getId());
               assert redmineRepository != null;
               add(issueName, issueId, redmineRepository);
            }
            repository.addPropertyChangeListener(this);
            // remove processed attributes
            repositoryIssues.remove(repository.getUrl());
         }
      }
   }

   private void remove(URL url, boolean savePermanently) {
      RedmineLazyIssue lazyIssue;
      synchronized (LOCK) {
         if (!isAdded(url)) {
            return;
         }
         lazyIssue = watchedIssues.remove(url.toString());
      }
      if (savePermanently) {
         saveIntern();
      }
      // notify tasklist
      super.remove(lazyIssue);
   }

   private void add(String issueName, String issueId, RedmineRepository repository) {
      URL issueUrl = getUrl(repository.getUrl(), issueId);
      RedmineLazyIssue issue;
      synchronized (LOCK) {
         if (issueUrl == null || isAdded(issueUrl)) {
            return;
         }
         watchedIssues.put(issueUrl.toString(), issue = new RedmineLazyIssue(issueName, issueUrl, issueId, repository, this));
      }
      // notify tasklist
      super.add(issue);
      if (LOG.isLoggable(Level.FINER)) {
         LOG.log(Level.FINER, "initializeIssues: issue added: {0}", issue); //NOI18N
      }
   }

   private static void runCancellableCommand(Runnable runnable, String progressMessage) {
      RequestProcessor.Task task = Redmine.getInstance().getRequestProcessor().post(runnable);
      ProgressHandle handle = ProgressHandleFactory.createHandle(progressMessage, task); //NOI18N
      handle.start();
      task.waitFinished();
      handle.finish();
   }

   private RedmineIssue getIssue(final RedmineRepository repository, final String issueId) {
      assert !EventQueue.isDispatchThread();
      // XXX is there a simpler way to obtain an issue?
      int status = repository.getIssueCache().getStatus(issueId);
      final RedmineIssue[] issue = new RedmineIssue[1];
      if (status == IssueCache.ISSUE_STATUS_UNKNOWN) { // not yet cached
         Runnable runnable = new Runnable() {
            @Override
            public void run() {
               LOG.log(Level.FINE, "getIssue: creating issue {0}", repository.getUrl() + "#" + issueId); //NOI18N
               issue[0] = repository.getIssue(issueId);
            }

         };
         runCancellableCommand(runnable, NbBundle.getMessage(RedmineTaskListProvider.class, "RedmineIssueProvider.loadingIssue"));
      } else {
         LOG.log(Level.FINER, "getIssue: getting issue {0} from the cache", repository.getUrl() + "#" + issueId); //NOI18N
         issue[0] = repository.getIssueCache().getIssue(issueId);
      }
      return issue[0];
   }

   private void fireIssueRemoved(RedmineLazyIssue lazyIssue) {
      RedmineIssue issue = lazyIssue.issueRef.get();
      if (issue != null) {
         support.firePropertyChange(PROPERTY_ISSUE_REMOVED, issue, null);
      }
   }


   /**
    * Redmine representation of a LazyIssue
    */
   private static class RedmineLazyIssue extends LazyIssue {

      private final String issueId;
      /**
       * cached repository for the issue
       */
      private WeakReference<RedmineRepository> repositoryRef;
      protected final RedmineTaskListProvider provider;
      private WeakReference<RedmineIssue> issueRef;
      private PropertyChangeListener issueListener;

      public RedmineLazyIssue(RedmineIssue issue, RedmineTaskListProvider provider) throws MalformedURLException {
         super(RedmineTaskListProvider.getUrl(issue), issue.getDisplayName());
         this.issueId = issue.getID();
         this.provider = provider;
         this.repositoryRef = new WeakReference<RedmineRepository>(issue.getRepository());
         this.issueRef = new WeakReference<RedmineIssue>(issue);
         attachIssueListener(issue);
      }

      public RedmineLazyIssue(String name, URL url, String issueId, RedmineRepository repository, RedmineTaskListProvider provider) {
         super(url, name);
         this.issueId = issueId;
         this.repositoryRef = new WeakReference<RedmineRepository>(repository);
         this.provider = provider;
         this.issueRef = new WeakReference<RedmineIssue>(null);
      }

      public RedmineIssue getIssue() {
         RedmineIssue issue = issueRef.get();
         if (issue == null) {
            RedmineRepository repository = getRepository();
            if (repository == null) {
               LOG.log(Level.INFO, "Repository unavailable for {0}", getUrl().toString()); //NOI18N
               if (canBeAutoRemoved()) {
                  // no repository found for this issue and the issue can be removed automaticaly
                  provider.remove(getUrl(), true);
               }
            } else {
               issue = provider.getIssue(repository, issueId);
            }
            setIssueReference(issue);
         }
         return issue;
      }

      private RedmineRepository getRepository() {
         return repositoryRef.get();
      }

      /**
       * Sets the reference to the issue and attaches an issue listener
       *
       * @param issue if null then this only clears the reference.
       */
      private void setIssueReference(RedmineIssue issue) {
         issueRef = new WeakReference<RedmineIssue>(issue);
         if (issue != null) {
            applyChangesFor(issue);
            attachIssueListener(issue);
         }
      }

      private void attachIssueListener(RedmineIssue issue) {
         if (issueListener == null) {
            issueListener = new PropertyChangeListener() {
               @Override
               public void propertyChange(PropertyChangeEvent evt) {
                  RedmineIssue issue = issueRef.get();
                  if (IssueProvider.EVENT_ISSUE_REFRESHED.equals(evt.getPropertyName()) && issue != null) {
                     // issue has somehow changed, checks for its changes and apply them in the tasklist
                     applyChangesFor(issue);
                  }
               }

            };
         }
         LOG.log(Level.FINE, "attachIssueListener: on issue {0}", issue.toString());
         issue.addPropertyChangeListener(WeakListeners.propertyChange(issueListener, issue));
      }

      private void applyChangesFor(RedmineIssue issue) {
         boolean requiresSave = false;
         if (!getName().equals(issue.getDisplayName())) {
            setName(issue.getDisplayName());
            requiresSave = true;
         }
         if (requiresSave) {
            provider.saveIntern();
         }
      }

      @Override
      public String getRepositoryUrl() {
         String repoUrl = null;
         RedmineRepository repository = getRepository();
         if (repository != null) {
            repoUrl = repository.getUrl();
         }
         return repoUrl;
      }

      @Override
      public List<? extends Action> getActions() {
         List<AbstractAction> actions = new LinkedList<AbstractAction>();
//         actions.add(new AbstractAction(NbBundle.getMessage(RedmineTaskListProvider.class, "RedmineIssueProvider.resolveAction")) { //NOI18N
//            @Override
//            public void actionPerformed(ActionEvent e) {
//               RequestProcessor.getDefault().post(new Runnable() {
//                  @Override
//                  public void run() {
//                     final RedmineIssue issue = getIssue();
//                     if (issue == null) {
//                        LOG.fine("Resole action: null issue returned"); //NOI18N
//                     } else {
//                        if (!issue.isResolveAvailable()) {
//                           DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
//                                   NbBundle.getMessage(RedmineTaskListProvider.class, "RedmineIssueProvider.resolveAction.notPermitted"),
//                                   NotifyDescriptor.INFORMATION_MESSAGE));
//                           return;
//                        }
//                        ResolveIssuePanel panel = new ResolveIssuePanel(issue);
//                        if (panel.showDialog()) {
//                           LOG.finer("Resolve action: resolving..."); //NOI18N
//                           String pattern = NbBundle.getMessage(RedmineTaskListProvider.class, "RedmineIssueProvider.resolveIssueMessage"); //NOI18N
//                           final String resolution = panel.getSelectedResolution();
//                           final String duplicateId = panel.getDuplicateId();
//                           final String comment = panel.getComment();
//                           runCancellableCommand(new Runnable() {
//                              @Override
//                              public void run() {
//                                 if (RedmineIssue.RESOLVE_DUPLICATE.equals(resolution)) {
//                                    issue.duplicate(duplicateId);
//                                 } else {
//                                    issue.resolve(resolution);
//                                 }
//                                 if (comment.length() > 0) {
//                                    issue.addComment(comment);
//                                 }
//                                 if (issue.submitAndRefresh()) {
//                                    RedmineUtil.openIssue(issue);
//                                 }
//                              }
//
//                           }, MessageFormat.format(pattern, issue.getID()));
//                        }
//                     }
//                  }
//
//               });
//            }
//
//            @Override
//            public boolean isEnabled() {
//               // try to disable the action for cached closed issues
//               boolean allowed = true;
//               RedmineIssue issue = issueRef.get();
//               if (issue != null) {
//                  allowed = issue.isResolveAvailable();
//               }
//               return allowed;
//            }
//
//         });
         return actions;
      }

      /**
       * Returns true if the issue can be automatically removed, which should not be met for kenai issues
       *
       * @return
       */
      protected boolean canBeAutoRemoved() {
         return true;
      }

      /**
       * Stores a reference to the repository for quick access
       *
       * @param repository
       */
      protected void setRepositoryReference(RedmineRepository repository) {
         if (repository != null) {
            repositoryRef = new WeakReference<RedmineRepository>(repository);
         }
      }

      @Override
      public void open() {
         RedmineIssue issue = getIssue();
         if (issue != null) {
            LOG.log(Level.FINER, "TaskListProvider: openning issue {0}", getName()); //NOI18N
            // openning the real issue in it's top component
            RedmineUtil.openIssue(issue);
         } else {
            LOG.log(Level.FINE, "null issue returned for {0}", getName()); //NOI18N
         }
      }

   }

}
