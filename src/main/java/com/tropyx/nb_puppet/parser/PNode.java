/*
 * Copyright (C) 2015 mkleint
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

import java.util.Arrays;

public class PNode extends PElement {
    private String[] names;
    
    public PNode(PElement parent, int offset) {
        super(NODE, parent, offset);
    }

    public String[] getNames() {
        return names;
    }

    void setNames(String[] names) {
        this.names = names;
    }


    @Override
    public String toString() {
        return super.toString() + "[" + Arrays.toString(names) + "]";
    }


}
