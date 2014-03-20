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
package com.kenai.redminenb.issue;

import com.kenai.redminenb.Redmine;
import com.kenai.redminenb.repository.RedmineRepository;
import com.kenai.redminenb.util.RedmineUtil;

import com.taskadapter.redmineapi.AuthenticationException;
import com.taskadapter.redmineapi.NotFoundException;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.TimeEntry;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.issuetable.ColumnDescriptor;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.netbeans.modules.bugtracking.commons.TextUtils;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

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
    static final DateFormat DATETIME_FORMAT = DateFormat.getDateTimeInstance(); //  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // NOI18N
    private static final int SHORTENED_SUMMARY_LENGTH = 22;
    //
    /**
     * Defines columns for a view table.
     */
    public static ColumnDescriptor<?>[] columnDescriptors;
    //
    private com.taskadapter.redmineapi.bean.Issue issue;
    private RedmineRepository repository;
    private RedmineIssueController controller;
    private IssueNode<RedmineIssue> node;
    //
    private final PropertyChangeSupport support;
    //
    private String remoteSummary;
    private String remoteDescription;
    private Object localSummary;
    private Object localDescription;

    public RedmineIssue(RedmineRepository repo) {
        repository = repo;
        support = new PropertyChangeSupport(this);
    }

    public RedmineIssue(RedmineRepository repo, String summary, String description) {
        repository = repo;
        support = new PropertyChangeSupport(this);
        remoteSummary = summary;
        remoteDescription = description;
    }

    public RedmineIssue(RedmineRepository repository, com.taskadapter.redmineapi.bean.Issue issue) {
        this(repository);
        setIssue(issue);
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
        if (issue == null) {
            return Bundle.CTL_NewIssue();
        }
        return issue.getId() == null
                ? issue.getSubject()
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
        return issue == null || issue.getId() == null || issue.getId() == 0;
    }

    public boolean hasParent() {
        return issue.getParentId() != null;
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
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        try {
            if (issue.getId() != null) {
                setIssue(getRepository().getManager().getIssueById(issue.getId()));
//            getRepository().getIssueCache().setIssueData(getID(), this);
            }
            return true;
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
//      wasSeen();
    }

    public void attachPatch(File file, String string) {
        // TODO Implement file addition as soon as the function is supported by Redmine API
        throw new UnsupportedOperationException("REDMINE API: Not supported yet.");
    }

    public IssueController getController() {
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
        List<RedmineIssue> convertedIssues = new LinkedList<>();

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

    public IssueNode<RedmineIssue> getNode() {
        if (node == null) {
            node = createNode();
        }
        return node;
    }

    private RedmineIssueNode createNode() {
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

    /*public void setSeen(boolean seen) throws IOException {
     repository.getIssueCache().setSeen(getID(), seen);
     }

     public boolean wasSeen() {
     return repository.getIssueCache().wasSeen(getID());
     }*/
    private TimeEntry createTimeEntry(String comment) throws IOException, AuthenticationException,
            RedmineException, NotFoundException {
        TimeEntry timeEntry = new TimeEntry();

        timeEntry.setIssueId(issue.getId());
        timeEntry.setProjectId(issue.getProject().getId());
        timeEntry.setUserId(getRepository().getCurrentUser().getId());
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
    @SuppressWarnings("unchecked")
    public <T> T getFieldValue(String fieldName) {
        try {
            Field f = issue.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (T) f.get(issue);

        } catch (NoSuchFieldException ex) {
            Exceptions.printStackTrace(ex);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public IssueStatusProvider.Status getStatus() {
        if (issue.getId() == null) {
            return IssueStatusProvider.Status.OUTGOING_NEW;
        }
        if (localDescription != null || localSummary != null) {
            return IssueStatusProvider.Status.OUTGOING_MODIFIED;
        }
        return IssueStatusProvider.Status.SEEN;
    }

    public static ColumnDescriptor<?>[] getColumnDescriptors(RedmineRepository repo) {
        if (columnDescriptors == null) {
            // setup column widths
            JTable t = new JTable();
            Map<String, Integer> widths = new HashMap<>();
            widths.put("issue." + FIELD_ID, UIUtils.getColumnWidthInPixels(4, t));
            widths.put("issue." + FIELD_SUBJECT, -1);

            Node.Property<?>[] props = new RedmineIssueNode(new RedmineIssue(repo)).getProperties();
            columnDescriptors = new ColumnDescriptor<?>[props.length];
            for (int i = 0; i < props.length; i++) {
                Node.Property<?> p = props[i];
                columnDescriptors[i] = RedmineUtil.convertNodePropertyToColumnDescriptor(p, widths.get(p.getName()));
            }
        }
        return columnDescriptors;
    }
    private IssueScheduleInfo scheduleInfo;
    private Date dueDate;

    Date getDueDate() {
        return dueDate;
    }

    IssueScheduleInfo getSchedule() {
        return scheduleInfo;
    }

    void setSchedule(IssueScheduleInfo scheduleInfo) {
        this.scheduleInfo = scheduleInfo;
    }

    void discardOutgoing() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    boolean submit() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
