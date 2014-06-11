/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.redminenb.issue;

import com.kenai.redminenb.util.markup.TextileUtil;
import com.taskadapter.redmineapi.bean.Journal;
import com.taskadapter.redmineapi.bean.JournalDetail;
import java.awt.Dimension;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.openide.util.NbBundle;

/**
 *
 * @author matthias
 */
public class JournalDisplay extends javax.swing.JPanel {
    private final MessageFormat leftTemplate= new MessageFormat("Updated by {0} on {1,date,medium}, {1,time,medium}");
    private final MessageFormat rightTemplate= new MessageFormat("# {0, number, integer}");
    
    /**
     * Creates new form JournalDisplay
     */
    public JournalDisplay() {
        initComponents();
    }
    
    public JournalDisplay(Journal jd) {
        initComponents();
        setJournalData(jd);
    }
    
    public synchronized void setJournalData(Journal jd) {
        leftLabel.setText(leftTemplate.format(new Object[]{
            jd.getUser().getFullName(),
            jd.getCreatedOn()}));
        rightLabel.setText(rightTemplate.format(new Object[]{jd.getId()}));
        String noteText = jd.getNotes();
        StringWriter writer = new StringWriter();
        if (StringUtils.isNotBlank(noteText)) {
            HtmlDocumentBuilder builder = new HtmlDocumentBuilder(writer);
            // avoid the <html> and <body> tags
            builder.setEmitAsDocument(false);

            MarkupParser parser = TextileUtil.getTextileMarkupParser();
            parser.setBuilder(builder);
            parser.parse(noteText);
            parser.setBuilder(null);
        }

        if (jd.getDetails() != null && jd.getDetails().size() > 0) {
            writer.append("<ul style='padding-top: 0px; margin-top: 0px; padding-bottom: 0px; margin-bottom: 0px'>");
            for (JournalDetail detail : jd.getDetails()) {
                writer.append("<li>");
                String fieldName = detail.getName();
                String translatedFieldName = fieldName;
                try {
                    translatedFieldName = NbBundle.getMessage(JournalDisplay.class, "field." + fieldName);
                } catch (MissingResourceException ex) {
                    // Ok, was not translated
                }
                
                Object[] formatParams = new Object[]{
                    escapeHTML(fieldName),
                    escapeHTML(translatedFieldName),
                    escapeHTML(detail.getOldValue()),
                    escapeHTML(detail.getNewValue())
                };
                
                String info;
                
                if (detail.getName().equals("description")) {
                    info = NbBundle.getMessage(JournalDisplay.class, detail.getProperty() + ".baseChanged", formatParams);
                } else if  (detail.getOldValue() != null && detail.getNewValue() != null) {
                    info = NbBundle.getMessage(JournalDisplay.class, detail.getProperty() + ".detailChanged", formatParams);
                } else if (detail.getOldValue() != null) {
                    info = NbBundle.getMessage(JournalDisplay.class, detail.getProperty() + ".deleted", formatParams);
                } else {
                    info = NbBundle.getMessage(JournalDisplay.class, detail.getProperty() + ".added", formatParams);
                }

                writer.append(info);
                writer.append("</li>");
            }
            writer.append("</ul>");
        }
        String output = writer.toString();
        if(output.length() > 0) {
            output = "<html><body>" + output + "</body></html>";
        }
        contentLabel.setText(output);
        this.validate();
        this.invalidate();
        Dimension leftDimension = leftLabel.getPreferredSize();
        Dimension rightDimension = rightLabel.getPreferredSize();
        Dimension contentDimension = contentLabel.getPreferredSize();
        Dimension seperatorDimension = separator.getPreferredSize();
        int width = (int) Math.max(
                leftDimension.getWidth() + rightDimension.getWidth() + 10,
                contentDimension.getWidth() + 10);
        int height = (int) (Math.max(leftDimension.getHeight(), rightDimension.getHeight())
                + seperatorDimension.getHeight()
                + contentDimension.getHeight());
        Dimension d = new Dimension(width, height);
        setMinimumSize(d);
        setPreferredSize(d);
    }

    private String escapeHTML(String input) {
        if(input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27")
                ;
        
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
        contentLabel = new javax.swing.JLabel();

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

        contentLabel.setFont(contentLabel.getFont().deriveFont(contentLabel.getFont().getStyle() & ~java.awt.Font.BOLD));
        org.openide.awt.Mnemonics.setLocalizedText(contentLabel, org.openide.util.NbBundle.getMessage(JournalDisplay.class, "JournalDisplay.contentLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(contentLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel contentLabel;
    private javax.swing.JLabel leftLabel;
    private javax.swing.JLabel rightLabel;
    private javax.swing.JSeparator separator;
    // End of variables declaration//GEN-END:variables
}
