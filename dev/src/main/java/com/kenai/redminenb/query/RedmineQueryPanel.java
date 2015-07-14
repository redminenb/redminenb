/*
 * This class has been copied from [bugzilla]/org.netbeans.modules.bugzilla.query.QueryPanel
 * @author Tomas Stupka, Jan Stola
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package com.kenai.redminenb.query;

import com.kenai.redminenb.ui.Defaults;
import com.kenai.redminenb.util.ExpandablePanel;
import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.kenai.redminenb.util.LinkButton;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 * Redmine Query Panel.
 *
 * @author Anchialas <anchialas@gmail.com>
 */
public class RedmineQueryPanel extends JPanel implements FocusListener {

   private static final long serialVersionUID = 8364291628284121318L;
   //
   final ExpandablePanel byText;
   final ExpandablePanel byDetails;
   final ExpandablePanel bySavedQuery;
   //
   private final Color defaultTextColor;
   
   // Handle list selection for list containing "NONE" value - selecting none
   // leeds to unselecting all other entries
   private static final ListSelectionListener listNoneHandler = new ListSelectionListener() {

          @Override
          public void valueChanged(ListSelectionEvent e) {
              if(! e.getValueIsAdjusting()) {
                  JList list = (JList) e.getSource();
                  boolean noneSelected = false;
                  boolean otherSelected = false;
                  for(Object o: list.getSelectedValues()) {
                      if(o instanceof ParameterValue) {
                          if(ParameterValue.NONE_PARAMETERVALUE.equals(o)) {
                              noneSelected = true;
                              continue;
                          }
                      }
                      otherSelected = true;
                  }
                  if(noneSelected && otherSelected) {
                      list.setSelectedIndex(0);
                  }
              }
          }
      };
   
   private final ActionListener clearActionListener = new ActionListener() {

       @Override
       public void actionPerformed(ActionEvent e) {
           switch (e.getActionCommand()) {
               case "tracker":
                   trackerList.clearSelection();
                   break;
               case "category":
                   categoryList.clearSelection();
                   break;
               case "version":
                   versionList.clearSelection();
                   break;
               case "status":
                   statusList.clearSelection();
                   break;
               case "priority":
                   priorityList.clearSelection();
                   break;
               case "assignee":
                   assigneeList.clearSelection();
                   break;
               case "watcher":
                   watcherList.clearSelection();
                   break;
               case "project":
                   projectList.clearSelection();
                   break;
               default:
                   assert false : "Unhandled actionCommand: " + e.getActionCommand();
                   break;
           }
       }
   };
   
