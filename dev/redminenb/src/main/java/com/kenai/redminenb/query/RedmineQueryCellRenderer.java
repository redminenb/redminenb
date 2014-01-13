/*
 * Copyright 2012 Anchialas.
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

import com.kenai.redminenb.RedmineConfig;
import com.kenai.redminenb.issue.RedmineIssueNode.PriorityProperty;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Custom table cell renderer for query result table.
 *
 * @author Anchialas <anchialas@gmail.com>
 */
public class RedmineQueryCellRenderer implements TableCellRenderer {

   private final TableCellRenderer defaultRenderer;

   public RedmineQueryCellRenderer(TableCellRenderer defaultRenderer) {
      this.defaultRenderer = defaultRenderer;
   }

   @Override
   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      JLabel renderer = (JLabel)defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

      try {
         if (value instanceof PriorityProperty) {
            PriorityProperty nodeProperty = (PriorityProperty)value;
            renderer.setIcon(RedmineConfig.getInstance().getPriorityIcon(nodeProperty.toString()));
         }
      } catch (Exception ex) {
      }
      return renderer;
   }
}
