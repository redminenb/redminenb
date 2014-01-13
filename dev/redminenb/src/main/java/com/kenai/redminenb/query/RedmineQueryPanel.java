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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicTreeUI;
import org.netbeans.modules.bugtracking.issuetable.Filter;

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
   //
   private Color defaultTextColor;

   public RedmineQueryPanel(JComponent tableComponent, RedmineQueryController controller) {
      super();
      initComponents();

      Font f = new JLabel().getFont();
      int s = f.getSize();
      nameLabel.setFont(nameLabel.getFont().deriveFont(s * 1.7f));
      defaultTextColor = noContentLabel.getForeground();

      tablePanel.add(tableComponent);

      BasicTreeUI tvui = (BasicTreeUI)new JTree().getUI();
      Icon ei = tvui.getExpandedIcon();
      Icon ci = tvui.getCollapsedIcon();

      byTextContainer.add(byTextPanel);
      byDetailsContainer.add(byDetailsPanel);

      byText = new ExpandablePanel(byTextLabel, byTextContainer, ei, ci);
      byDetails = new ExpandablePanel(byDetailsLabel, byDetailsContainer, ei, ci);

      byText.expand();
      byDetails.expand();

      queryHeaderPanel.setVisible(false);
      tableFieldsPanel.setVisible(false);
      saveChangesButton.setVisible(false);
      cancelChangesButton.setVisible(false);
      filterComboBox.setVisible(false);
      filterLabel.setVisible(false);
      refreshCheckBox.setVisible(false);
      noContentPanel.setVisible(false);

      refreshCheckBox.setOpaque(false);

      ListCellRenderer parameterValueLCR = new Defaults.ParameterValueLCR();
      trackerList.setCellRenderer(parameterValueLCR);
      categoryList.setCellRenderer(parameterValueLCR);
      versionList.setCellRenderer(parameterValueLCR);
      statusList.setCellRenderer(parameterValueLCR);
      priorityList.setCellRenderer(new Defaults.PriorityLCR());

      assigneeList.setCellRenderer(new Defaults.RepositoryUserLCR());

      //resolutionList.setCellRenderer(parameterValueLCR);
      //severityList.setCellRenderer(parameterValueLCR);
      resolutionLabel.setVisible(false);
      resolutionList.getParent().getParent().setVisible(false);
      severityLabel.setVisible(false);
      severityList.getParent().getParent().setVisible(false);

      filterComboBox.setRenderer(new FilterCellRenderer());

      setFocusListener(this);

      validate();
      repaint();
   }

   private void setFocusListener(FocusListener f) {
      cancelChangesButton.addFocusListener(f);

      categoryList.addFocusListener(f);
      priorityList.addFocusListener(f);
      resolutionList.addFocusListener(f);
      severityList.addFocusListener(f);
      trackerList.addFocusListener(f);
      statusList.addFocusListener(f);
      versionList.addFocusListener(f);
      assigneeList.addFocusListener(f);

      gotoIssueButton.addFocusListener(f);
      modifyButton.addFocusListener(f);
      seenButton.addFocusListener(f);
      refreshCheckBox.addFocusListener(f);
      removeButton.addFocusListener(f);
      saveButton.addFocusListener(f);
      saveChangesButton.addFocusListener(f);
      searchButton.addFocusListener(f);
      refreshButton.addFocusListener(f);
      webButton.addFocusListener(f);

      filterComboBox.addFocusListener(f);
      issueIdTextField.addFocusListener(f);
      queryTextField.addFocusListener(f);
      tablePanel.addFocusListener(f);
      tableSummaryLabel.addFocusListener(f);

      refreshConfigurationButton.addFocusListener(this);
   }

   void setQueryRunning(boolean running) {
      modifyButton.setEnabled(!running);
      seenButton.setEnabled(!running);
      removeButton.setEnabled(!running);
      refreshButton.setEnabled(!running);
      filterLabel.setEnabled(!running);
      filterComboBox.setEnabled(!running);
   }

   void setGoToIssueInfo(String iconName, String text) {
      //gotoIssueInfo.setIcon(new ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/" + iconName))); // NOI18N
      gotoIssueInfo.setIcon(iconName == null ? null : Defaults.getIcon(iconName));
      gotoIssueInfo.setText(text == null ? null : text);
   }

   /**
    * This method is called from within the constructor to initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is always
    * regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      byTextPanel = new javax.swing.JPanel();
      qSubjectCheckBox = new javax.swing.JCheckBox();
      qDescriptionCheckBox = new javax.swing.JCheckBox();
      qCommentsCheckBox = new javax.swing.JCheckBox();
      tableFieldsPanel = new javax.swing.JPanel();
      tableHeaderPanel = new javax.swing.JPanel();
      filterLabel = new javax.swing.JLabel();
      criteriaPanel = new javax.swing.JPanel();
      issueIdTextField = new JFormattedTextField(NumberFormat.getIntegerInstance());
      separatorLabel2 = new javax.swing.JLabel();
      separatorLabel1 = new javax.swing.JLabel();
      queryHeaderPanel = new javax.swing.JPanel();
      lastRefreshLabel = new javax.swing.JLabel();
      jLabel4 = new javax.swing.JLabel();
      jLabel5 = new javax.swing.JLabel();
      jLabel6 = new javax.swing.JLabel();
      jLabel7 = new javax.swing.JLabel();
      jLabel8 = new javax.swing.JLabel();
      noContentPanel = new javax.swing.JPanel();
      noContentLabel = new javax.swing.JLabel();

      byDetailsPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

      versionLabel.setFont(versionLabel.getFont().deriveFont(versionLabel.getFont().getStyle() | java.awt.Font.BOLD, versionLabel.getFont().getSize()-2));
      versionLabel.setLabelFor(versionList);
      org.openide.awt.Mnemonics.setLocalizedText(versionLabel, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.versionLabel.text")); // NOI18N

      jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

      versionList.setModel(new javax.swing.AbstractListModel() {
         String[] strings = { "" };
         public int getSize() { return strings.length; }
         public Object getElementAt(int i) { return strings[i]; }
      });
      versionList.setMinimumSize(new java.awt.Dimension(100, 2));
      versionList.setVisibleRowCount(6);
      jScrollPane2.setViewportView(versionList);
      versionList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.versionList.AccessibleContext.accessibleDescription")); // NOI18N

      statusLabel.setFont(statusLabel.getFont().deriveFont(statusLabel.getFont().getStyle() | java.awt.Font.BOLD, statusLabel.getFont().getSize()-2));
      statusLabel.setLabelFor(statusList);
      org.openide.awt.Mnemonics.setLocalizedText(statusLabel, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.statusLabel.text")); // NOI18N

      jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

      statusList.setModel(new javax.swing.AbstractListModel() {
         String[] strings = { "" };
         public int getSize() { return strings.length; }
         public Object getElementAt(int i) { return strings[i]; }
      });
      statusList.setMinimumSize(new java.awt.Dimension(100, 2));
      statusList.setVisibleRowCount(6);
      jScrollPane3.setViewportView(statusList);
      statusList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.statusList.AccessibleContext.accessibleDescription")); // NOI18N

      priorityLabel.setFont(priorityLabel.getFont().deriveFont(priorityLabel.getFont().getStyle() | java.awt.Font.BOLD, priorityLabel.getFont().getSize()-2));
      priorityLabel.setLabelFor(priorityList);
      org.openide.awt.Mnemonics.setLocalizedText(priorityLabel, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.priorityLabel.text")); // NOI18N

      jScrollPane4.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

      priorityList.setModel(new javax.swing.AbstractListModel() {
         String[] strings = { "" };
         public int getSize() { return strings.length; }
         public Object getElementAt(int i) { return strings[i]; }
      });
      priorityList.setMinimumSize(new java.awt.Dimension(100, 2));
      priorityList.setVisibleRowCount(6);
      jScrollPane4.setViewportView(priorityList);
      priorityList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.priorityList.AccessibleContext.accessibleDescription")); // NOI18N

      resolutionLabel.setFont(resolutionLabel.getFont().deriveFont(resolutionLabel.getFont().getStyle() | java.awt.Font.BOLD, resolutionLabel.getFont().getSize()-2));
      resolutionLabel.setLabelFor(resolutionList);
      org.openide.awt.Mnemonics.setLocalizedText(resolutionLabel, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.resolutionLabel.text")); // NOI18N

      jScrollPane5.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

      resolutionList.setModel(new javax.swing.AbstractListModel() {
         String[] strings = { "" };
         public int getSize() { return strings.length; }
         public Object getElementAt(int i) { return strings[i]; }
      });
      resolutionList.setMinimumSize(new java.awt.Dimension(100, 2));
      resolutionList.setVisibleRowCount(6);
      jScrollPane5.setViewportView(resolutionList);
      resolutionList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.resolutionList.AccessibleContext.accessibleDescription")); // NOI18N

      categoryLabel.setFont(categoryLabel.getFont().deriveFont(categoryLabel.getFont().getStyle() | java.awt.Font.BOLD, categoryLabel.getFont().getSize()-2));
      categoryLabel.setLabelFor(categoryList);
      org.openide.awt.Mnemonics.setLocalizedText(categoryLabel, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.categoryLabel.text")); // NOI18N

      jScrollPane6.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

      categoryList.setModel(new javax.swing.AbstractListModel() {
         String[] strings = { "" };
         public int getSize() { return strings.length; }
         public Object getElementAt(int i) { return strings[i]; }
      });
      categoryList.setMinimumSize(new java.awt.Dimension(100, 2));
      categoryList.setVisibleRowCount(6);
      jScrollPane6.setViewportView(categoryList);
      categoryList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.componentList.AccessibleContext.accessibleDescription")); // NOI18N

      severityLabel.setFont(severityLabel.getFont().deriveFont(severityLabel.getFont().getStyle() | java.awt.Font.BOLD, severityLabel.getFont().getSize()-2));
      severityLabel.setLabelFor(severityList);
      org.openide.awt.Mnemonics.setLocalizedText(severityLabel, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.severityLabel.text")); // NOI18N

      severityScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

      severityList.setModel(new javax.swing.AbstractListModel() {
         String[] strings = { "" };
         public int getSize() { return strings.length; }
         public Object getElementAt(int i) { return strings[i]; }
      });
      severityList.setMinimumSize(new java.awt.Dimension(100, 2));
      severityList.setVisibleRowCount(6);
      severityScrollPane.setViewportView(severityList);
      severityList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.severityList.AccessibleContext.accessibleDescription")); // NOI18N

      trackerLabel.setFont(trackerLabel.getFont().deriveFont(trackerLabel.getFont().getStyle() | java.awt.Font.BOLD, trackerLabel.getFont().getSize()-2));
      trackerLabel.setLabelFor(trackerList);
      org.openide.awt.Mnemonics.setLocalizedText(trackerLabel, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.trackerLabel.text")); // NOI18N

      issueTypeScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

      trackerList.setModel(new javax.swing.AbstractListModel() {
         String[] strings = { "" };
         public int getSize() { return strings.length; }
         public Object getElementAt(int i) { return strings[i]; }
      });
      trackerList.setMinimumSize(new java.awt.Dimension(100, 2));
      trackerList.setVisibleRowCount(6);
      issueTypeScrollPane.setViewportView(trackerList);

      assigneeLabel.setFont(assigneeLabel.getFont().deriveFont(assigneeLabel.getFont().getStyle() | java.awt.Font.BOLD, assigneeLabel.getFont().getSize()-2));
      assigneeLabel.setLabelFor(assigneeList);
      org.openide.awt.Mnemonics.setLocalizedText(assigneeLabel, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.assigneeLabel.text")); // NOI18N

      assigneeScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

      assigneeList.setModel(new javax.swing.AbstractListModel() {
         String[] strings = { "" };
         public int getSize() { return strings.length; }
         public Object getElementAt(int i) { return strings[i]; }
      });
      assigneeList.setMinimumSize(new java.awt.Dimension(100, 2));
      assigneeList.setVisibleRowCount(6);
      assigneeScrollPane.setViewportView(assigneeList);

      javax.swing.GroupLayout byDetailsPanelLayout = new javax.swing.GroupLayout(byDetailsPanel);
      byDetailsPanel.setLayout(byDetailsPanelLayout);
      byDetailsPanelLayout.setHorizontalGroup(
         byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(byDetailsPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(trackerLabel)
               .addComponent(issueTypeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(statusLabel)
               .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(priorityLabel)
               .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(assigneeLabel)
               .addComponent(assigneeScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(categoryLabel)
               .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(versionLabel)
               .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(resolutionLabel)
               .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(severityLabel)
               .addComponent(severityScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );
      byDetailsPanelLayout.setVerticalGroup(
         byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(byDetailsPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(byDetailsPanelLayout.createSequentialGroup()
                  .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(statusLabel)
                     .addComponent(resolutionLabel)
                     .addComponent(priorityLabel)
                     .addComponent(severityLabel)
                     .addComponent(assigneeLabel))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane4)
                     .addComponent(jScrollPane3)
                     .addComponent(jScrollPane5)
                     .addComponent(severityScrollPane)
                     .addComponent(assigneeScrollPane)))
               .addGroup(byDetailsPanelLayout.createSequentialGroup()
                  .addComponent(trackerLabel)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(issueTypeScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 18, Short.MAX_VALUE))
               .addGroup(byDetailsPanelLayout.createSequentialGroup()
                  .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(categoryLabel)
                     .addComponent(versionLabel))
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addGroup(byDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane6)
                     .addComponent(jScrollPane2))))
            .addContainerGap())
      );

      byTextPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

      queryLabel.setFont(queryLabel.getFont().deriveFont(queryLabel.getFont().getStyle() | java.awt.Font.BOLD, queryLabel.getFont().getSize()-2));
      queryLabel.setLabelFor(queryTextField);
      org.openide.awt.Mnemonics.setLocalizedText(queryLabel, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.queryLabel.text")); // NOI18N

      queryTextField.setColumns(30);

      qSubjectCheckBox.setSelected(true);
      org.openide.awt.Mnemonics.setLocalizedText(qSubjectCheckBox, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.qSubjectCheckBox.text")); // NOI18N

      org.openide.awt.Mnemonics.setLocalizedText(qDescriptionCheckBox, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.qDescriptionCheckBox.text")); // NOI18N

      org.openide.awt.Mnemonics.setLocalizedText(qCommentsCheckBox, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.qCommentsCheckBox.text")); // NOI18N
      qCommentsCheckBox.setEnabled(false);

      javax.swing.GroupLayout byTextPanelLayout = new javax.swing.GroupLayout(byTextPanel);
      byTextPanel.setLayout(byTextPanelLayout);
      byTextPanelLayout.setHorizontalGroup(
         byTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(byTextPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(queryLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(queryTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(qSubjectCheckBox)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(qDescriptionCheckBox)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(qCommentsCheckBox)
            .addContainerGap())
      );
      byTextPanelLayout.setVerticalGroup(
         byTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(byTextPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(byTextPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(queryLabel)
               .addComponent(queryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(qSubjectCheckBox)
               .addComponent(qDescriptionCheckBox)
               .addComponent(qCommentsCheckBox))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

      queryTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.summaryTextField.AccessibleContext.accessibleName")); // NOI18N
      queryTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.summaryTextField.AccessibleContext.accessibleDescription")); // NOI18N

      setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
      setOpaque(false);

      tableFieldsPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
      tableFieldsPanel.setOpaque(false);

      tableHeaderPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
      tableHeaderPanel.setOpaque(false);

      filterLabel.setLabelFor(filterComboBox);
      org.openide.awt.Mnemonics.setLocalizedText(filterLabel, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.filterLabel.text")); // NOI18N

      tableSummaryLabel.setFont(tableSummaryLabel.getFont().deriveFont(tableSummaryLabel.getFont().getStyle() | java.awt.Font.BOLD, tableSummaryLabel.getFont().getSize()-2));
      tableSummaryLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/redmine.png"))); // NOI18N
      org.openide.awt.Mnemonics.setLocalizedText(tableSummaryLabel, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.tableSummaryLabel.text")); // NOI18N

      javax.swing.GroupLayout tableHeaderPanelLayout = new javax.swing.GroupLayout(tableHeaderPanel);
      tableHeaderPanel.setLayout(tableHeaderPanelLayout);
      tableHeaderPanelLayout.setHorizontalGroup(
         tableHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(tableHeaderPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(tableSummaryLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(filterLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(filterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
      );
      tableHeaderPanelLayout.setVerticalGroup(
         tableHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(tableHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(filterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(filterLabel)
            .addComponent(tableSummaryLabel))
      );

      filterComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.filterComboBox.AccessibleContext.accessibleDescription")); // NOI18N

      tablePanel.setBackground(new java.awt.Color(224, 224, 224));
      tablePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
      tablePanel.setMinimumSize(new java.awt.Dimension(100, 350));
      tablePanel.setLayout(new java.awt.BorderLayout());

      javax.swing.GroupLayout tableFieldsPanelLayout = new javax.swing.GroupLayout(tableFieldsPanel);
      tableFieldsPanel.setLayout(tableFieldsPanelLayout);
      tableFieldsPanelLayout.setHorizontalGroup(
         tableFieldsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(tableFieldsPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(tableFieldsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(tablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addComponent(tableHeaderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())
      );
      tableFieldsPanelLayout.setVerticalGroup(
         tableFieldsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(tableFieldsPanelLayout.createSequentialGroup()
            .addComponent(tableHeaderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(tablePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 162, Short.MAX_VALUE)
            .addContainerGap())
      );

      searchPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
      searchPanel.setOpaque(false);

      criteriaPanel.setBackground(new java.awt.Color(224, 224, 224));
      criteriaPanel.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.shadow")));

      byTextLabel.setFont(byTextLabel.getFont().deriveFont(byTextLabel.getFont().getStyle() | java.awt.Font.BOLD));
      org.openide.awt.Mnemonics.setLocalizedText(byTextLabel, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.byTextLabel.text")); // NOI18N

      byTextContainer.setLayout(new java.awt.BorderLayout());

      byDetailsLabel.setFont(byDetailsLabel.getFont().deriveFont(byDetailsLabel.getFont().getStyle() | java.awt.Font.BOLD));
      org.openide.awt.Mnemonics.setLocalizedText(byDetailsLabel, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.byDetailsLabel.text")); // NOI18N

      byDetailsContainer.setLayout(new java.awt.BorderLayout());

      javax.swing.GroupLayout criteriaPanelLayout = new javax.swing.GroupLayout(criteriaPanel);
      criteriaPanel.setLayout(criteriaPanelLayout);
      criteriaPanelLayout.setHorizontalGroup(
         criteriaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(byTextContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addComponent(byDetailsContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addGroup(criteriaPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(criteriaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(byTextLabel)
               .addComponent(byDetailsLabel)))
      );
      criteriaPanelLayout.setVerticalGroup(
         criteriaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(criteriaPanelLayout.createSequentialGroup()
            .addComponent(byTextLabel)
            .addGap(0, 0, 0)
            .addComponent(byTextContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 0, 0)
            .addComponent(byDetailsLabel)
            .addGap(0, 0, 0)
            .addComponent(byDetailsContainer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
      );

      byTextLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.byTextLabel.AccessibleContext.accessibleName")); // NOI18N

      org.openide.awt.Mnemonics.setLocalizedText(cancelChangesButton, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.cancelChangesButton.text")); // NOI18N

      org.openide.awt.Mnemonics.setLocalizedText(saveChangesButton, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.saveChangesButton.text")); // NOI18N
      saveChangesButton.setSelected(true);

      gotoPanel.setBackground(new java.awt.Color(224, 224, 224));
      gotoPanel.setOpaque(false);

      org.openide.awt.Mnemonics.setLocalizedText(gotoIssueButton, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.gotoIssueButton.text")); // NOI18N

      issueIdTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);

      org.openide.awt.Mnemonics.setLocalizedText(searchButton, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.searchButton.text")); // NOI18N
      searchButton.setSelected(true);

      org.openide.awt.Mnemonics.setLocalizedText(separatorLabel2, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.separatorLabel2.text")); // NOI18N
      separatorLabel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

      org.openide.awt.Mnemonics.setLocalizedText(separatorLabel1, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.separatorLabel1.text")); // NOI18N
      separatorLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

      org.openide.awt.Mnemonics.setLocalizedText(saveButton, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.saveButton.text")); // NOI18N

      org.openide.awt.Mnemonics.setLocalizedText(webButton, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.webButton.text")); // NOI18N
      webButton.setActionCommand(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.webButton.actionCommand")); // NOI18N

      org.openide.awt.Mnemonics.setLocalizedText(refreshConfigurationButton, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.refreshConfigurationButton.text")); // NOI18N

      javax.swing.GroupLayout gotoPanelLayout = new javax.swing.GroupLayout(gotoPanel);
      gotoPanel.setLayout(gotoPanelLayout);
      gotoPanelLayout.setHorizontalGroup(
         gotoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(gotoPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(searchButton)
            .addGap(11, 11, 11)
            .addComponent(gotoIssueButton)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(issueIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(gotoIssueInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(separatorLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(webButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(separatorLabel2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(refreshConfigurationButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
      );
      gotoPanelLayout.setVerticalGroup(
         gotoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(gotoPanelLayout.createSequentialGroup()
            .addGroup(gotoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(gotoIssueButton)
               .addComponent(issueIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(searchButton))
            .addGap(0, 0, Short.MAX_VALUE))
         .addGroup(gotoPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(gotoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, gotoPanelLayout.createSequentialGroup()
                  .addGap(0, 0, Short.MAX_VALUE)
                  .addGroup(gotoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(gotoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(separatorLabel1)
                        .addComponent(separatorLabel2))
                     .addComponent(webButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(refreshConfigurationButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
               .addComponent(saveButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addComponent(gotoIssueInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())
      );

      gotoPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {separatorLabel1, separatorLabel2, webButton});

      gotoIssueButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.gotoIssueButton.AccessibleContext.accessibleDescription")); // NOI18N
      searchButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.searchButton.AccessibleContext.accessibleDescription")); // NOI18N
      saveButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.saveButton.AccessibleContext.accessibleDescription")); // NOI18N
      webButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.webButton.AccessibleContext.accessibleDescription")); // NOI18N
      refreshConfigurationButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.refreshConfigurationButton.AccessibleContext.accessibleDescription")); // NOI18N

      javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
      searchPanel.setLayout(searchPanelLayout);
      searchPanelLayout.setHorizontalGroup(
         searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(gotoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addGroup(searchPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(searchPanelLayout.createSequentialGroup()
                  .addComponent(saveChangesButton)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(cancelChangesButton)
                  .addGap(0, 0, Short.MAX_VALUE))
               .addComponent(criteriaPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap())
      );
      searchPanelLayout.setVerticalGroup(
         searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(searchPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(gotoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(criteriaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGap(8, 8, 8)
            .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(saveChangesButton)
               .addComponent(cancelChangesButton))
            .addContainerGap())
      );

      cancelChangesButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.cancelChangesButton.AccessibleContext.accessibleDescription")); // NOI18N
      saveChangesButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.saveChangesButton.AccessibleContext.accessibleDescription")); // NOI18N

      queryHeaderPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
      queryHeaderPanel.setOpaque(false);

      org.openide.awt.Mnemonics.setLocalizedText(seenButton, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.seenButton.text")); // NOI18N

      org.openide.awt.Mnemonics.setLocalizedText(lastRefreshLabel, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.lastRefreshLabel.text")); // NOI18N

      org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.removeButton.text")); // NOI18N

      org.openide.awt.Mnemonics.setLocalizedText(lastRefreshDateLabel, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.lastRefreshDateLabel.text")); // NOI18N

      org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.refreshButton.text")); // NOI18N

      org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.nameLabel.text")); // NOI18N

      org.openide.awt.Mnemonics.setLocalizedText(modifyButton, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.modifyButton.text")); // NOI18N

      org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.jLabel4.text")); // NOI18N
      jLabel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

      org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.jLabel5.text")); // NOI18N
      jLabel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

      org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.jLabel6.text")); // NOI18N
      jLabel6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

      org.openide.awt.Mnemonics.setLocalizedText(refreshCheckBox, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.refreshCheckBox.text")); // NOI18N

      org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.jLabel7.text")); // NOI18N
      jLabel7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

      org.openide.awt.Mnemonics.setLocalizedText(findIssuesButton, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.findIssuesButton.text")); // NOI18N

      org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.jLabel8.text")); // NOI18N
      jLabel8.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

      org.openide.awt.Mnemonics.setLocalizedText(cloneQueryButton, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.cloneQueryButton.text")); // NOI18N

      javax.swing.GroupLayout queryHeaderPanelLayout = new javax.swing.GroupLayout(queryHeaderPanel);
      queryHeaderPanel.setLayout(queryHeaderPanelLayout);
      queryHeaderPanelLayout.setHorizontalGroup(
         queryHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(queryHeaderPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(queryHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                  .addComponent(nameLabel)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(refreshCheckBox)
                  .addGap(18, 18, 18)
                  .addComponent(lastRefreshLabel)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(lastRefreshDateLabel))
               .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                  .addComponent(seenButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                  .addComponent(modifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(7, 7, 7)
                  .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(5, 5, 5)
                  .addComponent(jLabel6)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(jLabel7)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(findIssuesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(6, 6, 6)
                  .addComponent(jLabel8)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(cloneQueryButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap())
      );
      queryHeaderPanelLayout.setVerticalGroup(
         queryHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(queryHeaderPanelLayout.createSequentialGroup()
            .addGroup(queryHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                  .addGap(11, 11, 11)
                  .addComponent(nameLabel))
               .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                  .addContainerGap()
                  .addGroup(queryHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lastRefreshDateLabel)
                     .addComponent(lastRefreshLabel)
                     .addComponent(refreshCheckBox))))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
            .addGroup(queryHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
               .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabel6)
               .addComponent(modifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(seenButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabel7)
               .addComponent(findIssuesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(cloneQueryButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap())
      );

      queryHeaderPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel4, jLabel5, jLabel6, jLabel7, modifyButton, refreshButton, removeButton});

      seenButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.refreshButton.AccessibleContext.accessibleDescription")); // NOI18N
      removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.removeButton.AccessibleContext.accessibleDescription")); // NOI18N
      refreshButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.seenButton.AccessibleContext.accessibleDescription")); // NOI18N
      modifyButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.modifyButton.AccessibleContext.accessibleDescription")); // NOI18N
      refreshCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.refreshCheckBox.AccessibleContext.accessibleDescription")); // NOI18N

      noContentPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
      noContentPanel.setOpaque(false);
      noContentPanel.setLayout(new java.awt.GridBagLayout());

      org.openide.awt.Mnemonics.setLocalizedText(noContentLabel, org.openide.util.NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.noContentLabel.text")); // NOI18N
      noContentPanel.add(noContentLabel, new java.awt.GridBagConstraints());

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(queryHeaderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addComponent(searchPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(noContentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addContainerGap())
         .addComponent(tableFieldsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(queryHeaderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(searchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(tableFieldsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(noContentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
      );
   }// </editor-fold>//GEN-END:initComponents
   // Variables declaration - do not modify//GEN-BEGIN:variables
   final javax.swing.JLabel assigneeLabel = new javax.swing.JLabel();
   final javax.swing.JList assigneeList = new javax.swing.JList();
   final javax.swing.JScrollPane assigneeScrollPane = new HackedScrollPane();
   final javax.swing.JPanel byDetailsContainer = new javax.swing.JPanel();
   final javax.swing.JLabel byDetailsLabel = new javax.swing.JLabel();
   final javax.swing.JPanel byDetailsPanel = new javax.swing.JPanel();
   final javax.swing.JPanel byTextContainer = new javax.swing.JPanel();
   final javax.swing.JLabel byTextLabel = new javax.swing.JLabel();
   private javax.swing.JPanel byTextPanel;
   final javax.swing.JButton cancelChangesButton = new javax.swing.JButton();
   final javax.swing.JLabel categoryLabel = new javax.swing.JLabel();
   final javax.swing.JList categoryList = new javax.swing.JList();
   public final org.netbeans.modules.bugtracking.util.LinkButton cloneQueryButton = new org.netbeans.modules.bugtracking.util.LinkButton();
   private javax.swing.JPanel criteriaPanel;
   final javax.swing.JComboBox filterComboBox = new javax.swing.JComboBox();
   private javax.swing.JLabel filterLabel;
   public final org.netbeans.modules.bugtracking.util.LinkButton findIssuesButton = new org.netbeans.modules.bugtracking.util.LinkButton();
   final javax.swing.JButton gotoIssueButton = new javax.swing.JButton();
   private final javax.swing.JLabel gotoIssueInfo = new javax.swing.JLabel();
   final javax.swing.JPanel gotoPanel = new javax.swing.JPanel();
   javax.swing.JFormattedTextField issueIdTextField;
   final javax.swing.JScrollPane issueTypeScrollPane = new HackedScrollPane();
   private javax.swing.JLabel jLabel4;
   private javax.swing.JLabel jLabel5;
   private javax.swing.JLabel jLabel6;
   private javax.swing.JLabel jLabel7;
   private javax.swing.JLabel jLabel8;
   final javax.swing.JScrollPane jScrollPane2 = new HackedScrollPane();
   final javax.swing.JScrollPane jScrollPane3 = new HackedScrollPane();
   final javax.swing.JScrollPane jScrollPane4 = new HackedScrollPane();
   final javax.swing.JScrollPane jScrollPane5 = new HackedScrollPane();
   final javax.swing.JScrollPane jScrollPane6 = new HackedScrollPane();
   final javax.swing.JLabel lastRefreshDateLabel = new javax.swing.JLabel();
   private javax.swing.JLabel lastRefreshLabel;
   public final org.netbeans.modules.bugtracking.util.LinkButton modifyButton = new org.netbeans.modules.bugtracking.util.LinkButton();
   final javax.swing.JLabel nameLabel = new javax.swing.JLabel();
   private javax.swing.JLabel noContentLabel;
   private javax.swing.JPanel noContentPanel;
   final javax.swing.JLabel priorityLabel = new javax.swing.JLabel();
   final javax.swing.JList priorityList = new javax.swing.JList();
   javax.swing.JCheckBox qCommentsCheckBox;
   javax.swing.JCheckBox qDescriptionCheckBox;
   javax.swing.JCheckBox qSubjectCheckBox;
   private javax.swing.JPanel queryHeaderPanel;
   final javax.swing.JLabel queryLabel = new javax.swing.JLabel();
   final javax.swing.JTextField queryTextField = new javax.swing.JTextField();
   final org.netbeans.modules.bugtracking.util.LinkButton refreshButton = new org.netbeans.modules.bugtracking.util.LinkButton();
   final javax.swing.JCheckBox refreshCheckBox = new javax.swing.JCheckBox();
   final org.netbeans.modules.bugtracking.util.LinkButton refreshConfigurationButton = new org.netbeans.modules.bugtracking.util.LinkButton();
   public final org.netbeans.modules.bugtracking.util.LinkButton removeButton = new org.netbeans.modules.bugtracking.util.LinkButton();
   final javax.swing.JLabel resolutionLabel = new javax.swing.JLabel();
   final javax.swing.JList resolutionList = new javax.swing.JList();
   final org.netbeans.modules.bugtracking.util.LinkButton saveButton = new org.netbeans.modules.bugtracking.util.LinkButton();
   final javax.swing.JButton saveChangesButton = new javax.swing.JButton();
   final javax.swing.JButton searchButton = new javax.swing.JButton();
   final javax.swing.JPanel searchPanel = new javax.swing.JPanel();
   final org.netbeans.modules.bugtracking.util.LinkButton seenButton = new org.netbeans.modules.bugtracking.util.LinkButton();
   private javax.swing.JLabel separatorLabel1;
   private javax.swing.JLabel separatorLabel2;
   final javax.swing.JLabel severityLabel = new javax.swing.JLabel();
   final javax.swing.JList severityList = new javax.swing.JList();
   final javax.swing.JScrollPane severityScrollPane = new HackedScrollPane();
   final javax.swing.JLabel statusLabel = new javax.swing.JLabel();
   final javax.swing.JList statusList = new javax.swing.JList();
   private javax.swing.JPanel tableFieldsPanel;
   private javax.swing.JPanel tableHeaderPanel;
   final javax.swing.JPanel tablePanel = new javax.swing.JPanel();
   final javax.swing.JLabel tableSummaryLabel = new javax.swing.JLabel();
   final javax.swing.JLabel trackerLabel = new javax.swing.JLabel();
   final javax.swing.JList trackerList = new javax.swing.JList();
   final javax.swing.JLabel versionLabel = new javax.swing.JLabel();
   final javax.swing.JList versionList = new javax.swing.JList();
   final org.netbeans.modules.bugtracking.util.LinkButton webButton = new org.netbeans.modules.bugtracking.util.LinkButton();
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
      severityLabel.setEnabled(enable);
      resolutionLabel.setEnabled(enable);
      priorityLabel.setEnabled(enable);
      trackerLabel.setEnabled(enable);

      assigneeLabel.setEnabled(enable);

      searchButton.setEnabled(enable);
      saveButton.setEnabled(enable);
      webButton.setEnabled(enable);
      refreshConfigurationButton.setEnabled(enable);

      refreshCheckBox.setEnabled(enable);
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

      tableFieldsPanel.setVisible(!b);
      searchButton.setVisible(!b);
      saveButton.setVisible(!b);
      webButton.setVisible(!b);

      separatorLabel1.setVisible(!b);
      separatorLabel2.setVisible(!b);
   }

   void setSaved(String name, String lastRefresh) {
      searchPanel.setVisible(false);
      queryHeaderPanel.setVisible(true);
      filterComboBox.setVisible(true); // XXX move to bugtracking IssueTable component
      filterLabel.setVisible(true);
      tableHeaderPanel.setVisible(true);
      nameLabel.setText(name);
      setLastRefresh(lastRefresh);
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

   class ExpandablePanel {

      private final JPanel panel;
      private final JLabel label;
      private final Icon ei;
      private final Icon ci;
      private boolean expaned = true;

      public ExpandablePanel(JLabel l, JPanel p, final Icon ei, final Icon ci) {
         this.panel = p;
         this.label = l;
         this.ci = ci;
         this.ei = ei;
         this.label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
               if (panel.isVisible()) {
                  colapse();
               } else {
                  expand();
               }
            }
         });
      }

      public void expand() {
         expaned = true;
         panel.setVisible(true);
         label.setIcon(ei);
      }

      public void colapse() {
         expaned = false;
         panel.setVisible(false);
         label.setIcon(ci);
      }

      public void setVisible(boolean visible) {
         label.setVisible(visible);
         panel.setVisible(visible && expaned);
      }
   }

   private static class FilterCellRenderer extends DefaultListCellRenderer {

      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
         if (value instanceof Filter) {
            value = ((Filter)value).getDisplayName();
         }
         return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
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
}
