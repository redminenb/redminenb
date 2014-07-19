package com.kenai.redminenb.util;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicTreeUI;

public class ExpandablePanel {

    private final JComponent panel;
    private final JLabel label;
    private final Icon ei;
    private final Icon ci;
    private boolean expaned = true;

    public ExpandablePanel(JLabel l, JComponent p) {
        BasicTreeUI tvui = (BasicTreeUI) new JTree().getUI();
        ei = tvui.getExpandedIcon();
        ci = tvui.getCollapsedIcon();

        this.panel = p;
        this.label = l;
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

        expaned = p.isVisible();
        updateIcon();
    }

    public void expand() {
        expaned = true;
        panel.setVisible(true);
        updateIcon();
    }

    public void colapse() {
        expaned = false;
        panel.setVisible(false);
        updateIcon();
    }

    public void setVisible(boolean visible) {
        label.setVisible(visible);
        panel.setVisible(visible && expaned);
    }
    
    private void updateIcon() {
        if(expaned) {
            label.setIcon(ei);
        } else {
            label.setIcon(ci);
        }
    }
}
