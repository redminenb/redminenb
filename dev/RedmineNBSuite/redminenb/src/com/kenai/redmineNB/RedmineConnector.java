/*
 * Copyright 2012 Mykolas and Anchialas.
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
package com.kenai.redmineNB;

import com.kenai.redmineNB.repository.RedmineRepository;
import java.awt.Image;
import java.util.Collection;
import javax.swing.JOptionPane;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
@NbBundle.Messages({
   "LBL_ConnectorName=Redmine",
   "LBL_ConnectorTooltip=NetBeans plugin for integration with Redmine"
})
@ServiceProvider(service = BugtrackingConnector.class, position = 1)
public class RedmineConnector extends BugtrackingConnector {

   private static final String ID = "com.kenai.redmineNB";


   @Override
   public String getID() {
      return ID;
   }


   @Override
   public Image getIcon() {
      return Redmine.getIconImage();
   }


   @Override
   public String getDisplayName() {
      return getConnectorName();
   }


   @Override
   public String getTooltip() {
      return Bundle.LBL_ConnectorTooltip();
   }


   public static String getConnectorName() {
      return Bundle.LBL_ConnectorName();
   }


   @Override
   public Repository createRepository() {
      return new RedmineRepository(true);
   }


   @Override
   public Repository[] getRepositories() {
      try {
         return Redmine.getInstance().getRepositories().toArray(new Repository[0]);
      } catch (RedmineException ex) {
         JOptionPane.showMessageDialog(null, ex.getLocalizedMessage());
      }

      return new Repository[0];
   }


   @Override
   public Lookup getLookup() {
      return Lookups.singleton(this);
   }


   @Override
   public void fireRepositoriesChanged(Collection<Repository> oldRepositories,
                                       Collection<Repository> newRepositories) {
      super.fireRepositoriesChanged(oldRepositories, newRepositories);
   }
}
