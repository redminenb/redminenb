package com.kenai.redminenb.util.markup;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.apache.commons.lang.StringUtils;
import org.openide.util.Exceptions;

public class TextilePreview extends JScrollPane {

    private boolean resizing = false;
    private final JTextPane htmlOutputLabel = new JTextPane();

    public TextilePreview() {
        this.setBorder(new EmptyBorder(0, 0, 0, 0));
        this.setOpaque(false);
        
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet ss = new StyleSheet();
        try {
            ss.loadRules(new InputStreamReader(
                    TextilePreview.class.getResourceAsStream("/com/kenai/redminenb/util/markup/style.css"),
                    StandardCharsets.UTF_8),
                    new URL("file:///"));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        kit.setStyleSheet(ss);
        htmlOutputLabel.setOpaque(false);
        htmlOutputLabel.setEditable(false);
        htmlOutputLabel.setContentType("text/html");
        htmlOutputLabel.setEditorKit(kit);
        htmlOutputLabel.setDocument(kit.createDefaultDocument());
        htmlOutputLabel.setFont(new JLabel().getFont());
        htmlOutputLabel.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        htmlOutputLabel.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        this.setViewportView(htmlOutputLabel);
        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                refreshHeight();
            }

        });

        this.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                Container comp = TextilePreview.this.getParent();
                if(comp != null) {
                    comp.dispatchEvent(e);
                }
            }
        });
    }

    private void refreshHeight() {
        if (resizing) {
            return;
        }
        resizing = true;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Insets insets = TextilePreview.this.getInsets();
                TextilePreview.this.setPreferredSize(
                        new Dimension(1,
                                htmlOutputLabel.getPreferredSize().height
                                + insets.bottom
                                + insets.top));
            }
        });
        resizing = false;
    }

    public void setTextileText(String text) {
        if (StringUtils.isNotBlank(text)) {
            text = TextileUtil.convertToHTML(text);
        }
        setHTMLText(text);
    }

    public void setHTMLText(String text) {
        htmlOutputLabel.setText("<html>" + text + "</html>");
        refreshHeight();
    }
}
