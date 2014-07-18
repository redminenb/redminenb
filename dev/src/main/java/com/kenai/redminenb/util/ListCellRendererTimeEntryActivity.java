package com.kenai.redminenb.util;

import com.taskadapter.redmineapi.bean.TimeEntryActivity;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class ListCellRendererTimeEntryActivity extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof TimeEntryActivity) {
            value = ((TimeEntryActivity) value).getName();
        } else {
            if (value == null) {
                value = " ";
            }
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
