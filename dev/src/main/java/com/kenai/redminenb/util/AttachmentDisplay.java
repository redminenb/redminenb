package com.kenai.redminenb.util;

import com.kenai.redminenb.issue.RedmineIssue;
import com.taskadapter.redmineapi.bean.Attachment;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutionException;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public class AttachmentDisplay extends DelegatingBaseLineJPanel implements ActionListener {

    private static File lastDirectory;
//    private static final String COMMAND_DELETE = "delete";
    private static final String COMMAND_DOWNLOAD = "download";
    private final Attachment ad;
    private final RedmineIssue issue;
    private final JLabel leadingLabel = new JLabel();
//  Delete Button currently commented out, as API show no valid way to remotly
//  delete an attachment
//  @todo: Fix this when http://www.redmine.org/issues/14828 is resolved
//    private final LinkButton deleteButton = new LinkButton();
    private final LinkButton downloadButton = new LinkButton();

    public AttachmentDisplay(RedmineIssue issue, Attachment ad) {
        super();
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.ad = ad;
        this.issue = issue;
        leadingLabel.setText(String.format(
                "%1$s (%2$d bytes) %3$tY-%3$tm-%3$td %3$tH:%3$tM ",
                ad.getFileName(),
                ad.getFileSize(),
                ad.getCreatedOn()));
        leadingLabel.setToolTipText(ad.getDescription());
        this.add(leadingLabel);
        this.add(downloadButton);
//        this.add(deleteButton);
//        deleteButton.setBorder(null);
//        deleteButton.setText("delete");
//        deleteButton.addActionListener(this);
//        deleteButton.setActionCommand(COMMAND_DELETE);
        downloadButton.setBorder(null);
        downloadButton.setIcon(new javax.swing.ImageIcon(AttachmentDisplay.class.getResource("/com/kenai/redminenb/resources/document-save.png")));
        downloadButton.setToolTipText("download");
        downloadButton.addActionListener(this);
        downloadButton.setActionCommand(COMMAND_DOWNLOAD);
    }

    @Override
    @SuppressFBWarnings(
            value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", 
            justification = "Intended behaviour - if there is a race in lastDirectory reading/writing it is ok as it is only help for user")
    public void actionPerformed(ActionEvent e) {
//        if (COMMAND_DELETE.equals(e.getActionCommand())) {
//            throw new NotImplementedException();
//        }
        if (COMMAND_DOWNLOAD.equals(e.getActionCommand())) {
            JFileChooser fileChooser = new JFileChooser(lastDirectory);
            fileChooser.setDialogTitle("Save attachment");
            File preselected;
            File lastDirectoryBuffer = lastDirectory;
            if (lastDirectoryBuffer != null && lastDirectoryBuffer.canWrite()) {
                preselected = new File(lastDirectoryBuffer, ad.getFileName());
            } else {
                preselected = new File(ad.getFileName());
            }
            fileChooser.setSelectedFile(preselected);
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                lastDirectory = fileChooser.getCurrentDirectory();
                final File selectedFile = fileChooser.getSelectedFile();

                new SwingWorker() {

                    @Override
                    protected Object doInBackground() throws Exception {
                        try (FileOutputStream fos = new FileOutputStream(selectedFile)) {
                            issue.getRepository().getAttachmentManager().downloadAttachmentContent(ad, fos);
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get();
                        } catch (ExecutionException ex) {
                            NotifyDescriptor nd = new NotifyDescriptor.Exception(ex.getCause(),
                                    "Failed to retrieve attachment from issue");
                            DialogDisplayer.getDefault().notifyLater(nd);
                        } catch (InterruptedException ex) {
                            NotifyDescriptor nd = new NotifyDescriptor.Exception(ex,
                                    "Failed to retrieve attachment from issue");
                            DialogDisplayer.getDefault().notifyLater(nd);
                        }
                    }
                };
            }

        }
    }
}
