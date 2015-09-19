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

package com.tropyx.nb_puppet.indexer;

import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

public class PPIndexerFactory extends CustomIndexerFactory {

    public PPIndexerFactory() {
    }
    

    @Override
    public CustomIndexer createIndexer() {
        return new CustomIndexer() {

            @Override
            protected void index(Iterable<? extends Indexable> files, Context context) {
                for (Indexable indx : files) {
                    FileObject fo = URLMapper.findFileObject(indx.getURL());
                    if (fo != null) {
                        System.out.println("indexing " + fo);
                    }
                    
                }
            }
        };
    }

    @Override
    public boolean supportsEmbeddedIndexers() {
        return true;
    }

    @Override
    public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
        for (Indexable d : deleted) {
            System.out.println("de:" + d.getURL());
        }
    }

    @Override
    public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        for (Indexable d : dirty) {
            System.out.println("di:" + d.getURL());
        }
    }

    @Override
    public String getIndexerName() {
        return "puppet";
    }

    @Override
    public int getIndexVersion() {
        return 1;
    }

}
