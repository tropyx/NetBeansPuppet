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
    private PVariableDefinition variable;
    private PElement defaultValue;
    
    public PClassParam(PElement parent, int offset) {
        super(CLASS_PARAM, parent, offset);
    }

    PClassParam(PElement parent, int offset, String var) {
        this(parent, offset);
        setVariableDefinition(new PVariableDefinition(this, offset, var)); //TODO proper offset
    }

    public String getTypeType() {
        return type;
    }

    void setTypeType(String type) {
        this.type = type;
    }

    public PVariableDefinition getVariable() {
        return variable;
    }

    private void setVariableDefinition(PVariableDefinition variable) {
        this.variable = variable;
    }

    public PElement getDefaultValue() {
        return defaultValue;
    }

    void setDefaultValue(PElement defaultValue) {
        this.defaultValue = defaultValue;
    }

        @Override
    public String toString() {
        return super.toString() + "[" + getTypeType() + " : " + variable.toString() + "]";
    }

}
