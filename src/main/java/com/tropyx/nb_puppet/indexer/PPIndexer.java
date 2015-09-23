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

package com.tropyx.nb_puppet.indexer;

import com.tropyx.nb_puppet.parser.PClass;
import com.tropyx.nb_puppet.parser.PClassParam;
import com.tropyx.nb_puppet.parser.PClassRef;
import com.tropyx.nb_puppet.parser.PElement;
import com.tropyx.nb_puppet.parser.PVariable;
import com.tropyx.nb_puppet.parser.PVariableDefinition;
import com.tropyx.nb_puppet.parser.PuppetParserResult;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;

public class PPIndexer extends EmbeddingIndexer {
    public static final String FLD_VARREF = "varref";
    public static final String FLD_VAR = "var";
    public static final String FLD_PARAM = "param";
    public static final String FLD_CLASSREF = "classref";
    public static final String FLD_CLASS = "class";
    
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
        PuppetParserResult res = (PuppetParserResult) parserResult;
        PElement root = res.getRootNode();
        for (PElement ch : root.getChildren()) {
            if (ch.getType() == PElement.CLASS) {
                PClass cl = (PClass)ch;
                String name = cl.getName();
                document.addPair(FLD_CLASS, name, true, true);
                if (cl.getInherits() != null) {
                    document.addPair(FLD_CLASSREF, cl.getInherits().getName(), true, false);
                }
                List<PClassRef> refs = cl.getChildrenOfType(PClassRef.class, true);
                for (PClassRef ref : refs) {
                    document.addPair(FLD_CLASSREF, ref.getName(), true, false);
                }
                for (PClassParam param : cl.getParams()) {
                    document.addPair(FLD_PARAM, param.getVariable().getName(), false, true);
                }
                List<PVariableDefinition> varDefs = cl.getChildrenOfType(PVariableDefinition.class, true);
                for (PVariableDefinition vd : varDefs) {
                    document.addPair(FLD_VAR, vd.getName(), true, true);
                }
                List<PVariable> vars = cl.getChildrenOfType(PVariable.class, true);
                for (PVariable v : vars) {
                    document.addPair(FLD_VARREF, v.getName(), true, false);
                }
            }
        }

        support.addDocument(document);
    }

}
