/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kenai.redminenb.util;

import com.kenai.redminenb.RedmineConfig;
import com.taskadapter.redmineapi.bean.Tracker;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author Matthias Bläsing
 */
public class TableCellRendererPriority extends DefaultTableCellRenderer{

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (c instanceof JLabel && value instanceof String) {
            JLabel label = (JLabel) c;
            label.setIcon(RedmineConfig.getInstance().getPriorityIcon((String) value));
        }
        return c;
    }
    
}
