
package com.kenai.redminenb.util;

import com.kenai.redminenb.issue.RedmineIssue;
import com.taskadapter.redmineapi.bean.Attachment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NotImplementedException;

public class AttachmentDisplay extends DelegatingBaseLineJPanel implements ActionListener {
    private static File lastDirectory;
    private final String COMMAND_DELETE = "delete";
    private final String COMMAND_DOWNLOAD = "download";
    private final Attachment ad;
    private final RedmineIssue issue;
    private final JLabel leadingLabel = new JLabel();
//  Delete Button currently commented out, as API show no valid way to remotly
//  delete an attachment
//  @todo: Fix this when http://www.redmine.org/issues/14828 is resolved
//    private final LinkButton deleteButton = new LinkButton();
//    private final JLabel medianLabel = new JLabel();
    private final LinkButton downloadButton = new LinkButton();
    private final JLabel trailingLabel = new JLabel();

    public AttachmentDisplay(RedmineIssue issue, Attachment ad) {
        super();
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.ad = ad;
        this.issue = issue;
        leadingLabel.setText(String.format(
                "%1$s (%2$d bytes) %3$tY-%3$tm-%3$td %3$tH:%3$tM [",
                ad.getFileName(),
                ad.getFileSize(),
                ad.getCreatedOn()));
//        medianLabel.setText(", ");
        trailingLabel.setText("]");
        this.add(leadingLabel);
        this.add(downloadButton);
//        this.add(medianLabel);
//        this.add(deleteButton);
        this.add(trailingLabel);
//        deleteButton.setBorder(null);
//        deleteButton.setText("delete");
//        deleteButton.addActionListener(this);
//        deleteButton.setActionCommand(COMMAND_DELETE);
        downloadButton.setBorder(null);
        downloadButton.setText("download");
        downloadButton.addActionListener(this);
        downloadButton.setActionCommand(COMMAND_DOWNLOAD);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(COMMAND_DELETE.equals(e.getActionCommand())) {
            throw new NotImplementedException();
        }
        if (COMMAND_DOWNLOAD.equals(e.getActionCommand())) {
            try {
                JFileChooser fileChooser = new JFileChooser(lastDirectory);
                fileChooser.setDialogTitle("Save attachment");
                File preselected;
                if (lastDirectory != null && lastDirectory.canWrite()) {
                    preselected = new File(lastDirectory, ad.getFileName());
                } else {
                    preselected = new File(ad.getFileName());
                }
                fileChooser.setSelectedFile(preselected);
                int result = fileChooser.showSaveDialog(this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    FileOutputStream fos = null;
                    try {
                        lastDirectory = fileChooser.getCurrentDirectory();
                        fos = new FileOutputStream(fileChooser.getSelectedFile());
                        issue.getRepository().getManager().downloadAttachmentContent(ad, fos);
                        fos.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        try {
                            if (fos != null) {
                                fos.close();
                            }
                        } catch (IOException ex) {
                        }
                    }
                }
            } catch (Exception ex) {
                NotifyDescriptor nd = new NotifyDescriptor.Exception(ex,
                        "Failed to retrieve attachment from issue");
                DialogDisplayer.getDefault().notifyLater(nd);
            }
        }
    }
}
