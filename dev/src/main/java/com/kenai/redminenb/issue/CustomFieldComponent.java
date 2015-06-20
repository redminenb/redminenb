package com.kenai.redminenb.issue;

import com.kenai.redminenb.util.DelegatingBaseLineJPanel;
import com.kenai.redminenb.util.markup.StringUtil;
import com.taskadapter.redmineapi.bean.CustomFieldDefinition;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.WrapLayout;

public abstract class CustomFieldComponent extends DelegatingBaseLineJPanel implements ActionListener {
    private static final Logger LOG = Logger.getLogger(CustomFieldComponent.class.getName());
    
    private enum Type {
        display_only,
        bool_field,
        date_field,
        float_field,
        int_field,
        link_field,
        list_field,
        text_field,
        string_field,
        user_field,
        version_field
    }

    public static CustomFieldComponent create(CustomFieldDefinition cfd) {
        String type = cfd.getFieldFormat();
        Type resolvedType = Type.display_only;
        try {
            resolvedType = Type.valueOf(type + "_field");
        } catch (IllegalArgumentException ex) {
            LOG.info("Failed to resolve type: " + type);
        }

        switch (resolvedType) {
            case date_field:
                return new CustomFieldComponentDate(cfd);
            case string_field:
            case link_field:
                return new CustomFieldComponentLine(cfd);
            case text_field:
                return new CustomFieldComponentLongText(cfd);
            case list_field:
                return new CustomFieldComponentList(cfd);
            case bool_field:
                return new CustomFieldComponentBool(cfd);
            case int_field:
                return new CustomFieldComponentNumeric(cfd, false);
            case float_field:
                return new CustomFieldComponentNumeric(cfd, true);
            case version_field:
            case user_field:
                return new CustomFieldComponentListId(cfd);
            default:
                return new CustomFieldComponentDisplay(cfd, resolvedType);
        }
    }

    private final CustomFieldDefinition cfd;
    private JLabel label;
    protected JPopupMenu popup = new JPopupMenu();

    private CustomFieldComponent(CustomFieldDefinition cfd) {
        this.cfd = cfd;
        setOpaque(false);
        
        JMenuItem mi = new JMenuItem("Reset");
        mi.addActionListener(this);
        mi.setActionCommand("reset");
        popup.add(mi);
        
        this.setComponentPopupMenu(popup);
    }

    abstract public String getValue();

    abstract public void setValue(String value);

    abstract public void setValues(List<String> values);
    
    abstract public List<String> getValues();
    
    public CustomFieldDefinition getCustomFieldDefinition() {
        return cfd;
    }
    
    public void setDefaultValue() {
        setValue(cfd.getDefaultValue());
    }

