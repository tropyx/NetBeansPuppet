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

public class PCondition extends PElement  {
    
    private PBlob condition;
    
    private PBlob consequence;

    private PElement otherwise;

    public PCondition(PElement parent, int offset) {
        super(CONDITION, parent, offset);
    }

    public PBlob getCondition() {
        return condition;
    }

    public void setCondition(PBlob condition) {
        this.condition = condition;
    }

    public PBlob getConsequence() {
        return consequence;
    }

    public void setConsequence(PBlob consequence) {
        this.consequence = consequence;
    }

    public PElement getOtherwise() {
        return otherwise;
    }

    public void setOtherwise(PElement otherwise) {
        this.otherwise = otherwise;
    }


}
