/*
 * Copyright 2012 Anchialas and Mykolaas.
 * Copyright 2015 Matthias Bläsing
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
package com.kenai.redminenb.repository;

import com.kenai.redminenb.Redmine;
import com.kenai.redminenb.RedmineConfig;
import com.kenai.redminenb.RedmineConnector;
import com.kenai.redminenb.issue.RedmineIssue;
import com.kenai.redminenb.query.RedmineQuery;
import com.kenai.redminenb.query.RedmineQueryController;
import com.kenai.redminenb.user.RedmineUser;

import com.kenai.redminenb.api.AuthMode;
import static com.kenai.redminenb.repository.RedmineManagerFactoryHelper.getTransportFromManager;
import com.kenai.redminenb.util.AssigneeWrapper;
import com.kenai.redminenb.util.ExceptionHandler;
import com.kenai.redminenb.util.NestedProject;
import com.taskadapter.redmineapi.AttachmentManager;
import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.MembershipManager;
import com.taskadapter.redmineapi.NotFoundException;
import com.taskadapter.redmineapi.ProjectManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.CustomFieldDefinition;
import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssuePriority;
import com.taskadapter.redmineapi.bean.IssuePriorityFactory;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Membership;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.SavedQuery;
import com.taskadapter.redmineapi.bean.TimeEntryActivity;
import com.taskadapter.redmineapi.bean.TimeEntryActivityFactory;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.Version;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.bugtracking.spi.RepositoryController;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Redmine repository manager.
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
@NbBundle.Messages({
    "# {0} - repo name",
    "# {1} - user name",
    "# {2} - redmine url",
    "LBL_RepositoryTooltip=\"Redmine repository<br>{0} : {1}@{2}"
})
public class RedmineRepository {    
    private static final Logger LOG = Logger.getLogger(RedmineRepository.class.getName());
    
    static final String PROPERTY_AUTH_MODE = "authMode";                // NOI18N  
    static final String PROPERTY_ACCESS_KEY = "accessKey";              // NOI18N  
    static final String PROPERTY_PROJECT_ID = "projectId";              // NOI18N
    static final String PROPERTY_FEATURE_WATCHERS = "featureWatchers";  // NOI18N
    
    private static final List<TimeEntryActivity> fallbackTimeActivityEntries;
    
    static {
        TimeEntryActivity design = TimeEntryActivityFactory.create(8);
        design.setDefault(false);
        design.setName("Design");
        TimeEntryActivity development = TimeEntryActivityFactory.create(9);
        development.setDefault(false);
        development.setName("Development");
        fallbackTimeActivityEntries = Collections.unmodifiableList(
                Arrays.asList(design, development));
    }
 
    private static final List<IssuePriority> fallbackIssuePriorities;
            
    static {
        fallbackIssuePriorities = Collections.unmodifiableList( Arrays.asList(
                createIssuePriority(7, "Immediate", false),
                createIssuePriority(6, "Urgent", false),
                createIssuePriority(5, "High", false),
                createIssuePriority(4, "Normal", true),
                createIssuePriority(3, "Low", false)));
    }
                    
    private RepositoryInfo info;
    private transient RepositoryController controller;
    private Map<String, RedmineQuery> queries = null;
    private transient RedmineManager manager;
    private transient RedmineUser currentUser;
    private transient Lookup lookup;
    private final transient InstanceContent ic;

    private final Set<String> issuesToRefresh = new HashSet<>(5);
    private final Set<RedmineQuery> queriesToRefresh = new HashSet<>(3);
    private RequestProcessor.Task refreshIssuesTask;
    private RequestProcessor.Task refreshQueryTask;
    private RequestProcessor requestProcessor;

    private final IssueCache issueCache = new IssueCache(this);

    private final Set<RedmineIssue> newIssues = Collections.synchronizedSet(new HashSet<RedmineIssue>());
    private Map<Integer, NestedProject> projects;
    private final Map<Integer, List<Membership>> userCache = Collections.synchronizedMap(new HashMap<Integer,List<Membership>>());
    private final Map<Integer, List<IssueCategory>> categoryCache = Collections.synchronizedMap(new HashMap<Integer, List<IssueCategory>>());
    private final Map<Integer, List<Version>> versionCache = Collections.synchronizedMap(new HashMap<Integer, List<Version>>());
    private List<IssueStatus> statusCache = null;
    private List<TimeEntryActivity> timeEntryActivityCache = null;
    private List<Tracker> trackerCache = null;
    private List<CustomFieldDefinition> customFieldsCache = null;
    
    // Make sure we know all instances we created - a crude hack, but API does
    // not allow ourselfes ....
    private static final List<WeakReference<RedmineRepository>> repositoryList
            = Collections.synchronizedList(new LinkedList<WeakReference<RedmineRepository>>());

    {
        repositoryList.add(new WeakReference<>(this));
    }
    
    public static RedmineRepository getInstanceyById(@NonNull String id) {
        if( id == null ) {
            throw new NullPointerException("getInstanceById might not be called with null!");
        }
        synchronized(repositoryList) {
            Iterator<WeakReference<RedmineRepository>> it = repositoryList.iterator();
            RedmineRepository result = null;
            while(it.hasNext()) {
                WeakReference<RedmineRepository> weak = it.next();
                RedmineRepository hard = weak.get();
                if(hard == null) {
                    it.remove();
                } else {
                    if(id.equals(hard.getID()) && result == null) {
                        result = hard;
                    }
                }
            }
            return result;
        }
    }
    
    /**
     * Default constructor required for deserializing.
     */
    public RedmineRepository() {
        this.ic = new InstanceContent();
    }

    public RedmineRepository(RepositoryInfo info) { 
        this();
        this.info = info;
    }

    public IssueCache getIssueCache() {
        return issueCache;
    }

    public RepositoryInfo getInfo() {
        return info;
    }

    synchronized void setInfoValues(String name, String url, String user, char[] password,
            String accessKey, AuthMode authMode, Integer project, boolean featureWatchers,
            String httpUser, char[] httpPassword) {
        String id = info != null ? info.getID() : name + System.currentTimeMillis();
        RepositoryInfo ri = new RepositoryInfo(id,
                RedmineConnector.ID,
                url,
                name,
                getTooltip(name, user, url),
                user,
                httpUser,
                password,
                httpPassword);
        ri.putValue(PROPERTY_FEATURE_WATCHERS, Boolean.toString(featureWatchers));
        ri.putValue(PROPERTY_PROJECT_ID, project == null ? null : String.valueOf(project));
        info = ri;
        setAccessKey(accessKey);
        setAuthMode(authMode);
        this.projects = null;
    }

    public Map<Integer, NestedProject> getProjects() {
        try {
            if (projects == null) {
                Map<Integer, NestedProject> projectMap = 
                        convertProjectList(getProjectManager().getProjects());
                projects = Collections.unmodifiableMap(projectMap);
            }
        } catch (Exception ex) {
            Redmine.LOG.log(Level.WARNING, "Failed to retrieve project list", ex);
        }
        return projects;
    }

    public static Map<Integer, NestedProject> convertProjectList(List<Project> projects) {
        Map<Integer, NestedProject> projectMap = new HashMap<>();
        for (Project p : projects) {
            projectMap.put(p.getId(), new NestedProject(p));
        }
        for (NestedProject np : projectMap.values()) {
            Project p = np.getProject();
            if (p.getParentId() != null) {
                np.setParent(projectMap.get(p.getParentId()));
            }
        }
        return projectMap;
    }
    
    public String getDisplayName() {
        if (info == null) {
            return "";
        }
        return info.getDisplayName();
    }

    private String getTooltip(String repoName, String user, String url) {
        return Bundle.LBL_RepositoryTooltip(repoName, user, url);
    }

    public String getID() {
        return info.getID();
    }

    public String getUrl() {
        if(info == null) {
            return "";
        }
        return info.getUrl();
    }

    public AuthMode getAuthMode() {
        if(info == null) {
            return AuthMode.AccessKey;
        }
        return AuthMode.get(info.getValue(PROPERTY_AUTH_MODE));
    }

    public void setAuthMode(AuthMode authMode) {
        AuthMode old = getAuthMode();
        if (!Objects.equals(old, authMode)) {
            manager = null;
        }
        info.putValue(PROPERTY_AUTH_MODE, authMode == null ? null : authMode.name());
    }

    public String getAccessKey() {
        if(info == null) {
            return "";
        }
        return info.getValue(PROPERTY_ACCESS_KEY);
    }

    public void setAccessKey(String accessKey) {
        String old = getAccessKey();
        if (!Objects.equals(old, accessKey)) {
            manager = null; // force reconnect
        }
        info.putValue(PROPERTY_ACCESS_KEY, accessKey);
    }

    public char[] getPassword() {
        if(info == null) {
            return new char[0];
        }
        return info.getPassword();
    }

    public String getUsername() {
        if(info == null) {
            return "";
        }
        return info.getUsername();
    }

    public Project getProject() throws RedmineException {
        Integer projectId = getProjectID();
        if (projectId != null) {
            NestedProject np = getProjects().get(projectId);
            if (np != null) {
                return np.getProject();
            }
        }
        return null;
    }
    
    public Integer getProjectID() {
        try {
            String projectString = info.getValue(PROPERTY_PROJECT_ID);
            return Integer.valueOf(projectString);
        } catch (NullPointerException | NumberFormatException ex) {
            // Accept the potential slow path - the assumption is, that
            // in 99.9...% of all cases a projectId is set - only
            // while constructing a new repository info, this could be empty
            return null;
        }
    }

    public RedmineIssue getIssue(String issueId) {
        RedmineIssue redmineIssue = null;
        if (issueId != null) {
            redmineIssue = issueCache.get(issueId);
            if (redmineIssue == null) {
                try {
                    Issue issue = getIssueManager().getIssueById(Integer.valueOf(issueId));
                    redmineIssue = issueCache.cachedRedmineIssue(issue);
                } catch (NotFoundException ex) {
                    // do nothing
                } catch (RuntimeException | RedmineException ex) {
                    Redmine.LOG.log(Level.SEVERE, null, ex);
                }
            }
        }
        return redmineIssue;
    }

    public Collection<RedmineIssue> getIssues(final String... ids) {
        final List<RedmineIssue> ret = new ArrayList<>(ids.length);
        for (String id : ids) {
            RedmineIssue issue = getIssue(id);
            if (issue != null) {
                ret.add(issue);
            }
        }
        return ret;
    }

    public void remove() {
    }

    synchronized void resetRepository(boolean keepConfiguration) {
        if (!keepConfiguration) {
            manager = null;
        }
    }

    public RepositoryController getController() {
        if (controller == null) {
            controller = new RedmineRepositoryController(this);
        }
        return controller;
    }

    public RedmineIssue createIssue(String summary, String description) {
        RedmineIssue issue = new RedmineIssue(this, summary, description);
        newIssues.add(issue);
        return issue;
    }

    public RedmineIssue createIssue() {
        RedmineIssue issue = new RedmineIssue(this);
        newIssues.add(issue);
        return issue;
    }
    
    boolean canAttachFiles() {
        return true;
    }

    public RedmineQuery createQuery() {
        return new RedmineQuery(this);
    }

    private synchronized Map<String, RedmineQuery> getQueryMap() {
        if (queries == null) {
            queries = Collections.synchronizedMap(new HashMap<String, RedmineQuery>());
            String[] qs = RedmineConfig.getInstance().getQueries(getID());
            for (String queryName : qs) {
                RedmineQuery q = RedmineConfig.getInstance().getQuery(this, queryName);
                if (q != null) {
                    queries.put(queryName, q);
                } else {
                    Redmine.LOG.log(Level.WARNING, "Couldn''t find query with stored name {0}", queryName); // NOI18N
                }
            }
        }
        return queries;
    }

    public void removeQuery(String displayName) {
        RedmineConfig.getInstance().removeQuery(this, displayName);
        getQueryMap().remove(displayName);
        fireQueryListChanged();

    }

    public void saveQuery(RedmineQuery query) {
        assert info != null;
        RedmineConfig.getInstance().putQuery(this, query);
        getQueryMap().put(query.getDisplayName(), query);
        fireQueryListChanged();
    }

    private void fireQueryListChanged() {
        Redmine.LOG.log(Level.FINER, "firing query list changed for repository {0}", new Object[]{getDisplayName()}); // NOI18N
        propertyChangeSupport.firePropertyChange(RepositoryProvider.EVENT_QUERY_LIST_CHANGED, null, null);
    }

    public Collection<RedmineQuery> getQueries() {
        return getQueryMap().values();
    }

    public List<SavedQuery> getServersideQueries() {
        try {
            return getManager().getIssueManager().getSavedQueries();
        } catch (RedmineException | RuntimeException ex) {
            ExceptionHandler.handleException(LOG, "Can't search for Redmine issues", ex);
            return Collections.EMPTY_LIST;
        }
    }
    
    private Collection<Membership> getMemberships(Project p) {
        if(p == null) {
            return Collections.<Membership>emptyList();
        }
        if (!userCache.containsKey(p.getId())) {
            try {
                userCache.put(p.getId(), getMembershipManager().getMemberships(p.getId().toString()));
            } catch (RedmineException | RuntimeException ex) {
                ExceptionHandler.handleException(LOG, "Can't get Redmine Users", ex);
            }
        }
        List<Membership> memberships = userCache.get(p.getId());
        if(memberships == null) {
            return Collections.<Membership>emptyList();
        } else {
            return memberships;
        }
    }
    
    public Collection<RedmineUser> getUsers(Project p) {
        ArrayList<RedmineUser> users = new ArrayList<>();
        Collection<Membership> memberships = getMemberships(p);
        if(currentUser != null) {
            users.add(currentUser);
        }
        for (Membership m: memberships) {
            if (m.getUser() != null && (! userIsCurrentUser(m.getUser()))) {
                users.add(new RedmineUser(m.getUser()));
            }
        }
        return users;
    }
    
    public Collection<Group> getGroups(Project p) {
        ArrayList<Group> groups = new ArrayList<>();
        for (Membership m : getMemberships(p)) {
            if (m.getGroup() != null) {
                groups.add(m.getGroup());
            }
        }
        return groups;
    }
    
    public Collection<AssigneeWrapper> getAssigneeWrappers(Project p) {
        ArrayList<AssigneeWrapper> assignees = new ArrayList<>();
        Collection<Membership> memberships = getMemberships(p);
        if(currentUser != null) {
            assignees.add(new AssigneeWrapper(currentUser));
        }
        for (Membership m : memberships) {
            if (m.getUser() != null && (! userIsCurrentUser(m.getUser()))) {
                assignees.add(new AssigneeWrapper(m.getUser()));
            } else if ( m.getGroup() != null ) {
                assignees.add(new AssigneeWrapper(m.getGroup()));
            }
        }
        Collections.sort(assignees);
        return assignees;
    }

    private boolean userIsCurrentUser(User user) {
        if(currentUser == null || user == null) {
            return false;
        }
        return currentUser.getUser().getId().equals(user.getId());
    }
    
    public List<Tracker> getTrackers() {
        if(trackerCache == null) {
            try {
                trackerCache = getIssueManager().getTrackers();
            } catch (RedmineException | RuntimeException ex) {
                ExceptionHandler.handleException(LOG, "Can't get Redmine Issue Trackers", ex);
            }
        }
        return trackerCache;
    }
    
    public List<TimeEntryActivity> getTimeEntryActivities() {
        if (timeEntryActivityCache == null) {
            try {
                timeEntryActivityCache = getIssueManager().getTimeEntryActivities();
            } catch (RedmineException | RuntimeException ex) {
                LOG.log(Level.INFO
                        , "Failed to Redmine Time Entry Activities (either API is missing or no permission)"
                        , ex);
                timeEntryActivityCache = fallbackTimeActivityEntries;
            }
        }
        return timeEntryActivityCache;
    }

    public IssueStatus getStatus(int id) {
        for (IssueStatus issueStatus : getStatuses()) {
            if (id == issueStatus.getId()) {
                return issueStatus;
            }
        }
        return null;
    }

    public Collection<? extends IssueStatus> getStatuses() {
        if (statusCache == null) {
            try {
                statusCache = getIssueManager().getStatuses();
            } catch (NotFoundException ex) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        "Can't get Issue Statuses from Redmine:\n"
                        + ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                Redmine.LOG.log(Level.SEVERE, "Can't get Issue Statuses from Redmine", ex);
            } catch (Exception ex) {
                Redmine.LOG.log(Level.SEVERE, "Can't get Issue Statuses from Redmine", ex);
            }
        }

        return statusCache;
    }

    public Collection<? extends IssueCategory> reloadIssueCategories(Project p) {
        categoryCache.remove(p.getId());
        return getIssueCategories(p);
    }

    public Collection<? extends IssueCategory> getIssueCategories(Project p) {
        if (p != null && (!categoryCache.containsKey(p.getId()))) {
            try {
                List<IssueCategory> cats = getIssueManager().getCategories(p.getId());
                for(IssueCategory cat: cats) {
                    cat.setProject(null);
                    cat.setAssignee(null);
                }
                categoryCache.put(p.getId(), Collections.unmodifiableList(cats));
            } catch (NotFoundException ex) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        "Can't get Issue Categories for Redmine Project "
                        + p.getName()
                        + ":\n" + ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE));
                Redmine.LOG.log(Level.SEVERE, "Can't get Issue Categories for Redmine Project "
                        + p.getName(), ex);
            } catch (Exception ex) {
                Redmine.LOG.log(Level.SEVERE, "Can't get Issue Categories for Redmine Project "
                        + p.getName(), ex);
            }
        }
        if(p == null || (!categoryCache.containsKey(p.getId()))) {
            return Collections.EMPTY_LIST;
        }
        return categoryCache.get(p.getId());
    }
    
    public Collection<? extends Version> reloadVersions(Project p) {
        versionCache.remove(p.getId());
        return getVersions(p);
    }

    public List<Version> getVersions(Project p) {      
        if (p != null && (!versionCache.containsKey(p.getId()))) {
            try {
                versionCache.put(p.getId(), getProjectManager().getVersions(p.getId()));
            } catch (Exception ex) {
                Redmine.LOG.log(Level.SEVERE, "Can't get versions for project " + p.getName(), ex);
            }
        }
        if(p == null || (!versionCache.containsKey(p.getId()))) {
            return Collections.EMPTY_LIST;
        }
        return versionCache.get(p.getId());
    }

    public List<IssuePriority> getIssuePriorities() {
        if (issuePriorities == null) {
            try {
                // since Redmine V2.2.0
                issuePriorities = getIssueManager().getIssuePriorities();
                Collections.reverse(issuePriorities);
            } catch (Exception ex) {
                // LOG on info level, as SEVERE causes 
                issuePriorities = fallbackIssuePriorities;
                Redmine.LOG.log(Level.INFO, "Can't get issue priorities, using defaults", ex);
            }
        }
        return issuePriorities;
    }
    
    public void initCustomFieldDefinitions() {
        if (customFieldsCache == null) {
            try {
                // since Redmine V2.4.0
                customFieldsCache = getManager().getCustomFieldManager().getCustomFieldDefinitions();
            } catch (Exception ex) {
                LOG.info("Custom Fields are not available - query failed");
                customFieldsCache = Collections.EMPTY_LIST;
            }
        }
    }
    
    public List<CustomFieldDefinition> getCustomFieldDefinitions(String type, Project proj, Tracker t) {
        initCustomFieldDefinitions();
        List<CustomFieldDefinition> result = new ArrayList<>();
        for(CustomFieldDefinition cfd: customFieldsCache) {
            if (type.equals(cfd.getCustomizedType())
                    && (cfd.getTrackers().contains(t))
                    && cfd.getFieldFormat() != null)
            {
                switch (cfd.getFieldFormat()) {
                    case "version":
                        cfd.getPossibleValues().clear();
                        for (Version v : getVersions(proj)) {
                            cfd.getPossibleValues().add(
                                    v.getName() + " [" + v.getId() + "]");
                        }
                        break;
                    case "user":
                        cfd.getPossibleValues().clear();
                        for (RedmineUser ru : getUsers(proj)) {
                            cfd.getPossibleValues().add(
                                    ru.getUser().getFullName() + " ["
                                    + ru.getId() + "]");
                        }
                        break;
                }
                result.add(cfd);
            }
        }
        return result;
    }
    
    public CustomFieldDefinition getCustomFieldDefinitionById(int id) {
        initCustomFieldDefinitions();
        for(CustomFieldDefinition cfd: customFieldsCache) {
            if(cfd.getId().equals(id)) {
                return cfd;
            }
        }
        return null;  
    }

    public Collection<RedmineIssue> simpleSearch(String string) {
        try {
            IssueManager issueManager = getIssueManager();
            List<Issue> resultIssues = new LinkedList<>();

            try {
                resultIssues.add(issueManager.getIssueById(Integer.parseInt(string)));
            } catch (NumberFormatException | NotFoundException ex) {
            }

            resultIssues.addAll(issueManager.getIssuesBySummary(
                    null,
                    "*" + string + "*"));

            List<RedmineIssue> redmineIssues = new LinkedList<>();
            for (Issue issue : resultIssues) {
                RedmineIssue redmineIssue = issueCache.cachedRedmineIssue(issue);
                redmineIssues.add(redmineIssue);
            }
            return redmineIssues;
        } catch (RedmineException | RuntimeException ex) {
            ExceptionHandler.handleException(LOG, "Can't search for Redmine issues", ex);
        }
        return Collections.<RedmineIssue>emptyList();
    }

    public Lookup getLookup() {
        if (lookup == null) {
            lookup = new AbstractLookup(ic);
        }
        return lookup;
    }
    
    public final RedmineManager getManager() throws RedmineException {
        assert (! SwingUtilities.isEventDispatchThread()) : "Access to Redmine Manager must happen outside EDT!";
        AuthMode authMode = getAuthMode();
        if (manager == null) {
            if (authMode == null) {
                throw new IllegalArgumentException("authMode must be set");
            }
            if (authMode == AuthMode.AccessKey) {
                manager = RedmineManagerFactory.createWithApiKey(
                        getUrl(), 
                        getAccessKey(), 
                        RedmineManagerFactoryHelper.getTransportConfig()
                );
                if(getInfo().getHttpUsername() != null && (! getInfo().getHttpUsername().isEmpty())
                        && getInfo().getHttpPassword() != null && getInfo().getHttpPassword().length > 0) {
                    getTransportFromManager(manager).setCredentials(
                            getInfo().getHttpUsername(),
                            new String(getInfo().getHttpPassword()));
                }
            } else {
                manager = RedmineManagerFactory.createWithUserAuth(
                        getUrl(), 
                        getUsername(),
                        getPassword() == null ? "" : String.valueOf(getPassword()),
                        RedmineManagerFactoryHelper.getTransportConfig()
                );
            }
            currentUser = new RedmineUser(manager.getUserManager().getCurrentUser(), true);
            manager.setObjectsPerPage(100);
        }
        return manager;
    }

    public IssueManager getIssueManager() throws RedmineException {
        return getManager().getIssueManager();
    }

    public AttachmentManager getAttachmentManager() throws RedmineException {
        return getManager().getAttachmentManager();
    }

    public ProjectManager getProjectManager() throws RedmineException {
        return getManager().getProjectManager();
    }

    public MembershipManager getMembershipManager() throws RedmineException {
        return getManager().getMembershipManager();
    }
    
    public RedmineUser getCurrentUser() {
        return currentUser;
    }

    public RequestProcessor getRequestProcessor() {
        if (requestProcessor == null) {
            requestProcessor = new RequestProcessor("Redmine repository processor - " + getDisplayName(), 1, true); // NOI18N
        }
        return requestProcessor;
    }

    private void setupIssueRefreshTask() {
        if (refreshIssuesTask == null) {
            refreshIssuesTask = getRequestProcessor().create(new Runnable() {
                @Override
                public void run() {
                    Set<String> ids;
                    synchronized (issuesToRefresh) {
                        ids = new HashSet<>(issuesToRefresh);
                    }
                    if (ids.isEmpty()) {
                        Redmine.LOG.log(Level.FINE, "no issues to refresh {0}",
                                getDisplayName()); // NOI18N
                        return;
                    }
                    Redmine.LOG.log(Level.FINER, "preparing to refresh issue {0} - {1}",
                            new Object[]{getDisplayName(), ids}); // NOI18N
                    scheduleIssueRefresh();
                }
            });
            scheduleIssueRefresh();
        }
    }

    private void setupQueryRefreshTask() {
        if (refreshQueryTask == null) {
            refreshQueryTask = getRequestProcessor().create(new Runnable() {
                @Override
                public void run() {
                    try {
                        Set<RedmineQuery> queries;
                        synchronized (refreshQueryTask) {
                            queries = new HashSet<>(queriesToRefresh);
                        }
                        if (queries.isEmpty()) {
                            Redmine.LOG.log(Level.FINE, "no queries to refresh {0}",
                                    new Object[]{getDisplayName()}); // NOI18N
                            return;
                        }
                        for (RedmineQuery q : queries) {
                            Redmine.LOG.log(Level.FINER, "preparing to refresh query {0} - {1}",
                                    new Object[]{q.getDisplayName(), getDisplayName()}); // NOI18N
                            RedmineQueryController qc = q.getController();
                            qc.autoRefresh();
                        }
                    } finally {
                        scheduleQueryRefresh();
                    }
                }
            });
            scheduleQueryRefresh();
        }
    }

    private void scheduleIssueRefresh() {
        int delay = RedmineConfig.getInstance().getIssueRefreshInterval();
        Redmine.LOG.log(Level.FINE, "scheduling issue refresh for repository {0} in {1} minute(s)",
                new Object[]{getDisplayName(), delay}); // NOI18N
        if (delay < 5 && System.getProperty("netbeans.t9y.redmine.force.refresh.delay") == null) { // t9y: Testability
            Redmine.LOG.log(Level.WARNING, " wrong issue refresh delay {0}. Falling back to default {0}",
                    new Object[]{delay, RedmineConfig.DEFAULT_ISSUE_REFRESH}); // NOI18N
            delay = RedmineConfig.DEFAULT_ISSUE_REFRESH;
        }
        refreshIssuesTask.schedule(delay * 60 * 1000); // given in minutes
    }

    private void scheduleQueryRefresh() {
        String schedule = System.getProperty("netbeans.t9y.redmine.force.refresh.schedule", "");
        if (!schedule.isEmpty()) {
            int delay = Integer.parseInt(schedule);
            refreshQueryTask.schedule(delay);
            return;
        }

        int delay = RedmineConfig.getInstance().getQueryRefreshInterval();
        Redmine.LOG.log(Level.FINE, "scheduling query refresh for repository {0} in {1} minute(s)",
                new Object[]{getDisplayName(), delay}); // NOI18N
        if (delay < 5) {
            Redmine.LOG.log(Level.WARNING, " wrong query refresh delay {0}. Falling back to default {0}",
                    new Object[]{delay, RedmineConfig.DEFAULT_QUERY_REFRESH}); // NOI18N
            delay = RedmineConfig.DEFAULT_QUERY_REFRESH;
        }
        refreshQueryTask.schedule(delay * 60 * 1000); // given in minutes
    }

    public void stopRefreshing(String id) {
        Redmine.LOG.log(Level.FINE, "removing issue {0} from refresh on repository {1}",
                new Object[]{id, getDisplayName()}); // NOI18N
        synchronized (issuesToRefresh) {
            issuesToRefresh.remove(id);
        }
    }

    public void scheduleForRefresh(String id) {
        Redmine.LOG.log(Level.FINE, "scheduling issue {0} for refresh on repository {0}",
                new Object[]{id, getDisplayName()}); // NOI18N
        synchronized (issuesToRefresh) {
            issuesToRefresh.add(id);
        }
        setupIssueRefreshTask();
    }

    public void scheduleForRefresh(RedmineQuery query) {
        Redmine.LOG.log(Level.FINE, "scheduling query {0} for refresh on repository {1}",
                new Object[]{query.getDisplayName(), getDisplayName()}); // NOI18N
        synchronized (queriesToRefresh) {
            queriesToRefresh.add(query);
        }
        setupQueryRefreshTask();
    }

    public void stopRefreshing(RedmineQuery query) {
        Redmine.LOG.log(Level.FINE, "removing query {0} from refresh on repository {1}",
                new Object[]{query.getDisplayName(), getDisplayName()}); // NOI18N
        synchronized (queriesToRefresh) {
            queriesToRefresh.remove(query);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "["
                + getDisplayName()
                + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RedmineRepository)) {
            return false;
        }
        RedmineRepository other = (RedmineRepository) obj;
        return Objects.equals(this.getDisplayName(), other.getDisplayName())
                && Objects.equals(this.getUrl(), other.getUrl())
                && Objects.equals(getProjectID(), other.getProjectID());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.getDisplayName() != null ? this.getDisplayName().hashCode() : 0);
        hash = 97 * hash + (this.getUrl() != null ? this.getUrl().hashCode() : 0);
        hash = 97 * hash + (getProjectID() != null ? this.getProjectID().hashCode() : 0);
        return hash;
    }
    // 
    // Change Support
    //
    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    public boolean isFeatureWatchers() {
        if (info == null) {
            return false;
        }
        String supportsWatchers = info.getValue(PROPERTY_FEATURE_WATCHERS);
        if(supportsWatchers == null) {
            return false;
        } else {
            return Boolean.parseBoolean(supportsWatchers);
        }
    }

    public IssuePriority getDefaultIssuePriority() {
        for (IssuePriority issuePriority : getIssuePriorities()) {
            if (issuePriority.isDefault()) {
                return issuePriority;
            }
        }
        return null;
    }
    
    public static IssuePriority createIssuePriority(Integer id, String name, boolean isDefault) {
        IssuePriority ip = IssuePriorityFactory.create(id);
        ip.setName(name);
        ip.setDefault(isDefault);
        return ip;
    }

    private List<IssuePriority> issuePriorities;

    public IssuePriority getIssuePriority(Integer id) {
        for(IssuePriority ip: getIssuePriorities()) {
            if(ip.getId().equals(id)) {
                return ip;
            }
        }
        return null;
    }
}
