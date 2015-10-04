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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;

public class PPIndexerFactory extends EmbeddingIndexerFactory {
    public static final String INDEXER_TYPE = "puppet";
    public static final int INDEXER_VERSION = 1;

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
        return INDEXER_TYPE;
    }

    @Override
    public int getIndexVersion() {
        return INDEXER_VERSION;
    }

    @Override
    public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
        return new PPIndexer();
    }
    
    public static QuerySupport getQuerySupportFor(final Document document, boolean allOpenProjects) throws IOException {
        FileObject fo = NbEditorUtilities.getFileObject(document);
        Collection<FileObject> roots;
        if (allOpenProjects) {
            roots = GlobalPathRegistry.getDefault().getSourceRoots();
        } else {
            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
            if (cp != null) {
                roots = Arrays.asList(cp.getRoots());
            } else {
                roots = Collections.emptyList();
            }
        }
        return QuerySupport.forRoots(PPIndexerFactory.INDEXER_TYPE, PPIndexerFactory.INDEXER_VERSION, roots.toArray(new FileObject[0]));
    }


}
