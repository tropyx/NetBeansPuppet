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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PString extends PElement {
    private final String value;
    final static Pattern VAR = Pattern.compile("\\$\\{([a-zA-Z_:]+?)\\}");
    
    public PString(PElement parent, int offset, String value) {
        super(STRING, parent, offset);
        if (value.length() == 2) {
            this.value = "";
        } else {
            if (value.startsWith("\"") && value.endsWith("\"")) {
                Matcher m = VAR.matcher(value);
                while (m.find()) {
                    String var = m.group(1);
                    //+1 for the ${ character
                    new PVariable(this, offset + m.start() + 1, "$" + var);
                }
                this.value = value.substring(1, value.length() - 1);
            }
            else if (value.startsWith("'") && value.endsWith("'")) {
                this.value = value.substring(1, value.length() - 1);
            } else {
                this.value = "";
            }
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public int getEndOffset() {
        return getOffset() + value.length() + 2;
    }
    @Override
    public String toString() {
        return super.toString() + "[" +  value +  ']';
    }
    
}
