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
package com.kenai.redminenb.util;

import com.taskadapter.redmineapi.bean.Watcher;
import java.io.Serializable;
import java.util.Comparator;

public class WatcherComparator implements Comparator<Watcher>, Serializable {
    private static final long serialVersionUID = 1L;
    
    @Override
    public int compare(Watcher o1, Watcher o2) {
        String name1 = "";
        String name2 = "";
        if(o1 != null && o1.getName() != null)  {
            name1 = o1.getName();
        }
        if(o2 != null && o2.getName() != null)  {
            name2 = o2.getName();
        }
        int comparison = name1.compareTo(name2);
        if(comparison != 0) {
            return comparison;
        } else {
            int id1 = 0;
            int id2 = 0;
            if(o1 != null && o1.getId() != null) {
                id1 = o1.getId();
            }
            if(o2 != null && o2.getId() != null) {
                id2 = o2.getId();
            }
            return id1 - id2;
        }
    }
    
}
