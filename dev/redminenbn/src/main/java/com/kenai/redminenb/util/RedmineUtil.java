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
package com.kenai.redminenb.util;

import com.kenai.redminenb.issue.RedmineIssue;
import com.kenai.redminenb.query.RedmineQuery;
import com.kenai.redminenb.repository.RedmineRepository;

import com.taskadapter.redmineapi.bean.Identifiable;
import com.taskadapter.redmineapi.bean.Project;
import java.util.Collection;
import java.util.Comparator;
import javax.swing.JButton;
import org.apache.commons.lang.StringUtils;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.issuetable.ColumnDescriptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

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

    public static <T> ColumnDescriptor<T> convertNodePropertyToColumnDescriptor(Node.Property<T> prop, Integer width) {
        return new ColumnDescriptor<>(prop.getName(),
                prop.getValueType(),
                prop.getDisplayName(),
                prop.getShortDescription(),
                width == null ? 0 : width);
    }

    /**
     * Helper method to convert the first letter of a string to uppercase. And
     * prefix the string with some next string.
     */
    public static String capitalize(String s) {
        return StringUtils.isBlank(s) ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static int indexOfEqualId(Collection<? extends Identifiable> c, Identifiable idObject) {
        int i = 0;
        if (idObject == null) {
            for (Identifiable identifiable : c) {
                if (identifiable == null) {
                    return i;
                }
                ++i;
            }
        } else {
            for (Identifiable identifiable : c) {
                if (Is.equals(idObject.getId(), identifiable == null ? null : identifiable.getId())) {
                    return i;
                }
                ++i;
            }
        }
        return -1;
    }

    public static boolean show(ActionListenerPanel panel, String title, String okName) {
        JButton ok = new JButton(okName);
        JButton cancel = new JButton("Cancel"); // NOI18N
        final DialogDescriptor dd = new DialogDescriptor(panel,
                title,
                true,
                new Object[]{ok, cancel},
                ok,
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP,
                panel);

        ok.getAccessibleContext().setAccessibleDescription(ok.getText());
        cancel.getAccessibleContext().setAccessibleDescription(cancel.getText());

        panel.setOkButton(ok);
        panel.setCancelButton(cancel);
        panel.setDialogDescribtor(dd);

        dd.setClosingOptions(new Object[]{cancel});

        return DialogDisplayer.getDefault().notify(dd) == ok;
    }

    public static class ProjectComparator implements Comparator<Project> {

        public static ProjectComparator SINGLETON = new ProjectComparator();

        private ProjectComparator() {
            // suppressed to enforce using the SINGLETON
        }

        @Override
        public int compare(Project a, Project b) {
            return a.getName().compareTo(b.getName());
        }
    }

//    public static Repository getRepository(RedmineRepository redmineRepository) {
//        Repository repository = null;//Redmine.getInstance().getBugtrackingFactory().getRepository(
//        //     RedmineConnector.ID, redmineRepository.getID());
//   /*   if (repository == null) {
//         repository = Redmine.getInstance().getBugtrackingFactory().createRepository(
//         redmineRepository,
//         Redmine.getInstance().getRepositoryProvider(),
//         Redmine.getInstance().getQueryProvider(),
//         Redmine.getInstance().getIssueProvider());
//         }*/
//        return repository;
//    }

    /**
     * Get the RedmineIssue from the cache or from the Redmine application.
     *
     * @param redmineRepository the RedmineRepository
     * @param issueId the id of the issue
     * @return the RedmineIssue or {@code null} if no such issue available.
     */
    public static RedmineIssue getIssue(RedmineRepository redmineRepository, String issueId) {
        RedmineIssue redmineIssue = null;//= redmineRepository.getIssueCache().getIssue(issueId);
        if (redmineIssue == null) {
            redmineIssue = redmineRepository.getIssue(issueId);
        }
        return redmineIssue;
    }

    public static void openIssue(RedmineIssue redmineIssue) {
        /* Redmine.getInstance().getBugtrackingFactory().openIssue(
         getRepository(redmineIssue.getRepository()), redmineIssue);*/
    }

    public static void openQuery(RedmineQuery redmineQuery) {
        /*     Redmine.getInstance().getBugtrackingFactory().openQuery(
         getRepository(redmineQuery.getRepository()), redmineQuery);*/
    }
}
