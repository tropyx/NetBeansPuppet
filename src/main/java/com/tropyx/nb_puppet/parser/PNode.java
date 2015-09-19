/*
 * Copyright (C) 2014 mkleint
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tropyx.nb_puppet.parser;

import java.util.ArrayList;
import java.util.List;

public class PNode {
    
    public static int ROOT = 0;
    public static int RESOURCE = 1;
    

    private final int type;    
    private final List<PNode> children = new ArrayList<>();
    private final PNode parent;

    public PNode(int type, PNode parent) {
        this.type = type;
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }
    }

    public List<PNode> getChildren() {
        return children;
    }
    
    
    public int getType() {
        return type;
    }

    private void addChild(PNode aThis) {
        children.add(aThis);
    }
}
