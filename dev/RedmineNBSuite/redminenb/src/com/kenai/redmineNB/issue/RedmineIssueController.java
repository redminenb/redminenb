package com.kenai.redmineNB.issue;

import com.kenai.redmineNB.Redmine;
import com.kenai.redmineNB.ui.Defaults;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import javax.swing.*;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.util.UIUtils;
import org.openide.awt.HtmlBrowser;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
public class RedmineIssueController extends BugtrackingController {

   private final RedmineIssue issue;
   private JComponent component;
   private RedmineIssuePanel issuePanel;

   public RedmineIssueController(RedmineIssue issue) {
      this.issue = issue;
      issuePanel = new RedmineIssuePanel(issue);
      initActions();
      //issuePane.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

      JScrollPane scrollPane = new JScrollPane(issuePanel);
      scrollPane.setBorder(null);
      UIUtils.keepFocusedComponentVisible(scrollPane);

      JPanel pane = new JPanel(new BorderLayout());
      pane.setBorder(null);
      pane.setBackground(UIManager.getDefaults().getColor("EditorPane.background"));
//         if (!issue.isNew()) {
//            pane.add(issuePane.headPanel, BorderLayout.NORTH);
//         }
      pane.add(scrollPane, BorderLayout.CENTER);
      component = pane;
   }

   @Override
   public JComponent getComponent() {
      return component;
   }

   @Override
   public void opened() {
      if (issue != null) {
         issuePanel.opened();
         issue.opened();
      }
   }

   @Override
   public void closed() {
      if (issue != null) {
         issue.closed();
         issuePanel.closed();
      }
   }

   @Override
   public HelpCtx getHelpCtx() {
      return new HelpCtx(getClass().getName());
   }

   @Override
   public boolean isValid() {
      return true; // PENDING
   }

   @Override
   public void applyChanges() {
      System.out.println("applyChanges " + this);
   }

   @NbBundle.Messages({
      "CTL_ShowInBrowserAction=Show in Browser",
      "CTL_ActionListAction.add=Add to Action Items",
      "CTL_ActionListAction.remove=Remove from Action Items"
   })
   private void initActions() {

      Action showInBrowserAction = new AbstractAction(Bundle.CTL_ShowInBrowserAction(),
                                                      Defaults.getIcon("redmine.png")) {
         @Override
         public void actionPerformed(ActionEvent e) {
            try {
               URL url = new URL(issue.getRepository().getUrl() + "/issues/" + issue.getID()); // NOI18N
               HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            } catch (IOException ex) {
               Redmine.LOG.log(Level.INFO, "Unable to show the issue in the browser.", ex); // NOI18N
            }
         }

      };

      //issuePanel.setDefaultPopupAction(showInBrowserAction);
      issuePanel.addToolbarAction(showInBrowserAction, false);
      issuePanel.addToolbarAction(new ActionItemAction(), false);
   }


   private class ActionItemAction extends AbstractAction {

      private PropertyChangeListener actionItemListener;

      public ActionItemAction() {
         super("Action Item", Defaults.getIcon("hint.png"));
         putValue(Defaults.TOGGLE_BUTTON_KEY, Boolean.TRUE);
         updateTasklistButton();
      }

      private void setText(String txt) {
         putValue(Action.NAME, txt);
         putValue(Action.SHORT_DESCRIPTION, txt);
      }

      private void setSelected(boolean b) {
         putValue(Action.SELECTED_KEY, b);
      }

      @Override
      public void actionPerformed(ActionEvent e) {
         setEnabled(false);
         RedmineTaskListProvider provider = RedmineTaskListProvider.getInstance();
         if (provider.isAdded(issue)) {
            provider.remove(issue);
         } else {
            attachTasklistListener(provider);
            provider.add(issue, true);
         }
         updateTasklistButton();
      }

      private void attachTasklistListener(RedmineTaskListProvider provider) {
         if (actionItemListener == null) { // is not attached yet
            // listens on events comming from the tasklist, like when an issue is removed, etc.
            // needed to correctly update tasklistButton label and status
            actionItemListener = new PropertyChangeListener() {
               @Override
               public void propertyChange(PropertyChangeEvent evt) {
                  if (RedmineTaskListProvider.PROPERTY_ISSUE_REMOVED.equals(evt.getPropertyName()) && issue.equals(evt.getOldValue())) {
                     Runnable inAWT = new Runnable() {
                        @Override
                        public void run() {
                           updateTasklistButton();
                        }

                     };
                     if (EventQueue.isDispatchThread()) {
                        inAWT.run();
                     } else {
                        EventQueue.invokeLater(inAWT);
                     }
                  }
               }

            };
            provider.addPropertyChangeListener(org.openide.util.WeakListeners.propertyChange(actionItemListener, provider));
         }
      }

      private void updateTasklistButton() {
         setEnabled(false);
         RedmineIssuePanel.RP.post(new Runnable() {
            @Override
            public void run() {
               RedmineTaskListProvider provider = RedmineTaskListProvider.getInstance();
               if (provider == null || issue.isNew()) { // do not enable button for new issues
                  return;
               }
               final boolean isInTasklist = provider.isAdded(issue);
               if (isInTasklist) {
                  attachTasklistListener(provider);
               }
               EventQueue.invokeLater(new Runnable() {
                  @Override
                  public void run() {
                     ActionItemAction.this.setText(isInTasklist
                             ? Bundle.CTL_ActionListAction_remove()
                             : Bundle.CTL_ActionListAction_add());
                     setSelected(isInTasklist);
                     setEnabled(true);
                  }

               });
            }

         });
      }

   }

}
