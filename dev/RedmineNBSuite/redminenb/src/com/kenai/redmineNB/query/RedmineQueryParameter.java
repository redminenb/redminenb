/*
 * This class has been copied from [bugzilla]/org.netbeans.modules.bugzilla.query.QueryParameter
 * @author Tomas Stupka
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package com.kenai.redmineNB.query;

import com.kenai.redmineNB.util.ListListModel;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Anchialas <anchialas@gmail.com>
 */
public abstract class RedmineQueryParameter {

   static final ParameterValue[] EMPTY_PARAMETER_VALUE = new ParameterValue[]{new ParameterValue("", "")}; // NOI18N
   static final ParameterValue PV_CONTAINS = new ParameterValue("contains", "substring"); // NOI18N
   static final ParameterValue PV_IS = new ParameterValue("is", "exact"); // NOI18N
   static final ParameterValue PV_MATCHES_REGEX = new ParameterValue("matches the regexp", "regexp"); // NOI18N
   static final ParameterValue PV_DOESNT_MATCH_REGEX = new ParameterValue("doesn't match the regexp", "notregexp"); // NOI18N
   static final ParameterValue PV_FIELD_ALIAS = new ParameterValue("alias", "alias"); // NOI18N
   static final ParameterValue PV_FIELD_ASSIGNED_TO = new ParameterValue("assigned_to", "assigned_to"); // NOI18N
   static final ParameterValue PV_FIELD_LIST_ACCESSIBLE = new ParameterValue("cclist_accessible", "cclist_accessible"); // NOI18N
   static final ParameterValue PV_FIELD_COMPONENT = new ParameterValue("component", "component"); // NOI18N
   static final ParameterValue PV_FIELD_DEADLINE = new ParameterValue("deadline", "deadline"); // NOI18N
   static final ParameterValue PV_FIELD_EVER_CONFIRMED = new ParameterValue("everconfirmed", "everconfirmed"); // NOI18N
   static final ParameterValue PV_FIELD_REP_PLARFORM = new ParameterValue("rep_platform", "rep_platform"); // NOI18N
   static final ParameterValue PV_FIELD_REMAINING_TIME = new ParameterValue("remaining_time", "remaining_time"); // NOI18N
   static final ParameterValue PV_FIELD_WORK_TIME = new ParameterValue("work_time", "work_time"); // NOI18N
   static final ParameterValue PV_FIELD_KEYWORDS = new ParameterValue("keywords", "keywords"); // NOI18N
   static final ParameterValue PV_FIELD_ESTIMATED_TIME = new ParameterValue("estimated_time", "estimated_time"); // NOI18N
   static final ParameterValue PV_FIELD_OP_SYS = new ParameterValue("op_sys", "op_sys"); // NOI18N
   static final ParameterValue PV_FIELD_PRIORITY = new ParameterValue("priority", "priority"); // NOI18N
   static final ParameterValue PV_FIELD_PRODUCT = new ParameterValue("product", "product"); // NOI18N
   static final ParameterValue PV_FIELD_QA_CONTACT = new ParameterValue("qa_contact", "qa_contact"); // NOI18N
   static final ParameterValue PV_FIELD_REPORTER_ACCESSIBLE = new ParameterValue("reporter_accessible", "reporter_accessible"); // NOI18N
   static final ParameterValue PV_FIELD_RESOLUTION = new ParameterValue("resolution", "resolution"); // NOI18N
   static final ParameterValue PV_FIELD_BUG_SEVERITY = new ParameterValue("bug_severity", "bug_severity"); // NOI18N
   static final ParameterValue PV_FIELD_BUG_STATUS = new ParameterValue("bug_status", "bug_status"); // NOI18N
   static final ParameterValue PV_FIELD_SHORT_DESC = new ParameterValue("short_desc", "short_desc"); // NOI18N
   static final ParameterValue PV_FIELD_TARGET_MILESTONE = new ParameterValue("target_milestone", "target_milestone"); // NOI18N
   static final ParameterValue PV_FIELD_BUG_FILE_LOC = new ParameterValue("bug_file_loc", "bug_file_loc"); // NOI18N
   static final ParameterValue PV_FIELD_VERSION = new ParameterValue("version", "version"); // NOI18N
   static final ParameterValue PV_FIELD_VOTES = new ParameterValue("votes", "votes"); // NOI18N
   static final ParameterValue PV_FIELD_STATUS_WHITEBOARD = new ParameterValue("status_whiteboard", "status_whiteboard"); // NOI18N
   static final ParameterValue[] PV_TEXT_SEARCH_VALUES = new ParameterValue[]{
      PV_MATCHES_REGEX,
      PV_DOESNT_MATCH_REGEX
   };
   static final ParameterValue[] PV_PEOPLE_VALUES = new ParameterValue[]{
      PV_CONTAINS,
      PV_IS,
      PV_MATCHES_REGEX,
      PV_DOESNT_MATCH_REGEX
   };
   static final ParameterValue[] PV_LAST_CHANGE = new ParameterValue[]{
      PV_FIELD_ALIAS,
      PV_FIELD_ASSIGNED_TO,
      PV_FIELD_LIST_ACCESSIBLE,
      PV_FIELD_COMPONENT,
      PV_FIELD_DEADLINE,
      PV_FIELD_EVER_CONFIRMED,
      PV_FIELD_REP_PLARFORM,
      PV_FIELD_REMAINING_TIME,
      PV_FIELD_WORK_TIME,
      PV_FIELD_KEYWORDS,
      PV_FIELD_ESTIMATED_TIME,
      PV_FIELD_OP_SYS,
      PV_FIELD_PRIORITY,
      PV_FIELD_PRODUCT,
      PV_FIELD_QA_CONTACT,
      PV_FIELD_REPORTER_ACCESSIBLE,
      PV_FIELD_RESOLUTION,
      PV_FIELD_BUG_SEVERITY,
      PV_FIELD_BUG_STATUS,
      PV_FIELD_SHORT_DESC,
      PV_FIELD_TARGET_MILESTONE,
      PV_FIELD_BUG_FILE_LOC,
      PV_FIELD_VERSION,
      PV_FIELD_VOTES,
      PV_FIELD_STATUS_WHITEBOARD
   };
   //
   private final String parameter;
   protected boolean alwaysDisabled = false;

