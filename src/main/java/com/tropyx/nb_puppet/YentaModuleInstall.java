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
package com.tropyx.nb_puppet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.contrib.yenta.Yenta;

public class YentaModuleInstall extends Yenta {

    @Override
    protected Set<String> friends() {
        return new HashSet<>(Arrays.asList(new String[] {
            "org.netbeans.modules.editor.breadcrumbs",
            "org.netbeans.modules.jumpto"
        }));
    }
}
