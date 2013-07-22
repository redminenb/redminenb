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
package org.anchialas.nb.issuetracking.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(category = "Project",
          id = "org.anchialas.nb.issuetracking.action.CreateIssue")
@ActionRegistration(displayName = "#CTL_CreateIssue")
@ActionReference(path = "Menu/Versioning", position = -10)
@Messages("CTL_CreateIssue=New Issue…")
public final class CreateIssue implements ActionListener {

   @Override
   public void actionPerformed(ActionEvent e) {
      // TODO implement action body
   }
}
