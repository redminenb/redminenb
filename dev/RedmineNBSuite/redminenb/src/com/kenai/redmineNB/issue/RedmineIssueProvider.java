package com.kenai.redmineNB.issue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author Mykolas
 */
@ServiceProvider(service = IssueProvider.class)
public final class RedmineIssueProvider extends IssueProvider implements PropertyChangeListener {

   @Override
   public void removed(LazyIssue li) {
      throw new UnsupportedOperationException("Not supported yet.");
   }


   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      throw new UnsupportedOperationException("Not supported yet.");
   }
}
