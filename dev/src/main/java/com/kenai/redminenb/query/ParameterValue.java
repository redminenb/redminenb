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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.NbBundle;

/**
 * Immutable class representing a parameter value and displayName (both non-null).
 *
 * @author Anchialas <anchialas@gmail.com>
 */
@NbBundle.Messages("LBL_PVNone=(none)")
@XmlAccessorType(XmlAccessType.FIELD)
public class ParameterValue {

   static final String NONE_VALUE = "!*";
   static final ParameterValue NONE_PARAMETERVALUE = new ParameterValue(Bundle.LBL_PVNone(), NONE_VALUE);
   
   private String displayName;
   private String value;

   private ParameterValue() {
   }
   
   public ParameterValue(@NonNull String value) {
      this(value, value);
   }

   public ParameterValue(@NonNull String displayName, @NonNull String value) {
      assert displayName != null;
      assert value != null;
      this.displayName = displayName;
      this.value = value;
   }

   public ParameterValue(@NonNull String displayName, @NonNull Integer value) {
      assert displayName != null;
      assert value != null;
      this.displayName = displayName;
      this.value = String.valueOf(value);
   }

   static List<ParameterValue> convert(List<String> values) {
      List<ParameterValue> ret = new ArrayList<>(values.size());
      for (String v : values) {
         ret.add(new ParameterValue(v, v));
      }
      return ret;
   }

   public String getDisplayName() {
      return displayName;
   }

   public String getValue() {
      return value;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(displayName);
      sb.append(" [");
      sb.append(value);
      sb.append("]");
      return sb.toString();
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (!(obj instanceof ParameterValue)) {
         return false;
      }
      ParameterValue other = (ParameterValue)obj;
      return value.equals(other.value); // value cannot be null!
   }

   @Override
   public int hashCode() {
      return value.hashCode(); // value cannot be null!
   }
   
   public static String flattenList(ParameterValue... pvs) {
       if(pvs == null) {
           return "";
       }
       return flattenList(Arrays.asList(pvs));
   }
   
   public static String flattenList(List<ParameterValue> pvs) {
        StringBuilder sb = new StringBuilder();
        for (ParameterValue pv : pvs) {
            if (pv != null && pv.getValue() != null) {
                if (sb.length() > 0) {
                    sb.append("|");
                }
                sb.append(pv.getValue());
            }
        }
        return sb.toString();
    }
}
