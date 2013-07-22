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
package org.anchialas.nb.issuetracking;

import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.lang.StringUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.DelegatingConnector;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.util.RequestProcessor;

/**
 * Manages Issue Tracker to Project relationship.
 *
 * @author Anchialas <anchialas@gmail.com>
 */
public final class IssueTrackingManager {

   private IssueTrackingManager() {
      // omitted
   }

   @CheckForNull
   public static IssueTrackerData getData(@NonNull Project proj) {
      //AntProjectHelper helper = AntBasedProjectFactorySingleton.getHelperFor(proj);
      AntProjectHelper helper;
      if (proj instanceof NbModuleProject) {
         NbModuleProject nbModuleProject = (NbModuleProject)proj;
         helper = nbModuleProject.getHelper();
      } else {
         helper = null; // TODO
      }
      if (helper != null) {
         return IssueTrackerData.create(helper);
      }
      return null;
   }

   // similar method is package private in BugtrackingManager
   /**
    * Matches connector by ID
    * (TODO: with special support for "fake JIRA" and JIRA implementation)
    */
   public static DelegatingConnector getConnector(String connectorId) {
      for (DelegatingConnector c : getConnectors()) {
         if (StringUtils.equals(connectorId, c.getID())) {
            return c;
         }
      }
      // no match, 2nd try
//      for (DelegatingConnector c : getConnectors()) {
//         if (StringUtils.endsWith(connectorId, connectorId))
//      }
      return null;
   }
   
   public static Collection<DelegatingConnector> getConnectors() {
      return Arrays.asList(BugtrackingManager.getInstance().getConnectors());
   }

   /**
    * Returns all Issue Tracking repositories
    *
    * @return Collection of repositories
    */
   public static Collection<RepositoryImpl> getRepositories() {
      return RepositoryRegistry.getInstance().getRepositories();
   }

   /**
    * Returns all repositories for the connector with the given ID
    *
    * @param connectorID
    * @return Collection of repositories
    */
   public static Collection<RepositoryImpl> getRepositories(String connectorID) {
      return RepositoryRegistry.getInstance().getRepositories(connectorID);
   }
   
   /**
    * Returns the repository by id for the given connector.
    * @param connectorId
    * @param repoId
    * @return the matching RepositoryImpl or null
    */
   public static RepositoryImpl getRepository(String connectorId, String repoId) {
      return RepositoryRegistry.getInstance().getRepository(connectorId, repoId);
   }
   /**
    * Returns the repository by displayName for the given connector.
    * @param connectorId
    * @param repoId
    * @return the matching RepositoryImpl or null
    */
   public static RepositoryImpl getRepositoryByDisplayName(String connectorId, String displayName) {
      for (RepositoryImpl repo : getRepositories(connectorId)) {
         if (StringUtils.equals(repo.getDisplayName(), displayName)) {
            return repo;
         }
      }
      return null;
   }
   
   public static RepositoryImpl getRepository(@NonNull IssueTrackerData data) {
      //return getRepository(data.getConnector(), data.getName()); wrong!
      DelegatingConnector connector = getConnector(data.getConnectorId());
      if (connector != null) {
         return getRepositoryByDisplayName(connector.getID(), data.getName());
      }
      return null;
   }
   

//   private static RequestProcessor RP;
   public static RequestProcessor getRequestProcessor() {
//      if (RP == null) {
//         RP = new RequestProcessor("Issue Tracking tasks", 1); //NOI18N
//      }
//      return RP;
      return BugtrackingManager.getInstance().getRequestProcessor();
   }
   /**
    * @return lazy-loaded singleton instance
    *         public static IssueTrackingManager getDefault() {
    *         return LazyHolder.INSTANCE;
    *         }
    *
    * private static class LazyHolder {
    *
    * private static final IssueTrackingManager INSTANCE = new IssueTrackingManager();
    * }
    */
}
