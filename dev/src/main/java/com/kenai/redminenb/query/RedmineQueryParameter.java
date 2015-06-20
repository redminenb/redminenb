/*
 * Copyright 2012 Anchialas.
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
package com.kenai.redminenb.query;

import com.kenai.redminenb.util.ListListModel;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import javax.swing.*;

/**
 * GUI-Bindings for Parameter Values
 * 
 * @author Anchialas <anchialas@gmail.com>
 */
public abstract class RedmineQueryParameter {

   static final ParameterValue[] EMPTY_PARAMETER_VALUE = new ParameterValue[]{new ParameterValue(" ", "")}; // NOI18N
   //
   private final String parameter;
   protected boolean alwaysDisabled = false;

   public RedmineQueryParameter(String parameter) {
      this.parameter = parameter;
   }

   public String getParameter() {
      return parameter;
   }

   abstract void setValues(ParameterValue[] values);
   abstract ParameterValue[] getValues();

//   abstract void setValues(ParameterValue[] pvs);
   public abstract boolean isEmpty();

   abstract void setEnabled(boolean b);

   void setAlwaysDisabled(boolean bl) {
      this.alwaysDisabled = bl;
      setEnabled(false); // true or false, who cares. this is only to trigger the state change
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(getClass().getSimpleName()); // NOI18N
      sb.append("["); // NOI18N
      sb.append(parameter);
      sb.append("=");
      sb.append(ParameterValue.flattenList(getValues()));
      sb.append("]"); // NOI18N
      return sb.toString();
   }

   static class ComboParameter extends RedmineQueryParameter {

      private final JComboBox combo;

      public ComboParameter(JComboBox combo, String parameter) {
         super(parameter);
         this.combo = combo;
         combo.setModel(new DefaultComboBoxModel(EMPTY_PARAMETER_VALUE));
      }

      @Override
      public void setValues(ParameterValue[] values) {
          Object value = null;
          if(values.length > 0) {
              value = values[0];
          }
          combo.setSelectedItem(value);
      }
      
      @Override
      public ParameterValue[] getValues() {
         ParameterValue value = (ParameterValue)combo.getSelectedItem();
         return value != null ? new ParameterValue[]{value} : EMPTY_PARAMETER_VALUE;
      }

      public final void setParameterValues(ParameterValue[] values) {
         combo.setModel(new DefaultComboBoxModel(values));
      }
      
      public final void setParameterValues(List<ParameterValue> values) {
         combo.setModel(new DefaultComboBoxModel(values.toArray(new ParameterValue[values.size()])));
      }

      @Override
      void setEnabled(boolean b) {
         combo.setEnabled(alwaysDisabled ? false : b);
      }

      @Override
      public boolean isEmpty() {
         return combo.getModel().getSize() == 0;
      }
   }

   static class ListParameter extends RedmineQueryParameter {

      private final JList list;

      public ListParameter(JList list, String parameter) {
         super(parameter);
         this.list = list;
         //list.setModel(new DefaultListModel());
      }
      
      @Override
      public void setValues(ParameterValue[] values) {
          List<Integer> indices = new ArrayList<>();
          ListModel lm = list.getModel();
          int itemCount = lm.getSize();
          OUTER: for(int i = 0; i < itemCount; i++) {
              for(ParameterValue pv: values) {
                if(lm.getElementAt(i).equals(pv)) {
                    indices.add(i);
                    continue OUTER;
                }
              }
          }
          int[] indicesArray = new int[indices.size()];
          for(int i = 0; i < indicesArray.length; i++) {
              indicesArray[i] = indices.get(i);
          }
          list.setSelectedIndices(indicesArray);
      }

      @Override
      public ParameterValue[] getValues() {
         Object[] values = list.getSelectedValues();
         if (values == null || values.length == 0) {
            return EMPTY_PARAMETER_VALUE;
         }
         ParameterValue[] ret = new ParameterValue[values.length];
         for (int i = 0; i < values.length; i++) {
            ret[i] = (ParameterValue)values[i];
         }
         return ret;
      }

