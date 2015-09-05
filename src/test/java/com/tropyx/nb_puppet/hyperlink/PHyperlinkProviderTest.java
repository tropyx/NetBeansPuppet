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
package com.tropyx.nb_puppet.hyperlink;

import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Pair;

/**
 *
 * @author mkleint
 */
public class PHyperlinkProviderTest {
    
    public PHyperlinkProviderTest() {
    }


    @Test
    public void testGetPathAndVariable() {
        PHyperlinkProvider inst = new PHyperlinkProvider();
        Pair<String, String> tup = inst.getPathAndVariable("mod::params::var");
        assertEquals("mod/manifests/params.pp", tup.first());
        assertEquals("$var", tup.second());
        
        tup = inst.getPathAndVariable("mod::foo::params::var");
        assertEquals("mod/manifests/foo/params.pp", tup.first());
        assertEquals("$var", tup.second());
        
        tup = inst.getPathAndVariable("mod::var");
        assertEquals("mod/manifests/init.pp", tup.first());
        assertEquals("$var", tup.second());
    }

    
}
