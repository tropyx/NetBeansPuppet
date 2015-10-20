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
package com.tropyx.nb_puppet.completion;

public final class BuiltInFunctions {
    private final static String[] builtins = new String[] {
        "alert","collect","create_resources","crit", "defined", "each", "emerg",
        "epp", "extlookup", "file", "filter", "fqdn_rand", "generate", "hiera",
        "hiera_array", "hiera_hash", "hiera_include", "inline_epp", "inline_template",
        "lookup", "map", "md5", "reduce", "regsubst", "search", "select",
        "sha1", "shellquote", "slice", "split", "sprintf", "tagged", "template",
        "versioncmp"
    };

    public static String[] get() {
        return builtins;
    }

}
