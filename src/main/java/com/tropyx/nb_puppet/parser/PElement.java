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

public class PElement {
    
    public static final int ROOT = 0;
    public static final int RESOURCE = 1;
    public static final int CLASS = 2;
    public static final int CLASS_REF = 3;
    public static final int CLASS_PARAM = 4;
    public static final int VARIABLE = 5;
    public static final int STRING = 6;
    public static final int RESOURCE_ATTR = 7;

    

    private final int type;    
    private final List<PElement> children = new ArrayList<>();
    private PElement parent;

    public PElement(int type, PElement parent) {
        this.type = type;
        setParent(parent);
    }

    public List<PElement> getChildren() {
        return children;
    }

    public final void setParent(PElement parent) {
        if (this.parent != null) {
            throw new IllegalStateException("Cannot reassign parent element");
        }
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }
    }
    
    
    public int getType() {
        return type;
    }

    private void addChild(PElement aThis) {
        children.add(aThis);
    }
}
