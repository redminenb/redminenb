/*
 * Copyright 2014 Matthias Bläsing
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

import com.kenai.redminenb.issue.RedmineIssue;
import com.taskadapter.redmineapi.bean.IssueCategory;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Tracker;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.Version;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public class QueryListModel extends AbstractTableModel{

    private List<RedmineIssue> issues = new ArrayList<>();

    public void setIssues(Collection<RedmineIssue> issues) {
        this.issues = new ArrayList<>(issues);
        fireTableDataChanged();
    }
    
    public RedmineIssue getIssue(int pos) {
        return issues.get(pos);
    }
    
    @Override
    public int getRowCount() {
        return issues.size();
    }

    @Override
    public int getColumnCount() {
        return 9;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch(columnIndex) {
            case 0:
                return Integer.class;
            case 1:
                return String.class;
            case 2: 
                return Tracker.class;
            case 3:
                return String.class;
            case 4:
                return String.class;
            case 5:
                return User.class;
            case 6:
                return IssueCategory.class;
            case 7:
                return Version.class;
            case 8:
                return Project.class;
            default:
                return null;
        }
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        RedmineIssue ri = getIssue(rowIndex);
        switch(columnIndex) {
            case 0:
                return ri.getIssue().getId();
            case 1:
                return ri.getSummary();
            case 2: 
                return ri.getIssue().getTracker();
            case 3:
                return ri.getIssue().getPriorityText();
            case 4:
                return ri.getIssue().getStatusName();
            case 5:
                return ri.getIssue().getAssignee();
            case 6:
                return ri.getIssue().getCategory();
            case 7:
                return ri.getIssue().getTargetVersion();
            case 8:
                return ri.getIssue().getProject();
            default:
                return null;
        }
    }
    
}