    public JLabel getLabel() {
        if (label == null) {
            label = new JLabel(cfd.getName() + ":");
        }
        return label;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "reset":
                this.setDefaultValue();
                break;
            default:
                assert false : "Unhandled action command";
                break;
        }
    }

    private static class CustomFieldComponentDisplay extends CustomFieldComponent {
        private final List<String> values = new ArrayList<>();
        private final JLabel outputLabel;
        private final Type type;

        public CustomFieldComponentDisplay(CustomFieldDefinition cfdd, Type type) {
            super(cfdd);
            this.type = type;
            this.outputLabel = new JLabel();
            this.setLayout(new BorderLayout());
            this.add(outputLabel);
            this.setComponentPopupMenu(null);
        }

        @Override
        public void setValue(String value) {
            values.clear();
            values.add(value);
            this.toDisplay();
        }

        @Override
        public java.lang.String getValue() {
            if(values.size() > 0) {
                return values.get(0);
            } else {
                return null;
            }
        }

        @Override
        public void setValues(List<String> values) {
            values.clear();
            values.addAll(values);
            this.toDisplay();
        }

        @Override
        public List<String> getValues() {
            return Collections.unmodifiableList(values);
        }
        
        private void toDisplay() {
            StringBuilder sb = new StringBuilder();
            switch (type) {
                case bool_field:
                    switch (getValue()) {
                        case "0":
                            sb.append("No");
                            break;
                        case "1":
                            sb.append("Yes");
                            break;
                        default:
                            sb.append("");
                            break;
                    }
                    break;
                case user_field:
                case version_field:
                    List<String> ids = new ArrayList<>();
                    if (getCustomFieldDefinition().isMultiple()) {
                        for (String value : getValues()) {
                            ids.add(value);
                        }
                    } else {
                        ids.add(getValue());
                    }
                    for(String id: ids) {
                        for(String possibleValue: getCustomFieldDefinition().getPossibleValues()) {
                            if (possibleValue.endsWith("[" + id + "]")) {
                                if (sb.length() != 0) {
                                    sb.append("\n");
                                }
                                sb.append(possibleValue);
                                break;
                            }
                        }
                    }
                    break;
                case list_field:
                case date_field:
                case int_field:
                case float_field:
                case link_field:
                case text_field:
                case string_field:
                case display_only:
                default:
                    if (getCustomFieldDefinition().isMultiple()) {
                        for (String value : getValues()) {
                            if (sb.length() != 0) {
                                sb.append("\n");
                            }
                            sb.append(value);
                        }
                    } else {
                        sb.append(getValue());
                    }
                    break;
            }
            outputLabel.setText("<html>" + StringUtil.escapeHTML(sb.toString()));
        }
    }

    private static class CustomFieldComponentDate extends CustomFieldComponent {
        private static final SimpleDateFormat isoDate = new SimpleDateFormat("yyyy-MM-dd");
        private final JXDatePicker datePicker = new JXDatePicker();

        public CustomFieldComponentDate(CustomFieldDefinition cfdd) {
            super(cfdd);
            this.setLayout(new WrapLayout(WrapLayout.LEADING));
            this.add(datePicker);
            datePicker.setComponentPopupMenu(popup);
        }

        @Override
        public void setValue(String value) {
            synchronized (isoDate) {
                try {
                    Date d = isoDate.parse(value);
                    datePicker.setDate(d);
                } catch (Exception ex) {
                    datePicker.setDate(null);
                }
            }
        }

        @Override
        public java.lang.String getValue() {
            if (datePicker.getDate() != null) {
                synchronized(isoDate) {
                    return isoDate.format(datePicker.getDate());
                }
            } else {
                return null;
            }
        }

        @Override
        public void setValues(List<String> values) {
            throw new UnsupportedOperationException("Method not supported for type");
        }

        @Override
        public List<String> getValues() {
            throw new UnsupportedOperationException("Method not supported for type");
        }
        
        
    }

    private static class CustomFieldComponentLine extends CustomFieldComponent {
        private final JTextField line = new JTextField();

        public CustomFieldComponentLine(CustomFieldDefinition cfdd) {
            super(cfdd);
            this.setLayout(new BorderLayout());
            this.add(line);
            line.setComponentPopupMenu(popup);
        }

        @Override
        public void setValue(String value) {
            if(value == null) {
                value = "";
            }
            line.setText(value);
        }

        @Override
        public java.lang.String getValue() {
            return line.getText();
        }

        @Override
        public void setValues(List<String> values) {
            throw new UnsupportedOperationException("Method not supported for type");
        }

        @Override
        public List<String> getValues() {
            throw new UnsupportedOperationException("Method not supported for type");
        }
    }
    
    private static class CustomFieldComponentList extends CustomFieldComponent {
        private final JList<String> list = new JList<>();

        public CustomFieldComponentList(CustomFieldDefinition cfd) {
            super(cfd);
            this.setLayout(new BorderLayout());
            this.add(new JScrollPane(list));
            this.setPreferredSize(new Dimension(0, 75));
            DefaultListModel<String> dlm = new DefaultListModel<>();
            List<String> possibleValues = cfd.getPossibleValues();
            for(int i = 0; i < possibleValues.size(); i++) {
                dlm.add(i, possibleValues.get(i));
            }
            list.setModel(dlm);
            if(cfd.isMultiple()) {
                list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            } else {
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            }
            list.setComponentPopupMenu(popup);
        }

        @Override
        public void setValue(String value) {
            list.setSelectedValue(value, true);
        }

        @Override
        public java.lang.String getValue() {
            return list.getSelectedValue();
        }

        @Override
        public void setValues(List<String> selectedValues) {
            List<Integer> selectIndices = new ArrayList<>();
            ListModel<String> lm = list.getModel();
            for (int i = 0; i < lm.getSize(); i++) {
                if (selectedValues.contains(lm.getElementAt(i))) {
                    selectIndices.add(i);
                }
            }
            int selectedIndicesArray[] = new int[selectIndices.size()];
            for (int i = 0; i < selectIndices.size(); i++) {
                selectedIndicesArray[i] = selectIndices.get(i);
            }
            list.setSelectedIndices(selectedIndicesArray);
        }

        @Override
        public List<String> getValues() {
            List<String> result = new ArrayList<>(list.getSelectedIndices().length);
            for (int index : list.getSelectedIndices()) {
                result.add(list.getModel().getElementAt(index));
            }
            return result;
        }
    }
    
    private static class CustomFieldComponentListId extends CustomFieldComponent {

        private final DefaultListModel<String> dlm = new DefaultListModel<>();
        private final JList<String> list = new JList<>(dlm);

        public CustomFieldComponentListId(CustomFieldDefinition cfd) {
            super(cfd);
            this.setLayout(new BorderLayout());
            this.add(new JScrollPane(list));
            this.setPreferredSize(new Dimension(0, 75));
            List<String> possibleValues = cfd.getPossibleValues();
            for (int i = 0; i < possibleValues.size(); i++) {
                dlm.add(i, possibleValues.get(i));
            }
            if (cfd.isMultiple()) {
                list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            } else {
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            }
            list.setComponentPopupMenu(popup);
        }

        @Override
        public void setValue(String id) {
            list.clearSelection();
            for (int i = 0; i < dlm.getSize(); i++) {
                String candidate = dlm.getElementAt(i);
                if (candidate.endsWith("[" + id + "]")) {
                    list.setSelectedIndex(i);
                    break;
                }
            }
        }

        @Override
        public java.lang.String getValue() {
            String result = list.getSelectedValue();
            if(result != null) {
                result = extractId(result);
            }
            return result;
        }

        @Override
        public void setValues(List<String> selectedValues) {
            List<Integer> selectIndices = new ArrayList<>();
            for (int i = 0; i < dlm.getSize(); i++) {
                String candidate = dlm.getElementAt(i);
                for (String id : selectedValues) {
                    if (candidate.endsWith("[" + id + "]")) {
                        selectIndices.add(i);
                        break;
                    }
                }
            }
            int selectedIndicesArray[] = new int[selectIndices.size()];
            for (int i = 0; i < selectIndices.size(); i++) {
                selectedIndicesArray[i] = selectIndices.get(i);
            }
            list.setSelectedIndices(selectedIndicesArray);
        }

        @Override
        public List<String> getValues() {
            List<String> result = new ArrayList<>(list.getSelectedIndices().length);
            for (int index : list.getSelectedIndices()) {
                String value = dlm.getElementAt(index);
                result.add(extractId(value));
            }
            return result;
        }
        
        private String extractId(String input) {
            return input.substring(input.lastIndexOf('[') + 1, input.lastIndexOf(']'));
        }
    }
    
    private static class CustomFieldComponentLongText extends CustomFieldComponent {
        private final JTextArea textarea = new JTextArea();

        public CustomFieldComponentLongText(CustomFieldDefinition cfd) {
            super(cfd);
            this.setLayout(new BorderLayout());
            this.add(new JScrollPane(textarea));
            this.setPreferredSize(new Dimension(0, 75));
            textarea.setComponentPopupMenu(popup);
        }

        @Override
        public void setValue(String value) {
            textarea.setText(value);
        }

        @Override
        public java.lang.String getValue() {
            return textarea.getText();
        }
        
        @Override
        public void setValues(List<String> values) {
            throw new UnsupportedOperationException("Method not supported for type");
        }

        @Override
        public List<String> getValues() {
            throw new UnsupportedOperationException("Method not supported for type");
        }
    }
    
    private static class CustomFieldComponentBool extends CustomFieldComponent {
        private final JComboBox<String> combo = new JComboBox<>();

        public CustomFieldComponentBool(CustomFieldDefinition cfdd) {
            super(cfdd);
            this.setLayout(new WrapLayout(WrapLayout.LEADING));
            DefaultComboBoxModel<String> dcbm = new DefaultComboBoxModel<>();
            dcbm.addElement(" ");
            dcbm.addElement("Yes");
            dcbm.addElement("No");
            combo.setModel(dcbm);
            this.add(combo);
        }

        @Override
        public void setValue(String value) {
            if("1".equals(value)) {
                combo.setSelectedIndex(1);
            } else if ("0".equals(value)) {
                combo.setSelectedIndex(2);
            } else {
                combo.setSelectedIndex(0);
            }
        }

        @Override
        public java.lang.String getValue() {
            switch(combo.getSelectedIndex()) {
                case 1:
                    return "1";
                case 2:
                    return "0";
                default:
                    return null;
            }
        }

        @Override
        public void setValues(List<String> values) {
            throw new UnsupportedOperationException("Method not supported for type");
        }

        @Override
        public List<String> getValues() {
            throw new UnsupportedOperationException("Method not supported for type");
        }
    }

    private static class CustomFieldComponentNumeric extends CustomFieldComponent implements FocusListener {
        private final JTextField line = new JTextField();
        private final boolean floating;
        private String oldValue = null;

        public CustomFieldComponentNumeric(CustomFieldDefinition cfd, boolean floating) {
            super(cfd);
            this.setLayout(new BorderLayout());
            this.add(line);
            this.floating = floating;
            line.setComponentPopupMenu(popup);
        }

        @Override
        public void setValue(String value) {
            if(value == null) {
                value = "";
            }
            line.setText(value);
        }

        @Override
        public java.lang.String getValue() {
            String text = line.getText();
            if(text.isEmpty()) {
                return null;
            } else {
                return text;
            }
        }
        
        @Override
        public void setValues(List<String> values) {
            throw new UnsupportedOperationException("Method not supported for type");
        }

        @Override
        public List<String> getValues() {
            throw new UnsupportedOperationException("Method not supported for type");
        }
        
        @Override
        public void focusGained(FocusEvent e) {
            oldValue = line.getText();
        }

        @Override
        public void focusLost(FocusEvent e) {
            String newValue = line.getText();
            if("".equals(newValue)) {
                return;
            }
            try {
                if (floating) {
                    Double.parseDouble(newValue);
                } else {
                    Integer.parseInt(newValue);
                }
            } catch (NumberFormatException ne) {
                line.setText(oldValue);
            }
        }
    }
}
