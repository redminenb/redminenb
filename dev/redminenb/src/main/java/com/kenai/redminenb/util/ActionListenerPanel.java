package com.kenai.redminenb.util;

import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;

/**
 *
 * @author Mykolas
 */
public abstract class ActionListenerPanel extends JPanel implements ActionListener {

    protected JButton okButton;
    protected JButton cancelButton;
    protected DialogDescriptor dialogDescribtor;

    public void setOkButton(JButton okButton) {
        this.okButton = okButton;
    }

    public void setCancelButton(JButton cancelButton) {
        this.cancelButton = cancelButton;
    }

    public void setDialogDescribtor(DialogDescriptor dialogDescribtor) {
        this.dialogDescribtor = dialogDescribtor;
    }
}
