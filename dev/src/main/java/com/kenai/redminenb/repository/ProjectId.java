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

import java.util.Objects;

public class ProjectId implements Comparable<ProjectId> {

    private Integer id;
    private String name;

    public ProjectId() {
    }

    public ProjectId(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.id;
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
        final ProjectId other = (ProjectId) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public int compareTo(ProjectId o) {
        String name1 = "";
        String name2 = "";
        if (getName() != null) {
            name1 = getName();
        }
        if (o.getName() != null) {
            name2 = o.getName();
        }
        return name1.compareToIgnoreCase(name2);
    }
}
