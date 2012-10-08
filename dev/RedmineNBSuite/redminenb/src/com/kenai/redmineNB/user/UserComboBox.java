package com.kenai.redmineNB.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.netbeans.modules.bugtracking.kenai.spi.RepositoryUser;


/**
 * {@link RepositoryUser} type-aware combo box.
 * 
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
public class UserComboBox extends JComboBox {

   private List<RepositoryUser> users;

   public UserComboBox() {
      setModel(new UserComboBoxModel());
   }

   public List<RepositoryUser> getUsers() {
      return users;
   }

   public void setUsers(Collection<RepositoryUser> users) {
      this.users = new ArrayList<RepositoryUser>(users);
      revalidate();
   }

   public RepositoryUser getSelectedUser() {
      return (RepositoryUser)super.getSelectedItem();
   }

   public void setSelectedUser(org.redmine.ta.beans.User user) {
      super.setSelectedItem(user == null ? null : RedmineUser.getUser(user));
   }

   public void setSelectedUser(RepositoryUser user) {
      super.setSelectedItem(user);
   }


   private class UserComboBoxModel extends DefaultComboBoxModel {

      @Override
      public Object getElementAt(int element) {
         if (users == null) {
            return null;
         }
         return users.get(element);
      }

      @Override
      public int getSize() {
         if (users == null) {
            return 0;
         }
         return users.size();
      }

   }

}
