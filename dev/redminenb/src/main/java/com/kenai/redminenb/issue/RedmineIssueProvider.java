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
package com.kenai.redminenb.issue;

import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.IssueProvider;


/**
 * RedmineNB {@link IssueProvider}.
 *
 * @author Anchialas <anchialas@gmail.com>
 */
public final class RedmineIssueProvider extends IssueProvider<RedmineIssue> {

   @Override
   public String getDisplayName(RedmineIssue data) {
      return data.getDisplayName();
   }

   @Override
   public String getTooltip(RedmineIssue data) {
      return data.getTooltip();
   }

   @Override
   public String getID(RedmineIssue data) {
      return data.getID();
   }

   @Override
   public String[] getSubtasks(RedmineIssue data) {
      return new String[0];
   }

   @Override
   public String getSummary(RedmineIssue data) {
      return data.getSummary();
   }

   @Override
   public boolean isNew(RedmineIssue data) {
      return data.isNew();
   }

   @Override
   public boolean isFinished(RedmineIssue data) {
      return data.isFinished();
   }

   @Override
   public boolean refresh(RedmineIssue data) {
      return data.refresh();
   }

   @Override
   public void addComment(RedmineIssue data, String comment, boolean closeAsFixed) {
      data.addComment(comment, closeAsFixed);
   }

   @Override
   public void attachPatch(RedmineIssue data, File file, String description) {
      data.attachPatch(file, description);
   }

   @Override
   public BugtrackingController getController(RedmineIssue data) {
      return data.getController();
   }

   @Override
   public void addPropertyChangeListener(RedmineIssue data, PropertyChangeListener listener) {
      data.addPropertyChangeListener(listener);
   }

   @Override
   public void removePropertyChangeListener(RedmineIssue data, PropertyChangeListener listener) {
      data.removePropertyChangeListener(listener);
   }

}
