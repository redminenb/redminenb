/*
 * Copyright 2014 Matthias Bläsing <mblaesing@doppel-helix.eu>.
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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import javax.swing.JLayeredPane;

public class FullSizeLayout implements LayoutManager {

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            int maxX = 0;
            int maxY = 0;
            for (Component c : parent.getComponents()) {
                Dimension d = c.getPreferredSize();
                if (d.getHeight() > maxY) {
                    maxY = (int) d.getHeight();
                }
                if (d.getWidth() > maxX) {
                    maxX = (int) d.getWidth();
                }
            }
            return new Dimension(maxX, maxY);
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            int maxX = 0;
            int maxY = 0;
            for (Component c : parent.getComponents()) {
                Dimension d = c.getMinimumSize();
                if (d.getHeight() > maxY) {
                    maxY = (int) d.getHeight();
                }
                if (d.getWidth() > maxX) {
                    maxX = (int) d.getWidth();
                }
            }
            return new Dimension(maxX, maxY);
        }
    }

    @Override
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            Rectangle innerBounds = parent.getBounds();
            innerBounds.setLocation(0, 0);
            for (Component c : parent.getComponents()) {
                c.setBounds(innerBounds);
                c.doLayout();
            }
        }
    }
}