      public void setParameterValues(List<ParameterValue> values) {
         if (values.isEmpty()) {
            list.setModel(new DefaultListModel());
            list.setPrototypeCellValue("    ");
         } else {
            list.setModel(new ListListModel<>(values));
            list.setPrototypeCellValue(null);
         }
      }

      @Override
      void setEnabled(boolean b) {
         list.setEnabled(alwaysDisabled ? false : b);
      }

      @Override
      public boolean isEmpty() {
         return list.getModel().getSize() == 0;
      }
   }

   static class TextFieldParameter extends RedmineQueryParameter {

      private final JTextField txt;

      public TextFieldParameter(JTextField txt, String parameter) {
         super(parameter);
         this.txt = txt;
      }

      @Override
      public void setValues(ParameterValue[] values) {
         if(values.length > 0 && values[0] != null) {
            txt.setText(values[0].getValue());
         } 
      }
      
      @Override
      public ParameterValue[] getValues() {
         String value = txt.getText();
         if (value == null || value.equals("")) { // NOI18N
            return EMPTY_PARAMETER_VALUE;
         }
//         String[] split = value.split(" "); // NOI18N
//         StringBuilder sb = new StringBuilder();
//         for (int i = 0; i < split.length; i++) {
//            String s = split[i];
//            sb.append(s);
//            if (i < split.length - 1) {
//               sb.append("+"); // NOI18N
//            }
//         }
//         String v = sb.toString();
//         return new ParameterValue[]{new ParameterValue(v, v)};
         return new ParameterValue[]{new ParameterValue(value)};
      }

      @Override
      void setEnabled(boolean b) {
         txt.setEnabled(alwaysDisabled ? false : b);
      }

      @Override
      public boolean isEmpty() {
         return false;
      }
   }

   static class CheckBoxParameter extends RedmineQueryParameter {

      private static final ParameterValue[] SELECTED_VALUE = new ParameterValue[]{new ParameterValue("1")}; // NOI18N
      //
      private final JCheckBox chk;

      public CheckBoxParameter(JCheckBox chk, String parameter) {
         super(parameter);
         this.chk = chk;
      }

      @Override
      void setValues(ParameterValue[] values) {
        if(values.length > 0 && values[0] != null && values[0].equals(SELECTED_VALUE[0])) {
            this.chk.setSelected(true);
        } else {
            this.chk.setSelected(false);
        }
      }
      
      @Override
      public ParameterValue[] getValues() {
         return chk.isSelected() ? SELECTED_VALUE : EMPTY_PARAMETER_VALUE;
      }

      @Override
      void setEnabled(boolean b) {
         chk.setEnabled(alwaysDisabled ? false : b);
      }

      @Override
      public boolean isEmpty() {
         return false;
      }
   }

   public static class SimpleQueryParameter extends RedmineQueryParameter {

      private String[] values;

      public SimpleQueryParameter(String parameter, String[] values) {
         super(parameter);
         this.values = new String[values.length];
         System.arraycopy(values, 0, this.values, 0, values.length);
      }
      
      @Override
      void setValues(ParameterValue[] values) {
          if(! Arrays.equals(values, EMPTY_PARAMETER_VALUE)) {
              this.values = new String[values.length];
              for(int i = 0; i < values.length; i++) {
                  this.values[i] = values[i].getValue();
              }
          } else {
              this.values = new String[0];
          }
      }
      
      @Override
      ParameterValue[] getValues() {
         if (values == null || values.length == 0) {
            return EMPTY_PARAMETER_VALUE;
         }
         ParameterValue[] ret = new ParameterValue[values.length];
         for (int i = 0; i < values.length; i++) {
            ret[i] = new ParameterValue(values[i]);
         }
         return ret;
      }

      @Override
      void setEnabled(boolean b) {
         // interested
      }

      @Override
      public boolean isEmpty() {
         return values == null || values.length == 0;
      }
   }
}
