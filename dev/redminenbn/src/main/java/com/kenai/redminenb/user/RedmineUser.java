package com.kenai.redminenb.user;

import com.kenai.redminenb.util.Is;
import com.taskadapter.redmineapi.bean.User;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.team.spi.RepositoryUser;

/**
 * A Redmine {@link RepositoryUser repository user}.
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
public class RedmineUser extends RepositoryUser {

    private final User user;
    /**
     * true if this user is the current logged in user.
     */
    private final boolean isCurrentUser;

    public RedmineUser(User user) {
        this(user, false);
    }

    public RedmineUser(User user, boolean isCurrentUser) {
        super(user.getLogin(), user.getFullName());
        this.user = user;
        this.isCurrentUser = isCurrentUser;
    }

    public User getUser() {
        return user;
    }

    public boolean isIsCurrentUser() {
        return isCurrentUser;
    }

    public Integer getId() {
        return user.getId();
    }

    @Override
    public String toString() {
        return getFullName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RedmineUser)) {
            return false;
        }
        RedmineUser other = (RedmineUser) obj;
        return (this.user == null && other.user == null)
                || (this.user != null && other.user != null && Is.equals(this.user.getId(), other.user.getId()))
                || false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.user != null && this.user.getId() != null ? this.user.getId().hashCode() : 0);
        return hash;
    }

    public static RedmineUser fromIssue(com.taskadapter.redmineapi.bean.Issue issue) {
        return get(issue.getAssignee());
    }

    public static RedmineUser get(User user) {
        return user == null ? null : new RedmineUser(user);
    }

    public static Collection<RepositoryUser> convert(List<User> users) {
        Collection<RepositoryUser> convertedUsers = new LinkedList<>();
        for (User user : users) {
            convertedUsers.add(new RedmineUser(user));
        }
        return convertedUsers;
    }
}
