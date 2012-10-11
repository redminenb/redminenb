package com.kenai.redmineNB.project;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import com.taskadapter.redmineapi.bean.Project;

/**
 *
 * @author Mykolas
 */
public class ProjectComboBox extends JComboBox {

//    private List<Project> projects;
    public ProjectComboBox() {
        super();
        //setModel(new ProjectComboBoxModel());
    }

//    public List<Project> getProjects() {
//        return projects;
//    }
    public void setProjects(List<Project> projects) {
        //        this.projects = projects;
        //        revalidate();

        Project[] arr;
        if (projects == null) {
            arr = new Project[0];
        } else {
            arr = projects.toArray(new Project[0]);
            Arrays.sort(arr, new Comparator<Project>() {

                @Override
                public int compare(Project a, Project b) {
                    return a.getName().compareTo(b.getName());
                }

            });
        }
        setModel(new DefaultComboBoxModel(arr));
        revalidate();
    }

//    private class ProjectComboBoxModel extends DefaultComboBoxModel {
//
//        @Override
//        public Object getElementAt(int element) {
//            if (projects == null) {
//                return null;
//            }
//
//            return projects.get(element);
//        }
//
//        @Override
//        public int getSize() {
//            if (projects == null) {
//                return 0;
//            }
//
//            return projects.size();
//        }
//    }
}
