
package com.kenai.redminenb.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

import javax.swing.UIManager;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.MattePainter;

public class DatePicker extends JXDatePicker {
    private static final Logger LOG = Logger.getLogger(DatePicker.class.getName());

    public DatePicker() {
        super();
        setLinkDay(new Date());
    }
    
    @Override
    public void setLinkPanel(JPanel linkPanel) {
        // Fucking ugly hack!
        Boolean dark = (Boolean) UIManager.get("nb.dark.theme");
        if(dark != null && dark) {
            // Jay - someone decided hardcoding colors was a good idea *arg*
            if(! (linkPanel instanceof JXPanel)) {
                LOG.log(Level.INFO, 
                        "Failed to modify LinkPanel for JXDatePicker - expected JXPanel but got: {0}", 
                        linkPanel.getClass());
            }
            JXPanel jXPanel = (JXPanel) linkPanel;
            jXPanel.setBackgroundPainter(new MattePainter(new GradientPaint(0, 0, new Color(17, 17, 17), 0, 1, Color.BLACK)));
            Component c = jXPanel.getComponent(0);
            if(! (c instanceof JXHyperlink)) {
                LOG.log(Level.INFO, 
                        "Failed to modify LinkPanel for JXDatePicker - expected JXHyperLink but got: {0}", 
                        c.getClass());
            }
            JXHyperlink linkButton = (JXHyperlink) c;
            linkButton.setClickedColor(new Color(170, 170, 170));
            linkButton.setUnclickedColor(new Color(170, 170, 170));
        }
        super.setLinkPanel(linkPanel);
    }
    
}
