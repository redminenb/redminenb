
package com.kenai.redminenb.util;

import com.taskadapter.redmineapi.bean.Project;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NestedProject implements Comparable<NestedProject>{
    private NestedProject parent;
    private Project project;

    public NestedProject(NestedProject parent, Project project) {
        this.parent = parent;
        this.project = project;
    }
    
    public NestedProject(Project project) {
        this.parent = null;
        this.project = project;
    }

    public NestedProject getParent() {
        return parent;
    }

    public void setParent(NestedProject parent) {
        this.parent = parent;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.project);
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
        final NestedProject other = (NestedProject) obj;
        if (!Objects.equals(this.project, other.project)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(NestedProject o) {
        List<NestedProject> chain1 = new ArrayList<>();
        List<NestedProject> chain2 = new ArrayList<>();
        for(NestedProject np = this; np != null; np = np.getParent()) {
            chain1.add(np);
        }
        for(NestedProject np = o; np != null; np = np.getParent()) {
            chain2.add(np);
        }
        Collections.reverse(chain1);
        Collections.reverse(chain2);
        for(int i = 0; i < Math.max(chain1.size(), chain2.size()); i++) {
            String s1 = "";
            String s2 = "";
            try {
                if (i < chain1.size() 
                        && chain1.get(i).getProject().getName() != null) {
                    s1 = chain1.get(i).getProject().getName();
                }
            } catch (RuntimeException ex) {
            }
            try {
                if (i < chain2.size() 
                        && chain2.get(i).getProject().getName() != null) {
                    s2 = chain2.get(i).getProject().getName();
                }
            } catch (RuntimeException ex) {
            }
            int stringResult = s1.compareToIgnoreCase(s2);
            if(stringResult != 0) {
                return stringResult;
            }
        }
        return 0;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (NestedProject parent = this.getParent(); parent != null; parent = parent.getParent()) {
            sb.append("\u00BB ");
        }
        sb.append(this.getProject().getName());
        return sb.toString();
    }
}
