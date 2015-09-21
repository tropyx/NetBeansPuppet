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

public class PString extends PElement {
    private String value;
    
    public PString(PElement parent, int offset, String value) {
        super(STRING, parent, offset);
        this.value = value;
        if (this.value.startsWith("\"")) {
            this.value = this.value.substring(1);
        }
        if (this.value.endsWith("\"")) {
            this.value = this.value.substring(0, this.value.length());
        }
    }

    public String getValue() {
        return value;
    }

    void setValue(String value) {
        this.value = value;
    }
    
}
