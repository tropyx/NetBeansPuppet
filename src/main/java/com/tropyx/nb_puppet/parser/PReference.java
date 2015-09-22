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

public class PReference extends PElement {

    private final String resourceType;

    private PElement title;


    public PReference(PElement parent, int offset, String resourceType) {
        super(REFERENCE, parent, offset);
        this.resourceType = resourceType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public PElement getTitle() {
        return title;
    }

    public void setTitle(PElement title) {
        this.title = title;
    }
}
