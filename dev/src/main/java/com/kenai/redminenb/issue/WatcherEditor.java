/*
 * Copyright 2014 Matthias Bläsing.
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

import com.kenai.redminenb.user.RedmineUser;
import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Watcher;
import java.awt.Dialog;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Utilities;

public class WatcherEditor {
    private RedmineIssue redmineIssue;
    private List<Watcher> originalWatchers;
    private List<Watcher> newWatchers;
    private Collection<RedmineUser> users;
    
    public WatcherEditor(RedmineIssue redmineIssue) {
        this.redmineIssue = redmineIssue;
    }
    
    public void run()  {
        new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                originalWatchers = new ArrayList<>(redmineIssue.getIssue().getWatchers());
                users = redmineIssue.getRepository().getUsers(redmineIssue.getIssue().getProject());
                return null;
            }

            @Override
            protected void done() {
                try {
                    // Pull Exception into EDT
                    get();
                } catch (InterruptedException | ExecutionException ex) {
                    throw new RuntimeException(ex);
                }
                WatchersEditorFrame we = new WatchersEditorFrame(originalWatchers, users);
                DialogDescriptor dd = new DialogDescriptor(we, "Watchers", true, null);
                Dialog d = DialogDisplayer.getDefault().createDialog(dd);
                d.setSize(500, 400);
                Rectangle r = Utilities.findCenterBounds(d.getSize());
                d.setBounds(r);
                d.setVisible(true);
                
                newWatchers = we.getWatchers();
                
                if(dd.getValue() == DialogDescriptor.OK_OPTION) {
                    saveIssueData();
                }
            }
        }.execute();
    }
    
    private void saveIssueData() {
        new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                List<Watcher> addedWatchers = new ArrayList<>(newWatchers);
                addedWatchers.removeAll(originalWatchers);

                List<Watcher> removedWatchers = new ArrayList<>(originalWatchers);
                removedWatchers.removeAll(newWatchers);
                
                Issue issue = redmineIssue.getIssue();
                IssueManager manager = redmineIssue.getRepository().getIssueManager();
                
                for(Watcher added: addedWatchers) {
                    manager.addWatcherToIssue(added, issue);
                }
                
                for(Watcher removed: removedWatchers) {
                    manager.deleteWatcherFromIssue(removed, issue);
                }
                
                redmineIssue.refresh();
                
                return null;
            }

            @Override
            protected void done() {
                try {
                    // Pull Exception into EDT
                    get();
                } catch (InterruptedException | ExecutionException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }.execute();

    }
}
