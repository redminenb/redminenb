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
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.NbBundle;

/**
 * Immutable class representing a parameter value and displayName (both non-null).
 *
 * @author Anchialas <anchialas@gmail.com>
 */
@NbBundle.Messages("LBL_PVNone=(none)")
public class ParameterValue {

   static final String NONE_VALUE = "!*";
   static final ParameterValue NONE_PARAMETERVALUE = new ParameterValue(Bundle.LBL_PVNone(), NONE_VALUE);
   
   private final String displayName;
   private final String value;
   private Object userObject;

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

   public ParameterValue(@NonNull String displayName, @NonNull Integer value, Object userObject) {
      this(displayName, value);
      this.userObject = userObject;
   }

   static List<ParameterValue> convert(List<String> values) {
      List<ParameterValue> ret = new ArrayList<ParameterValue>(values.size());
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

   public Object getUserObject() {
      return userObject;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(displayName);
      sb.append("[");
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
}
