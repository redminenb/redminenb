package com.kenai.redmineNB.repository;

import com.kenai.redmineNB.Redmine;
import com.kenai.redmineNB.RedmineConfig;
import com.kenai.redmineNB.issue.RedmineIssue;
import com.kenai.redmineNB.query.ParameterValue;
import com.kenai.redmineNB.query.RedmineQuery;
import com.kenai.redmineNB.query.RedmineQueryController;
import com.kenai.redmineNB.user.RedmineUser;
import com.kenai.redmineNB.util.Is;
import com.kenai.redminenb.api.AuthMode;
import java.awt.EventQueue;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.*;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.redmine.ta.AuthenticationException;
import org.redmine.ta.NotFoundException;
import org.redmine.ta.RedmineException;
import org.redmine.ta.RedmineManager;
import org.redmine.ta.beans.*;


/**
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
public class RedmineRepository extends Repository {

   private static final String TOOLTIP = "Redmine repository: ";
   //
   private String name;
   private String id;
   private String url;
   private transient AuthMode authMode;
   private String accessKey;
   private String username;
   private transient char[] password;
   private transient BugtrackingController controller;
   private Collection<RedmineQuery> queries;
   // TODO Create manager wrapping class to handle Redmine related errors
   private transient RedmineManager manager;
   private transient Project project;
   private transient Lookup lookup;
   private final transient InstanceContent ic;
   private transient IssueCache<org.redmine.ta.beans.Issue> cache;
   private transient boolean fresh;
   //
   private final Set<String> issuesToRefresh = new HashSet<String>(5);
   private final Set<RedmineQuery> queriesToRefresh = new HashSet<RedmineQuery>(3);
   private RequestProcessor.Task refreshIssuesTask;
   private RequestProcessor.Task refreshQueryTask;
   private RequestProcessor refreshProcessor;


   /**
    * Default constructor required for deserializing.
    */
   public RedmineRepository() {
      this(false);
   }


   public RedmineRepository(boolean fresh) {
      this.id = String.valueOf(System.currentTimeMillis());
      this.ic = new InstanceContent();
      setFresh(fresh);
   }


   @Override
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


   public String getName() {
      return name;
   }


   @Override
   public String getDisplayName() {
      try {
         if (isReachable()) {
            return name;
         }
      } catch (IOException ex) {
      }
      return name + " (offline)";
   }


   @Override
   public String getTooltip() {
      return TOOLTIP + name;
   }


   @Override
   public String getID() {
      return id;
   }


   @Override
   public String getUrl() {
      return url;
   }


   @Override
   @Deprecated
   public RedmineIssue getIssue(String issueId) {
      try {
         org.redmine.ta.beans.Issue issue = getManager().getIssueById(Integer.valueOf(issueId));
         RedmineIssue redmineIssue = (RedmineIssue) getIssueCache().setIssueData(issueId, issue);
         //ensureConfigurationUptodate(issue);
         return redmineIssue;

      } catch (NotFoundException ex) {
         return null;
      } catch (Exception ex) {
         Redmine.LOG.log(Level.SEVERE, null, ex);
         return null;
      }
   }


   @Override
   public void remove() {
      try {
         Redmine.getInstance().removeRepository(this);
      } catch (com.kenai.redmineNB.RedmineException ex) {
         JOptionPane.showMessageDialog(null, ex.getLocalizedMessage());
      }
   }


   @Override
   public BugtrackingController getController() {
      if (controller == null) {
         controller = new RedmineRepositoryController(this);
      }
      return controller;
   }


   @Override
   public Issue createIssue() {
      return new RedmineIssue(this);
   }


   @Override
   public Query createQuery() {
      Query query = new RedmineQuery(this);
      return query;
   }


   public void removeQuery(RedmineQuery query) {
//      RedmineConfig.getInstance().removeQuery(this, query);
//      getIssueCache().removeQuery(query.getStoredQueryName());
//      doGetQueries().remove(query);
//      stopRefreshing(query);
   }


   public void saveQuery(RedmineQuery query) {
//      assert id != null;
//      RedmineConfig.getInstance().putQuery(this, query);
//      doGetQueries().add(query);
   }


   private Collection<RedmineQuery> doGetQueries() {
      if (queries == null) {
         queries = new HashSet<RedmineQuery>(10);
//         String[] qs = RedmineConfig.getInstance().getQueries(id);
//         for (String queryName : qs) {
//            RedmineQuery q = RedmineConfig.getInstance().getQuery(this, queryName);
//            if (q != null) {
//               queries.add(q);
//            } else {
//               Redmine.LOG.log(Level.WARNING, "Couldn''t find query with stored name {0}", queryName); // NOI18N
//            }
//         }
      }
      return queries;
   }


   @Override
   public Query[] getQueries() {
      return doGetQueries().toArray(new Query[0]);
   }


   @Override
   public Collection<RepositoryUser> getUsers() {
      try {
         return RedmineUser.getUsers(getManager().getUsers());
      } catch (IOException ex) {
         // TODO Notify user that it is not possible to connect to Redmine
         Redmine.LOG.log(Level.SEVERE, "Can't get Redmine Users", ex);
      } catch (AuthenticationException ex) {
         // TODO Notify user that it is not possible to login to Redmine
         Redmine.LOG.log(Level.SEVERE, "Can't get Redmine Users", ex);
      } catch (NotFoundException ex) {
         // TODO Notify user that the issue no longer exists
         Redmine.LOG.log(Level.SEVERE, "Can't get Redmine Users", ex);
      } catch (RedmineException ex) {
         // TODO Notify user that Redmine internal error has happened
         Redmine.LOG.log(Level.SEVERE, "Can't get Redmine Users", ex);
      }
      return Collections.<RepositoryUser>emptyList();
   }


   public Collection<Tracker> getTrackers() {
      try {
         return getManager().getTrackers();
      } catch (IOException ex) {
         // TODO Notify user that it is not possible to connect to Redmine
         Redmine.LOG.log(Level.SEVERE, "Can't get Redmine Issue Trackers", ex);
      } catch (AuthenticationException ex) {
         // TODO Notify user that it is not possible to login to Redmine
         Redmine.LOG.log(Level.SEVERE, "Can't get Redmine Issue Trackers", ex);
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
      return c;
   }


   public Collection<? extends Version> reloadVersions() {
      for (Version v : getLookup().lookupAll(Version.class)) {
         ic.remove(v);
      }
      return getVersions();
   }


   public Collection<Version> getVersions() {
      try {
         return getManager().getVersions(project.getId());
      } catch (IOException ex) {
         // TODO Notify user that it is not possible to connect to Redmine
         Redmine.LOG.log(Level.SEVERE, "Can't get Versions for Redmine Project " + project.getName(), ex);
      } catch (AuthenticationException ex) {
         // TODO Notify user that it is not possible to login to Redmine
         Redmine.LOG.log(Level.SEVERE, "Can't get Versions for Redmine Project " + project.getName(), ex);
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


   public List<ParameterValue> getIssuePriorities() {
      // XXX not yet supported by redmine-java-api
      return Arrays.asList(
              new ParameterValue("Low", 3),
              new ParameterValue("Normal", 4),
              new ParameterValue("High", 5),
              new ParameterValue("Urgent", 6),
              new ParameterValue("Immediate", 7));
   }


   public IssueCache<org.redmine.ta.beans.Issue> getIssueCache() {
      if (cache == null) {
         cache = new Cache();
      }
      return cache;
   }


   @Override
   public Issue[] simpleSearch(String string) {
      try {
         List<org.redmine.ta.beans.Issue> issuesByID =
                 new LinkedList<org.redmine.ta.beans.Issue>();

         try {
            issuesByID.add(getManager().getIssueById(Integer.parseInt(string)));
         } catch (NumberFormatException ex) {
         } catch (NotFoundException ex) {
         }

         return RedmineIssue.getIssues(this, issuesByID, getManager().getIssuesBySummary(
                 project.getIdentifier(), "*" + string + "*"));
      } catch (IOException ex) {
         // TODO Notify user that it is not possible to connect to Redmine
         Redmine.LOG.log(Level.SEVERE, "Can't search for Redmine issues", ex);
      } catch (AuthenticationException ex) {
         // TODO Notify user that it is not possible to login to Redmine
         Redmine.LOG.log(Level.SEVERE, "Can't search for Redmine issues", ex);
      } catch (NotFoundException ex) {
         // TODO Notify user that the issue no longer exists
         Redmine.LOG.log(Level.SEVERE, "Can't search for Redmine issues", ex);
      } catch (RedmineException ex) {
         // TODO Notify user that Redmine internal error has happened
         Redmine.LOG.log(Level.SEVERE, "Can't search for Redmine issues", ex);
      }

      return new Issue[0];
   }


   @Override
   public Lookup getLookup() {
      if (lookup == null) {
         ic.add(getIssueCache());
         lookup = new AbstractLookup(ic);
         //lookup = Lookups.fixed(getIssueCache());
      }
      return lookup;
   }


   @Override
   public void fireQueryListChanged() {
      super.fireQueryListChanged();
   }


   public void setUrl(String url) {
      this.url = url;
   }


   public void setName(String name) {
      this.name = name;
   }


   public AuthMode getAuthMode() {
      return authMode;
   }


   public String getAccessKey() {
      return accessKey;
   }


   public void setAccessKey(String accessKey) {
      String old = this.accessKey;
      this.accessKey = accessKey;
      if (!Is.equals(old, accessKey)) {
         manager = null;
      }
   }


   public char[] getPassword() {
      return password;
   }


   public void setPassword(char[] password) {
      this.password = password;
   }


   public String getUsername() {
      return username;
   }


   public void setUsername(String username) {
      this.username = username;
   }


   public void setAuthMode(AuthMode authMode) {
      if (authMode == null) {
         throw new IllegalArgumentException("authMode cannot be null");
      }
      if (this.authMode != authMode) {
         this.authMode = authMode;
         manager = null;
      }
   }


   public RedmineManager getManager() {
      if (manager == null) {
         if (authMode == null) {
            throw new IllegalArgumentException("authMode must be set");
         }
         if (authMode == AuthMode.AccessKey) {
            manager = new RedmineManager(url, accessKey);
         } else {
            manager = new RedmineManager(url);
         }
         manager.setObjectsPerPage(100);
      }
      if (authMode == AuthMode.Credentials) {
         manager.setLogin(username);
         manager.setPassword(password == null ? "" : String.valueOf(password));
      }
      return manager;
   }


   public Project getProject() {
      return project;
   }


   public void setProject(Project project) {
      this.project = project;
   }


   public boolean isFresh() {
      return fresh;
   }


   public final void setFresh(boolean fresh) {
      this.fresh = fresh;
   }


   private RequestProcessor getRefreshProcessor() {
      if (refreshProcessor == null) {
         refreshProcessor = new RequestProcessor("Redmine refresh - " + name); // NOI18N
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
                  Redmine.LOG.log(Level.FINE, "no issues to refresh {0}", name); // NOI18N
                  return;
               }
               Redmine.LOG.log(Level.FINER, "preparing to refresh issue {0} - {1}", new Object[]{name, ids}); // NOI18N
//               GetMultiTaskDataCommand cmd = new GetMultiTaskDataCommand(BugzillaRepository.this, ids, new IssuesCollector());
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
                     Redmine.LOG.log(Level.FINE, "no queries to refresh {0}", new Object[]{name}); // NOI18N
                     return;
                  }
                  for (RedmineQuery q : queries) {
                     Redmine.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}", new Object[]{q.getDisplayName(), name}); // NOI18N
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
      Redmine.LOG.log(Level.FINE, "scheduling issue refresh for repository {0} in {1} minute(s)", new Object[]{name, delay}); // NOI18N
      if (delay < 5 && System.getProperty("netbeans.t9y.redmine.force.refresh.delay") == null) { // t9y: Testability
         Redmine.LOG.log(Level.WARNING, " wrong issue refresh delay {0}. Falling back to default {0}", new Object[]{delay, RedmineConfig.DEFAULT_ISSUE_REFRESH}); // NOI18N
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
      Redmine.LOG.log(Level.FINE, "scheduling query refresh for repository {0} in {1} minute(s)", new Object[]{name, delay}); // NOI18N
      if (delay < 5) {
         Redmine.LOG.log(Level.WARNING, " wrong query refresh delay {0}. Falling back to default {0}", new Object[]{delay, RedmineConfig.DEFAULT_QUERY_REFRESH}); // NOI18N
         delay = RedmineConfig.DEFAULT_QUERY_REFRESH;
      }
      refreshQueryTask.schedule(delay * 60 * 1000); // given in minutes
   }


   public void stopRefreshing(String id) {
      Redmine.LOG.log(Level.FINE, "removing issue {0} from refresh on repository {1}", new Object[]{id, name}); // NOI18N
      synchronized (issuesToRefresh) {
         issuesToRefresh.remove(id);
      }
   }


   public void scheduleForRefresh(RedmineQuery query) {
      Redmine.LOG.log(Level.FINE, "scheduling query {0} for refresh on repository {1}", new Object[]{query.getDisplayName(), name}); // NOI18N
      synchronized (queriesToRefresh) {
         queriesToRefresh.add(query);
      }
      setupQueryRefreshTask();
   }


   public void stopRefreshing(RedmineQuery query) {
      Redmine.LOG.log(Level.FINE, "removing query {0} from refresh on repository {1}", new Object[]{query.getDisplayName(), name}); // NOI18N
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
            Query[] qs = getQueries();
            for (Query q : qs) {
               if (!onlyOpened || !BugtrackingUtil.isOpened(q)) {
                  continue;
               }
               Redmine.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}", new Object[]{q.getDisplayName(), name}); // NOI18N
               RedmineQueryController qc = ((RedmineQuery) q).getController();
               qc.onRefresh();
            }
         }

      });
   }


   @Override
   public String toString() {
      return getClass().getSimpleName()
              + "["
              + name
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
      RedmineRepository other = (RedmineRepository) obj;
      return Is.equals(this.name, other.name)
              && Is.equals(this.url, other.url)
              && Is.equals(this.project, other.project);
   }


   @Override
   public int hashCode() {
      int hash = 3;
      hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
      hash = 97 * hash + (this.url != null ? this.url.hashCode() : 0);
      hash = 97 * hash + (this.project != null ? this.project.hashCode() : 0);
      return hash;
   }


   private class Cache extends IssueCache<org.redmine.ta.beans.Issue> {

      Cache() {
         super(RedmineRepository.this.getUrl() + RedmineRepository.this.getProject().
                 getIdentifier(), new RedmineIssueAccessor());
      }

   }


   private class RedmineIssueAccessor implements
           IssueCache.IssueAccessor<org.redmine.ta.beans.Issue> {

      @Override
      public Issue createIssue(org.redmine.ta.beans.Issue issue) {
         RedmineIssue redmineIssue = new RedmineIssue(RedmineRepository.this, issue);
         //org.netbeans.modules.bugzilla.issue.BugzillaIssueProvider.getInstance().notifyIssueCreated(issue);
         return redmineIssue;
      }


      @Override
      public void setIssueData(Issue redmineIssue, org.redmine.ta.beans.Issue issue) {
         assert redmineIssue != null && issue != null;
         ((RedmineIssue) redmineIssue).setIssue(issue);
      }


      @Override
      public String getRecentChanges(Issue issue) {
         assert issue != null;
         // TODO Implement recent changes methods
         return ""; //((RedmineIssue) issue).getRecentChanges();
      }


      @Override
      public long getLastModified(Issue issue) {
         assert issue != null;
         return ((RedmineIssue) issue).getLastModify();
      }


      @Override
      public long getCreated(Issue issue) {
         assert issue != null;
         return ((RedmineIssue) issue).getCreated();
      }


      @Override
      public String getID(org.redmine.ta.beans.Issue issue) {
         assert issue != null;
         return new RedmineIssue(RedmineRepository.this, issue).getID();
      }


      @Override
      public Map<String, String> getAttributes(Issue issue) {
         assert issue != null;
         // TODO Implement attributes method
         return new HashMap<String, String>(); //((RedmineIssue) issue).getAttributes();
      }

   }

}
