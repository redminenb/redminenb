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
import com.kenai.redminenb.util.ExceptionHandler;
import com.kenai.redminenb.util.SafeAutoCloseable;
import com.taskadapter.redmineapi.Include;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.Attachment;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.apache.commons.lang.StringUtils;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.openide.util.Mutex;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
@Messages({
    "# {0} - Tracker Name",
    "# {1} - Issue ID",
    "# {2} - Issue subject",
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
    private static final Logger LOG = Logger.getLogger(RedmineIssue.class.getName());

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
    static final DateFormat DATETIME_FORMAT = DateFormat.getDateTimeInstance();

    private com.taskadapter.redmineapi.bean.Issue issue;
    private RedmineRepository repository;
    private RedmineIssueController controller;

    private final PropertyChangeSupport support;

    private Object localSummary;
    private Object localDescription;

    public RedmineIssue(RedmineRepository repo) {
        repository = repo;
        support = new PropertyChangeSupport(this);
    }

    public RedmineIssue(RedmineRepository repo, String summary, String description) {
        repository = repo;
        support = new PropertyChangeSupport(this);
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
    
    private Integer busy = 0;
    
    private final SafeAutoCloseable busyHelper = new SafeAutoCloseable() {
        @Override
        public void close() {
            setBusy(false);
        }
    };
    
    public SafeAutoCloseable busy() {
        setBusy(true);
        return busyHelper;
    }

    public synchronized boolean isBusy() {
        return busy != 0;
    }
    
    private synchronized void setBusy(boolean busyBool) {
        final boolean oldBusy = isBusy();
        if (busyBool) {
            busy++;
        } else {
            busy--;
        }
        if (busy < 0) {
            throw new IllegalStateException("Inbalanced busy/nonbusy");
        }
        Mutex.EVENT.writeAccess(new Runnable() {
            @Override
            public void run() {
                 support.firePropertyChange("busy", oldBusy, busy != 0);
            }
        });
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
        return issue != null && issue.getParentId() != null;
    }

    public boolean isFinished() {
        // TODO: improve this
        return "closed".equalsIgnoreCase(issue.getStatusName());
    }

    void opened() {
        if (Redmine.LOG.isLoggable(Level.FINE)) {
            Redmine.LOG.log(Level.FINE, "issue {0} open start", new Object[]{getID()});
        }
        String refresh = System.getProperty("org.netbeans.modules.bugzilla.noIssueRefresh"); // NOI18N
        if (refresh != null && refresh.equals("true")) {                                      // NOI18N
            return;
        }
        if(SwingUtilities.isEventDispatchThread()) {
            new SwingWorker<Object, Object>() {
                @Override
                protected Object doInBackground() throws Exception {
                    refresh();
                    return null;
                };
            }.execute();
        } else{
            refresh();
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
        if (Redmine.LOG.isLoggable(Level.FINE)) {
            Redmine.LOG.log(Level.FINE, "issue {0} close finish", new Object[]{getID()});
        }
    }

    public synchronized boolean refresh() {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        try {
            if (issue != null && issue.getId() != null) {
                setIssue(getRepository().getIssueManager().getIssueById(
                        issue.getId(), Include.journals, Include.attachments, Include.watchers));
            }
            return true;
        } catch (RedmineException | RuntimeException ex) {
            ExceptionHandler.handleException(LOG, "Can't refresh Redmine issue", ex);
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
            }
            
            getRepository().getIssueManager().update(issue);
            
            return;

        } catch (RedmineException | RuntimeException ex) {
            ExceptionHandler.handleException(LOG, "Can't add comment for a Redmine issue", ex);
        }

        issue.setStatusId(oldStatusId);
    }

    public void attachFile(File file, String description, String comment, boolean patch) {
        try {
            Attachment a = getRepository().getAttachmentManager().uploadAttachment("application/octed-stream", file);
            a.setDescription(description);
            issue.addAttachment(a);
            if(! StringUtils.isBlank(comment)) {
                issue.setNotes(comment);
            }
            getRepository().getIssueManager().update(issue);
        } catch (RedmineException | IOException ex) {
            ExceptionHandler.handleException(LOG, "Can't attach file to a Redmine issue", ex);
        }
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
        support.firePropertyChange(Issue.EVENT_ISSUE_DATA_CHANGED, null, null);
    }

    public RedmineRepository getRepository() {
        return repository;
    }

    @Override
    public String toString() {
        return getTooltip();
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

    Date getDueDate() {
        if(issue != null) {
            return issue.getDueDate();
        } else {
            return null;
        }
    }

    IssueScheduleInfo getSchedule() {
        if(issue != null && issue.getStartDate() != null) {
            return new IssueScheduleInfo(issue.getStartDate());
        } else {
            return null;
        }
    }

    void setSchedule(IssueScheduleInfo scheduleInfo) {
        if(issue == null) {
            return; // Silently igonre setSchedule on not yet saved issues
        }
        if (scheduleInfo == null) {
            issue.setStartDate(null);
        } else {
            issue.setStartDate(scheduleInfo.getDate());
        }
        getRepository().getRequestProcessor().execute(issueUpdate);
    }
    
    private Runnable issueUpdate = new Runnable() {
        @Override
        public void run() {
            try {
                getRepository().getIssueManager().update(issue);
            } catch (RedmineException | RuntimeException ex) {
                ExceptionHandler.handleException(LOG, "Failed to update start date for issue", ex);
            }
        }
    };
}
