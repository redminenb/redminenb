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
package com.kenai.redmineNB.util;

import com.kenai.redmineNB.Redmine;
import com.kenai.redmineNB.RedmineConnector;
import com.kenai.redmineNB.issue.RedmineIssue;
import com.kenai.redmineNB.query.RedmineQuery;
import com.kenai.redmineNB.repository.RedmineRepository;
import javax.swing.JButton;
import org.apache.commons.lang.StringUtils;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.issuetable.ColumnDescriptor;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * Redmine utility class.
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
public final class RedmineUtil {

   private RedmineUtil() {
      // default constructor suppressed for non-instantiability
   }

   public static <T> ColumnDescriptor<T> convertNodePropertyToColumnDescriptor(Node.Property<T> prop) {
      return new ColumnDescriptor<T>(prop.getName(),
                                     prop.getValueType(),
                                     prop.getDisplayName(),
                                     prop.getShortDescription());
   }

   /**
    * Helper method to convert the first letter of a string to uppercase. And
    * prefix the string with some next string.
    */
   public static String capitalize(String s) {
      return StringUtils.isBlank(s) ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
   }

   public static boolean show(ActionListenerPanel panel, String title, String okName) {
      JButton ok = new JButton(okName);
      JButton cancel = new JButton(NbBundle.getMessage(BugtrackingUtil.class, "LBL_Cancel")); // NOI18N
      final DialogDescriptor dd =
              new DialogDescriptor(
              panel,
              title,
              true,
              new Object[]{ok, cancel},
              ok,
              DialogDescriptor.DEFAULT_ALIGN,
              new HelpCtx(panel.getClass()),
              panel);

      ok.getAccessibleContext().setAccessibleDescription(ok.getText());
      cancel.getAccessibleContext().setAccessibleDescription(cancel.getText());

      panel.setOkButton(ok);
      panel.setCancelButton(cancel);
      panel.setDialogDescribtor(dd);

      dd.setClosingOptions(new Object[]{cancel});

      return DialogDisplayer.getDefault().notify(dd) == ok;
   }

   public static Repository getRepository(RedmineRepository redmineRepository) {
      Repository repository = Redmine.getInstance().getBugtrackingFactory().getRepository(
              RedmineConnector.ID, redmineRepository.getID());
      if (repository == null) {
         repository = Redmine.getInstance().getBugtrackingFactory().createRepository(
                 redmineRepository,
                 Redmine.getInstance().getRepositoryProvider(),
                 Redmine.getInstance().getQueryProvider(),
                 Redmine.getInstance().getIssueProvider());
      }
      return repository;
   }

   public static void openIssue(RedmineIssue redmineIssue) {
      Redmine.getInstance().getBugtrackingFactory().openIssue(
              getRepository(redmineIssue.getRepository()), redmineIssue);
   }

   public static void openQuery(RedmineQuery redmineQuery) {
      Redmine.getInstance().getBugtrackingFactory().openQuery(
              getRepository(redmineQuery.getRepository()), redmineQuery);
   }

}
