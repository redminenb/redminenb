/*
 * Copyright 2012 Mykolas and Anchialas.
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
package com.kenai.redmineNB.issue;

import com.kenai.redmineNB.util.RedmineUtil;

import java.util.Date;
import java.util.logging.Level;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.Version;


/**
 * Redmine specific {@link IssueNode} that is rendered in the IssuesTable.
 *
 * @author Anchialas <anchialas@gmail.com>
 */
public class RedmineIssueNode extends IssueNode<RedmineIssue> {

   public RedmineIssueNode(RedmineIssue issue) {
      super(RedmineUtil.getRepository(issue.getRepository()), issue);
   }

   @Override
   public final Node.Property<?>[] getProperties() {
      return new Node.Property<?>[]{
                 new IDProperty(),
                 new TrackerProperty(),
                 //new SeverityProperty(),
                 new PriorityProperty(),
                 new StatusProperty(),
                 new CategoryProperty(),
                 new RedmineFieldProperty(RedmineIssue.FIELD_ASSIGNEE, String.class),
                 new SummaryProperty(),
                 new UpdatedProperty(),
                 new VersionProperty()
              };
   }

   @Override
   public void fireDataChanged() {
      super.fireDataChanged();
   }


   private class IDProperty extends RedmineFieldProperty<String> {

      public IDProperty() {
         super(RedmineIssue.FIELD_ID, String.class);
      }

      @Override
      public String getValue() {
         return getIssue().getID();
      }

      @Override
      public int compareTo(IssueProperty p) {
         if (p == null) {
            return 1;
         }
         Integer i1 = Integer.parseInt(getIssue().getID());
         Integer i2 = Integer.parseInt(p.getIssue().getID());
         return i1.compareTo(i2);
      }

   }


   private class TrackerProperty extends RedmineFieldProperty<Tracker> {

      public TrackerProperty() {
         super(RedmineIssue.FIELD_TRACKER, Tracker.class);
      }

      @Override
      public Object getValue(String attributeName) {
         Tracker tracker = getValue();
         if ("sortkey".equals(attributeName)) {
            return tracker.getId();
         } else {
            return tracker.getName();
         }
      }

      @Override
      public String toString() {
         try {
            return getValue().getName();
         } catch (Exception e) {
            BugtrackingManager.LOG.log(Level.INFO, null, e);
            return e.getLocalizedMessage();
         }
      }

   }


   private class PriorityProperty extends RedmineFieldProperty<String> {

      public PriorityProperty() {
         super(RedmineIssue.FIELD_PRIORITY_TEXT, String.class);
      }

      @Override
      public Object getValue(String attributeName) {
         if ("sortkey".equals(attributeName)) {
            return getIssueData().getFieldValue(RedmineIssue.FIELD_PRIORITY_ID);
         } else {
            return super.getValue(attributeName);
         }
      }

   }


   private class StatusProperty extends RedmineFieldProperty<String> {

      public StatusProperty() {
         super(RedmineIssue.FIELD_STATUS_NAME, String.class);
      }

      @Override
      public Object getValue(String attributeName) {
         if ("sortkey".equals(attributeName)) {
            return getIssueData().getFieldValue(RedmineIssue.FIELD_STATUS_ID);
         } else {
            return super.getValue(attributeName);
         }
      }

   }


   private class VersionProperty extends RedmineFieldProperty<Version> {

      public VersionProperty() {
         super(RedmineIssue.FIELD_VERSION, Version.class);
      }

      @Override
      public Object getValue(String attributeName) {
         Version version = getValue();
         if ("sortkey".equals(attributeName)) {
            return version.getId();
         } else {
            return version.getName();
         }
      }

      @Override
      public String toString() {
         try {
            return getValue().getName();
         } catch (NullPointerException e) {
            return null;
         } catch (Exception e) {
            BugtrackingManager.LOG.log(Level.INFO, null, e);
            return e.getLocalizedMessage();
         }
      }

   }


   private class CategoryProperty extends RedmineFieldProperty<IssueCategory> {

      public CategoryProperty() {
         super(RedmineIssue.FIELD_CATEGORY, IssueCategory.class);
      }

      @Override
      public Object getValue(String attributeName) {
         IssueCategory ic = getValue();
         if ("sortkey".equals(attributeName)) {
            return ic.getId();
         } else {
            return ic.getName();
         }
      }

      @Override
      public String toString() {
         try {
            return getValue().getName();
         } catch (NullPointerException e) {
            return null;
         } catch (Exception e) {
            BugtrackingManager.LOG.log(Level.INFO, null, e);
            return e.getLocalizedMessage();
         }
      }

   }


   private class UpdatedProperty extends RedmineFieldProperty<Date> {

      public UpdatedProperty() {
         super(RedmineIssue.FIELD_UPDATED, Date.class);
      }

   }


   private class RedmineFieldProperty<T> extends IssueProperty<T> {

      protected final String fieldName;

      public RedmineFieldProperty(String fieldName, Class<T> type) {
         this("issue." + fieldName, fieldName, type);
      }

      public RedmineFieldProperty(String name, String fieldName, Class<T> type) {
         this(name, fieldName, type, "CTL_Issue_" + RedmineUtil.capitalize(fieldName));
      }

      private RedmineFieldProperty(String name, String fieldName, Class<T> type, String titleProp) {
         super(name,
               type,
               NbBundle.getMessage(RedmineIssue.class, titleProp),
               NbBundle.getMessage(RedmineIssue.class, titleProp + "_Desc"));
         this.fieldName = fieldName;
      }

      @Override
      public String toString() {
         T value = getValue();
         return value == null ? null : value.toString();
      }

      @Override
      public T getValue() {
         return getIssueData().getFieldValue(fieldName);
      }

      @Override
      public int compareTo(IssueProperty p) {
         if (p == null) {
            return 1;
         }
         T o1 = getValue();
         T o2 = ((RedmineFieldProperty<T>)p).getValue();
         if (o1 == null) {
            return o2 == null ? 0 : 1;
         }
         if (o1 instanceof Comparable) {
            return o2 == null ? -1 : ((Comparable)o1).compareTo(o2);
         }
         return 0;
      }

   }

}
