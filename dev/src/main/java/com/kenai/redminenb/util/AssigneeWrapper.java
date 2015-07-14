
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

    public AssigneeWrapper(RedmineUser ru) {
        this.id = ru.getUser().getId();
        this.name = ru.getUser().getFullName();
        this.isCurrentUser = ru.isIsCurrentUser();
        this.isGroup = false;
    }
    
    public AssigneeWrapper(Integer id, String name) {
        this.id = id;
        this.name = name;
        this.isCurrentUser = false;
        this.isGroup = false;
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
    
    public boolean equals(Object object) {
        if(object instanceof RedmineUser) {
            object = ((RedmineUser) object).getUser();
        }
        if(! (object instanceof User 
                || object instanceof Group 
                || object instanceof AssigneeWrapper)) {
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
