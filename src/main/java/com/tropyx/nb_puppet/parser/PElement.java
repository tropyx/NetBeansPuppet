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
    public static final int ARRAY = 8;
    public static final int HASH = 9;
    public static final int REGEXP = 10;
    public static final int REFERENCE = 11;
    public static final int BLOB = 12;
    public static final int VARIABLE_DEFINITION = 13;
    public static final int DEFINE = 14;
    public static final int NODE = 15;

    private final int type;    
    private final List<PElement> children = new ArrayList<>();
    private PElement parent;
    private final int offset;

    public PElement(int type, PElement parent, int offset) {
        this.type = type;
        this.offset = offset;
        setParent(parent);
    }

    public List<PElement> getChildren() {
        return children;
    }

    public <T extends PElement> List<T> getChildrenOfType(Class<T> clazz, boolean recursive) {
        List<T> toRet = new ArrayList<>();
        for (PElement ch : children) {
            if (clazz.equals(ch.getClass())) {
                toRet.add((T)ch);
            }
            if (recursive) {
                toRet.addAll(ch.getChildrenOfType(clazz, recursive));
            }
        }
        return toRet;
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

    public int getOffset() {
        return offset;
    }
    
    public int getType() {
        return type;
    }

    private void addChild(PElement aThis) {
        children.add(aThis);
    }

    public String toStringRecursive() {
        StringBuilder sb = new StringBuilder(toString());
        if (children.size() > 0) {
            for (PElement ch : children) {
                String s = ch.toStringRecursive();
                s = s.replace("\n", "\n  ");
                sb.append("\n  ").append(s);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }


}
