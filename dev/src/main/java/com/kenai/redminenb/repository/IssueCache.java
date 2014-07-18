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

package com.kenai.redminenb.repository;

import com.kenai.redminenb.issue.RedmineIssue;
import com.taskadapter.redmineapi.bean.Issue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache class to ensure issues are only opened/used once.
 * 
 * Every time a new RedmineIssue is to be created from backend issue data
 * the creation process has to go through cachedRedmineIssue. That method
 * is synchronized, so even if to threads try to create a RedmineIssue in
 * parallel from the same backend data they will get the same RedmineIssue
 * instance.
 * 
 * @author matthias
 */
public class IssueCache {
    private RedmineRepository repository;
    private final Map<String,WeakReference<RedmineIssue>> cache = new HashMap<>();

    public IssueCache(RedmineRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Access cached RedmineIssue by ID. 
     * 
     * ID is in this case Integer, as the redmie api uses integer ids.
     * 
     * @param id
     * @return 
     */
    public RedmineIssue get(Integer id) {
        if(id == null) {
            return null;
        } else {
            return get(id.toString());
        }
    }
    
    /**
     * Access cached RedmineIssue by ID. 
     * 
     * ID is in this case string, as the issue uses string ids.
     * 
     * @param id
     * @return 
     */
    public RedmineIssue get(String id) {
        WeakReference<RedmineIssue> valueReference = cache.get(id);
        if(valueReference == null) {
            return null;
        } else {
            return valueReference.get();
        }
    }
    
    /**
     * Place a RedmineIssue into the issue cache.
     * 
     * This method may only be called when a new RedmineIssue is persisted.
     * 
     * @param ri 
     */
    public synchronized void put(RedmineIssue ri) {
        if(ri.getID() == null || "0".equals(ri.getID())) {
            return;
        }
        cache.put(ri.getID(), new WeakReference<>(ri));
    }
    
    /**
     * If the supplied issue data is already associated with a RedmineIssue 
     * instance, that instance is returned, else a new RedmineIssue is created
     * and cached.
     * 
     * @param issue backend issue data
     * @return 
     */
    public synchronized RedmineIssue cachedRedmineIssue(Issue issue) {
        RedmineIssue cached = get(issue.getId());
        if(cached != null) {
            return cached;
        } else {
            RedmineIssue ri = new RedmineIssue(repository, issue);
            put(ri);
            return ri;
        }
    }
}
