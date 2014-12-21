/*
 * Copyright 2014 Matthias Bläsing.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kenai.redminenb.issue;

import com.kenai.redminenb.ui.Defaults;
import com.kenai.redminenb.user.RedmineUser;
import com.kenai.redminenb.util.ListListModel;
import com.kenai.redminenb.util.WatcherComparator;
import com.taskadapter.redmineapi.bean.Watcher;
import com.taskadapter.redmineapi.bean.WatcherFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class WatchersEditorFrame extends javax.swing.JPanel {

    public WatchersEditorFrame(List<Watcher> currentWatchers, Collection<RedmineUser> users) {
        initComponents();
        
        Set<Integer> watcherIds = new HashSet<>();
        for(Watcher w: currentWatchers) {
            watcherIds.add(w.getId());
        }
        
        List<Watcher> potentialWatchers = new ArrayList<>();
        for(RedmineUser ru: users) {
            if(! watcherIds.contains(ru.getId())) {
                Watcher w = WatcherFactory.create(ru.getId());
                w.setName(ru.getUser().getFullName());
                potentialWatchers.add(w);
            }
        }
        
        getAvailableUsersModel().addAll(potentialWatchers);
        getWatchersModel().addAll(currentWatchers);

        availableFilter.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                String filterString = availableFilter.getText();
                RowFilter rf = null;
                if(! filterString.isEmpty()) {
                    rf = RowFilter.regexFilter("(?i)(?u)" + Pattern.quote(filterString));
                }
                availableList.setRowFilter(rf);
            }
            
        });
        
        watchersFilter.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                String filterString = watchersFilter.getText();
                RowFilter rf = null;
                if(! filterString.isEmpty()) {
                    rf = RowFilter.regexFilter(Pattern.quote(filterString));
                }
                watchersList.setRowFilter(rf);
            }
            
        });
        
        addButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for(Object watcherObject: availableList.getSelectedValues()) {
                    addWatcher((Watcher) watcherObject);
                }
            }
        });
        
        removeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for(Object watcherObject: watchersList.getSelectedValues()) {
                    removeWatcher((Watcher) watcherObject);
                }
            }
        });
        
        availableList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                addButton.setEnabled(availableList.getSelectedValues().length > 0);
            }
        });
        
        watchersList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                removeButton.setEnabled(watchersList.getSelectedValues().length > 0);
            }
        });
        
        watchersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int clickIdx = watchersList.locationToIndex(e.getPoint());
                    Watcher w = (Watcher) watchersList.getElementAt(clickIdx);
                    removeWatcher(w);
                }
            }
        });
        
        availableList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int clickIdx = availableList.locationToIndex(e.getPoint());
                    Watcher w = (Watcher) availableList.getElementAt(clickIdx);
                    addWatcher(w);
                }
            }
        });
        
        watchersList.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    for (Object watcherObject : watchersList.getSelectedValues()) {
                        removeWatcher((Watcher) watcherObject);
                    }
                }
            }
            
        });
        
        availableList.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    for (Object watcherObject : availableList.getSelectedValues()) {
                        addWatcher((Watcher) watcherObject);
                    }
                }
            }
            
        });
    }

    private void removeWatcher(Watcher watcherObject) {
        getWatchersModel().remove((Watcher) watcherObject);
        getAvailableUsersModel().add((Watcher) watcherObject);
    }
    
    private void addWatcher(Watcher watcherObject) {
        getWatchersModel().add((Watcher) watcherObject);
        getAvailableUsersModel().remove((Watcher) watcherObject);
    }
    
    private ListListModel<Watcher> getAvailableUsersModel() {
        return (ListListModel<Watcher>) availableList.getModel();
    }

    private ListListModel<Watcher> getWatchersModel() {
        return (ListListModel<Watcher>) watchersList.getModel();
    }

    public List<Watcher> getWatchers() {
        List<Watcher> result = new ArrayList(getWatchersModel().getSize());
        result.addAll(getWatchersModel().getElements());
        return result;
    }
            
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonsPanel = new javax.swing.JPanel();
        removeButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        availableUsersPanel = new javax.swing.JPanel();
        availableLabel = new javax.swing.JLabel();
        availableFilterLabel = new javax.swing.JLabel();
        availableListScrollPane = new javax.swing.JScrollPane();
        availableList = new org.jdesktop.swingx.JXList();
        availableFilter = new javax.swing.JTextField();
        watchersPanel = new javax.swing.JPanel();
        watchesFilterLabel = new javax.swing.JLabel();
        watchersLabel = new javax.swing.JLabel();
        watchersFilter = new javax.swing.JTextField();
        watchersListScrollPane = new javax.swing.JScrollPane();
        watchersList = new org.jdesktop.swingx.JXList();

        setLayout(new java.awt.GridBagLayout());

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(WatchersEditorFrame.class, "WatchersEditorFrame.removeButton.text")); // NOI18N
        removeButton.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        buttonsPanel.add(removeButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(WatchersEditorFrame.class, "WatchersEditorFrame.addButton.text")); // NOI18N
        addButton.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
        buttonsPanel.add(addButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        add(buttonsPanel, gridBagConstraints);

        availableUsersPanel.setMinimumSize(new java.awt.Dimension(20, 20));
        availableUsersPanel.setPreferredSize(new java.awt.Dimension(200, 100));
        availableUsersPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(availableLabel, org.openide.util.NbBundle.getMessage(WatchersEditorFrame.class, "WatchersEditorFrame.availableLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        availableUsersPanel.add(availableLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(availableFilterLabel, org.openide.util.NbBundle.getMessage(WatchersEditorFrame.class, "WatchersEditorFrame.availableFilterLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        availableUsersPanel.add(availableFilterLabel, gridBagConstraints);

        availableList.setModel(new ListListModel<Watcher>());
        availableList.setAutoCreateRowSorter(true);
        availableList.setCellRenderer(new Defaults.WatcherLCR());
        availableList.setComparator(new WatcherComparator()
        );
        availableList.setSortOrder(javax.swing.SortOrder.ASCENDING);
        availableListScrollPane.setViewportView(availableList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        availableUsersPanel.add(availableListScrollPane, gridBagConstraints);

        availableFilter.setText(org.openide.util.NbBundle.getMessage(WatchersEditorFrame.class, "WatchersEditorFrame.availableFilter.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        availableUsersPanel.add(availableFilter, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(availableUsersPanel, gridBagConstraints);

        watchersPanel.setMinimumSize(new java.awt.Dimension(20, 20));
        watchersPanel.setPreferredSize(new java.awt.Dimension(200, 100));
        watchersPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(watchesFilterLabel, org.openide.util.NbBundle.getMessage(WatchersEditorFrame.class, "WatchersEditorFrame.watchesFilterLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        watchersPanel.add(watchesFilterLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(watchersLabel, org.openide.util.NbBundle.getMessage(WatchersEditorFrame.class, "WatchersEditorFrame.watchersLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        watchersPanel.add(watchersLabel, gridBagConstraints);

        watchersFilter.setText(org.openide.util.NbBundle.getMessage(WatchersEditorFrame.class, "WatchersEditorFrame.watchersFilter.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        watchersPanel.add(watchersFilter, gridBagConstraints);

        watchersList.setModel(new ListListModel<Watcher>());
        watchersList.setAutoCreateRowSorter(true);
        watchersList.setCellRenderer(new Defaults.WatcherLCR());
        watchersList.setComparator(new WatcherComparator());
        watchersList.setSortOrder(javax.swing.SortOrder.ASCENDING);
        watchersListScrollPane.setViewportView(watchersList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        watchersPanel.add(watchersListScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(watchersPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JTextField availableFilter;
    private javax.swing.JLabel availableFilterLabel;
    private javax.swing.JLabel availableLabel;
    private org.jdesktop.swingx.JXList availableList;
    private javax.swing.JScrollPane availableListScrollPane;
    private javax.swing.JPanel availableUsersPanel;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton removeButton;
    private javax.swing.JTextField watchersFilter;
    private javax.swing.JLabel watchersLabel;
    private org.jdesktop.swingx.JXList watchersList;
    private javax.swing.JScrollPane watchersListScrollPane;
    private javax.swing.JPanel watchersPanel;
    private javax.swing.JLabel watchesFilterLabel;
    // End of variables declaration//GEN-END:variables
}