   public RedmineQueryPanel(JComponent tableComponent, RedmineQueryController controller) {
      super();
      initComponents();

      Font f = new JLabel().getFont();
      int s = f.getSize();
      nameLabel.setFont(nameLabel.getFont().deriveFont(s * 1.7f));
      defaultTextColor = noContentLabel.getForeground();

      tablePanel.add(tableComponent);

      byTextContainer.add(byTextPanel);
      byDetailsContainer.add(byDetailsPanel);
      bySavedQueryContainer.add(bySavedQueryPanel);

      byText = new ExpandablePanel(byTextLabel, byTextContainer);
      byDetails = new ExpandablePanel(byDetailsLabel, byDetailsContainer);
      bySavedQuery = new ExpandablePanel(bySavedQueryLabel, bySavedQueryContainer);

      tableFieldsPanel.setVisible(false);
      saveChangesButton.setVisible(false);
      cancelChangesButton.setVisible(false);
      refreshCheckBox.setVisible(false);
      noContentPanel.setVisible(false);

      refreshCheckBox.setOpaque(false);

      ListCellRenderer parameterValueLCR = new Defaults.ParameterValueLCR();
      
      trackerList.setCellRenderer(parameterValueLCR);
      trackerClear.addActionListener(clearActionListener);
      categoryList.setCellRenderer(parameterValueLCR);
      categoryClear.addActionListener(clearActionListener);
      versionList.setCellRenderer(parameterValueLCR);
      versionClear.addActionListener(clearActionListener);
      statusList.setCellRenderer(parameterValueLCR);
      statusClear.addActionListener(clearActionListener);
      priorityList.setCellRenderer(new Defaults.PriorityLCR());
      priorityClear.addActionListener(clearActionListener);
      assigneeList.setCellRenderer(new Defaults.RepositoryUserLCR());
      assigneeClear.addActionListener(clearActionListener);
      watcherList.setCellRenderer(new Defaults.RepositoryUserLCR());
      watcherClear.addActionListener(clearActionListener);
      projectList.setCellRenderer(new Defaults.ProjectLCR());
      // redmine API fails to query multiple projects
      projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      projectClear.addActionListener(clearActionListener);

      setFocusListener(this);
      
      categoryList.addListSelectionListener(listNoneHandler);
      priorityList.addListSelectionListener(listNoneHandler);
      trackerList.addListSelectionListener(listNoneHandler);
      statusList.addListSelectionListener(listNoneHandler);
      versionList.addListSelectionListener(listNoneHandler);
      assigneeList.addListSelectionListener(listNoneHandler);
      
      queryTypeCombo.setSelectedIndex(0);
      updateQueryType();
      queryTypeCombo.addActionListener(new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent e) {
              updateQueryType();
          }
      });
      
      validate();
      repaint();
   }

   private void updateQueryType() {
       if(queryTypeCombo.getSelectedIndex() == 1) {
           byText.colapse();
           byDetails.colapse();
           bySavedQuery.expand();
           byText.setVisible(false);
           byDetails.setVisible(false);
           bySavedQuery.setVisible(true);
       } else {
           byText.expand();
           byDetails.expand();
           bySavedQuery.colapse();
           byText.setVisible(true);
           byDetails.setVisible(true);
           bySavedQuery.setVisible(false);
       }
   }
   
   private void setFocusListener(FocusListener f) {
      cancelChangesButton.addFocusListener(f);

      categoryList.addFocusListener(f);
      priorityList.addFocusListener(f);
      trackerList.addFocusListener(f);
      statusList.addFocusListener(f);
      versionList.addFocusListener(f);
      assigneeList.addFocusListener(f);

      gotoIssueButton.addFocusListener(f);
      modifyButton.addFocusListener(f);
      refreshCheckBox.addFocusListener(f);
      removeButton.addFocusListener(f);
      saveButton.addFocusListener(f);
      saveChangesButton.addFocusListener(f);
      searchButton.addFocusListener(f);
      refreshButton.addFocusListener(f);
      webButton.addFocusListener(f);

      issueIdTextField.addFocusListener(f);
      queryTextField.addFocusListener(f);
      tablePanel.addFocusListener(f);
      tableSummaryLabel.addFocusListener(f);

      refreshConfigurationButton.addFocusListener(this);
   }

   void setQueryRunning(boolean running) {
      modifyButton.setEnabled(!running);
      removeButton.setEnabled(!running);
      refreshButton.setEnabled(!running);
   }

   /**
    * This method is called from within the constructor to initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is always
    * regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        trackerClear = new LinkButton();
        statusClear = new LinkButton();
        priorityClear = new LinkButton();
        assigneeClear = new LinkButton();
        categoryClear = new LinkButton();
        versionClear = new LinkButton();
        filler1 = new Box.Filler(new Dimension(0, 0), new Dimension(0, 0), new Dimension(0, 0));
        watcherClear = new LinkButton();
        projectClear = new LinkButton();
        byTextPanel = new JPanel();
        qSubjectCheckBox = new JCheckBox();
        qDescriptionCheckBox = new JCheckBox();
        bySavedQueryPanel = new JPanel();
        bySaveQueryProjectLabel = new JLabel();
        bySaveQueryQueryLabel = new JLabel();
        bySaveQueryProjectCB = new JComboBox();
        bySaveQueryQueryCB = new JComboBox();
        innerPanel = new JPanel();
        tableFieldsPanel = new JPanel();
        tableHeaderPanel = new JPanel();
        criteriaPanel = new JPanel();
        queryTypeLabel = new JLabel();
        queryTypeCombo = new JComboBox();
        issueIdTextField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        separatorLabel2 = new JLabel();
        separatorLabel1 = new JLabel();
        topButtonPanel = new JPanel();
        jLabel5 = new JLabel();
        jLabel6 = new JLabel();
        queryHeaderPanel = new JPanel();
        lastRefreshLabel = new JLabel();
        noContentPanel = new JPanel();
        noContentLabel = new JLabel();

        byDetailsPanel.setBackground(UIManager.getDefaults().getColor("TextArea.background"));
        byDetailsPanel.setLayout(new GridBagLayout());

        versionLabel.setFont(versionLabel.getFont().deriveFont(versionLabel.getFont().getStyle() | Font.BOLD, versionLabel.getFont().getSize()-2));
        versionLabel.setLabelFor(versionList);
        Mnemonics.setLocalizedText(versionLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.versionLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 2, 5);
        byDetailsPanel.add(versionLabel, gridBagConstraints);

        jScrollPane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setMinimumSize(new Dimension(100, 120));
        jScrollPane2.setPreferredSize(new Dimension(100, 120));

        versionList.setModel(new StringListModel());
        versionList.setVisibleRowCount(6);
        jScrollPane2.setViewportView(versionList);
        versionList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.versionList.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        byDetailsPanel.add(jScrollPane2, gridBagConstraints);

        statusLabel.setFont(statusLabel.getFont().deriveFont(statusLabel.getFont().getStyle() | Font.BOLD, statusLabel.getFont().getSize()-2));
        statusLabel.setLabelFor(statusList);
        Mnemonics.setLocalizedText(statusLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.statusLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 2, 5);
        byDetailsPanel.add(statusLabel, gridBagConstraints);

        jScrollPane3.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane3.setMinimumSize(new Dimension(100, 120));
        jScrollPane3.setPreferredSize(new Dimension(100, 120));

        statusList.setModel(new StringListModel());
        statusList.setVisibleRowCount(6);
        jScrollPane3.setViewportView(statusList);
        statusList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.statusList.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        byDetailsPanel.add(jScrollPane3, gridBagConstraints);

        priorityLabel.setFont(priorityLabel.getFont().deriveFont(priorityLabel.getFont().getStyle() | Font.BOLD, priorityLabel.getFont().getSize()-2));
        priorityLabel.setLabelFor(priorityList);
        Mnemonics.setLocalizedText(priorityLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.priorityLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 2, 5);
        byDetailsPanel.add(priorityLabel, gridBagConstraints);

        jScrollPane4.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane4.setMinimumSize(new Dimension(100, 120));
        jScrollPane4.setPreferredSize(new Dimension(100, 120));

        priorityList.setModel(new StringListModel());
        priorityList.setVisibleRowCount(6);
        jScrollPane4.setViewportView(priorityList);
        priorityList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.priorityList.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        byDetailsPanel.add(jScrollPane4, gridBagConstraints);

        categoryLabel.setFont(categoryLabel.getFont().deriveFont(categoryLabel.getFont().getStyle() | Font.BOLD, categoryLabel.getFont().getSize()-2));
        categoryLabel.setLabelFor(categoryList);
        Mnemonics.setLocalizedText(categoryLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.categoryLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 2, 5);
        byDetailsPanel.add(categoryLabel, gridBagConstraints);

        jScrollPane6.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane6.setMinimumSize(new Dimension(100, 120));
        jScrollPane6.setPreferredSize(new Dimension(100, 120));

        categoryList.setModel(new StringListModel());
        categoryList.setVisibleRowCount(6);
        jScrollPane6.setViewportView(categoryList);
        categoryList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.componentList.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        byDetailsPanel.add(jScrollPane6, gridBagConstraints);

        trackerLabel.setFont(trackerLabel.getFont().deriveFont(trackerLabel.getFont().getStyle() | Font.BOLD, trackerLabel.getFont().getSize()-2));
        trackerLabel.setLabelFor(trackerList);
        Mnemonics.setLocalizedText(trackerLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.trackerLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(5, 5, 2, 5);
        byDetailsPanel.add(trackerLabel, gridBagConstraints);

        issueTypeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        issueTypeScrollPane.setMinimumSize(new Dimension(100, 120));
        issueTypeScrollPane.setName(""); // NOI18N
        issueTypeScrollPane.setPreferredSize(new Dimension(100, 120));

        trackerList.setModel(new StringListModel());
        trackerList.setVisibleRowCount(6);
        issueTypeScrollPane.setViewportView(trackerList);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        byDetailsPanel.add(issueTypeScrollPane, gridBagConstraints);

        assigneeLabel.setFont(assigneeLabel.getFont().deriveFont(assigneeLabel.getFont().getStyle() | Font.BOLD, assigneeLabel.getFont().getSize()-2));
        assigneeLabel.setLabelFor(assigneeList);
        Mnemonics.setLocalizedText(assigneeLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.assigneeLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 2, 5);
        byDetailsPanel.add(assigneeLabel, gridBagConstraints);

        assigneeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        assigneeScrollPane.setMinimumSize(new Dimension(100, 120));
        assigneeScrollPane.setPreferredSize(new Dimension(100, 120));

        assigneeList.setModel(new StringListModel());
        assigneeList.setVisibleRowCount(6);
        assigneeScrollPane.setViewportView(assigneeList);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        byDetailsPanel.add(assigneeScrollPane, gridBagConstraints);

        trackerClear.setBorder(null);
        Mnemonics.setLocalizedText(trackerClear, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.trackerClear.text")); // NOI18N
        trackerClear.setActionCommand(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.trackerClear.actionCommand")); // NOI18N
        trackerClear.setFont(trackerClear.getFont().deriveFont(trackerClear.getFont().getStyle() & ~Font.BOLD, trackerClear.getFont().getSize()-2));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(2, 5, 5, 5);
        byDetailsPanel.add(trackerClear, gridBagConstraints);

        statusClear.setBorder(null);
        Mnemonics.setLocalizedText(statusClear, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.statusClear.text")); // NOI18N
        statusClear.setActionCommand(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.statusClear.actionCommand")); // NOI18N
        statusClear.setFont(statusClear.getFont().deriveFont(statusClear.getFont().getStyle() & ~Font.BOLD, statusClear.getFont().getSize()-2));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(2, 5, 5, 5);
        byDetailsPanel.add(statusClear, gridBagConstraints);

        priorityClear.setBorder(null);
        Mnemonics.setLocalizedText(priorityClear, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.priorityClear.text")); // NOI18N
        priorityClear.setActionCommand(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.priorityClear.actionCommand")); // NOI18N
        priorityClear.setFont(priorityClear.getFont().deriveFont(priorityClear.getFont().getStyle() & ~Font.BOLD, priorityClear.getFont().getSize()-2));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(2, 5, 5, 5);
        byDetailsPanel.add(priorityClear, gridBagConstraints);

        assigneeClear.setBorder(null);
        Mnemonics.setLocalizedText(assigneeClear, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.assigneeClear.text")); // NOI18N
        assigneeClear.setActionCommand(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.assigneeClear.actionCommand")); // NOI18N
        assigneeClear.setFont(assigneeClear.getFont().deriveFont(assigneeClear.getFont().getStyle() & ~Font.BOLD, assigneeClear.getFont().getSize()-2));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(2, 5, 5, 5);
        byDetailsPanel.add(assigneeClear, gridBagConstraints);

        categoryClear.setBorder(null);
        Mnemonics.setLocalizedText(categoryClear, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.categoryClear.text")); // NOI18N
        categoryClear.setActionCommand(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.categoryClear.actionCommand")); // NOI18N
        categoryClear.setFont(categoryClear.getFont().deriveFont(categoryClear.getFont().getStyle() & ~Font.BOLD, categoryClear.getFont().getSize()-2));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(2, 5, 5, 5);
        byDetailsPanel.add(categoryClear, gridBagConstraints);

        versionClear.setBorder(null);
        Mnemonics.setLocalizedText(versionClear, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.versionClear.text")); // NOI18N
        versionClear.setActionCommand(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.versionClear.actionCommand")); // NOI18N
        versionClear.setFont(versionClear.getFont().deriveFont(versionClear.getFont().getStyle() & ~Font.BOLD, versionClear.getFont().getSize()-2));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(2, 5, 5, 5);
        byDetailsPanel.add(versionClear, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
        byDetailsPanel.add(filler1, gridBagConstraints);

        jScrollPane5.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane5.setMinimumSize(new Dimension(100, 120));
        jScrollPane5.setPreferredSize(new Dimension(100, 120));

        watcherList.setModel(new StringListModel());
        watcherList.setVisibleRowCount(6);
        jScrollPane5.setViewportView(watcherList);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        byDetailsPanel.add(jScrollPane5, gridBagConstraints);

        watcherLabel.setFont(watcherLabel.getFont().deriveFont(watcherLabel.getFont().getStyle() | Font.BOLD, watcherLabel.getFont().getSize()-2));
        watcherLabel.setLabelFor(watcherList);
        Mnemonics.setLocalizedText(watcherLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.watcherLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 2, 5);
        byDetailsPanel.add(watcherLabel, gridBagConstraints);

        watcherClear.setBorder(null);
        Mnemonics.setLocalizedText(watcherClear, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.watcherClear.text")); // NOI18N
        watcherClear.setActionCommand(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.watcherClear.actionCommand")); // NOI18N
        watcherClear.setFont(watcherClear.getFont().deriveFont(watcherClear.getFont().getStyle() & ~Font.BOLD, watcherClear.getFont().getSize()-2));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(2, 5, 5, 5);
        byDetailsPanel.add(watcherClear, gridBagConstraints);

        jScrollPane7.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane7.setMinimumSize(new Dimension(100, 120));
        jScrollPane7.setPreferredSize(new Dimension(100, 120));

        projectList.setModel(new StringListModel());
        projectList.setVisibleRowCount(6);
        jScrollPane7.setViewportView(projectList);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 5, 0, 5);
        byDetailsPanel.add(jScrollPane7, gridBagConstraints);

        projectLabel.setFont(projectLabel.getFont().deriveFont(projectLabel.getFont().getStyle() | Font.BOLD, projectLabel.getFont().getSize()-2));
        projectLabel.setLabelFor(watcherList);
        Mnemonics.setLocalizedText(projectLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.projectLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(5, 5, 2, 5);
        byDetailsPanel.add(projectLabel, gridBagConstraints);

        projectClear.setBorder(null);
        Mnemonics.setLocalizedText(projectClear, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.projectClear.text")); // NOI18N
        projectClear.setActionCommand(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.projectClear.actionCommand")); // NOI18N
        projectClear.setFont(projectClear.getFont().deriveFont(projectClear.getFont().getStyle() & ~Font.BOLD, projectClear.getFont().getSize()-2));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(2, 5, 5, 5);
        byDetailsPanel.add(projectClear, gridBagConstraints);

        byTextPanel.setBackground(UIManager.getDefaults().getColor("TextArea.background"));
        byTextPanel.setLayout(new GridBagLayout());

        queryLabel.setFont(queryLabel.getFont().deriveFont(queryLabel.getFont().getStyle() | Font.BOLD, queryLabel.getFont().getSize()-2));
        queryLabel.setLabelFor(queryTextField);
        Mnemonics.setLocalizedText(queryLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.queryLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(12, 12, 12, 5);
        byTextPanel.add(queryLabel, gridBagConstraints);

        queryTextField.setColumns(30);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(12, 5, 12, 5);
        byTextPanel.add(queryTextField, gridBagConstraints);
        queryTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.summaryTextField.AccessibleContext.accessibleName")); // NOI18N
        queryTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.summaryTextField.AccessibleContext.accessibleDescription")); // NOI18N

        qSubjectCheckBox.setSelected(true);
        Mnemonics.setLocalizedText(qSubjectCheckBox, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.qSubjectCheckBox.text")); // NOI18N
        qSubjectCheckBox.setOpaque(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(12, 5, 12, 5);
        byTextPanel.add(qSubjectCheckBox, gridBagConstraints);

        Mnemonics.setLocalizedText(qDescriptionCheckBox, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.qDescriptionCheckBox.text")); // NOI18N
        qDescriptionCheckBox.setOpaque(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(12, 5, 12, 12);
        byTextPanel.add(qDescriptionCheckBox, gridBagConstraints);

        bySavedQueryPanel.setBackground(UIManager.getDefaults().getColor("TextArea.background"));
        bySavedQueryPanel.setLayout(new GridBagLayout());

        Mnemonics.setLocalizedText(bySaveQueryProjectLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.bySaveQueryProjectLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(3, 3, 3, 3);
        bySavedQueryPanel.add(bySaveQueryProjectLabel, gridBagConstraints);

        Mnemonics.setLocalizedText(bySaveQueryQueryLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.bySaveQueryQueryLabel.text_1")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(3, 3, 3, 3);
        bySavedQueryPanel.add(bySaveQueryQueryLabel, gridBagConstraints);

        bySaveQueryProjectCB.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(3, 3, 3, 3);
        bySavedQueryPanel.add(bySaveQueryProjectCB, gridBagConstraints);

        bySaveQueryQueryCB.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(3, 3, 3, 3);
        bySavedQueryPanel.add(bySaveQueryQueryCB, gridBagConstraints);

        setLayout(new BorderLayout());

        tableFieldsPanel.setBackground(UIManager.getDefaults().getColor("EditorPane.background"));
        tableFieldsPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        tableFieldsPanel.setOpaque(false);
        tableFieldsPanel.setLayout(new BorderLayout());

        tableHeaderPanel.setBackground(UIManager.getDefaults().getColor("EditorPane.background"));
        tableHeaderPanel.setOpaque(false);
        tableHeaderPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        tableSummaryLabel.setFont(tableSummaryLabel.getFont().deriveFont(tableSummaryLabel.getFont().getStyle() | Font.BOLD, tableSummaryLabel.getFont().getSize()-2));
        tableSummaryLabel.setIcon(new ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/redmine.png"))); // NOI18N
        Mnemonics.setLocalizedText(tableSummaryLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.tableSummaryLabel.text")); // NOI18N
        tableHeaderPanel.add(tableSummaryLabel);

        tableFieldsPanel.add(tableHeaderPanel, BorderLayout.NORTH);

        tablePanel.setBackground(new Color(224, 224, 224));
        tablePanel.setBorder(BorderFactory.createEtchedBorder());
        tablePanel.setMinimumSize(new Dimension(100, 350));
        tablePanel.setLayout(new BorderLayout());
        tableFieldsPanel.add(tablePanel, BorderLayout.CENTER);

        searchPanel.setBackground(UIManager.getDefaults().getColor("EditorPane.background"));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        searchPanel.setOpaque(false);

        criteriaPanel.setBorder(BorderFactory.createLineBorder(UIManager.getDefaults().getColor("Button.shadow")));
        criteriaPanel.setLayout(new GridBagLayout());

        Mnemonics.setLocalizedText(queryTypeLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.queryTypeLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(3, 3, 3, 3);
        criteriaPanel.add(queryTypeLabel, gridBagConstraints);

        queryTypeCombo.setModel(new DefaultComboBoxModel(new String[] { "Custom query", "Serverside saved query" }));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(3, 3, 3, 3);
        criteriaPanel.add(queryTypeCombo, gridBagConstraints);

        byTextLabel.setFont(byTextLabel.getFont().deriveFont(byTextLabel.getFont().getStyle() | Font.BOLD));
        Mnemonics.setLocalizedText(byTextLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.byTextLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(3, 3, 3, 3);
        criteriaPanel.add(byTextLabel, gridBagConstraints);
        byTextLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.byTextLabel.AccessibleContext.accessibleName")); // NOI18N

        byTextContainer.setLayout(new BorderLayout());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(0, 1, 0, 1);
        criteriaPanel.add(byTextContainer, gridBagConstraints);

        byDetailsLabel.setFont(byDetailsLabel.getFont().deriveFont(byDetailsLabel.getFont().getStyle() | Font.BOLD));
        Mnemonics.setLocalizedText(byDetailsLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.byDetailsLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(3, 3, 3, 3);
        criteriaPanel.add(byDetailsLabel, gridBagConstraints);

        byDetailsContainer.setLayout(new BorderLayout());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(0, 1, 1, 1);
        criteriaPanel.add(byDetailsContainer, gridBagConstraints);

        bySavedQueryLabel.setFont(bySavedQueryLabel.getFont().deriveFont(bySavedQueryLabel.getFont().getStyle() | Font.BOLD));
        Mnemonics.setLocalizedText(bySavedQueryLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.bySavedQueryLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(3, 3, 3, 3);
        criteriaPanel.add(bySavedQueryLabel, gridBagConstraints);

        bySavedQueryContainer.setLayout(new BorderLayout());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new Insets(0, 1, 1, 1);
        criteriaPanel.add(bySavedQueryContainer, gridBagConstraints);

        Mnemonics.setLocalizedText(cancelChangesButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.cancelChangesButton.text")); // NOI18N

        Mnemonics.setLocalizedText(saveChangesButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.saveChangesButton.text")); // NOI18N
        saveChangesButton.setSelected(true);

        gotoPanel.setBackground(new Color(224, 224, 224));
        gotoPanel.setOpaque(false);
        gotoPanel.setLayout(new GridBagLayout());

        Mnemonics.setLocalizedText(gotoIssueButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.gotoIssueButton.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        gotoPanel.add(gotoIssueButton, gridBagConstraints);
        gotoIssueButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.gotoIssueButton.AccessibleContext.accessibleDescription")); // NOI18N

        issueIdTextField.setHorizontalAlignment(JTextField.RIGHT);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 64;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        gotoPanel.add(issueIdTextField, gridBagConstraints);

        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.searchButton.text")); // NOI18N
        searchButton.setSelected(true);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(5, 0, 5, 5);
        gotoPanel.add(searchButton, gridBagConstraints);
        searchButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.searchButton.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(separatorLabel2, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.separatorLabel2.text")); // NOI18N
        separatorLabel2.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipady = 20;
        gotoPanel.add(separatorLabel2, gridBagConstraints);

        Mnemonics.setLocalizedText(separatorLabel1, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.separatorLabel1.text")); // NOI18N
        separatorLabel1.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipady = 20;
        gotoPanel.add(separatorLabel1, gridBagConstraints);

        saveButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        Mnemonics.setLocalizedText(saveButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.saveButton.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gotoPanel.add(saveButton, gridBagConstraints);
        saveButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.saveButton.AccessibleContext.accessibleDescription")); // NOI18N

        webButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        Mnemonics.setLocalizedText(webButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.webButton.text")); // NOI18N
        webButton.setActionCommand(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.webButton.actionCommand")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gotoPanel.add(webButton, gridBagConstraints);
        webButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.webButton.AccessibleContext.accessibleDescription")); // NOI18N

        refreshConfigurationButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        Mnemonics.setLocalizedText(refreshConfigurationButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.refreshConfigurationButton.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        gotoPanel.add(refreshConfigurationButton, gridBagConstraints);
        refreshConfigurationButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.refreshConfigurationButton.AccessibleContext.accessibleDescription")); // NOI18N

        GroupLayout searchPanelLayout = new GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(searchPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(gotoPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addComponent(saveChangesButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelChangesButton)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(criteriaPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        searchPanelLayout.setVerticalGroup(searchPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addComponent(gotoPanel, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(criteriaPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(saveChangesButton)
                    .addComponent(cancelChangesButton))
                .addGap(0, 0, 0))
        );

        cancelChangesButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.cancelChangesButton.AccessibleContext.accessibleDescription")); // NOI18N
        saveChangesButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.saveChangesButton.AccessibleContext.accessibleDescription")); // NOI18N

        topButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        topButtonPanel.setOpaque(false);
        topButtonPanel.setLayout(new GridBagLayout());

        modifyButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        Mnemonics.setLocalizedText(modifyButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.modifyButton.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        topButtonPanel.add(modifyButton, gridBagConstraints);
        modifyButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.modifyButton.AccessibleContext.accessibleDescription")); // NOI18N

        removeButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        Mnemonics.setLocalizedText(removeButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.removeButton.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        topButtonPanel.add(removeButton, gridBagConstraints);
        removeButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.removeButton.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(jLabel5, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.jLabel5.text")); // NOI18N
        jLabel5.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipady = 20;
        topButtonPanel.add(jLabel5, gridBagConstraints);

        refreshButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        Mnemonics.setLocalizedText(refreshButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.refreshButton.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        topButtonPanel.add(refreshButton, gridBagConstraints);
        refreshButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.seenButton.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(jLabel6, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.jLabel6.text")); // NOI18N
        jLabel6.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipady = 20;
        topButtonPanel.add(jLabel6, gridBagConstraints);

        queryHeaderPanel.setBackground(UIManager.getDefaults().getColor("EditorPane.background"));
        queryHeaderPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        queryHeaderPanel.setOpaque(false);

        Mnemonics.setLocalizedText(lastRefreshLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.lastRefreshLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(lastRefreshDateLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.lastRefreshDateLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(nameLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.nameLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(refreshCheckBox, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.refreshCheckBox.text")); // NOI18N
        refreshCheckBox.setOpaque(false);

        GroupLayout queryHeaderPanelLayout = new GroupLayout(queryHeaderPanel);
        queryHeaderPanel.setLayout(queryHeaderPanelLayout);
        queryHeaderPanelLayout.setHorizontalGroup(queryHeaderPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(nameLabel)
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(refreshCheckBox)
                .addGap(18, 18, 18)
                .addComponent(lastRefreshLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lastRefreshDateLabel)
                .addGap(0, 0, 0))
        );
        queryHeaderPanelLayout.setVerticalGroup(queryHeaderPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(nameLabel)
            .addGroup(queryHeaderPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(lastRefreshDateLabel)
                .addComponent(lastRefreshLabel)
                .addComponent(refreshCheckBox))
        );

        refreshCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.refreshCheckBox.AccessibleContext.accessibleDescription")); // NOI18N

        noContentPanel.setBackground(UIManager.getDefaults().getColor("EditorPane.background"));
        noContentPanel.setOpaque(false);
        noContentPanel.setLayout(new GridBagLayout());

        Mnemonics.setLocalizedText(noContentLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.noContentLabel.text")); // NOI18N
        noContentPanel.add(noContentLabel, new GridBagConstraints());

        GroupLayout innerPanelLayout = new GroupLayout(innerPanel);
        innerPanel.setLayout(innerPanelLayout);
        innerPanelLayout.setHorizontalGroup(innerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(innerPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(innerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(queryHeaderPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(searchPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tableFieldsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(innerPanelLayout.createSequentialGroup()
                        .addComponent(topButtonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(noContentPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
        innerPanelLayout.setVerticalGroup(innerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(innerPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(queryHeaderPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(topButtonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableFieldsPanel, GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noContentPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        add(innerPanel, BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    LinkButton assigneeClear;
    final JLabel assigneeLabel = new JLabel();
    final JList assigneeList = new JList();
    final JScrollPane assigneeScrollPane = new HackedScrollPane();
    final JPanel byDetailsContainer = new JPanel();
    final JLabel byDetailsLabel = new JLabel();
    final JPanel byDetailsPanel = new JPanel();
    JComboBox bySaveQueryProjectCB;
    JLabel bySaveQueryProjectLabel;
    JComboBox bySaveQueryQueryCB;
    JLabel bySaveQueryQueryLabel;
    final JPanel bySavedQueryContainer = new JPanel();
    final JLabel bySavedQueryLabel = new JLabel();
    JPanel bySavedQueryPanel;
    final JPanel byTextContainer = new JPanel();
    final JLabel byTextLabel = new JLabel();
    JPanel byTextPanel;
    final JButton cancelChangesButton = new JButton();
    LinkButton categoryClear;
    final JLabel categoryLabel = new JLabel();
    final JList categoryList = new JList();
    JPanel criteriaPanel;
    Box.Filler filler1;
    final JButton gotoIssueButton = new JButton();
    final JPanel gotoPanel = new JPanel();
    JPanel innerPanel;
    JFormattedTextField issueIdTextField;
    final JScrollPane issueTypeScrollPane = new HackedScrollPane();
    JLabel jLabel5;
    JLabel jLabel6;
    final JScrollPane jScrollPane2 = new HackedScrollPane();
    final JScrollPane jScrollPane3 = new HackedScrollPane();
    final JScrollPane jScrollPane4 = new HackedScrollPane();
    final JScrollPane jScrollPane5 = new HackedScrollPane();
    final JScrollPane jScrollPane6 = new HackedScrollPane();
    final JScrollPane jScrollPane7 = new HackedScrollPane();
    final JLabel lastRefreshDateLabel = new JLabel();
    JLabel lastRefreshLabel;
    public final LinkButton modifyButton = new LinkButton();
    final JLabel nameLabel = new JLabel();
    JLabel noContentLabel;
    JPanel noContentPanel;
    LinkButton priorityClear;
    final JLabel priorityLabel = new JLabel();
    final JList priorityList = new JList();
    LinkButton projectClear;
    final JLabel projectLabel = new JLabel();
    final JList projectList = new JList();
    JCheckBox qDescriptionCheckBox;
    JCheckBox qSubjectCheckBox;
    JPanel queryHeaderPanel;
    final JLabel queryLabel = new JLabel();
    final JTextField queryTextField = new JTextField();
    JComboBox queryTypeCombo;
    JLabel queryTypeLabel;
    final LinkButton refreshButton = new LinkButton();
    final JCheckBox refreshCheckBox = new JCheckBox();
    final LinkButton refreshConfigurationButton = new LinkButton();
    public final LinkButton removeButton = new LinkButton();
    final LinkButton saveButton = new LinkButton();
    final JButton saveChangesButton = new JButton();
    final JButton searchButton = new JButton();
    final JPanel searchPanel = new JPanel();
    JLabel separatorLabel1;
    JLabel separatorLabel2;
    LinkButton statusClear;
    final JLabel statusLabel = new JLabel();
    final JList statusList = new JList();
    JPanel tableFieldsPanel;
    JPanel tableHeaderPanel;
    final JPanel tablePanel = new JPanel();
    final JLabel tableSummaryLabel = new JLabel();
    JPanel topButtonPanel;
    LinkButton trackerClear;
    final JLabel trackerLabel = new JLabel();
    final JList trackerList = new JList();
    LinkButton versionClear;
    final JLabel versionLabel = new JLabel();
    final JList versionList = new JList();
    LinkButton watcherClear;
    final JLabel watcherLabel = new JLabel();
    final JList watcherList = new JList();
    final LinkButton webButton = new LinkButton();
    // End of variables declaration//GEN-END:variables

   /**
    * enables/disables all but the parameter fields
    *
    * @param enable
    */
   void enableFields(boolean enable) {
      queryLabel.setEnabled(enable);

      categoryLabel.setEnabled(enable);
      versionLabel.setEnabled(enable);
      statusLabel.setEnabled(enable);
      priorityLabel.setEnabled(enable);
      trackerLabel.setEnabled(enable);

      assigneeLabel.setEnabled(enable);

      searchButton.setEnabled(enable);
      saveButton.setEnabled(enable);
      saveChangesButton.setEnabled(enable);
      webButton.setEnabled(enable);
      refreshConfigurationButton.setEnabled(enable);

      refreshCheckBox.setEnabled(enable);
      
      modifyButton.setEnabled(enable);
      refreshButton.setEnabled(enable);
      removeButton.setEnabled(enable);
   }

   void switchQueryFields(boolean showAdvanced) {
      byDetails.setVisible(showAdvanced);
      byText.setVisible(showAdvanced);
   }

   void showError(String text) {
      noContentPanel.setVisible(true);
      tableSummaryLabel.setVisible(false);
      tableFieldsPanel.setVisible(false);
      if (text != null) {
         noContentLabel.setForeground(Defaults.COLOR_ERROR);
         noContentLabel.setText(text);
      }
   }

   void showSearchingProgress(boolean on, String text) {
      noContentPanel.setVisible(on);
      tableSummaryLabel.setVisible(!on);
      tableFieldsPanel.setVisible(!on);
      if (on && text != null) {
         noContentLabel.setForeground(defaultTextColor);
         noContentLabel.setText(text);
      }
   }

   void showRetrievingProgress(boolean on, String text, boolean searchPanelVisible) {
      noContentPanel.setVisible(on);
      noContentLabel.setForeground(Color.red);
      if (searchPanelVisible) {
         searchPanel.setVisible(!on);
      }
      if (on && text != null) {
         noContentLabel.setForeground(defaultTextColor);
         noContentLabel.setText(text);
      }
   }

   void showNoContentPanel(boolean on) {
      showSearchingProgress(on, null);
   }

   void setModifyVisible(boolean b) {
      searchPanel.setVisible(b);
      cancelChangesButton.setVisible(b);
      saveChangesButton.setVisible(b);

      topButtonPanel.setVisible(!b);
      saveButton.setVisible(!b);
      webButton.setVisible(!b);

      separatorLabel1.setVisible(!b);
      separatorLabel2.setVisible(!b);
   }

   void setTitle(String name) {
       nameLabel.setText(name);
   }

   void setLastRefresh(String lastRefresh) {
      lastRefreshDateLabel.setText(lastRefresh);
   }

   @Override
   public void focusGained(FocusEvent e) {
      Component c = e.getComponent();
      if (c instanceof JComponent) {
         Point p = SwingUtilities.convertPoint(c.getParent(), c.getLocation(), RedmineQueryPanel.this);
         final Rectangle r = new Rectangle(p, c.getSize());
         SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               RedmineQueryPanel.this.scrollRectToVisible(r);
            }
         });
      }
   }

   @Override
   public void focusLost(FocusEvent e) {
      // do nothing
   }

   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(Defaults.paintGradient((Graphics2D)g, getWidth(), getHeight()));
   }

   static class HackedScrollPane extends JScrollPane {

      @Override
      public Dimension getPreferredSize() {
         setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         Dimension dim = super.getPreferredSize();
         setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
         return dim;
      }
   }
   
    static class StringListModel extends AbstractListModel {

        String[] strings = {""};

        @Override
        public int getSize() {
            return strings.length;
        }

        @Override
        public Object getElementAt(int i) {
            return strings[i];
        }
    };
}
