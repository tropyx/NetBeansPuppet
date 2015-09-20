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

import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;

public class PPIndexerFactory extends EmbeddingIndexerFactory {

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

    @Override
    public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
        return new EmbeddingIndexer() {

             @Override
             protected void index(Indexable indexable, Parser.Result parserResult, Context context) {
                 System.out.println("index:" + indexable.getURL());
                 System.out.println("result:" + parserResult);
             }
         };

    }

}
