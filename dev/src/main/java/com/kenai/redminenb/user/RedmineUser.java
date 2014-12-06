package com.kenai.redminenb.user;

import com.taskadapter.redmineapi.bean.User;
import java.util.Objects;

/**
 * A Redmine {@link RepositoryUser repository user}.
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
public class RedmineUser {

    private final User user;
    /**
     * true if this user is the current logged in user.
     */
    private final boolean isCurrentUser;

    public RedmineUser(User user) {
        this(user, false);
    }

    public RedmineUser(User user, boolean isCurrentUser) {
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
        return user.getFullName();
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
                || (this.user != null && other.user != null && Objects.equals(this.user.getId(), other.user.getId()))
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
}
