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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTreeUI;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugtracking.commons.LinkButton;
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

        byTextPanel = new JPanel();
        qSubjectCheckBox = new JCheckBox();
        qDescriptionCheckBox = new JCheckBox();
        qCommentsCheckBox = new JCheckBox();
        tableFieldsPanel = new JPanel();
        tableHeaderPanel = new JPanel();
        filterLabel = new JLabel();
        criteriaPanel = new JPanel();
        issueIdTextField = new JFormattedTextField(NumberFormat.getIntegerInstance());
        separatorLabel2 = new JLabel();
        separatorLabel1 = new JLabel();
        queryHeaderPanel = new JPanel();
        lastRefreshLabel = new JLabel();
        jLabel4 = new JLabel();
        jLabel5 = new JLabel();
        jLabel6 = new JLabel();
        jLabel7 = new JLabel();
        jLabel8 = new JLabel();
        noContentPanel = new JPanel();
        noContentLabel = new JLabel();

        byDetailsPanel.setBackground(UIManager.getDefaults().getColor("TextArea.background"));

        versionLabel.setFont(versionLabel.getFont().deriveFont(versionLabel.getFont().getStyle() | Font.BOLD, versionLabel.getFont().getSize()-2));
        versionLabel.setLabelFor(versionList);
        Mnemonics.setLocalizedText(versionLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.versionLabel.text")); // NOI18N

        jScrollPane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        versionList.setModel(new AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        versionList.setMinimumSize(new Dimension(100, 2));
        versionList.setVisibleRowCount(6);
        jScrollPane2.setViewportView(versionList);
        versionList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.versionList.AccessibleContext.accessibleDescription")); // NOI18N

        statusLabel.setFont(statusLabel.getFont().deriveFont(statusLabel.getFont().getStyle() | Font.BOLD, statusLabel.getFont().getSize()-2));
        statusLabel.setLabelFor(statusList);
        Mnemonics.setLocalizedText(statusLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.statusLabel.text")); // NOI18N

        jScrollPane3.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        statusList.setModel(new AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        statusList.setMinimumSize(new Dimension(100, 2));
        statusList.setVisibleRowCount(6);
        jScrollPane3.setViewportView(statusList);
        statusList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.statusList.AccessibleContext.accessibleDescription")); // NOI18N

        priorityLabel.setFont(priorityLabel.getFont().deriveFont(priorityLabel.getFont().getStyle() | Font.BOLD, priorityLabel.getFont().getSize()-2));
        priorityLabel.setLabelFor(priorityList);
        Mnemonics.setLocalizedText(priorityLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.priorityLabel.text")); // NOI18N

        jScrollPane4.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        priorityList.setModel(new AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        priorityList.setMinimumSize(new Dimension(100, 2));
        priorityList.setVisibleRowCount(6);
        jScrollPane4.setViewportView(priorityList);
        priorityList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.priorityList.AccessibleContext.accessibleDescription")); // NOI18N

        resolutionLabel.setFont(resolutionLabel.getFont().deriveFont(resolutionLabel.getFont().getStyle() | Font.BOLD, resolutionLabel.getFont().getSize()-2));
        resolutionLabel.setLabelFor(resolutionList);
        Mnemonics.setLocalizedText(resolutionLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.resolutionLabel.text")); // NOI18N

        jScrollPane5.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        resolutionList.setModel(new AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        resolutionList.setMinimumSize(new Dimension(100, 2));
        resolutionList.setVisibleRowCount(6);
        jScrollPane5.setViewportView(resolutionList);
        resolutionList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.resolutionList.AccessibleContext.accessibleDescription")); // NOI18N

        categoryLabel.setFont(categoryLabel.getFont().deriveFont(categoryLabel.getFont().getStyle() | Font.BOLD, categoryLabel.getFont().getSize()-2));
        categoryLabel.setLabelFor(categoryList);
        Mnemonics.setLocalizedText(categoryLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.categoryLabel.text")); // NOI18N

        jScrollPane6.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        categoryList.setModel(new AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        categoryList.setMinimumSize(new Dimension(100, 2));
        categoryList.setVisibleRowCount(6);
        jScrollPane6.setViewportView(categoryList);
        categoryList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.componentList.AccessibleContext.accessibleDescription")); // NOI18N

        severityLabel.setFont(severityLabel.getFont().deriveFont(severityLabel.getFont().getStyle() | Font.BOLD, severityLabel.getFont().getSize()-2));
        severityLabel.setLabelFor(severityList);
        Mnemonics.setLocalizedText(severityLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.severityLabel.text")); // NOI18N

        severityScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        severityList.setModel(new AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        severityList.setMinimumSize(new Dimension(100, 2));
        severityList.setVisibleRowCount(6);
        severityScrollPane.setViewportView(severityList);
        severityList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.severityList.AccessibleContext.accessibleDescription")); // NOI18N

        trackerLabel.setFont(trackerLabel.getFont().deriveFont(trackerLabel.getFont().getStyle() | Font.BOLD, trackerLabel.getFont().getSize()-2));
        trackerLabel.setLabelFor(trackerList);
        Mnemonics.setLocalizedText(trackerLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.trackerLabel.text")); // NOI18N

        issueTypeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        trackerList.setModel(new AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        trackerList.setMinimumSize(new Dimension(100, 2));
        trackerList.setVisibleRowCount(6);
        issueTypeScrollPane.setViewportView(trackerList);

        assigneeLabel.setFont(assigneeLabel.getFont().deriveFont(assigneeLabel.getFont().getStyle() | Font.BOLD, assigneeLabel.getFont().getSize()-2));
        assigneeLabel.setLabelFor(assigneeList);
        Mnemonics.setLocalizedText(assigneeLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.assigneeLabel.text")); // NOI18N

        assigneeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        assigneeList.setModel(new AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        assigneeList.setMinimumSize(new Dimension(100, 2));
        assigneeList.setVisibleRowCount(6);
        assigneeScrollPane.setViewportView(assigneeList);

        GroupLayout byDetailsPanelLayout = new GroupLayout(byDetailsPanel);
        byDetailsPanel.setLayout(byDetailsPanelLayout);
        byDetailsPanelLayout.setHorizontalGroup(
            byDetailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(byDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(byDetailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(trackerLabel)
                    .addComponent(issueTypeScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byDetailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(statusLabel)
                    .addComponent(jScrollPane3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byDetailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(priorityLabel)
                    .addComponent(jScrollPane4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byDetailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(assigneeLabel)
                    .addComponent(assigneeScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byDetailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(categoryLabel)
                    .addComponent(jScrollPane6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byDetailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(versionLabel)
                    .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(byDetailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(resolutionLabel)
                    .addComponent(jScrollPane5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byDetailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(severityLabel)
                    .addComponent(severityScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        byDetailsPanelLayout.setVerticalGroup(
            byDetailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(byDetailsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(byDetailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(byDetailsPanelLayout.createSequentialGroup()
                        .addGroup(byDetailsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(statusLabel)
                            .addComponent(resolutionLabel)
                            .addComponent(priorityLabel)
                            .addComponent(severityLabel)
                            .addComponent(assigneeLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(byDetailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4)
                            .addComponent(jScrollPane3)
                            .addComponent(jScrollPane5)
                            .addComponent(severityScrollPane)
                            .addComponent(assigneeScrollPane)))
                    .addGroup(byDetailsPanelLayout.createSequentialGroup()
                        .addComponent(trackerLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(issueTypeScrollPane, GroupLayout.DEFAULT_SIZE, 18, Short.MAX_VALUE))
                    .addGroup(byDetailsPanelLayout.createSequentialGroup()
                        .addGroup(byDetailsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(categoryLabel)
                            .addComponent(versionLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(byDetailsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane6)
                            .addComponent(jScrollPane2))))
                .addContainerGap())
        );

        byTextPanel.setBackground(UIManager.getDefaults().getColor("TextArea.background"));

        queryLabel.setFont(queryLabel.getFont().deriveFont(queryLabel.getFont().getStyle() | Font.BOLD, queryLabel.getFont().getSize()-2));
        queryLabel.setLabelFor(queryTextField);
        Mnemonics.setLocalizedText(queryLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.queryLabel.text")); // NOI18N

        queryTextField.setColumns(30);

        qSubjectCheckBox.setSelected(true);
        Mnemonics.setLocalizedText(qSubjectCheckBox, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.qSubjectCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(qDescriptionCheckBox, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.qDescriptionCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(qCommentsCheckBox, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.qCommentsCheckBox.text")); // NOI18N
        qCommentsCheckBox.setEnabled(false);

        GroupLayout byTextPanelLayout = new GroupLayout(byTextPanel);
        byTextPanel.setLayout(byTextPanelLayout);
        byTextPanelLayout.setHorizontalGroup(
            byTextPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(byTextPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(queryLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(queryTextField, GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(qSubjectCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(qDescriptionCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(qCommentsCheckBox)
                .addContainerGap())
        );
        byTextPanelLayout.setVerticalGroup(
            byTextPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(byTextPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(byTextPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(queryLabel)
                    .addComponent(queryTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(qSubjectCheckBox)
                    .addComponent(qDescriptionCheckBox)
                    .addComponent(qCommentsCheckBox))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        queryTextField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.summaryTextField.AccessibleContext.accessibleName")); // NOI18N
        queryTextField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.summaryTextField.AccessibleContext.accessibleDescription")); // NOI18N

        setBackground(UIManager.getDefaults().getColor("TextArea.background"));
        setOpaque(false);

        tableFieldsPanel.setBackground(UIManager.getDefaults().getColor("EditorPane.background"));
        tableFieldsPanel.setOpaque(false);

        tableHeaderPanel.setBackground(UIManager.getDefaults().getColor("EditorPane.background"));
        tableHeaderPanel.setOpaque(false);

        filterLabel.setLabelFor(filterComboBox);
        Mnemonics.setLocalizedText(filterLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.filterLabel.text")); // NOI18N

        tableSummaryLabel.setFont(tableSummaryLabel.getFont().deriveFont(tableSummaryLabel.getFont().getStyle() | Font.BOLD, tableSummaryLabel.getFont().getSize()-2));
        tableSummaryLabel.setIcon(new ImageIcon(getClass().getResource("/com/kenai/redminenb/resources/redmine.png"))); // NOI18N
        Mnemonics.setLocalizedText(tableSummaryLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.tableSummaryLabel.text")); // NOI18N

        GroupLayout tableHeaderPanelLayout = new GroupLayout(tableHeaderPanel);
        tableHeaderPanel.setLayout(tableHeaderPanelLayout);
        tableHeaderPanelLayout.setHorizontalGroup(
            tableHeaderPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(tableHeaderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tableSummaryLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(filterLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterComboBox, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE))
        );
        tableHeaderPanelLayout.setVerticalGroup(
            tableHeaderPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(tableHeaderPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(filterComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(filterLabel)
                .addComponent(tableSummaryLabel))
        );

        filterComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.filterComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        tablePanel.setBackground(new Color(224, 224, 224));
        tablePanel.setBorder(BorderFactory.createEtchedBorder());
        tablePanel.setMinimumSize(new Dimension(100, 350));
        tablePanel.setLayout(new BorderLayout());

        GroupLayout tableFieldsPanelLayout = new GroupLayout(tableFieldsPanel);
        tableFieldsPanel.setLayout(tableFieldsPanelLayout);
        tableFieldsPanelLayout.setHorizontalGroup(
            tableFieldsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(tableFieldsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tableFieldsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(tablePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tableHeaderPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        tableFieldsPanelLayout.setVerticalGroup(
            tableFieldsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(tableFieldsPanelLayout.createSequentialGroup()
                .addComponent(tableHeaderPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tablePanel, GroupLayout.PREFERRED_SIZE, 162, Short.MAX_VALUE)
                .addContainerGap())
        );

        searchPanel.setBackground(UIManager.getDefaults().getColor("EditorPane.background"));
        searchPanel.setOpaque(false);

        criteriaPanel.setBackground(new Color(224, 224, 224));
        criteriaPanel.setBorder(BorderFactory.createLineBorder(UIManager.getDefaults().getColor("Button.shadow")));

        byTextLabel.setFont(byTextLabel.getFont().deriveFont(byTextLabel.getFont().getStyle() | Font.BOLD));
        Mnemonics.setLocalizedText(byTextLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.byTextLabel.text")); // NOI18N

        byTextContainer.setLayout(new BorderLayout());

        byDetailsLabel.setFont(byDetailsLabel.getFont().deriveFont(byDetailsLabel.getFont().getStyle() | Font.BOLD));
        Mnemonics.setLocalizedText(byDetailsLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.byDetailsLabel.text")); // NOI18N

        byDetailsContainer.setLayout(new BorderLayout());

        GroupLayout criteriaPanelLayout = new GroupLayout(criteriaPanel);
        criteriaPanel.setLayout(criteriaPanelLayout);
        criteriaPanelLayout.setHorizontalGroup(
            criteriaPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(byTextContainer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(byDetailsContainer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(criteriaPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(criteriaPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(byTextLabel)
                    .addComponent(byDetailsLabel)))
        );
        criteriaPanelLayout.setVerticalGroup(
            criteriaPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(criteriaPanelLayout.createSequentialGroup()
                .addComponent(byTextLabel)
                .addGap(0, 0, 0)
                .addComponent(byTextContainer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(byDetailsLabel)
                .addGap(0, 0, 0)
                .addComponent(byDetailsContainer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );

        byTextLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.byTextLabel.AccessibleContext.accessibleName")); // NOI18N

        Mnemonics.setLocalizedText(cancelChangesButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.cancelChangesButton.text")); // NOI18N

        Mnemonics.setLocalizedText(saveChangesButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.saveChangesButton.text")); // NOI18N
        saveChangesButton.setSelected(true);

        gotoPanel.setBackground(new Color(224, 224, 224));
        gotoPanel.setOpaque(false);

        Mnemonics.setLocalizedText(gotoIssueButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.gotoIssueButton.text")); // NOI18N

        issueIdTextField.setHorizontalAlignment(JTextField.CENTER);

        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.searchButton.text")); // NOI18N
        searchButton.setSelected(true);

        Mnemonics.setLocalizedText(separatorLabel2, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.separatorLabel2.text")); // NOI18N
        separatorLabel2.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

        Mnemonics.setLocalizedText(separatorLabel1, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.separatorLabel1.text")); // NOI18N
        separatorLabel1.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

        Mnemonics.setLocalizedText(saveButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.saveButton.text")); // NOI18N

        Mnemonics.setLocalizedText(webButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.webButton.text")); // NOI18N
        webButton.setActionCommand(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.webButton.actionCommand")); // NOI18N

        Mnemonics.setLocalizedText(refreshConfigurationButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.refreshConfigurationButton.text")); // NOI18N

        GroupLayout gotoPanelLayout = new GroupLayout(gotoPanel);
        gotoPanel.setLayout(gotoPanelLayout);
        gotoPanelLayout.setHorizontalGroup(
            gotoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(gotoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(searchButton)
                .addGap(11, 11, 11)
                .addComponent(gotoIssueButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(issueIdTextField, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gotoIssueInfo, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(saveButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabel1)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabel2)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(refreshConfigurationButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        gotoPanelLayout.setVerticalGroup(
            gotoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(gotoPanelLayout.createSequentialGroup()
                .addGroup(gotoPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(gotoIssueButton)
                    .addComponent(issueIdTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchButton))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(gotoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(gotoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, gotoPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(gotoPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(gotoPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(separatorLabel1)
                                .addComponent(separatorLabel2))
                            .addComponent(webButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(refreshConfigurationButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                    .addComponent(saveButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gotoIssueInfo, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        gotoPanelLayout.linkSize(SwingConstants.VERTICAL, new Component[] {separatorLabel1, separatorLabel2, webButton});

        gotoIssueButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.gotoIssueButton.AccessibleContext.accessibleDescription")); // NOI18N
        searchButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.searchButton.AccessibleContext.accessibleDescription")); // NOI18N
        saveButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.saveButton.AccessibleContext.accessibleDescription")); // NOI18N
        webButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.webButton.AccessibleContext.accessibleDescription")); // NOI18N
        refreshConfigurationButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.refreshConfigurationButton.AccessibleContext.accessibleDescription")); // NOI18N

        GroupLayout searchPanelLayout = new GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(gotoPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(searchPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addComponent(saveChangesButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelChangesButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(criteriaPanel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gotoPanel, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(criteriaPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(8, 8, 8)
                .addGroup(searchPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(saveChangesButton)
                    .addComponent(cancelChangesButton))
                .addContainerGap())
        );

        cancelChangesButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.cancelChangesButton.AccessibleContext.accessibleDescription")); // NOI18N
        saveChangesButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.saveChangesButton.AccessibleContext.accessibleDescription")); // NOI18N

        queryHeaderPanel.setBackground(UIManager.getDefaults().getColor("EditorPane.background"));
        queryHeaderPanel.setOpaque(false);

        Mnemonics.setLocalizedText(seenButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.seenButton.text")); // NOI18N

        Mnemonics.setLocalizedText(lastRefreshLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.lastRefreshLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(removeButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.removeButton.text")); // NOI18N

        Mnemonics.setLocalizedText(lastRefreshDateLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.lastRefreshDateLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(refreshButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.refreshButton.text")); // NOI18N

        Mnemonics.setLocalizedText(nameLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.nameLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(modifyButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.modifyButton.text")); // NOI18N

        Mnemonics.setLocalizedText(jLabel4, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.jLabel4.text")); // NOI18N
        jLabel4.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

        Mnemonics.setLocalizedText(jLabel5, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.jLabel5.text")); // NOI18N
        jLabel5.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

        Mnemonics.setLocalizedText(jLabel6, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.jLabel6.text")); // NOI18N
        jLabel6.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

        Mnemonics.setLocalizedText(refreshCheckBox, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.refreshCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(jLabel7, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.jLabel7.text")); // NOI18N
        jLabel7.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

        Mnemonics.setLocalizedText(findIssuesButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.findIssuesButton.text")); // NOI18N

        Mnemonics.setLocalizedText(jLabel8, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.jLabel8.text")); // NOI18N
        jLabel8.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

        Mnemonics.setLocalizedText(cloneQueryButton, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.cloneQueryButton.text")); // NOI18N

        GroupLayout queryHeaderPanelLayout = new GroupLayout(queryHeaderPanel);
        queryHeaderPanel.setLayout(queryHeaderPanelLayout);
        queryHeaderPanelLayout.setHorizontalGroup(
            queryHeaderPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(queryHeaderPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                        .addComponent(nameLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(refreshCheckBox)
                        .addGap(18, 18, 18)
                        .addComponent(lastRefreshLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lastRefreshDateLabel))
                    .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                        .addComponent(seenButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(modifyButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(7, 7, 7)
                        .addComponent(jLabel5, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(refreshButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(jLabel6)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(findIssuesButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(jLabel8)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cloneQueryButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        queryHeaderPanelLayout.setVerticalGroup(
            queryHeaderPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                .addGroup(queryHeaderPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(nameLabel))
                    .addGroup(queryHeaderPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(queryHeaderPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(lastRefreshDateLabel)
                            .addComponent(lastRefreshLabel)
                            .addComponent(refreshCheckBox))))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addGroup(queryHeaderPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel4, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(modifyButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(refreshButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(seenButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(findIssuesButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
                    .addComponent(cloneQueryButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        queryHeaderPanelLayout.linkSize(SwingConstants.VERTICAL, new Component[] {jLabel4, jLabel5, jLabel6, jLabel7, modifyButton, refreshButton, removeButton});

        seenButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.refreshButton.AccessibleContext.accessibleDescription")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.removeButton.AccessibleContext.accessibleDescription")); // NOI18N
        refreshButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.seenButton.AccessibleContext.accessibleDescription")); // NOI18N
        modifyButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.modifyButton.AccessibleContext.accessibleDescription")); // NOI18N
        refreshCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.refreshCheckBox.AccessibleContext.accessibleDescription")); // NOI18N

        noContentPanel.setBackground(UIManager.getDefaults().getColor("EditorPane.background"));
        noContentPanel.setOpaque(false);
        noContentPanel.setLayout(new GridBagLayout());

        Mnemonics.setLocalizedText(noContentLabel, NbBundle.getMessage(RedmineQueryPanel.class, "RedmineQueryPanel.noContentLabel.text")); // NOI18N
        noContentPanel.add(noContentLabel, new GridBagConstraints());

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(queryHeaderPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(searchPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(noContentPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(tableFieldsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(queryHeaderPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(searchPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableFieldsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noContentPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final JLabel assigneeLabel = new JLabel();
    final JList assigneeList = new JList();
    final JScrollPane assigneeScrollPane = new HackedScrollPane();
    final JPanel byDetailsContainer = new JPanel();
    final JLabel byDetailsLabel = new JLabel();
    final JPanel byDetailsPanel = new JPanel();
    final JPanel byTextContainer = new JPanel();
    final JLabel byTextLabel = new JLabel();
    private JPanel byTextPanel;
    final JButton cancelChangesButton = new JButton();
    final JLabel categoryLabel = new JLabel();
    final JList categoryList = new JList();
    public final LinkButton cloneQueryButton = new LinkButton();
    private JPanel criteriaPanel;
    final JComboBox filterComboBox = new JComboBox();
    private JLabel filterLabel;
    public final LinkButton findIssuesButton = new LinkButton();
    final JButton gotoIssueButton = new JButton();
    private final JLabel gotoIssueInfo = new JLabel();
    final JPanel gotoPanel = new JPanel();
    JFormattedTextField issueIdTextField;
    final JScrollPane issueTypeScrollPane = new HackedScrollPane();
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JLabel jLabel8;
    final JScrollPane jScrollPane2 = new HackedScrollPane();
    final JScrollPane jScrollPane3 = new HackedScrollPane();
    final JScrollPane jScrollPane4 = new HackedScrollPane();
    final JScrollPane jScrollPane5 = new HackedScrollPane();
    final JScrollPane jScrollPane6 = new HackedScrollPane();
    final JLabel lastRefreshDateLabel = new JLabel();
    private JLabel lastRefreshLabel;
    public final LinkButton modifyButton = new LinkButton();
    final JLabel nameLabel = new JLabel();
    private JLabel noContentLabel;
    private JPanel noContentPanel;
    final JLabel priorityLabel = new JLabel();
    final JList priorityList = new JList();
    JCheckBox qCommentsCheckBox;
    JCheckBox qDescriptionCheckBox;
    JCheckBox qSubjectCheckBox;
    private JPanel queryHeaderPanel;
    final JLabel queryLabel = new JLabel();
    final JTextField queryTextField = new JTextField();
    final LinkButton refreshButton = new LinkButton();
    final JCheckBox refreshCheckBox = new JCheckBox();
    final LinkButton refreshConfigurationButton = new LinkButton();
    public final LinkButton removeButton = new LinkButton();
    final JLabel resolutionLabel = new JLabel();
    final JList resolutionList = new JList();
    final LinkButton saveButton = new LinkButton();
    final JButton saveChangesButton = new JButton();
    final JButton searchButton = new JButton();
    final JPanel searchPanel = new JPanel();
    final LinkButton seenButton = new LinkButton();
    private JLabel separatorLabel1;
    private JLabel separatorLabel2;
    final JLabel severityLabel = new JLabel();
    final JList severityList = new JList();
    final JScrollPane severityScrollPane = new HackedScrollPane();
    final JLabel statusLabel = new JLabel();
    final JList statusList = new JList();
    private JPanel tableFieldsPanel;
    private JPanel tableHeaderPanel;
    final JPanel tablePanel = new JPanel();
    final JLabel tableSummaryLabel = new JLabel();
    final JLabel trackerLabel = new JLabel();
    final JList trackerList = new JList();
    final JLabel versionLabel = new JLabel();
    final JList versionList = new JList();
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
