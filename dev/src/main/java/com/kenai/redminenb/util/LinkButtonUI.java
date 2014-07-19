/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kenai.redminenb.util;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.TextAttribute;
import java.util.Map;
import static javafx.scene.text.Font.font;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 *
 * @author matthias
 */
public class LinkButtonUI extends BasicButtonUI implements java.io.Serializable,
        MouseListener, FocusListener {

//    private final Color baseColor = Color.decode("0x0645AD");
//    
    private final Color baseColor = Color.BLUE;
    private final Color activeColor = Color.decode("0x0B0080");
    private final static LinkButtonUI INSTANCE = new LinkButtonUI();

    public LinkButtonUI() {

    }

    public static ComponentUI createUI(JComponent c) {
        return INSTANCE;
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        JButton jbutton = (JButton) c;
        Map attributes = jbutton.getFont().getAttributes();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        jbutton.setFont(jbutton.getFont().deriveFont(attributes));
        jbutton.setBorderPainted(false);
        jbutton.setOpaque(false);
        jbutton.setForeground(baseColor);
        jbutton.addMouseListener(this);
        jbutton.addFocusListener(this);
        jbutton.repaint();
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        c.removeMouseListener(this);
        c.removeFocusListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        e.getComponent().setForeground(baseColor);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        JComponent c = (JComponent) e.getComponent();
        c.setForeground(activeColor);
        c.repaint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        JComponent c = (JComponent) e.getComponent();
        c.setForeground(baseColor);
        c.repaint();
    }

    @Override
    public void focusGained(FocusEvent e) {
        JComponent c = (JComponent) e.getComponent();
        c.setForeground(activeColor);
        c.repaint();
    }

    @Override
    public void focusLost(FocusEvent e) {
        JComponent c = (JComponent) e.getComponent();
        c.setForeground(baseColor);
        c.repaint();
    }

}
