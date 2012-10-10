package com.kenai.redmineNB;

import com.kenai.redmineNB.issue.RedmineIssue;
import com.kenai.redmineNB.issue.RedmineIssueProvider;
import com.kenai.redmineNB.query.RedmineQuery;
import com.kenai.redmineNB.query.RedmineQueryProvider;
import com.kenai.redmineNB.repository.RedmineRepository;
import com.kenai.redmineNB.repository.RedmineRepositoryController;
import com.kenai.redmineNB.repository.RedmineRepositoryProvider;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import org.netbeans.modules.bugtracking.api.Repository;
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
   private static Redmine instance;
   //
   private Set<RedmineRepository> repositories;
   private RedmineConnector connector;
   private RequestProcessor rp;
   //
   private RedmineIssueProvider rip;
   private RedmineQueryProvider rqp;
   private RedmineRepositoryProvider rrp;
   private BugtrackingFactory<RedmineRepository, RedmineQuery, RedmineIssue> bf;

   @SuppressWarnings("unchecked")
   private Redmine() throws RedmineException {
//      try {
//         repositories = RedmineConfig.getInstance().getRepositories();
//      } catch (BackingStoreException ex) {
//         LOG.log(Level.SEVERE, "Redmine preference error", ex);
//         throw new RedmineException("Unable to get Redmine repositories");
//      } catch (InstantiationException ex) {
//         LOG.log(Level.SEVERE, "Redmine preference error", ex);
//         throw new RedmineException("Unable to get Redmine repositories");
//      } catch (IllegalAccessException ex) {
//         LOG.log(Level.SEVERE, "Redmine preference error", ex);
//         throw new RedmineException("Unable to get Redmine repositories");
//      }
   }

//   public void addRepository(RedmineRepository repository) throws RedmineException {
//      try {
//         if (isRepositoryNameExists(repository.getDisplayName())) {
//            throw new RedmineException("Redmine repository already exists");
//         }
//
//         RedmineConfig.getInstance().putRepository(repository);
//
//         Collection<RedmineRepository> oldRepos = Collections.unmodifiableCollection(
//                 new LinkedList<RedmineRepository>(repositories));
//
//         repositories.add(repository);
////         getConnector().fireRepositoriesChanged(oldRepos, repositories);
//         
//      } catch (BackingStoreException ex) {
//         LOG.log(Level.SEVERE, "Redmine preference error", ex);
//         throw new RedmineException("Unable to save Redmine repository");
//      } catch (IllegalArgumentException ex) {
//         LOG.log(Level.SEVERE, "Redmine preference error", ex);
//         throw new RedmineException("Unable to save Redmine repository");
//      } catch (IllegalAccessException ex) {
//         LOG.log(Level.SEVERE, "Redmine preference error", ex);
//         throw new RedmineException("Unable to save Redmine repository");
//      }
//   }
//
//   public void removeRepository(RedmineRepository repository) throws RedmineException {
//      try {
//         Collection<RedmineRepository> oldRepos = Collections.unmodifiableCollection(
//                 new ArrayList<RedmineRepository>(repositories));
//
//         RedmineConfig.getInstance().removeRepository(repository);
//         repositories.remove(repository);
//
////         getConnector().fireRepositoriesChanged(oldRepos, Collections.unmodifiableCollection(repositories));
//
//      } catch (BackingStoreException ex) {
//         LOG.log(Level.SEVERE, "Redmine preference error", ex);
//         throw new RedmineException("Unable to remove Redmine repository");
//      }
//   }
//
//   public void updateRepository(RedmineRepository repository) throws RedmineException {
//      try {
//         if (!isRepositoryExists(repository)) {
//            throw new RedmineException("Redmine repository does not exist, so can not be updated");
//         }
//
//         RedmineConfig.getInstance().putRepository(repository);
//
//         //getConnector().fireRepositoriesChanged(null, null);
//
//      } catch (BackingStoreException ex) {
//         LOG.log(Level.SEVERE, "Redmine preference error", ex);
//         throw new RedmineException("Unable to save Redmine repository");
//      } catch (Exception ex) {
//         LOG.log(Level.SEVERE, "Redmine preference error", ex);
//         throw new RedmineException("Unable to save Redmine repository");
//      }
//   }
//
//   public Collection<RedmineRepository> getRepositories() throws RedmineException {
//      return repositories;
//   }
//
//   public boolean isRepositoryNameExists(String name) {
//      for (RedmineRepository repository : repositories) {
//         if (repository.getDisplayName().equals(name)) {
//            return true;
//         }
//      }
//      return false;
//   }
//
//   public boolean isRepositoryExists(RedmineRepository repository) {
//      for (RedmineRepository confRepository : repositories) {
//         if (confRepository.equals(repository)) {
//            return true;
//         }
//      }
//      return false;
//   }
//
//   public RedmineRepository repositoryExists(RedmineRepository repository) {
//      for (RedmineRepository confRepository : repositories) {
//         if (confRepository.equals(repository)) {
//            return confRepository;
//         }
//      }
//      return null;
//   }

   public static synchronized Redmine getInstance() {
      if (instance == null) {
         try {
            instance = new Redmine();
         } catch (RedmineException ex) {
            Exceptions.printStackTrace(ex);
         }
      }
      return instance;
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
        if(rqp == null) {
            rqp = new RedmineQueryProvider();
        }
        return rqp; 
    }
    public RedmineRepositoryProvider getRepositoryProvider() {
        if(rrp == null) {
            rrp = new RedmineRepositoryProvider();
        }
        return rrp; 
    }

   public BugtrackingFactory<RedmineRepository, RedmineQuery, RedmineIssue> getBugtrackingFactory() {
      if (bf == null) {
         bf = new BugtrackingFactory<RedmineRepository, RedmineQuery, RedmineIssue>();
      }
      return bf;
   }

}
