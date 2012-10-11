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
package com.kenai.redmineNB.issue;

import com.kenai.redmineNB.Redmine;
import com.kenai.redmineNB.repository.RedmineRepository;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.util.TextUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import com.taskadapter.redmineapi.AuthenticationException;
import com.taskadapter.redmineapi.NotFoundException;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntry;

/**
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
@NbBundle.Messages({
   "CTL_Issue={0} #{1}: {2}",
   "CTL_NewIssue=New Issue",
   //
   "CTL_Issue_Id=ID",
   "CTL_Issue_Id_Desc=Issue ID",
   "CTL_Issue_Project=Project",
   "CTL_Issue_Project_Desc=Project",
   "CTL_Issue_Tracker=Tracker",
   "CTL_Issue_Tracker_Desc=Issue Type",
   "CTL_Issue_ParentId=Parent task",
   "CTL_Issue_ParentId_Desc=Parent task",
   "CTL_Issue_StatusName=Status",
   "CTL_Issue_StatusName_Desc=Issue Status",
   "CTL_Issue_Category=Category",
   "CTL_Issue_Category_Desc=Issue Category",
   "CTL_Issue_PriorityText=Priority",
   "CTL_Issue_PriorityText_Desc=Issue Priority",
   "CTL_Issue_Subject=Subject", // Summary in Redmine
   "CTL_Issue_Subject_Desc=Issue Summary",
   "CTL_Issue_Author=Author", // Reporter in Redmine
   "CTL_Issue_Author_Desc=Issue Author", // Reporter in Redmine
   "CTL_Issue_Assignee=Assigned To",
   "CTL_Issue_Assignee_Desc=User to whom the issue is assigned",
   "CTL_Issue_CreatedOn=Created",
   "CTL_Issue_CreatedOn_Desc=creation time of the issue",
   "CTL_Issue_UpdatedOn=Updated", // Modification in Redmine
   "CTL_Issue_UpdatedOn_Desc=Last time the issue was modified",
   "CTL_Issue_TargetVersion=Target Version",
   "CTL_Issue_TargetVersion_Desc=Issue Target Version"
})
public final class RedmineIssue {

   static final String FIELD_ID = "id";                           // NOI18N
   static final String FIELD_PROJECT = "project";                 // NOI18N
   static final String FIELD_SUBJECT = "subject";                 // NOI18N
   static final String FIELD_PARENT = "parentId";                 // NOI18N
   static final String FIELD_ASSIGNEE = "assignee";               // NOI18N
   static final String FIELD_AUTHOR = "author";                   // NOI18N
   static final String FIELD_PRIORITY_ID = "priorityId";          // NOI18N
   static final String FIELD_PRIORITY_TEXT = "priorityText";      // NOI18N
   static final String FIELD_DONERATIO = "doneRatio";             // NOI18N
   static final String FIELD_ESTIMATED_HOURS = "estimatedHours";  // NOI18N
   static final String FIELD_SPENT_HOURS = "spentHours";          // NOI18N
   static final String FIELD_START_DATE = "startDate";            // NOI18N
   static final String FIELD_DUE_DATE = "dueDate";                // NOI18N
   static final String FIELD_TRACKER = "tracker";                 // NOI18N
   static final String FIELD_STATUS_ID = "statusId";              // NOI18N
   static final String FIELD_STATUS_NAME = "statusName";          // NOI18N
   static final String FIELD_DESCRIPTION = "description";         // NOI18N
   static final String FIELD_CREATED = "createdOn";               // NOI18N
   static final String FIELD_UPDATED = "updatedOn";               // NOI18N
   static final String FIELD_VERSION = "targetVersion";           // NOI18N
   static final String FIELD_CATEGORY = "category";               // NOI18N
   //
   private static final int SHORTENED_SUMMARY_LENGTH = 22;
   //
   private com.taskadapter.redmineapi.bean.Issue issue;
   private RedmineRepository repository;
   private RedmineIssueController controller;
   private IssueNode node;
   //
   private final PropertyChangeSupport support;

   public RedmineIssue(RedmineRepository repo) {
      repository = repo;
      support = new PropertyChangeSupport(this);
   }

   public RedmineIssue(RedmineRepository repository, com.taskadapter.redmineapi.bean.Issue issue) {
      this(repository);
      setIssue(issue);

//      try {
//         repository.getIssueCache().setIssueData(this, issue);
//      } catch (IOException ex) {
//         Redmine.LOG.log(Level.SEVERE, "Can not set the Redmine issue", ex);
//      }
   }

   public void addPropertyChangeListener(PropertyChangeListener listener) {
      support.addPropertyChangeListener(listener);
   }

   public void removePropertyChangeListener(PropertyChangeListener listener) {
      support.removePropertyChangeListener(listener);
   }

   public String getDisplayName() {
      return getDisplayName(issue);
   }

   public static String getDisplayName(com.taskadapter.redmineapi.bean.Issue issue) {
      return issue == null
              ? Bundle.CTL_NewIssue()
              : Bundle.CTL_Issue(issue.getTracker().getName(), issue.getId(), issue.getSubject());
   }

   public String getShortenedDisplayName() {
      if (isNew()) {
         return getDisplayName();
      }
      String shortSummary = TextUtils.shortenText(getSummary(),
                                                  2, //try at least 2 words
                                                  SHORTENED_SUMMARY_LENGTH);
      return Bundle.CTL_Issue(issue.getTracker().getName(), getID(), shortSummary);
   }

   public String getTooltip() {
      return getDisplayName();
   }

   public String getID() {
      return isNew() ? null : String.valueOf(issue.getId());
   }

   public String getSummary() {
      return isNew() ? Bundle.CTL_NewIssue() : issue.getSubject();
   }

   public boolean isNew() {
      return issue == null;
   }

   public boolean isFinished() {
      // TODO: improve this
      return "closed".equalsIgnoreCase(issue.getStatusName());
   }

   void opened() {
      if (Redmine.LOG.isLoggable(Level.FINE)) {
         Redmine.LOG.log(Level.FINE, "issue {0} open start", new Object[]{getID()});
      }
//      if (!data.isNew()) {
//         // 1.) to get seen attributes makes no sense for new issues
//         // 2.) set seenAtributes on issue open, before its actuall
//         //     state is written via setSeen().
//         seenAtributes = repository.getIssueCache().getSeenAttributes(getID());
//      }
      String refresh = System.getProperty("org.netbeans.modules.bugzilla.noIssueRefresh"); // NOI18N
      if (refresh != null && refresh.equals("true")) {                                      // NOI18N
         return;
      }
      repository.scheduleForRefresh(getID());
      if (Redmine.LOG.isLoggable(Level.FINE)) {
         Redmine.LOG.log(Level.FINE, "issue {0} open finish", new Object[]{getID()});
      }
   }

   void closed() {
      if (Redmine.LOG.isLoggable(Level.FINE)) {
         Redmine.LOG.log(Level.FINE, "issue {0} close start", new Object[]{getID()});
      }
      repository.stopRefreshing(getID());
//      seenAtributes = null;
      if (Redmine.LOG.isLoggable(Level.FINE)) {
         Redmine.LOG.log(Level.FINE, "issue {0} close finish", new Object[]{getID()});
      }
   }

   public boolean refresh() {
      try {
         if (issue.getId() != null) {
            setIssue(getRepository().getManager().getIssueById(issue.getId()));

            if (!SwingUtilities.isEventDispatchThread()) {
               getRepository().getIssueCache().setIssueData(this, issue);
            }
         }

         return true;
      } catch (IOException ex) {
         // TODO Notify user that it is not possible to connect to Redmine
         Redmine.LOG.log(Level.SEVERE, "Can't refresh Redmine issue", ex);
      } catch (NotFoundException ex) {
         // TODO Notify user that the issue no longer exists
         Redmine.LOG.log(Level.SEVERE, "Can't refresh Redmine issue", ex);
      } catch (RedmineException ex) {
         // TODO Notify user that Redmine internal error has happened
         Redmine.LOG.log(Level.SEVERE, "Can't refresh Redmine issue", ex);
      }

      return false;
   }

   public void addComment(String comment, boolean resolve) {
      Integer oldStatusId = issue.getStatusId();

      try {
         issue.setNotes(comment);

         if (resolve) {
            // TODO This works for default Redmine Settings only. Add resolved status ID configuration to Redmine Option.
            issue.setStatusId(3);
            //issue.setStatusName("Resolved"); // not needed

            getRepository().getManager().update(issue);
         }
         return;

      } catch (NotFoundException ex) {
         // TODO Notify user that the issue no longer exists
         Redmine.LOG.log(Level.SEVERE, "Can't add comment for a Redmine issue", ex);
      } catch (RedmineException ex) {
         // TODO Notify user that Redmine internal error has happened
         Redmine.LOG.log(Level.SEVERE, "Can't add comment for a Redmine issue", ex);
      }

      issue.setStatusId(oldStatusId);
      getRepository().getIssueCache().wasSeen(getID());
   }

   public void attachPatch(File file, String string) {
      // TODO Implement file addition as soon as the function is supported by Redmine API
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public BugtrackingController getController() {
      if (controller == null) {
         controller = new RedmineIssueController(this);
      }
      return controller;
   }

   public com.taskadapter.redmineapi.bean.Issue getIssue() {
      return issue;
   }

   public void setIssue(com.taskadapter.redmineapi.bean.Issue issue) {
      this.issue = issue;
   }

   public static Collection<RedmineIssue> getIssues(RedmineRepository repository,
                                                    List<com.taskadapter.redmineapi.bean.Issue>... issueList) {
      List<RedmineIssue> convertedIssues = new LinkedList<RedmineIssue>();

      for (List<com.taskadapter.redmineapi.bean.Issue> issues : issueList) {
         if (issues != null) {
            for (com.taskadapter.redmineapi.bean.Issue issue : issues) {
               convertedIssues.add(new RedmineIssue(repository, issue));
            }
         }
      }

      return convertedIssues;
   }

   public RedmineRepository getRepository() {
      return repository;
   }

   public Map<String, String> getAttributes() {
      // TODO: implement
//        if(attributes == null) {
//            attributes = new HashMap<String, String>();
//            String value;
//            for (IssueField field : getRepository().getConfiguration().getFields()) {
//                value = getFieldValue(field);
//                if(value != null && !value.trim().equals("")) {                 // NOI18N
//                    attributes.put(field.getKey(), value);
//                }
//            }
//        }
//        return attributes;
      return Collections.<String, String>emptyMap();
   }

   @Override
   public String toString() {
      return getTooltip();
   }

   public IssueNode getNode() {
      if (node == null) {
         node = createNode();
      }
      return node;
   }

   private IssueNode createNode() {
      return new RedmineIssueNode(this);
   }

   public long getLastModify() {
      if (issue != null) {
         return issue.getUpdatedOn().getTime();
      }
      return -1;
   }

   public String getRecentChanges() {
      return ""; // TODO implement
   }

   public long getCreated() {
      if (issue != null) {
         return issue.getCreatedOn().getTime();
      }
      return -1;
   }

   public void setSeen(boolean seen) throws IOException {
      repository.getIssueCache().setSeen(getID(), seen);
   }

   private boolean wasSeen() {
      // TODO: use this method
      return repository.getIssueCache().wasSeen(getID());
   }

   private TimeEntry createTimeEntry(String comment) throws IOException, AuthenticationException,
                                                            RedmineException, NotFoundException {
      TimeEntry timeEntry = new TimeEntry();

      timeEntry.setIssueId(issue.getId());
      timeEntry.setProjectId(issue.getProject().getId());
      timeEntry.setUserId(getRepository().getManager().getCurrentUser().getId());
      // TODO This works for default Redmine Settings only. Add activity ID configuration to Redmine Option.
      timeEntry.setActivityId(9);
      timeEntry.setComment(comment);
      // TODO Implement spend date selection for the user.
      timeEntry.setSpentOn(new Date());
      // TODO Implement hours input for the user.
      timeEntry.setHours(0f);

      return timeEntry;
   }

   /**
    * Returns the value represented by the given field name
    *
    * @param fieldName the name of the field
    * @return value of the field
    */
   public <T> T getFieldValue(String fieldName) {
      try {
         Field f = issue.getClass().getDeclaredField(fieldName);
         f.setAccessible(true);
         return (T)f.get(issue);

      } catch (NoSuchFieldException ex) {
         Exceptions.printStackTrace(ex);
      } catch (Exception ex) {
         Exceptions.printStackTrace(ex);
      }
      return null;
   }
}
