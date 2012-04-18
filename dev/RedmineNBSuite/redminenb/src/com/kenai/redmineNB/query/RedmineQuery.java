package com.kenai.redmineNB.query;

import com.kenai.redmineNB.Redmine;
import com.kenai.redmineNB.RedmineConnector;
import com.kenai.redmineNB.issue.RedmineIssue;
import com.kenai.redmineNB.issue.RedmineIssueNode;
import com.kenai.redmineNB.repository.RedmineRepository;
import com.kenai.redmineNB.util.RedmineUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.apache.commons.lang.StringUtils;
import org.netbeans.modules.bugtracking.issuetable.ColumnDescriptor;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.nodes.Node.Property;
import org.openide.util.Exceptions;
import org.redmine.ta.AuthenticationException;
import org.redmine.ta.NotFoundException;
import org.redmine.ta.RedmineException;


/**
 *
 * @author Mykolas
 */
public class RedmineQuery extends Query {

   private String name;
   private final RedmineRepository repository;
   private final Set<String> issues;
   //
   //private String urlParameters;
   private boolean initialUrlDef;
   private boolean firstRun = true;
   //
   private RedmineQueryController queryController;
   private ColumnDescriptor[] columnDescriptors;


   public RedmineQuery(RedmineRepository repository) {
      this(null, repository, false, false, true);
   }


   public RedmineQuery(String name, RedmineRepository repository, //String urlParameters,
                       boolean saved, boolean urlDef, boolean initControler) {
      this.name = name;
      this.repository = repository;
      this.saved = saved;
      //this.urlParameters = urlParameters;
      this.initialUrlDef = urlDef;
      this.issues = new HashSet<String>();
   }


   @Override
   public String getDisplayName() {
      return name;
   }


   @Override
   public String getTooltip() {
      return name + " - " + repository.getDisplayName(); // NOI18N
   }


   @Override
   public RedmineQueryController getController() {
      if (queryController == null) {
         queryController = new RedmineQueryController(repository, this);
      }
      return queryController;
   }


   @Override
   public RedmineRepository getRepository() {
      return repository;
   }


//    public ColumnDescriptor[] getColumnDescriptors() {
//        return NbJiraIssue.getColumnDescriptors(repository);
//    }

   public ColumnDescriptor[] getColumnDescriptors() {
      if (columnDescriptors == null) {
         Property<?>[] props = new RedmineIssueNode(new RedmineIssue(repository)).getProperties();
         columnDescriptors = new ColumnDescriptor[props.length];
         for (int i = 0; i < props.length; i++) {
            Property<?> p = props[i];
            columnDescriptors[i] = RedmineUtil.convertNodePropertyToColumnDescriptor(p);
         }
      }
      return columnDescriptors;
   }

//
//   public String getUrlParameters() {
//      return urlParameters;
//   }
//

   public void setName(String name) {
      this.name = name;
   }


   @Override
   public void setSaved(boolean saved) {
      super.setSaved(saved);
   }


   protected void logQueryEvent(int count, boolean autoRefresh) {
      BugtrackingUtil.logQueryEvent(
              RedmineConnector.getConnectorName(),
              name,
              count,
              false,
              autoRefresh);
   }


   void refresh(boolean autoReresh) {
      doRefresh(autoReresh);
   }


   public void refresh() { // XXX what if already running! - cancel task
      doRefresh(false);
   }