   public RedmineQueryParameter(String parameter) {
      this.parameter = parameter;
   }

   public String getParameter() {
      return parameter;
   }

   abstract ParameterValue[] getValues();

//   abstract void setValues(ParameterValue[] pvs);
   public abstract boolean isEmpty();

   abstract void setEnabled(boolean b);

   void setAlwaysDisabled(boolean bl) {
      this.alwaysDisabled = bl;
      setEnabled(false); // true or false, who cares. this is only to trigger the state change
   }

   public String getValueString() {
      StringBuilder sb = new StringBuilder();
      for (ParameterValue pv : getValues()) {
         if (sb.length() > 0) {
            sb.append(",");
         }
         sb.append(pv.getValue());
      }
      return sb.toString();
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(getClass().getSimpleName()); // NOI18N
      sb.append("["); // NOI18N
      sb.append(parameter);
      sb.append("=");
      sb.append(getValueString());
      sb.append("]"); // NOI18N
      return sb.toString();
   }

   static class ComboParameter extends RedmineQueryParameter {

      private final JComboBox combo;

      public ComboParameter(JComboBox combo, String parameter) {
         super(parameter);
         this.combo = combo;
         combo.setModel(new DefaultComboBoxModel());
      }

      @Override
      public ParameterValue[] getValues() {
         ParameterValue value = (ParameterValue)combo.getSelectedItem();
         return value != null ? new ParameterValue[]{value} : EMPTY_PARAMETER_VALUE;
      }

      public final void setParameterValues(ParameterValue[] values) {
         combo.setModel(new DefaultComboBoxModel(values));
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
         list.setModel(new DefaultListModel());
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
         list.setModel(new ListListModel(values));
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

      private static ParameterValue[] SELECTED_VALUE = new ParameterValue[]{new ParameterValue("1")}; // NOI18N
      //
      private final JCheckBox chk;

      public CheckBoxParameter(JCheckBox chk, String parameter) {
         super(parameter);
         this.chk = chk;
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

      private final String[] values;

      public SimpleQueryParameter(String parameter, String[] values) {
         super(parameter);
         this.values = values;
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
