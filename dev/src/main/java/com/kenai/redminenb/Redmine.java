package com.kenai.redminenb;

import com.kenai.redminenb.issue.RedmineIssue;
import com.kenai.redminenb.issue.RedmineIssuePriorityProvider;
import com.kenai.redminenb.issue.RedmineIssueProvider;
import com.kenai.redminenb.issue.RedmineIssueScheduleProvider;
import com.kenai.redminenb.query.RedmineQuery;
import com.kenai.redminenb.query.RedmineQueryProvider;
import com.kenai.redminenb.repository.RedmineRepository;
import com.kenai.redminenb.repository.RedmineRepositoryProvider;
import java.awt.Image;
import java.util.logging.Logger;
import org.netbeans.modules.bugtracking.spi.BugtrackingSupport;
import org.openide.util.*;

/**
 * RedmineNB integration base class.
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
public final class Redmine {

    public static final Logger LOG = Logger.getLogger(Redmine.class.getName());
    public static final String IMAGE_PATH = "com/kenai/redminenb/resources/";
    public static final String ICON_IMAGE = "redmine.png";

    private static RedmineIssuePriorityProvider ipp;
    private static volatile RedmineIssueScheduleProvider issp;

    private RedmineIssueProvider rip;
    private RedmineQueryProvider rqp;
    private RedmineRepositoryProvider rrp;
    private BugtrackingSupport<RedmineRepository, RedmineQuery, RedmineIssue> support;
    
    private Redmine() {
        // omitted
    }

    public BugtrackingSupport<RedmineRepository, RedmineQuery, RedmineIssue> getSupport() {
        if (support == null) {
            support = new BugtrackingSupport<>(getRepositoryProvider(), getQueryProvider(), getIssueProvider());
        }
        return support;
    }

    private static class Holder {
        private static final Redmine SINGLETON = new Redmine();
    }

    public static Redmine getInstance() {
        return Holder.SINGLETON;
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

    public RedmineIssueProvider getIssueProvider() {
        if (rip == null) {
            rip = new RedmineIssueProvider();
        }
        return rip;
    }

    public RedmineQueryProvider getQueryProvider() {
        if (rqp == null) {
            rqp = new RedmineQueryProvider();
        }
        return rqp;
    }

    public RedmineIssueScheduleProvider getIssueScheduleProvider() {
        if (issp == null) {
            synchronized (this) {
                if (issp == null) {
                    issp = new RedmineIssueScheduleProvider();
                }
            }
        }
        return issp;
    }

    private RedmineRepositoryProvider getRepositoryProvider() {
        if (rrp == null) {
            rrp = new RedmineRepositoryProvider();
        }
        return rrp;
    }
}
