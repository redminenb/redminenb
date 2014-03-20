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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.MutableComboBoxModel;

/**
 *
 * @author Anchialas <anchialas@gmail.com>
 */
@SuppressWarnings("unchecked")
public class ListComboBoxModel<T> extends ListListModel<T> implements MutableComboBoxModel, Serializable {

   private T selectedObject;

   public ListComboBoxModel() {
      this(new ArrayList<T>());
   }

   public ListComboBoxModel(List<T> list) {
      super(list);
   }

   public void setSelectedObject(T anObject) {
      if ((selectedObject != null && !selectedObject.equals(anObject))
              || selectedObject == null && anObject != null) {
         selectedObject = anObject;
         fireContentsChanged(this, -1, -1);
      }
   }

   // ComboBoxModel implementation --------------------------------------------
   @Override
   public void setSelectedItem(Object anItem) {
      setSelectedObject((T)anItem);
   }

   @Override
   public Object getSelectedItem() {
      return selectedObject;
   }

   @Override
   public void add(T e) {
      super.add(e);
      if (list.size() == 1 && selectedObject == null && e != null) {
         setSelectedItem(e);
      }
   }

   @Override
   public T remove(int index) {
      if (getElementAt(index) == selectedObject) {
         if (index == 0) {
            setSelectedItem(getSize() == 1 ? null : getElementAt(index + 1));
         } else {
            setSelectedItem(getElementAt(index - 1));
         }
      }
      return super.remove(index);
   }

   @Override
   public void clear() {
      selectedObject = null;
      super.clear();
   }

   @Override
   public void addElement(Object obj) {
      add((T)obj);
   }

   @Override
   public void removeElement(Object obj) {
      remove((T)obj);
   }

   @Override
   public void insertElementAt(Object obj, int index) {
      add(index, (T)obj);
   }

   @Override
   public void removeElementAt(int index) {
      remove(index);
   }
}
