/*
 * Copyright 2014 Matthias Bläsing
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

import com.kenai.redminenb.repository.RedmineRepository;
import com.kenai.redminenb.util.AssigneeWrapper;
import com.kenai.redminenb.util.NestedProject;
import static com.kenai.redminenb.util.markup.StringUtil.escapeHTML;
import com.kenai.redminenb.util.markup.TextileUtil;
import com.taskadapter.redmineapi.bean.CustomFieldDefinition;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.IssuePriority;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Journal;
import com.taskadapter.redmineapi.bean.JournalDetail;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.Version;
import java.io.StringWriter;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.commons.lang.StringUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author matthias
 */
public class JournalDisplay extends javax.swing.JPanel {
    public static class JournalData {
        private final Integer id;
        private final Integer pos;
        private final String username;
        private final Date create;
        private final String htmlContent;

        public JournalData(Integer id, Integer pos, String username, Date create, String htmlContent) {
            this.id = id;
            this.pos = pos;
            this.username = username;
            this.create = (Date) create.clone();
            this.htmlContent = htmlContent;
        }
        
        public Integer getId() {
            return id;
        }

        public Integer getPos() {
            return pos;
        }

        public String getUsername() {
            return username;
        }

        public Date getCreate() {
            return (Date) create.clone();
        }

        public String getHtmlContent() {
            return htmlContent;
        }
        
    }
    
    public JournalDisplay(JournalData jd) {
        initComponents();
        
        leftLabel.setText(NbBundle.getMessage(JournalDisplay.class, 
                "journalDisplay.leftTemplate", new Object[]{jd.getUsername(), jd.getCreate()}));
        leftLabel.setToolTipText(NbBundle.getMessage(JournalDisplay.class, 
                "journalDisplay.leftTemplateTooltip", new Object[]{jd.getUsername(), jd.getCreate()}));
        rightLabel.setText(NbBundle.getMessage(JournalDisplay.class, 
                "journalDisplay.rightTemplate", new Object[]{jd.getId(), jd.getPos()}));
        rightLabel.setToolTipText(NbBundle.getMessage(JournalDisplay.class, 
                "journalDisplay.rightTemplateTooltip", new Object[]{jd.getId(), jd.getPos()}));

        content.setHTMLText(jd.getHtmlContent());
    }

    public static JournalData buildJournalData(RedmineIssue ri, Journal jd, int index) {
        RedmineRepository repo = ri.getRepository();
        
        String noteText = jd.getNotes();
        StringWriter writer = new StringWriter();

        if (jd.getDetails() != null && jd.getDetails().size() > 0) {
            writer.append("<ul>");
            for (JournalDetail detail : jd.getDetails()) {
                writer.append("<li>");
                String fieldName = detail.getName();
                String translatedFieldName = fieldName;
                try {
                    translatedFieldName = NbBundle.getMessage(JournalDisplay.class, "field." + fieldName);
                } catch (MissingResourceException ex) {
                    // Ok, was not translated
                }

                String oldValue = detail.getOldValue();
                String newValue = detail.getNewValue();

                switch(fieldName) {
                    case "category_id":
                        oldValue = formatCategory(repo, ri, oldValue);
                        newValue = formatCategory(repo, ri, newValue);
                        break;
                    case "fixed_version_id":
                        oldValue = formatVersion(repo, ri, oldValue);
                        newValue = formatVersion(repo, ri, newValue);
                        break;
                    case "priority_id":
                        oldValue = formatPriority(repo, oldValue);
                        newValue = formatPriority(repo, newValue);
                        break;
                    case "status_id":
                        oldValue = formatStatus(repo, oldValue);
                        newValue = formatStatus(repo, newValue);
                        break;
                    case "tracker_id":
                        oldValue = formatTracker(repo, oldValue);
                        newValue = formatTracker(repo, newValue);
                        break;
                    case "assigned_to_id":
                        oldValue = formatUser(repo, ri, oldValue);
                        newValue = formatUser(repo, ri, newValue);
                        break;
                    case "project_id":
                        oldValue = formatProject(repo, ri, oldValue);
                        newValue = formatProject(repo, ri, newValue);
                        break;
                    default:
                        // Identity (silence findbugs)
                        break;
                }
                
                if("cf".equals(detail.getProperty())) {
                    try {
                        int fieldId = Integer.parseInt(fieldName);
                        CustomFieldDefinition cfd = ri.getRepository()
                                .getCustomFieldDefinitionById(fieldId);
                        if (cfd != null) {
                            translatedFieldName = cfd.getName();
                            switch (cfd.getFieldFormat()) {
                                case "user":
                                    oldValue = formatUser(repo, ri, oldValue);
                                    newValue = formatUser(repo, ri, newValue);
                                    break;
                                case "version":
                                    oldValue = formatVersion(repo, ri, oldValue);
                                    newValue = formatVersion(repo, ri, newValue);
                                    break;
                                case "bool":
                                    oldValue = formatBool(repo, ri, oldValue);
                                    newValue = formatBool(repo, ri, newValue);
                                    break;
                                default:
                                    // Identity (silence findbugs)
                                    break;
                            }
                        } else {
                            translatedFieldName = "Custom field ID " + fieldId;
                        }
                    } catch (NumberFormatException ex) {
                    }
                }
                
                Object[] formatParams = new Object[]{
                    escapeHTML(fieldName),
                    escapeHTML(translatedFieldName),
                    escapeHTML(oldValue),
                    escapeHTML(newValue)
                };
                
                ResourceBundle rb = NbBundle.getBundle(JournalDisplay.class);
                
                String key;
                String alternativeKey;
                
                if ("description".equals(fieldName) 
                        || (oldValue == null && newValue == null)) {
                    key = detail.getProperty() + ".baseChanged";
                    alternativeKey = "attr.baseChanged";
                } else if  (oldValue != null && newValue != null) {
                    key = detail.getProperty() + ".changed";
                    alternativeKey = "attr.changed";
                } else if (oldValue != null) {
                    key = detail.getProperty() + ".deleted";
                    alternativeKey = "attr.deleted";
                } else {
                    key = detail.getProperty() + ".added";
                    alternativeKey = "attr.added";
                }
                
                String info;
                if(! rb.containsKey(key)) {
                    info = NbBundle.getMessage(JournalDisplay.class, alternativeKey, formatParams);
                } else {
                    info = NbBundle.getMessage(JournalDisplay.class, key, formatParams);
                }
                
                writer.append(info);
                writer.append("</li>");
            }
            writer.append("</ul>");
        }
        
        if (StringUtils.isNotBlank(noteText)) {
            writer.append("<div class='note'>");
            TextileUtil.convertToHTML(noteText, writer);
            writer.append("</div>");
        }

        return new JournalData(
                jd.getId(),
                index + 1,
                jd.getUser().getFullName(),
                jd.getCreatedOn(),
                writer.toString()
        );
    }
    
