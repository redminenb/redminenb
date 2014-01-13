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
package com.kenai.redminenb;

import com.kenai.redminenb.repository.RedmineRepository;

import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.IssueFinder;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.util.SimpleIssueFinder;
import org.openide.util.NbBundle;

/**
 * RedmineNB {@link BugtrackingConnector connector to NetBeans Bugtracking SPI}.
 *
 * @author Anchialas <anchialas@gmail.com>
 */
@NbBundle.Messages({
   "LBL_ConnectorName=Redmine",
   "LBL_ConnectorTooltip=NetBeans plugin for integration with Redmine"
})
@BugtrackingConnector.Registration(id = RedmineConnector.ID,
                                   displayName = "#LBL_ConnectorName",
                                   tooltip = "#LBL_ConnectorTooltip",
                                   iconPath = "com/kenai/redminenb/resources/redmine.png")
public class RedmineConnector extends BugtrackingConnector {

   public static final String ID = "com.kenai.redminenb";

   public static String getConnectorName() {
      return Bundle.LBL_ConnectorName();
   }

   @Override
   public Repository createRepository(RepositoryInfo info) {
      return Redmine.getInstance().getBugtrackingFactory().createRepository(
              RedmineRepository.create(info),
              Redmine.getInstance().getRepositoryProvider(),
              Redmine.getInstance().getQueryProvider(),
              Redmine.getInstance().getIssueProvider());
   }

   @Override
   public Repository createRepository() {
      return createRepository(null);
   }

   @Override
   public IssueFinder getIssueFinder() {
      return SimpleIssueFinder.getInstance();
   }

}
