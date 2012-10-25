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
package com.kenai.redminenb.api;

import com.taskadapter.redmineapi.bean.Identifiable;


/**
 * Redmine Issue Priority (enumeration).
 *
 * @author Anchialas <anchialas@gmail.com>
 */
public class IssuePriority implements Identifiable {

   private Integer id;
   private String name;

   public IssuePriority(Integer id, String name) {
      this.id = id;
      this.name = name;
   }
   
   public static IssuePriority fromIssue(com.taskadapter.redmineapi.bean.Issue issue) {
      return new IssuePriority(issue.getPriorityId(), issue.getPriorityText());
   }

   @Override
   public Integer getId() {
      return id;
   }

   public String getName() {
      return name;
   }

   @Override
   public int hashCode() {
      int hash = 7;
      hash = 11 * hash + (this.id != null ? this.id.hashCode() : 0);
      return hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final IssuePriority other = (IssuePriority) obj;
      if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
         return false;
      }
      return true;
   }
 
   
   
}
