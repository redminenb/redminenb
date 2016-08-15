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

    private final int id;
    private final String username;
    
    /**
     * true if this user is the current logged in user.
     */
    private final boolean isCurrentUser;

    public RedmineUser(User user, boolean isCurrentUser) {
        this(user.getId(), user.getFullName(), isCurrentUser);
    }

    public RedmineUser(int id, String username) {
        this(id, username, false);
    }

    public RedmineUser(int id, String username, boolean isCurrentUser) {
        this.id = id;
        this.username = username;
        this.isCurrentUser = isCurrentUser;
    }

    public boolean isIsCurrentUser() {
        return isCurrentUser;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return username;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RedmineUser other = (RedmineUser) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    public static RedmineUser fromIssue(com.taskadapter.redmineapi.bean.Issue issue) {
        return new RedmineUser(issue.getAssigneeId(), issue.getAssigneeName());
    }
}
