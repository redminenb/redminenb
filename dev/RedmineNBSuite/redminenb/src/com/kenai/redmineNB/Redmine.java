package com.kenai.redmineNB;

import com.kenai.redmineNB.issue.RedmineIssue;
import com.kenai.redmineNB.issue.RedmineIssueProvider;
import com.kenai.redmineNB.query.RedmineQuery;
import com.kenai.redmineNB.query.RedmineQueryProvider;
import com.kenai.redmineNB.repository.RedmineRepository;
import com.kenai.redmineNB.repository.RedmineRepositoryProvider;

import java.awt.Image;
import java.util.logging.Logger;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.bugtracking.spi.BugtrackingFactory;
import org.openide.util.*;

/**
 * RedmineNB integration base class.
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
public final class Redmine {

   public static final Logger LOG = Logger.getLogger(Redmine.class.getName());
   public static final String IMAGE_PATH = "com/kenai/redmineNB/resources/";
   public static final String ICON_IMAGE = "redmine.png";
   //
//   private Set<RedmineRepository> repositories;
   private RedmineConnector connector;
   private RequestProcessor rp;
   //
   private BugtrackingFactory<RedmineRepository, RedmineQuery, RedmineIssue> bf;
   private RedmineIssueProvider rip;
   private RedmineQueryProvider rqp;
   private RedmineRepositoryProvider rrp;
   private IssueNode.ChangesProvider<RedmineIssue> rcp;

   @SuppressWarnings("unchecked")
   private Redmine() {
      // omitted
   }

   private static class Holder {

      private static final Redmine SINGLETON = new Redmine();
   }

   public static synchronized Redmine getInstance() {
      return Holder.SINGLETON;
   }

   public static Image getIconImage() {
      return getImage(ICON_IMAGE);
   }

   public static Image getImage(String name) {
      return ImageUtilities.loadImage(IMAGE_PATH + name);
   }

   public static String getMessage(String resName, String... param) {
      return NbBundle.getMessage(Redmine.class, resName, param);
   }

   public RedmineConnector getConnector() {
      if (connector == null) {
         connector = Lookup.getDefault().lookup(RedmineConnector.class);
      }
      return connector;
   }

   /**
    * Returns the request processor for common tasks in Redmine. Do not use this
    * when accesing a remote repository.
    *
    * @return the RequestProcessor
    */
   public final RequestProcessor getRequestProcessor() {
      if (rp == null) {
         rp = new RequestProcessor("Redmine", 1, true); // NOI18N
      }
      return rp;
   }

   public RedmineIssueProvider getIssueProvider() {
      if (rip == null) {
         rip = new RedmineIssueProvider();
      }
      return rip;
   }

   public RedmineQueryProvider getQueryProvider() {
      if (rqp == null) {
         rqp = new RedmineQueryProvider();
      }
      return rqp;
   }

   public RedmineRepositoryProvider getRepositoryProvider() {
      if (rrp == null) {
         rrp = new RedmineRepositoryProvider();
      }
      return rrp;
   }

   public IssueNode.ChangesProvider<RedmineIssue> getChangesProvider() {
        if (rcp == null) {
            rcp = new IssueNode.ChangesProvider<RedmineIssue>() {
                @Override
                public String getRecentChanges(RedmineIssue i) {
                    return i.getRecentChanges();
                }
            };
        }
        return rcp;
    }
   
   public BugtrackingFactory<RedmineRepository, RedmineQuery, RedmineIssue> getBugtrackingFactory() {
      if (bf == null) {
         bf = new BugtrackingFactory<RedmineRepository, RedmineQuery, RedmineIssue>();
      }
      return bf;
   }
}
