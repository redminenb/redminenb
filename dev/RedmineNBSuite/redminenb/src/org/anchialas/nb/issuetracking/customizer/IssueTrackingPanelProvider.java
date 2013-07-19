/*
 * Copyright 2013 Anchialas <anchialas@gmail.com>.
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
package org.anchialas.nb.issuetracking.customizer;

import java.beans.BeanInfo;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ui.customizer.ModuleProperties;
import org.netbeans.modules.bugtracking.ui.nodes.BugtrackingRootNode;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider.Registration;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.CompositeCategoryProvider.Registrations;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 * Register a project customizer panel (project properties).
 *
 * @author Anchialas <anchialas@gmail.com>
 * @see [apisupport.ant]
 * @see org.netbeans.modules.apisupport.project.ui.customizer.CustomizerLibrariesFactory
 */
@Registrations({
   @Registration(projectType = "org-netbeans-modules-java-j2seproject", position = 900),
   @Registration(projectType = "org-netbeans-modules-apisupport-project", position = 900)
})
@Messages("CTL_IssueTracker=Issue Tracker")
public class IssueTrackingPanelProvider implements ProjectCustomizer.CompositeCategoryProvider {

   @Override
   public ProjectCustomizer.Category createCategory(Lookup context) {
      return ProjectCustomizer.Category.create("IssueTracking",
                                               Bundle.CTL_IssueTracker(),
                                               BugtrackingRootNode.getDefault().getIcon(BeanInfo.ICON_COLOR_16x16));
   }

   @Override
   public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
      //SingleModuleProperties props = context.lookup(SingleModuleProperties.class);
      ModuleProperties props = context.lookup(ModuleProperties.class);
      assert props != null;
      //Project p = props.getProject();
      NbModuleProject p = context.lookup(NbModuleProject.class);
      if (p == null) {
         return new JPanel(); // broken project?
      }
      return new CustomizerIssueTracking(props, category, p);
   }
}
