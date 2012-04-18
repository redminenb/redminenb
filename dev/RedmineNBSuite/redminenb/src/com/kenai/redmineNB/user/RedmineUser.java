package com.kenai.redmineNB.user;

import com.kenai.redmineNB.util.Is;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.bugtracking.spi.RepositoryUser;
import org.redmine.ta.beans.User;


/**
 *
 * @author Mykolas
 */
public class RedmineUser extends RepositoryUser {

   private final User user;


   private RedmineUser(User user) {
      super(user.getLogin(), user.getFullName());
      this.user = user;
   }


   public User getUser() {
      return user;
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

   

   public static RepositoryUser getUser(User user) {
      return new RedmineUser(user);
   }


   public static Collection<RepositoryUser> getUsers(List<User> users) {
      Collection<RepositoryUser> convertedUsers = new LinkedList<RepositoryUser>();
      for (User user : users) {
         convertedUsers.add(new RedmineUser(user));
      }
      return convertedUsers;
   }
}
