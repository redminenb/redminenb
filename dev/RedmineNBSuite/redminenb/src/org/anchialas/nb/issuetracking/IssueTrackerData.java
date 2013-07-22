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
package org.anchialas.nb.issuetracking;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;

/**
 * IssueTracker settings data.
 *
 * @author Anchialas <anchialas@gmail.com>
 */
public class IssueTrackerData {

   static final String KEY_ENABLED = "issuetracking.enabled"; // NOI18N
   static final String KEY_NAME = "issuetracking.name"; // NOI18N
   static final String KEY_URL = "issuetracking.url"; // NOI18N
   /**
    * Connector ID
    *
    * @see org.netbeans.modules.bugtracking.api.Repository#getId()
    */
   static final String KEY_CONNECTOR = "issuetracking.connector"; // NOI18N
   //
   private AntProjectHelper helper;
   //
   private boolean enabled;
   //private String connector;
   private String connectorId;
   private String name;
   private String url;

   private IssueTrackerData() {
   }

   static String getPropertiesPath() {
      boolean shared = true;
      String path = shared ? AntProjectHelper.PROJECT_PROPERTIES_PATH : AntProjectHelper.PRIVATE_PROPERTIES_PATH;
      return path;
   }

   @CheckForNull
   public static IssueTrackerData create(@NonNull AntProjectHelper helper) {
      EditableProperties props = helper.getProperties(getPropertiesPath());
      if (props == null) {
         return null;
      }

      IssueTrackerData data = new IssueTrackerData();
      data.helper = helper;

      data.enabled = Boolean.valueOf(props.getProperty(KEY_ENABLED));
      data.connectorId = props.getProperty(KEY_CONNECTOR);
      data.name = props.getProperty(KEY_NAME);
      data.url = props.getProperty(KEY_URL);
      return data;
   }

   public void store() {
      String path = getPropertiesPath();
      EditableProperties props = helper.getProperties(getPropertiesPath());

      props.setProperty(KEY_ENABLED, String.valueOf(enabled));
      props.setProperty(KEY_CONNECTOR, connectorId);
      props.setProperty(KEY_NAME, name);
      props.setProperty(KEY_URL, url);

      helper.putProperties(path, props);
   }

   public AntProjectHelper getHelper() {
      return helper;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   //   public void setConnector(String connector) {
   //      this.connector = connector;
   //   }
   //   public String getConnector() {
   //      return connector;
   //   }
   public String getConnectorId() {
      return connectorId;
   }

   public void setConnectorId(String connectorId) {
      this.connectorId = connectorId;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setUrl(String url) {
      this.url = url;
   }

   public boolean isEnabled() {
      return enabled;
   }

   public String getName() {
      return name;
   }

   public String getUrl() {
      return url;
   }
}
