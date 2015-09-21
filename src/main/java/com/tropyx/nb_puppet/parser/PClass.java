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

import java.util.ArrayList;
import java.util.List;

public class PClass extends PElement {
    private String name;
    private PClassRef inherits;
    private PClassParam[] params = new PClassParam[0];
    private final List<PClassRef> includes = new ArrayList<>();
    private final List<PClassRef> requires = new ArrayList<>();
    
    public PClass(PElement parent, int offset) {
        super(CLASS, parent, offset);
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public PClassRef getInherits() {
        return inherits;
    }

    void setInherits(PClassRef inherits) {
        this.inherits = inherits;
    }

    public PClassParam[] getParams() {
        return params;
    }

    public void setParams(PClassParam[] params) {
        assert params != null;
        this.params = params;
    }

    public void addInclude(PClassRef ref) {
        includes.add(ref);
    }

    public List<PClassRef> getIncludes() {
        return includes;
    }

    void addRequire(PClassRef pClassRef) {
        requires.add(pClassRef);
    }
    
    public List<PClassRef> getRequires() {
        return requires;
    }
}
