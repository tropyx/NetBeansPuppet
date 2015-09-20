
package com.tropyx.nb_puppet.indexer;

import com.tropyx.nb_puppet.parser.PuppetParserResult;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;

public class PPIndexer extends EmbeddingIndexer {
    
    private static final Logger LOG = Logger.getLogger(PPIndexer.class.getName());

    @Override
    protected void index(Indexable indexable, Parser.Result parserResult, Context context) {
        if (!(parserResult instanceof PuppetParserResult)) {
            return;
        }

        IndexingSupport support;
        try {
            support = IndexingSupport.getInstance(context);
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
            return;
        }
        // we need to remove old documents (document per object, not file)
        support.removeDocuments(indexable);

        IndexDocument document = support.createDocument(indexable);
        //TODO...
        PuppetParserResult res = (PuppetParserResult) parserResult;
        res.getRootNode();

        support.addDocument(document);
    }

}
