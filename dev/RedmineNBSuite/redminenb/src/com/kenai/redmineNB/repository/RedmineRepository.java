/*
 * Copyright 2012 Anchialas and Mykolaas.
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
package com.kenai.redmineNB.repository;

import com.kenai.redmineNB.Redmine;
import com.kenai.redmineNB.RedmineConfig;
import com.kenai.redmineNB.RedmineConnector;
import com.kenai.redmineNB.issue.RedmineIssue;
import com.kenai.redmineNB.issue.RedmineTaskListProvider;
import com.kenai.redmineNB.query.ParameterValue;
import com.kenai.redmineNB.query.RedmineQuery;
import com.kenai.redmineNB.query.RedmineQueryController;
import com.kenai.redmineNB.user.RedmineUser;
import com.kenai.redmineNB.util.Is;
import com.kenai.redmineNB.util.RedmineUtil;
import com.kenai.redminenb.api.AuthMode;
import com.kenai.redminenb.api.IssuePriority;
import java.awt.EventQueue;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import org.netbeans.modules.bugtracking.kenai.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import com.taskadapter.redmineapi.NotFoundException;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.*;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;

/**
 * Redmine repository manager.
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
@NbBundle.Messages({
   "LBL_RepositoryTooltip=\"Redmine repository<br>{0} : {1}@{2}",
   "LBL_RepositoryTooltipNoUser=\"{0} : {1}"
})
public class RedmineRepository {

   static final String PROPERTY_AUTH_MODE = "authMode";        // NOI18N  
   static final String PROPERTY_ACCESS_KEY = "accessKey";      // NOI18N  
   static final String PROPERTY_PROJECT_ID = "projectId";      // NOI18N  
   // 
   private RepositoryInfo info;
   private transient RepositoryController controller;
   private Collection<RedmineQuery> queries;
   // TODO Create manager wrapping class to handle Redmine related errors
   private transient RedmineManager manager;
   private transient RedmineUser currentUser;
   private transient Project project;
   private transient Lookup lookup;
   private final transient InstanceContent ic;
   private transient RedmineIssueCache cache;
   //
   private final Set<String> issuesToRefresh = new HashSet<String>(5);
   private final Set<RedmineQuery> queriesToRefresh = new HashSet<RedmineQuery>(3);
   private RequestProcessor.Task refreshIssuesTask;
   private RequestProcessor.Task refreshQueryTask;
   private RequestProcessor refreshProcessor;
   //
   private final Object QUERIES_LOCK = new Object();

   /**
    * Default constructor required for deserializing.
    */
   public RedmineRepository() {
      this.ic = new InstanceContent();
   }

   public RedmineRepository(RepositoryInfo info) {
      this();
      this.info = info;

      try {
         String projectId = info.getValue(PROPERTY_PROJECT_ID);
         if (projectId != null) {
            setProject(getManager().getProjectByKey(projectId));
         }
      } catch (RedmineException ex) {
         Exceptions.printStackTrace(ex);
      }
      RedmineTaskListProvider.getInstance().notifyRepositoryCreated(this);
   }

   public Image getIcon() {
      return Redmine.getIconImage();
   }

   public boolean isReachable() throws IOException {
      URL url = new URL(getUrl());
      //URLConnection conn = url.openConnection();
      //return InetAddress.getByName(url.getHost()).isReachable(1000);
      // TODO InetAddress#isReachable does not work with some systems
      return true;
   }

   public RepositoryInfo getInfo() {
      return info;
   }

   synchronized void setInfoValues(String name, String url, String user, char[] password,
                                   String accessKey, AuthMode authMode, Project project) {
      String id = info != null ? info.getId() : name + System.currentTimeMillis();
      String httpUser = null;
      char[] httpPassword = null;
      RepositoryInfo ri = new RepositoryInfo(id,
                                             RedmineConnector.ID,
                                             url,
                                             name,
                                             getTooltip(name, user, url),
                                             user,
                                             httpUser,
                                             password,
                                             httpPassword);
      info = ri;
      setAccessKey(accessKey);
      setAuthMode(authMode);
      setProject(project);
   }

   public String getDisplayName() {
      try {
         if (isReachable()) {
            return info.getDisplayName();
         }
      } catch (IOException ex) {
      }
      return info.getDisplayName() + " (offline)";
   }

   private String getTooltip(String repoName, String user, String url) {
      return Bundle.LBL_RepositoryTooltip(repoName, user, url);
   }

   public String getID() {
      return info.getId();
   }

   public String getUrl() {
      //return taskRepository != null ? taskRepository.getUrl() : null;
      return info.getUrl();
   }

   public AuthMode getAuthMode() {
      return AuthMode.get(info.getValue(PROPERTY_AUTH_MODE));
   }

   public void setAuthMode(AuthMode authMode) {
      AuthMode old = getAuthMode();
      if (!Is.equals(old, authMode)) {
         manager = null;
      }
      info.putValue(PROPERTY_AUTH_MODE, authMode == null ? null : authMode.name());
   }

   public String getAccessKey() {
      return info.getValue(PROPERTY_ACCESS_KEY);
   }

   public void setAccessKey(String accessKey) {
      String old = getAccessKey();
      if (!Is.equals(old, accessKey)) {
         manager = null; // force reconnect
      }
      info.putValue(PROPERTY_ACCESS_KEY, accessKey);
   }

   public char[] getPassword() {
      return info.getPassword();
   }

   public String getUsername() {
      return info.getUsername();
   }

   public Project getProject() {
      return project;
   }

   public void setProject(Project project) {
      this.project = project;
      info.putValue(PROPERTY_PROJECT_ID, project == null ? null : String.valueOf(project.getId()));
   }

   public RedmineIssue getIssue(String issueId) {
      try {
         com.taskadapter.redmineapi.bean.Issue issue = getManager().getIssueById(Integer.valueOf(issueId));
         RedmineIssue redmineIssue = (RedmineIssue)getIssueCache().setIssueData(issueId, issue);
         //ensureConfigurationUptodate(issue);
         return redmineIssue;

      } catch (NotFoundException ex) {
         return null;
      } catch (Exception ex) {
         Redmine.LOG.log(Level.SEVERE, null, ex);
         return null;
      }
   }

   public Collection<RedmineIssue> getIssues(final String... ids) {
      final List<RedmineIssue> ret = new ArrayList<RedmineIssue>(ids.length);
      for (String id : ids) {
         RedmineIssue issue = getIssue(id);
         if (issue != null) {
            ret.add(issue);
         }
      }
      return ret;
   }

   public void remove() {
//      try {
//         Redmine.getInstance().removeRepository(this);
//      } catch (com.kenai.redmineNB.RedmineException ex) {
//         JOptionPane.showMessageDialog(null, ex.getLocalizedMessage());
//      }
      synchronized (QUERIES_LOCK) {
         for (RedmineQuery rq : doGetQueries()) {
            removeQuery(rq);
         }
      }
      resetRepository(true);
      RedmineTaskListProvider.getInstance().notifyRepositoryRemoved(this);
   }

   synchronized void resetRepository(boolean keepConfiguration) {
      if (!keepConfiguration) {
         manager = null;
      }
   }

   public RepositoryController getController() {
      if (controller == null) {
         controller = new RedmineRepositoryController(this);
      }
      return controller;
   }

   public RedmineIssue createIssue() {
      return new RedmineIssue(this);
   }

   public RedmineQuery createQuery() {
      return new RedmineQuery(this);
   }

   public void removeQuery(RedmineQuery query) {
      RedmineConfig.getInstance().removeQuery(this, query);
      getIssueCache().removeQuery(query.getStoredQueryName());
      doGetQueries().remove(query);
      stopRefreshing(query);
   }

   public void saveQuery(RedmineQuery query) {
      assert info != null;
      RedmineConfig.getInstance().putQuery(this, query);
      doGetQueries().add(query);
   }

   private Collection<RedmineQuery> doGetQueries() {
      if (queries == null) {
         queries = new HashSet<RedmineQuery>(10);
         String[] qs = RedmineConfig.getInstance().getQueries(getID());
         for (String queryName : qs) {
            RedmineQuery q = RedmineConfig.getInstance().getQuery(this, queryName);
            if (q != null) {
               queries.add(q);
            } else {
               Redmine.LOG.log(Level.WARNING, "Couldn''t find query with stored name {0}", queryName); // NOI18N
            }
         }
      }
      return queries;
   }

   public Collection<RedmineQuery> getQueries() {
      return doGetQueries();
   }

   /**
    * Get this {@link #project}'s users.
    *
    * @return
    */
   public Collection<RedmineUser> getUsers() {
      List<RedmineUser> users = new ArrayList<RedmineUser>();
      try {
         users.add(currentUser);
         for (Membership m : manager.getMemberships(project)) {
            if (m.getUser() != null && !currentUser.getUser().getId().equals(m.getUser().getId())) {
               users.add(new RedmineUser(m.getUser()));
            }
         }
      } catch (RedmineException ex) {
         // TODO Notify user that Redmine internal error has happened
         Redmine.LOG.log(Level.SEVERE, "Can't get Redmine Users", ex);
      }
      return users;
   }

   public List<Tracker> getTrackers() {
      try {
         return getManager().getTrackers();
      } catch (NotFoundException ex) {
         // TODO Notify user that the issue no longer exists
         Redmine.LOG.log(Level.SEVERE, "Can't get Redmine Issue Trackers", ex);
      } catch (RedmineException ex) {
         // TODO Notify user that Redmine internal error has happened
         Redmine.LOG.log(Level.SEVERE, "Can't get Redmine Issue Trackers", ex);
      }
      return Collections.<Tracker>emptyList();
   }

   public IssueStatus getStatus(int id) {
      for (IssueStatus issueStatus : getStatuses()) {
         if (id == issueStatus.getId()) {
            return issueStatus;
         }
      }
      return null;
   }

   public Collection<? extends IssueStatus> getStatuses() {
      Collection<? extends IssueStatus> c = getLookup().lookupAll(IssueStatus.class);
      if (!c.isEmpty()) {
         return c;
      }
      try {
         c = getManager().getStatuses();
      } catch (NotFoundException ex) {
         DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                 "Can't get Issue Statuses from Redmine:\n" + ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
         Redmine.LOG.log(Level.SEVERE, "Can't get Issue Statuses from Redmine", ex);
      } catch (Exception ex) {
         Redmine.LOG.log(Level.SEVERE, "Can't get Issue Statuses from Redmine", ex);
      }
      if (c.isEmpty()) {
         c = Collections.singleton(new IssueStatus(-1, "[n/a]"));
      }
      for (IssueStatus issueStatus : c) {
         ic.add(issueStatus);
      }
      return c;
   }

   public Collection<? extends IssueCategory> reloadIssueCategories() {
      for (IssueCategory issueCategory : getLookup().lookupAll(IssueCategory.class)) {
         ic.remove(issueCategory);
      }
      return getIssueCategories();
   }

   public Collection<? extends IssueCategory> getIssueCategories() {
      Collection<? extends IssueCategory> c = getLookup().lookupAll(IssueCategory.class);
      if (!c.isEmpty()) {
         return c;
      }
      try {
         c = getManager().getCategories(project.getId());
      } catch (NotFoundException ex) {
         DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                 "Can't get Issue Categories for Redmine Project " + project.getName()
                 + ":\n" + ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
         Redmine.LOG.log(Level.SEVERE, "Can't get Issue Categories for Redmine Project " + project.getName(), ex);
      } catch (Exception ex) {
         Redmine.LOG.log(Level.SEVERE, "Can't get Issue Categories for Redmine Project " + project.getName(), ex);
      }
      if (c != null) {
//      if (c.isEmpty()) {
//         IssueCategory category = new IssueCategory();
//         category.setId(-1);
//         category.setName("[n/a]");
//         c = Collections.singleton(category);
//      }
         for (IssueCategory category : c) {
            category.setProject(null);
            category.setAssignee(null);
            ic.add(category);
         }
      }
      return c;
   }

   public Collection<? extends Version> reloadVersions() {
      for (Version v : getLookup().lookupAll(Version.class)) {
         ic.remove(v);
      }
      return getVersions();
   }

   public List<Version> getVersions() {
      try {
         return getManager().getVersions(project.getId());
      } catch (NotFoundException ex) {
         // TODO Notify user that the issue no longer exists
         Redmine.LOG.log(Level.SEVERE, "Can't get Versions for Redmine Project " + project.getName(), ex);
      } catch (RedmineException ex) {
         // TODO Notify user that Redmine internal error has happened
         Redmine.LOG.log(Level.SEVERE, "Can't get Versions for Redmine Project " + project.getName(), ex);
      }
      // TODO: return a default set of Categories
      return Collections.<Version>emptyList();
   }

   public List<IssuePriority> getIssuePriorities() {
      // XXX not yet supported by redmine-java-api
      return Arrays.asList(
              new IssuePriority(7, "Immediate"),
              new IssuePriority(6, "Urgent"),
              new IssuePriority(5, "High"),
              new IssuePriority(4, "Normal"),
              new IssuePriority(3, "Low"));
   }

   public IssueCache<RedmineIssue, com.taskadapter.redmineapi.bean.Issue> getIssueCache() {
      if (cache == null) {
         cache = new RedmineIssueCache();
      }
      return cache;
   }

   public Collection<RedmineIssue> simpleSearch(String string) {
      try {
         List<com.taskadapter.redmineapi.bean.Issue> issuesByID =
                 new LinkedList<com.taskadapter.redmineapi.bean.Issue>();

         try {
            issuesByID.add(getManager().getIssueById(Integer.parseInt(string)));
         } catch (NumberFormatException ex) {
         } catch (NotFoundException ex) {
         }

         return RedmineIssue.getIssues(this,
                                       issuesByID,
                                       getManager().getIssuesBySummary(project.getIdentifier(), "*" + string + "*"));

      } catch (NotFoundException ex) {
         // TODO Notify user that the issue no longer exists
         Redmine.LOG.log(Level.SEVERE, "Can't search for Redmine issues", ex);
      } catch (RedmineException ex) {
         // TODO Notify user that Redmine internal error has happened
         Redmine.LOG.log(Level.SEVERE, "Can't search for Redmine issues", ex);
      }

      return Collections.<RedmineIssue>emptyList();
   }

   public Lookup getLookup() {
      if (lookup == null) {
         ic.add(getIssueCache());
         lookup = new AbstractLookup(ic);
         //lookup = Lookups.fixed(getIssueCache());
      }
      return lookup;
   }

   public final RedmineManager getManager() throws RedmineException {
      AuthMode authMode = getAuthMode();
      if (manager == null) {
         if (authMode == null) {
            throw new IllegalArgumentException("authMode must be set");
         }
         if (authMode == AuthMode.AccessKey) {
            manager = new RedmineManager(getUrl(), getAccessKey());
         } else {
            manager = new RedmineManager(getUrl());
            manager.setLogin(getUsername());
            manager.setPassword(getPassword() == null ? "" : String.valueOf(getPassword()));
         }
         currentUser = new RedmineUser(manager.getCurrentUser(), true);
         manager.setObjectsPerPage(100);
      }
      return manager;
   }

   public RedmineUser getCurrentUser() {
      return currentUser;
   }

   private RequestProcessor getRefreshProcessor() {
      if (refreshProcessor == null) {
         refreshProcessor = new RequestProcessor("Redmine refresh - " + getDisplayName()); // NOI18N
      }
      return refreshProcessor;
   }

   private void setupIssueRefreshTask() {
      if (refreshIssuesTask == null) {
         refreshIssuesTask = getRefreshProcessor().create(new Runnable() {
            @Override
            public void run() {
               Set<String> ids;
               synchronized (issuesToRefresh) {
                  ids = new HashSet<String>(issuesToRefresh);
               }
               if (ids.isEmpty()) {
                  Redmine.LOG.log(Level.FINE, "no issues to refresh {0}",
                                  getDisplayName()); // NOI18N
                  return;
               }
               Redmine.LOG.log(Level.FINER, "preparing to refresh issue {0} - {1}",
                               new Object[]{getDisplayName(), ids}); // NOI18N
//               GetMultiTaskDataCommand cmd = new GetMultiTaskDataCommand(RedmineRepository.this, ids, new IssuesCollector());
//               getExecutor().execute(cmd, false);
               scheduleIssueRefresh();
            }
         });
         scheduleIssueRefresh();
      }
   }

   private void setupQueryRefreshTask() {
      if (refreshQueryTask == null) {
         refreshQueryTask = getRefreshProcessor().create(new Runnable() {
            @Override
            public void run() {
               try {
                  Set<RedmineQuery> queries;
                  synchronized (refreshQueryTask) {
                     queries = new HashSet<RedmineQuery>(queriesToRefresh);
                  }
                  if (queries.isEmpty()) {
                     Redmine.LOG.log(Level.FINE, "no queries to refresh {0}",
                                     new Object[]{getDisplayName()}); // NOI18N
                     return;
                  }
                  for (RedmineQuery q : queries) {
                     Redmine.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}",
                                     new Object[]{q.getDisplayName(), getDisplayName()}); // NOI18N
                     RedmineQueryController qc = q.getController();
                     qc.autoRefresh();
                  }
               } finally {
                  scheduleQueryRefresh();
               }
            }
         });
         scheduleQueryRefresh();
      }
   }

   private void scheduleIssueRefresh() {
      int delay = RedmineConfig.getInstance().getIssueRefreshInterval();
      Redmine.LOG.log(Level.FINE, "scheduling issue refresh for repository {0} in {1} minute(s)",
                      new Object[]{getDisplayName(), delay}); // NOI18N
      if (delay < 5 && System.getProperty("netbeans.t9y.redmine.force.refresh.delay") == null) { // t9y: Testability
         Redmine.LOG.log(Level.WARNING, " wrong issue refresh delay {0}. Falling back to default {0}",
                         new Object[]{delay, RedmineConfig.DEFAULT_ISSUE_REFRESH}); // NOI18N
         delay = RedmineConfig.DEFAULT_ISSUE_REFRESH;
      }
      refreshIssuesTask.schedule(delay * 60 * 1000); // given in minutes
   }

   private void scheduleQueryRefresh() {
      String schedule = System.getProperty("netbeans.t9y.redmine.force.refresh.schedule", "");
      if (!schedule.isEmpty()) {
         int delay = Integer.parseInt(schedule);
         refreshQueryTask.schedule(delay);
         return;
      }

      int delay = RedmineConfig.getInstance().getQueryRefreshInterval();
      Redmine.LOG.log(Level.FINE, "scheduling query refresh for repository {0} in {1} minute(s)",
                      new Object[]{getDisplayName(), delay}); // NOI18N
      if (delay < 5) {
         Redmine.LOG.log(Level.WARNING, " wrong query refresh delay {0}. Falling back to default {0}",
                         new Object[]{delay, RedmineConfig.DEFAULT_QUERY_REFRESH}); // NOI18N
         delay = RedmineConfig.DEFAULT_QUERY_REFRESH;
      }
      refreshQueryTask.schedule(delay * 60 * 1000); // given in minutes
   }

   public void stopRefreshing(String id) {
      Redmine.LOG.log(Level.FINE, "removing issue {0} from refresh on repository {1}",
                      new Object[]{id, getDisplayName()}); // NOI18N
      synchronized (issuesToRefresh) {
         issuesToRefresh.remove(id);
      }
   }

   public void scheduleForRefresh(String id) {
      Redmine.LOG.log(Level.FINE, "scheduling issue {0} for refresh on repository {0}",
                      new Object[]{id, getDisplayName()}); // NOI18N
      synchronized (issuesToRefresh) {
         issuesToRefresh.add(id);
      }
      setupIssueRefreshTask();
   }

   public void scheduleForRefresh(RedmineQuery query) {
      Redmine.LOG.log(Level.FINE, "scheduling query {0} for refresh on repository {1}",
                      new Object[]{query.getDisplayName(), getDisplayName()}); // NOI18N
      synchronized (queriesToRefresh) {
         queriesToRefresh.add(query);
      }
      setupQueryRefreshTask();
   }

   public void stopRefreshing(RedmineQuery query) {
      Redmine.LOG.log(Level.FINE, "removing query {0} from refresh on repository {1}",
                      new Object[]{query.getDisplayName(), getDisplayName()}); // NOI18N
      synchronized (queriesToRefresh) {
         queriesToRefresh.remove(query);
      }
   }

   public void refreshAllQueries() {
      refreshAllQueries(true);
   }

   protected void refreshAllQueries(final boolean onlyOpened) {
      EventQueue.invokeLater(new Runnable() {
         @Override
         public void run() {
            for (RedmineQuery q : getQueries()) {
               if (!onlyOpened || !Redmine.getInstance().getBugtrackingFactory().isOpen(
                       RedmineUtil.getRepository(RedmineRepository.this), q)) {
                  continue;
               }
               Redmine.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}",
                               new Object[]{q.getDisplayName(), getDisplayName()}); // NOI18N
               RedmineQueryController qc = q.getController();
               qc.onRefresh();
            }
         }
      });
   }

   @Override
   public String toString() {
      return getClass().getSimpleName()
              + "["
              + getDisplayName()
              + "]";
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }
      if (!(obj instanceof RedmineRepository)) {
         return false;
      }
      RedmineRepository other = (RedmineRepository)obj;
      return Is.equals(this.getDisplayName(), other.getDisplayName())
              && Is.equals(this.getUrl(), other.getUrl())
              && Is.equals(this.project, other.project);
   }

   @Override
   public int hashCode() {
      int hash = 3;
      hash = 97 * hash + (this.getDisplayName() != null ? this.getDisplayName().hashCode() : 0);
      hash = 97 * hash + (this.getUrl() != null ? this.getUrl().hashCode() : 0);
      hash = 97 * hash + (this.project != null ? this.project.hashCode() : 0);
      return hash;
   }

   private class RedmineIssueCache extends IssueCache<RedmineIssue, com.taskadapter.redmineapi.bean.Issue> {

      RedmineIssueCache() {
         super(RedmineRepository.this.getUrl() + RedmineRepository.this.getProject().getIdentifier(),
               new RedmineIssueAccessor(),
               Redmine.getInstance().getIssueProvider(),
               RedmineUtil.getRepository(RedmineRepository.this));
      }
   }

   private class RedmineIssueAccessor implements IssueCache.IssueAccessor<RedmineIssue, com.taskadapter.redmineapi.bean.Issue> {

      @Override
      public RedmineIssue createIssue(Issue issueData) {
         RedmineIssue redmineIssue = new RedmineIssue(RedmineRepository.this, issueData);
         RedmineTaskListProvider.getInstance().notifyIssueCreated(redmineIssue);
         return redmineIssue;
      }

      @Override
      public void setIssueData(RedmineIssue redmineIssue, com.taskadapter.redmineapi.bean.Issue issue) {
         assert redmineIssue != null && issue != null;
         redmineIssue.setIssue(issue);
      }

      @Override
      public String getRecentChanges(RedmineIssue redmineIssue) {
         assert redmineIssue != null;
         return redmineIssue.getRecentChanges();
      }

      @Override
      public long getLastModified(RedmineIssue redmineIssue) {
         assert redmineIssue != null;
         return redmineIssue.getLastModify();
      }

      @Override
      public long getCreated(RedmineIssue redmineIssue) {
         assert redmineIssue != null;
         return redmineIssue.getCreated();
      }

      @Override
      public Map<String, String> getAttributes(RedmineIssue redmineIssue) {
         assert redmineIssue != null;
         return redmineIssue.getAttributes();
      }

      @Override
      public String getID(com.taskadapter.redmineapi.bean.Issue issue) {
         assert issue != null;
         return String.valueOf(issue.getId());
      }
   }
}
