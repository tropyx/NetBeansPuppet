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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;

public class PPIndexerFactory extends EmbeddingIndexerFactory {

    private static final Logger LOG = Logger.getLogger(PPIndexerFactory.class.getName());

    public PPIndexerFactory() {
    }

    @Override
    public void scanFinished(Context context) {
//        System.out.println("scan finished" + context.getRootURI());
        super.scanFinished(context); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean scanStarted(Context context) {
//        System.out.println("scan started" + context.getRootURI());
        return super.scanStarted(context); //To change body of generated methods, choose Tools | Templates.
    }
    

    @Override
    public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
        try {
            IndexingSupport is = IndexingSupport.getInstance(context);
            for (Indexable i : deleted) {
                is.removeDocuments(i);
            }
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
        }
    }

    @Override
    public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        try {
            IndexingSupport is = IndexingSupport.getInstance(context);
            for (Indexable i : dirty) {
                is.markDirtyDocuments(i);
            }
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
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

    @Override
    public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
        return new PPIndexer();
    }

}
