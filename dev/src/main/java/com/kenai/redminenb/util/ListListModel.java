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
package com.kenai.redminenb.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 *
 * @author Anchialas <anchialas@gmail.com>
 */
public class ListListModel<T> extends AbstractListModel {

   protected final List<T> list;

   public ListListModel(List<T> list) {
      this.list = list;
   }

   public List<T> getElements() {
      return Collections.unmodifiableList(list);
   }
   
   public void add(T e) {
      list.add(e);
      fireIntervalAdded(this, list.size() - 1, list.size() - 1);
   }

   public void add(int index, T element) {
      list.add(index, element);
      fireIntervalAdded(this, index, index);
   }

   public T remove(int index) {
      T removed = list.remove(index);
      fireIntervalRemoved(this, index, index);
      return removed;
   }

   public void remove(T o) {
      int index = list.indexOf(o);
      if (index != -1) {
         remove(index);
      }
   }

   public void addAll(Collection<? extends T> c) {
      int idx1 = list.size() - 1;
      list.addAll(c);
      int idx2 = list.size() - 1;
      fireIntervalAdded(this, idx1, idx2);
   }

   public void removeAll(Collection<? extends T> c) {
      list.removeAll(c);
      int firstIndex = 0;
      int lastIndex = list.size() - 1;
      fireContentsChanged(this, firstIndex, lastIndex);
   }

   public void clear() {
      if (!list.isEmpty()) {
         int firstIndex = 0;
         int lastIndex = list.size() - 1;
         list.clear();
         fireIntervalRemoved(this, firstIndex, lastIndex);
      }
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
}