    private static String formatCategory(RedmineRepository repo, RedmineIssue issue, String value) {
        if(value == null) {
            return null;
        }
        try {
            Integer id = Integer.valueOf(value);
            for (IssueCategory ic : repo.getIssueCategories(issue.getIssue().getProject())) {
                if (ic.getId().equals(id)) {
                    return ic.getName() + " (ID: " + id.toString() + ")";
                }
            }
        } catch (NumberFormatException ex) {}
        return "(ID: " + value + ")";
    }
    
    private static String formatVersion(RedmineRepository repo, RedmineIssue issue, String value) {
        if(value == null) {
            return null;
        }
        try {
            Integer id = Integer.valueOf(value);
            for (Version v : repo.getVersions(issue.getIssue().getProject())) {
                if (v.getId().equals(id)) {
                    return v.getName() + " (ID: " + id.toString() + ")";
                }
            }
        } catch (NumberFormatException ex) {}
        return "(ID: " + value + ")";
    }
    
    private static String formatPriority(RedmineRepository repo, String value) {
        if(value == null) {
            return null;
        }
        try {
            Integer id = Integer.valueOf(value);
            for (IssuePriority ip : repo.getIssuePriorities()) {
                if (ip.getId().equals(id)) {
                    return ip.getName() + " (ID: " + id.toString() + ")";
                }
            }
        } catch (NumberFormatException ex) {}
        return "(ID: " + value + ")";
    }
    
    private static String formatStatus(RedmineRepository repo, String value) {
        if(value == null) {
            return null;
        }
        try {
            Integer id = Integer.valueOf(value);
            for (IssueStatus is : repo.getStatuses()) {
                if (is.getId().equals(id)) {
                    return is.getName() + " (ID: " + id.toString() + ")";
                }
            }
        } catch (NumberFormatException ex) {}
        return "(ID: " + value + ")";
    }
    
    private static String formatTracker(RedmineRepository repo, String value) {
        if(value == null) {
            return null;
        }
        try {
            Integer id = Integer.valueOf(value);
            for (Tracker t : repo.getTrackers()) {
                if (t.getId().equals(id)) {
                    return t.getName() + " (ID: " + id.toString() + ")";
                }
            }
        } catch (NumberFormatException ex) {}
        return "(ID: " + value + ")";
    }
    
    private static String formatUser(RedmineRepository repo, RedmineIssue issue, String value) {
        if(value == null) {
            return null;
        }
        try {
            Integer id = Integer.valueOf(value);
            for (AssigneeWrapper ru : repo.getAssigneeWrappers(issue.getIssue().getProject())) {
                if (ru.getId().equals(id)) {
                    return ru.getName() + " (ID: " + id.toString() + ")";
                }
            }
        } catch (NumberFormatException ex) {}
        return "(ID: " + value + ")";
    }
    
    private static String formatProject(RedmineRepository repo, RedmineIssue issue, String value) {
        if(value == null) {
            return null;
        }
        try {
            Integer id = Integer.valueOf(value);
            NestedProject np = repo.getProjects().get(id);
            return np.toString() + " (ID: " + id.toString() + ")";
        } catch (NumberFormatException | NullPointerException ex) {}
        return "(ID: " + value + ")";
    }
    
    private static String formatBool(RedmineRepository repo, RedmineIssue issue, String value) {
        if (value == null) {
            return null;
        }
        switch(value) {
            case "0":
                return "No";
            case "1":
                return "Yes";
            default:
                return null;
        }
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

        leftLabel = new javax.swing.JLabel();
        rightLabel = new javax.swing.JLabel();
        separator = new javax.swing.JSeparator();
        content = new com.kenai.redminenb.util.markup.TextilePreview();

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        leftLabel.setFont(leftLabel.getFont().deriveFont(leftLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(leftLabel, org.openide.util.NbBundle.getMessage(JournalDisplay.class, "JournalDisplay.leftLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        add(leftLabel, gridBagConstraints);

        rightLabel.setFont(rightLabel.getFont().deriveFont(rightLabel.getFont().getStyle() | java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(rightLabel, org.openide.util.NbBundle.getMessage(JournalDisplay.class, "JournalDisplay.rightLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        add(rightLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(separator, gridBagConstraints);

        content.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(content, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.kenai.redminenb.util.markup.TextilePreview content;
    private javax.swing.JLabel leftLabel;
    private javax.swing.JLabel rightLabel;
    private javax.swing.JSeparator separator;
    // End of variables declaration//GEN-END:variables
}
