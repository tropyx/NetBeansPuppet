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

import java.util.HashMap;
import java.util.Map;

public class PCase extends PElement  {
    
    private PBlob control;
    
    private final Map<PBlob, PBlob> cases = new HashMap<>();

    public PCase(PElement parent, int offset) {
        super(CASE, parent, offset);
    }

    public PBlob getControl() {
        return control;
    }

    public void setControl(PBlob control) {
        this.control = control;
    }

    public Map<PBlob, PBlob> getCases() {
        return cases;
    }


    void addCase(PBlob cas, PBlob caseBody) {
        cases.put(cas, caseBody);
    }

}
