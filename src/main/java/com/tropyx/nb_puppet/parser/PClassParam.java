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

public class PClassParam extends PElement {
    private String type;
    private final PVariableDefinition variable;
    private PElement defaultValue;
    
    PClassParam(PElement parent, int offset, PVariableDefinition var) {
        super(CLASS_PARAM, parent, offset);
        this.variable = var;
        this.variable.setParent(this);
    }

    public String getTypeType() {
        return type;
    }

    void setTypeType(String type) {
        this.type = type;
    }

    public String getVariable() {
        return variable.getName();
    }

    public PElement getDefaultValue() {
        return defaultValue;
    }

    void setDefaultValue(PElement defaultValue) {
        this.defaultValue = defaultValue;
    }

        @Override
    public String toString() {
        return super.toString() + "[" + getTypeType() + " : " + variable.getName() + "]";
    }

}
