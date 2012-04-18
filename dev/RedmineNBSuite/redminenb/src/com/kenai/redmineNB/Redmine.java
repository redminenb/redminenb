package com.kenai.redmineNB;

import com.kenai.redmineNB.repository.RedmineRepository;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.openide.util.*;


/**
 *
 * @author Mykolas
 */
public class Redmine {

   public static final Logger LOG = Logger.getLogger(Redmine.class.getName());
   public static final String IMAGE_PATH = "com/kenai/redmineNB/resources/";
   public static final String ICON_IMAGE = "redmine.png";
   private static Redmine instance;
   //
   private Set<Repository> repositories;
   private RedmineConnector connector;
   private RequestProcessor rp;


   @SuppressWarnings("unchecked")
   public Redmine() throws RedmineException {
      try {
         repositories = (Set<Repository>) RedmineConfig.getInstance().getRepositories();
      } catch (BackingStoreException ex) {
         Redmine.LOG.log(Level.SEVERE, "Redmine preference error", ex);
         throw new RedmineException("Unable to get Redmine repositories");
      } catch (InstantiationException ex) {
         Redmine.LOG.log(Level.SEVERE, "Redmine preference error", ex);
         throw new RedmineException("Unable to get Redmine repositories");
      } catch (IllegalAccessException ex) {
         Redmine.LOG.log(Level.SEVERE, "Redmine preference error", ex);
         throw new RedmineException("Unable to get Redmine repositories");
      }
   }


   public void addRepository(RedmineRepository repository) throws RedmineException {
      try {
         if (isRepositoryNameExists(repository.getName())) {
            throw new RedmineException("Redmine repository already exists");
         }

         RedmineConfig.getInstance().putRepository(repository);

         Collection<Repository> oldRepos = Collections.unmodifiableCollection(
                 new LinkedList<Repository>(repositories));

         repositories.add(repository);
         getConnector().fireRepositoriesChanged(oldRepos, repositories);
      } catch (BackingStoreException ex) {
         Redmine.LOG.log(Level.SEVERE, "Redmine preference error", ex);
         throw new RedmineException("Unable to save Redmine repository");
      } catch (IllegalArgumentException ex) {
         Redmine.LOG.log(Level.SEVERE, "Redmine preference error", ex);
         throw new RedmineException("Unable to save Redmine repository");
      } catch (IllegalAccessException ex) {
         Redmine.LOG.log(Level.SEVERE, "Redmine preference error", ex);
         throw new RedmineException("Unable to save Redmine repository");
      }
   }


   public void removeRepository(RedmineRepository repository) throws RedmineException {
      try {
         Collection<Repository> oldRepos = Collections.unmodifiableCollection(
                 new ArrayList<Repository>(repositories));

         RedmineConfig.getInstance().removeRepository(repository);
         repositories.remove(repository);

         getConnector().fireRepositoriesChanged(oldRepos, Collections.unmodifiableCollection(repositories));

      } catch (BackingStoreException ex) {
         Redmine.LOG.log(Level.SEVERE, "Redmine preference error", ex);
         throw new RedmineException("Unable to remove Redmine repository");
      }
   }


   public void updateRepository(RedmineRepository repository) throws RedmineException {
      try {
         if (!isRepositoryExists(repository)) {
            throw new RedmineException("Redmine repository does not exist, so can not be updated");
         }

         RedmineConfig.getInstance().putRepository(repository);

         //getConnector().fireRepositoriesChanged(null, null);

      } catch (BackingStoreException ex) {
         Redmine.LOG.log(Level.SEVERE, "Redmine preference error", ex);
         throw new RedmineException("Unable to save Redmine repository");
      } catch (Exception ex) {
         Redmine.LOG.log(Level.SEVERE, "Redmine preference error", ex);
         throw new RedmineException("Unable to save Redmine repository");
      }
   }


   public Collection<Repository> getRepositories() throws RedmineException {
      return repositories;
   }


   public boolean isRepositoryNameExists(String name) {
      for (Repository repository : repositories) {
         if (((RedmineRepository) repository).getName().equals(name)) {
            return true;
         }
      }
      return false;
   }


   public boolean isRepositoryExists(RedmineRepository repository) {
      for (Repository confRepository : repositories) {
         if (confRepository.equals(repository)) {
            return true;
         }
      }
      return false;
   }


   public RedmineRepository repositoryExists(RedmineRepository repository) {
      for (Repository confRepository : repositories) {
         if (confRepository.equals(repository)) {
            return (RedmineRepository) confRepository;
         }
      }
      return null;
   }


   public static synchronized Redmine getInstance() throws RedmineException {
      if (instance == null) {
         instance = new Redmine();
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

}
