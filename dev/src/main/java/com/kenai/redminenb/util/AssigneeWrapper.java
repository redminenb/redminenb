
package com.kenai.redminenb.util;

import com.kenai.redminenb.user.RedmineUser;
import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.Identifiable;
import com.taskadapter.redmineapi.bean.User;
import java.util.Objects;


public class AssigneeWrapper implements Identifiable, Comparable<AssigneeWrapper> {
    private final Integer id;
    private final String name;
    private final boolean isCurrentUser;
    private final boolean isGroup;

    public AssigneeWrapper(RedmineUser user) {
        this.id = user.getId();
        this.name = user.toString();
        this.isCurrentUser = false;
        this.isGroup = false;
    }
    
    public AssigneeWrapper(User user) {
        this.id = user.getId();
        this.name = user.getFullName();
        this.isCurrentUser = false;
        this.isGroup = false;
    }

    public AssigneeWrapper(Group group) {
        this.id = group.getId();
        this.name = group.getName() + " [Group]";
        this.isCurrentUser = false;
        this.isGroup = true;
    }
    
    public AssigneeWrapper(Integer id, String name) {
        this.id = id;
        this.name = name;
        this.isCurrentUser = false;
        this.isGroup = false;
    }
    
    public AssigneeWrapper(Integer id, String name, boolean isCurrentUser, boolean isGroup) {
        this.id = id;
        this.name = name;
        this.isCurrentUser = isCurrentUser;
        this.isGroup = isGroup;
    }
    
    @Override
    public Integer getId() {
        return id;
    }

    public String getName() {
        if(name == null) {
            return "ID: " + getId();
        }
        return name;
    }

    public boolean isIsCurrentUser() {
        return isCurrentUser;
    }
    
    @Override
    public int hashCode() {
        if(getId() == null) {
            return 0;
        } else {
            return getId();
        }
    }
    
    @Override
    public boolean equals(Object object) {
        if(object == null || (! (object instanceof Identifiable))) {
            return false;
        }
        return Objects.equals(getId(), ((Identifiable) object).getId());
    }

    @Override
    public int compareTo(AssigneeWrapper o) {
        if(isGroup != o.isGroup) {
            return isGroup ? 1 : -1;
        }
        return getName().compareToIgnoreCase(o.getName());
    }
}
