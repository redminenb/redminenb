
package com.kenai.redminenb.util;

import java.awt.LayoutManager;

public class DelegatingBaseLineJPanel extends javax.swing.JPanel {

    public DelegatingBaseLineJPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    public DelegatingBaseLineJPanel(LayoutManager layout) {
        super(layout);
    }

    public DelegatingBaseLineJPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }

    public DelegatingBaseLineJPanel() {
    }

    @Override
    public int getBaseline(int width, int height) {
        if(getComponentCount() > 0) {
            return getComponent(0).getBaseline(width, height);
        } else {
            return super.getBaseline(width, height);
        }
    }  
}
