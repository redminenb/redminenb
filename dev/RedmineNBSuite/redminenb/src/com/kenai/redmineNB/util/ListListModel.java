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
package com.kenai.redmineNB.util;

import java.util.List;
import javax.swing.ListModel;


/**
 *
 * @author koe
 */
public class ListListModel<T> implements ListModel {

   private final List<? extends T> list;


   public ListListModel(List<? extends T> list) {
      this.list = list;
   }

   // ListModel implementation --------------------------------------------

   @Override
   public T getElementAt(int index) {
      return list.get(index);
   }


   @Override
   public int getSize() {
      return list.size();
   }


   @Override
   public void removeListDataListener(javax.swing.event.ListDataListener l) {
      // Does nothing - unmodifiable
   }


   @Override
   public void addListDataListener(javax.swing.event.ListDataListener l) {
      // Does nothing - unmodifiable
   }
}
