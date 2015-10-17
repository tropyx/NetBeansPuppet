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

public class PClass extends PElement implements PParamContainer {
    private PIdentifier name;
    private PClassRef inherits;
    private PClassParam[] params = new PClassParam[0];
    
    public PClass(PElement parent, int offset) {
        super(CLASS, parent, offset);
    }

    public String getName() {
        return name.getName();
    }

    void setName(PIdentifier name) {
        this.name = name;
    }

    public PClassRef getInherits() {
        return inherits;
    }

    void setInherits(PClassRef inherits) {
        this.inherits = inherits;
    }

    @Override
    public PClassParam[] getParams() {
        return params;
    }

    @Override
    public void setParams(PClassParam[] params) {
        assert params != null;
        this.params = params;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + name + "]";
    }


}
