package com.kenai.redmineNB.user;

import com.kenai.redmineNB.util.ListComboBoxModel;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.JComboBox;
import org.netbeans.modules.bugtracking.kenai.spi.RepositoryUser;


/**
 * {@link RepositoryUser} type-aware combo box.
 * 
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
public class UserComboBox extends JComboBox {

//   private List<RepositoryUser> users;

   public UserComboBox() {
      setModel(new ListComboBoxModel<RepositoryUser>());
   }

//   public List<RepositoryUser> getUsers() {
//      return users;
//   }

   public void addUser(RepositoryUser ... users) {
      ((ListComboBoxModel)getModel()).addAll(Arrays.asList(users));
   }
      
   public void setUsers(Collection<RepositoryUser> users) {
//      this.users = new ArrayList<RepositoryUser>(users);
//      revalidate();
      ((ListComboBoxModel)getModel()).clear();
      ((ListComboBoxModel)getModel()).addAll(users);
   }

   public RepositoryUser getSelectedUser() {
      return (RepositoryUser)super.getSelectedItem();
   }

   public void setSelectedUser(com.taskadapter.redmineapi.bean.User user) {
      super.setSelectedItem(user == null ? null : new RedmineUser(user));
   }

   public void setSelectedUser(RepositoryUser user) {
      super.setSelectedItem(user);
   }

//
//   private class UserComboBoxModel extends DefaultComboBoxModel {
//
//      @Override
//      public Object getElementAt(int element) {
//         if (users == null) {
//            return null;
//         }
//         return users.get(element);
//      }
//
//      @Override
//      public int getSize() {
//         if (users == null) {
//            return 0;
//         }
//         return users.size();
//      }
//
//   }

}
