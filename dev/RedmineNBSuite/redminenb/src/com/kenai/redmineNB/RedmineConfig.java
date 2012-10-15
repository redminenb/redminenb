package com.kenai.redmineNB;

import com.kenai.redmineNB.query.RedmineQuery;
import com.kenai.redmineNB.repository.RedmineRepository;
import com.kenai.redmineNB.ui.Defaults;
import com.kenai.redmineNB.util.RedminePreferences;

import com.kenai.redminenb.api.IssuePriority;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import org.apache.commons.io.FileUtils;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;


/**
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
public class RedmineConfig {

   private static final String REPO_ID = "redmine.repository";                      // NOI18N
   private static final String QUERY_NAME = "redmine.query_";                       // NOI18N
   private static final String QUERY_REFRESH_INT = "redmine.query_refresh";         // NOI18N
   private static final String QUERY_AUTO_REFRESH = "redmine.query_auto_refresh_";  // NOI18N
   private static final String ISSUE_REFRESH_INT = "redmine.issue_refresh";         // NOI18N
   private static final String DELIMITER = "<=>";                                   // NOI18N
   private static final String CHECK_UPDATES = "redmine.check_updates";             // NOI18N
   private static final String LAST_CHANGE_FROM = "redmine.last_change_from";       // NOI18N
   private static final String ACTIONITEMISSUES_STORAGE_FILE = "actionitemissues.data"; //NOI18N
   //
   public static final int DEFAULT_QUERY_REFRESH = 30;
   public static final int DEFAULT_ISSUE_REFRESH = 15;
   //
   private Map<String, Icon> priorityIcons;

   public static RedmineConfig getInstance() {
      return LazyHolder.INSTANCE;
   }

   private RedmineConfig() {
      // suppressed for non-instantiability
   }

   private Preferences getPreferences() {
      return NbPreferences.forModule(RedmineConfig.class);
   }

   public void setQueryRefreshInterval(int i) {
      getPreferences().putInt(QUERY_REFRESH_INT, i);
   }

   public void setIssueRefreshInterval(int i) {
      getPreferences().putInt(ISSUE_REFRESH_INT, i);
   }

   public void setCheckUpdates(boolean bl) {
      getPreferences().putBoolean(CHECK_UPDATES, bl);
   }

   public int getIssueRefreshInterval() {
      return getPreferences().getInt(ISSUE_REFRESH_INT, DEFAULT_ISSUE_REFRESH);
   }

   public void setQueryAutoRefresh(String queryName, boolean refresh) {
      getPreferences().putBoolean(QUERY_AUTO_REFRESH + queryName, refresh);
   }

   public int getQueryRefreshInterval() {
      return getPreferences().getInt(QUERY_REFRESH_INT, DEFAULT_QUERY_REFRESH);
   }

   public boolean getQueryAutoRefresh(String queryName) {
      return getPreferences().getBoolean(QUERY_AUTO_REFRESH + queryName, false);
   }

   public void putQuery(RedmineRepository repository, RedmineQuery query) {
      getPreferences().put(getQueryKey(repository.getID(), query.getDisplayName()),
                           query.getUrlParameters());
   }

   public void removeQuery(RedmineRepository repository, RedmineQuery query) {
      getPreferences().remove(getQueryKey(repository.getID(), query.getDisplayName()));
   }

   public String[] getQueries(String repoID) {
      return getKeysWithPrefix(QUERY_NAME + repoID + DELIMITER);
   }

   private String getQueryKey(String repositoryID, String queryName) {
      return QUERY_NAME + repositoryID + DELIMITER + queryName;
   }

   private String getStoredQuery(RedmineRepository repository, String queryName) {
      return getPreferences().get(getQueryKey(repository.getID(), queryName), null);
   }

   public RedmineQuery getQuery(RedmineRepository repository, String queryName) {
      String value = getStoredQuery(repository, queryName);
      if (value == null) {
         return null;
      }
      String[] values = value.split(DELIMITER);
      assert values.length >= 2;
      String urlParams = values[0];
      boolean urlDef = values.length > 2 ? Boolean.parseBoolean(values[2]) : false;
      return new RedmineQuery(queryName, repository, urlParams, true, urlDef, true);
   }

   public boolean getCheckUpdates() {
      return getPreferences().getBoolean(CHECK_UPDATES, true);
   }

//   public void putRepository(RedmineRepository repository) throws IllegalArgumentException,
//                                                                  IllegalAccessException,
//                                                                  BackingStoreException {
//      String baseKey = getRepositoryKey(repository);
//      RedminePreferences.putObject(baseKey, repository);
//      RedminePreferences.getPreferences().put(baseKey + ".authMode", repository.getAuthMode().name());
//      RedminePreferences.getPreferences().put(baseKey + ".project.id", repository.getProject().getIdentifier());
//
//      BugtrackingUtil.savePassword(String.valueOf(repository.getPassword()),
//                                   null,
//                                   repository.getUsername(),
//                                   repository.getUrl());
//   }
//
//   @Messages("MSG_HostNotReachable=Redmine host ''{0}'' is not reachable!")
//   public RedmineRepository getRepository(String id) throws InstantiationException,
//                                                            IllegalAccessException,
//                                                            BackingStoreException {
//      String baseKey = getRepositoryKey(id);
//      RedmineRepository repo = RedminePreferences.getObject(baseKey, RedmineRepository.class);
////      repo.setPassword(BugtrackingUtil.readPassword("", null, repo.getUsername(), repo.getUrl()));
////      repo.setAuthMode(AuthMode.valueOf(RedminePreferences.getPreferences().get(baseKey + ".authMode", AuthMode.AccessKey.name())));
//      String projectId = RedminePreferences.getPreferences().get(baseKey + ".project.id", null);
//      try {
//         URL url = new URL(repo.getUrl());
//         if (repo.isReachable()) {
//            repo.setProject(repo.getManager().getProjectByKey(projectId));
//         } else {
//            NotificationDisplayer.getDefault().notify("Redmine: " + repo.getDisplayName(),
//                                                      Defaults.getIcon("error.png"),
//                                                      Bundle.MSG_HostNotReachable(url.getHost()), null);
//         }
//      } catch (Exception ex) {
//         Exceptions.printStackTrace(ex);
//      }
//      return repo;
//   }

   public void removeRepository(RedmineRepository repository) throws BackingStoreException {
      RedminePreferences.removeObject(getRepositoryKey(repository));
   }

   private String[] getKeysWithPrefix(String prefix) {
      String[] keys = null;
      try {
         keys = getPreferences().keys();
      } catch (BackingStoreException ex) {
         Redmine.LOG.log(Level.SEVERE, null, ex); // XXX
      }
      if (keys == null || keys.length == 0) {
         return new String[0];
      }
      List<String> ret = new ArrayList<String>();
      for (String key : keys) {
         if (key.startsWith(prefix)) {
            ret.add(key.substring(prefix.length()));
         }
      }
      return ret.toArray(keys);
   }

//   public Set<RedmineRepository> getRepositories() throws BackingStoreException,
//                                                          InstantiationException,
//                                                          IllegalAccessException {
//      Set<RedmineRepository> repositories = new HashSet<RedmineRepository>();
//      Set<String> repositorySet = new HashSet<String>();
//
//      for (String string : RedminePreferences.getPreferences().keys()) {
//         if (string.startsWith(REPO_ID)) {
//            String id = getRepositoryId(string);
//
//            if (!repositorySet.contains(id)) {
//               repositories.add(getRepository(id));
//               repositorySet.add(id);
//            }
//         }
//      }
//
//      return repositories;
//   }

   private String getRepositoryId(String key) {
      return key.substring(REPO_ID.length() + 1, key.indexOf('.', REPO_ID.length() + 1));
   }

   private String getRepositoryKey(RedmineRepository repository) {
      return getRepositoryKey(repository.getID());
   }

   private String getRepositoryKey(String id) {
      return REPO_ID + "." + id;
   }

   public void setLastChangeFrom(String value) {
      getPreferences().put(LAST_CHANGE_FROM, value);
   }

   public String getLastChangeFrom() {
      return getPreferences().get(LAST_CHANGE_FROM, "");                      // NOI18N
   }

   public Icon getPriorityIcon(String priorityName) {
      if (priorityIcons == null) {
         priorityIcons = new HashMap<String, Icon>();
         priorityIcons.put("Immediate", Defaults.getIcon("blocker.png")); // NOI18N
         priorityIcons.put("Urgent", Defaults.getIcon("critical.png")); // NOI18N
         priorityIcons.put("High", Defaults.getIcon("major.png")); // NOI18N
         priorityIcons.put("Normal", Defaults.getIcon("arrow_right.png")); // NOI18N
         priorityIcons.put("Low", Defaults.getIcon("minor.png")); // NOI18N
      }
      return priorityIcons.get(priorityName);
   }

   /**
    * Saves issue ActionItem's permanently.
    *
    * @param issues
    */
   public void setActionItemIssues(HashMap<String, List<String>> issues) {
      Redmine.LOG.fine("setActionItemIssues: saving issues");              //NOI18N
      File f = new File(getConfigPath());
      f.mkdirs();
      if (!f.canWrite()) {
         Redmine.LOG.warning("setActionItemIssues: Cannot create perm storage"); //NOI18N
         return;
      }
      java.io.ObjectOutputStream out = null;
      File file = new File(f, ACTIONITEMISSUES_STORAGE_FILE + ".tmp");
      boolean success = false;
      try {
         // saving to a temp file
         out = new java.io.ObjectOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(file)));
         out.writeInt(issues.size());
         for (Map.Entry<String, List<String>> entry : issues.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeInt(entry.getValue().size());
            for (String issueAttributes : entry.getValue()) {
               out.writeUTF(issueAttributes);
            }
         }
         success = true;
      } catch (IOException ex) {
         Redmine.LOG.log(Level.FINE, null, ex);
      } finally {
         if (out != null) {
            try {
               out.close();
            } catch (IOException e) {
            }
         }
      }
      if (success) {
         // rename the temp file to the permanent one
         File newFile = new File(f, ACTIONITEMISSUES_STORAGE_FILE);
         try {
            FileUtils.moveFile(file, newFile);
         } catch (IOException ex) {
            Redmine.LOG.log(Level.FINE, null, ex);
            success = false;
         }
      }
      if (!success) {
         Redmine.LOG.warning("setActionItemIssues: could not save issues"); //NOI18N
         if (!file.delete()) {
            file.deleteOnExit();
         }
      }
   }

   /**
    * Loads issues from a permanent storage
    *
    * @return
    */
   public Map<String, List<String>> getActionItemIssues() {
      Redmine.LOG.fine("loadActionItemIssues: loading issues");            //NOI18N
      File f = new File(getConfigPath());
      java.io.ObjectInputStream ois = null;
      File file = new File(f, ACTIONITEMISSUES_STORAGE_FILE);
      if (!file.canRead()) {
         Redmine.LOG.fine("loadActionItemIssues: no saved data");         //NOI18N
         return Collections.emptyMap();
      }
      try {
         ois = new java.io.ObjectInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(file)));
         int size = ois.readInt();
         Redmine.LOG.log(Level.FINE, "loadActionItemIssues: loading {0} records", size); //NOI18N
         HashMap<String, List<String>> issuesPerRepo = new HashMap<String, List<String>>(size);
         while (size-- > 0) {
            String repoUrl = ois.readUTF();
            Redmine.LOG.log(Level.FINE, "loadActionItemIssues: loading issues for {0}", repoUrl); //NOI18N
            int issueCount = ois.readInt();
            Redmine.LOG.log(Level.FINE, "loadActionItemIssues: loading {0} issues", issueCount); //NOI18N
            LinkedList<String> issues = new LinkedList<String>();
            while (issueCount-- > 0) {
               issues.add(ois.readUTF());
            }
            issuesPerRepo.put(repoUrl, issues);
         }
         return issuesPerRepo;
      } catch (IOException ex) {
         Redmine.LOG.log(Level.FINE, null, ex);
      } finally {
         if (ois != null) {
            try {
               ois.close();
            } catch (IOException e) {
            }
         }
      }
      return Collections.emptyMap();
   }

   /**
    * Returns the path for the Redmine configuration directory.
    *
    * @return the path
    */
   private static String getConfigPath() {
      //T9Y - nb redmine confing should be changable
      String t9yNbConfigPath = System.getProperty("netbeans.t9y.redmine.nb.config.path"); //NOI18N
      if (t9yNbConfigPath != null && t9yNbConfigPath.length() > 0) {
         return t9yNbConfigPath;
      }
      String nbHome = System.getProperty("netbeans.user");              //NOI18N
      return nbHome + "/config/issue-tracking/com-kenai-redmineNB";     //NOI18N
   }


   private static class LazyHolder {

      private static final RedmineConfig INSTANCE = new RedmineConfig();
   }

}