   private boolean doRefresh(final boolean autoRefresh) {
      // XXX what if already running! - cancel task
      //assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

      final boolean ret[] = new boolean[1];
      executeQuery(new Runnable() {

         @Override
         public void run() {
            Redmine.LOG.log(Level.FINE, "refresh start - {0}", name); // NOI18N
            try {

               // keeps all issues we will retrieve from the server
               // - those matching the query criteria
               // - and the obsolete ones
               Set<String> queryIssues = new HashSet<String>();

               issues.clear();
//                    archivedIssues.clear();
               if (isSaved()) {
                  // read the stored state ...
                  queryIssues.addAll(repository.getIssueCache().readQueryIssues(getStoredQueryName()));
                  queryIssues.addAll(repository.getIssueCache().readArchivedQueryIssues(getStoredQueryName()));
                  // ... and they might be rendered obsolete if not returned by the query
//                        archivedIssues.addAll(queryIssues);
               }
               firstRun = false;
               try {
                  List<org.redmine.ta.beans.Issue> issueArr = doSearch(queryController.getSearchParameterMap());
                  for (org.redmine.ta.beans.Issue issue : issueArr) {
                     getController().addProgressUnit(RedmineIssue.getDisplayName(issue));
                     try {
                        RedmineIssue redmineIssue = (RedmineIssue) repository.getIssueCache().setIssueData(
                                String.valueOf(issue.getId()), issue);
                        issues.add(redmineIssue.getID());

                        fireNotifyData(redmineIssue); // XXX - !!! triggers getIssues()

                     } catch (IOException ex) {
                        Redmine.LOG.log(Level.SEVERE, null, ex);
                        return;
                     }
                  }

               } catch (Exception e) {
                  Exceptions.printStackTrace(e);
               }

               // only issues not returned by the query are obsolete
//                    archivedIssues.removeAll(issues);
               if (isSaved()) {
                  // ... and store all issues you got
                  repository.getIssueCache().storeQueryIssues(getStoredQueryName(), issues.toArray(new String[issues.size()]));
//                        repository.getIssueCache().storeArchivedQueryIssues(getStoredQueryName(), archivedIssues.toArray(new String[0]));
               }

               // now get the task data for
               // - all issue returned by the query
               // - and issues which were returned by some previous run and are archived now
               queryIssues.addAll(issues);

               getController().switchToDeterminateProgress(queryIssues.size());

            } finally {
               logQueryEvent(issues.size(), autoRefresh);
               Redmine.LOG.log(Level.FINE, "refresh finish - {0}", name); // NOI18N
            }
         }
      });

      return ret[0];
   }


   /**
    * Performs the issue search with the attributes and values provided by the
    * map.
    *
    * Note: The Redmine REST API does not support full search support for all
    * fields. So the issues are post filtered here.
    *
    * @see http://www.redmine.org/projects/redmine/wiki/Rest_Issues
    * @param searchParameterMap
    */
   private List<org.redmine.ta.beans.Issue> doSearch(Map<String, String> searchParameterMap) throws IOException, AuthenticationException, NotFoundException, RedmineException {
      // see RedmineQueryController constructor
      boolean isSubject = "1".equals(searchParameterMap.remove("is_subject"));
      boolean isDescription = "1".equals(searchParameterMap.remove("is_description"));
      boolean isComments = "1".equals(searchParameterMap.remove("is_comments"));
      String queryStr = searchParameterMap.remove("query");

      // Perform search
      List<org.redmine.ta.beans.Issue> issueArr = repository.getManager().getIssues(searchParameterMap);

      // Post filtering
      if (StringUtils.isNotBlank(queryStr) && (isSubject || isDescription || isComments)) {
         List<org.redmine.ta.beans.Issue> newArr = new ArrayList<org.redmine.ta.beans.Issue>();
         for (org.redmine.ta.beans.Issue issue : issueArr) {
            if ((isSubject && StringUtils.containsIgnoreCase(issue.getSubject(), queryStr))
                    || (isDescription && StringUtils.containsIgnoreCase(issue.getDescription(), queryStr))
                    /* || (isComments && StringUtils.containsIgnoreCase(..., queryStr))*/ ) {
               newArr.add(issue);
            }
         }
         issueArr = newArr;
      }
      return issueArr;
   }


   public String getStoredQueryName() {
      return getDisplayName();
   }


   void remove() {
      repository.removeQuery(this);
      fireQueryRemoved();
   }


   @Override
   public void fireQuerySaved() {
      super.fireQuerySaved();
      repository.fireQueryListChanged();
   }


   @Override
   public void fireQueryRemoved() {
      super.fireQueryRemoved();
      repository.fireQueryListChanged();
   }


   boolean wasRun() {
      return !firstRun;
   }


   @Override
   public Issue[] getIssues(int includeStatus) {
      if (issues == null) {
         return new Issue[0];
      }
      List<String> ids = new ArrayList<String>();
      synchronized (issues) {
         ids.addAll(issues);
      }

      IssueCache cache = repository.getIssueCache();
      List<Issue> ret = new ArrayList<Issue>();
      for (String id : ids) {
         int status = getIssueStatus(id);
         if ((status & includeStatus) != 0) {
            ret.add(cache.getIssue(id));
         }
      }
      return ret.toArray(new Issue[0]);
   }


   @Override
   public boolean contains(Issue issue) {
      return issues.contains(issue.getID());
   }


   @Override
   public int getIssueStatus(Issue issue) {
      return getIssueStatus(issue.getID());
   }


   public int getIssueStatus(String id) {
      return repository.getIssueCache().getStatus(id);
   }
}
