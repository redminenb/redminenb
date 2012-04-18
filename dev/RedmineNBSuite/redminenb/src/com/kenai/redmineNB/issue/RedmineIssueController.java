package com.kenai.redmineNB.issue;

import java.awt.*;
import javax.swing.*;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.util.UIUtils;
import org.openide.util.HelpCtx;


/**
 *
 * @author Mykolas
 * @author Anchialas <anchialas@gmail.com>
 */
public class RedmineIssueController extends BugtrackingController {

   private final RedmineIssue issue;
   private JComponent component;
//    private RedmineIssuePanel issuePanel;


   public RedmineIssueController(RedmineIssue issue) {
      this.issue = issue;
   }


   @Override
   public JComponent getComponent() {
      if (component == null) {
         RedmineIssuePanel issuePane = new RedmineIssuePanel(issue);
         //issuePane.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

         JScrollPane scrollPane = new JScrollPane(issuePane);
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
      return component;
   }


   @Override
   public HelpCtx getHelpCtx() {
      return new HelpCtx(RedmineIssueController.class);
   }


   @Override
   public boolean isValid() {
      return true; // PENDING
   }


   @Override
   public void applyChanges() {
      System.out.println("applyChanges " + this);
   }

}
