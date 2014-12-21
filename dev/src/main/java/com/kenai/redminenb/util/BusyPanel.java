/*
 * Copyright 2014 Matthias Bläsing <mblaesing@doppel-helix.eu>.
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
package com.kenai.redminenb.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class BusyPanel extends JPanel implements ActionListener {
    private static final String CMD_HIDE = "hide";
    private static final String CMD_SHOW = "show";
    private final NoopListener noopListener = new NoopListener();
    private Timer hideShowTimer = null;

    public BusyPanel() {
        super(new BorderLayout());
        JLabel label = new JLabel("Busy");
        label.setFont(label.getFont().deriveFont(label.getFont().getStyle()
                & java.awt.Font.BOLD,
                AffineTransform.getScaleInstance(4, 4)));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label);
        setFocusable(true);
        setOpaque(false);
        super.setVisible(false);
        // Swallow Mouse + Keyboard events
        addMouseListener(noopListener);
        addKeyListener(noopListener);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(0.5f, 0.5f, 0.5f, 0.5f));
        Rectangle r = getBounds();
        g2.fillRect((int) r.getX(), (int) r.getY(), (int) r.getWidth(), (int) r.getHeight());
        super.paintComponent(g);
    }

    @Override
    public void setVisible(boolean flag) {
        if (hideShowTimer != null) {
            if ((flag) && CMD_HIDE.equals(hideShowTimer.getActionCommand())) {
                hideShowTimer.stop();
                hideShowTimer = null;
            } else if ((! flag) && CMD_SHOW.equals(hideShowTimer.getActionCommand())) {
                hideShowTimer.stop();
                hideShowTimer = null;
            }
        } else {
            if (flag) {
                hideShowTimer = new Timer(200, this);
                hideShowTimer.setActionCommand(CMD_SHOW);
            } else {
                hideShowTimer = new Timer(200, this);
                hideShowTimer.setActionCommand(CMD_HIDE);
            }
            hideShowTimer.setRepeats(false);
            hideShowTimer.start();
        }
    }
    
    private void realSetVisible(boolean flag) {
        super.setVisible(flag);
        requestFocus();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(CMD_SHOW.equals(e.getActionCommand())) {
            realSetVisible(true);
            hideShowTimer = null;
        } else if (CMD_HIDE.equals(e.getActionCommand())) {
            realSetVisible(false);
            hideShowTimer = null;
        }
    }
    
}
