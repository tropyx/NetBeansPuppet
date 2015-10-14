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
import com.tropyx.nb_puppet.parser.PDefine;
import com.tropyx.nb_puppet.parser.PElement;
import com.tropyx.nb_puppet.parser.PParamContainer;
import com.tropyx.nb_puppet.parser.PResource;
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
    public static final String FLD_REQ_PARAM = "reqparam"; //define, class parameter without default value
    public static final String FLD_INHERIT = "inherit";
    public static final String FLD_CLASSREF = "classref";
    /**
     * root class or define name, stored searcheable
     */
    public static final String FLD_ROOT = "root";
    /**
     * class name if file is class, searchable only -> stored is FLD_ROOT
     */
    public static final String FLD_CLASS = "class";
    /**
     * define name if file is define, searchable only -> stored is FLD_ROOT
     */
    public static final String FLD_DEFINE = "define";
    public static final String FLD_RESOURCE = "resource";
    
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
                document.addPair(FLD_ROOT, name, true, true);
                document.addPair(FLD_CLASS, name, true, false);
                if (cl.getInherits() != null) {
                    document.addPair(FLD_CLASSREF, cl.getInherits().getName(), true, false);
                    document.addPair(FLD_INHERIT, cl.getInherits().getName(), true, true);
                }
            }
            if (ch.getType() == PElement.DEFINE) {
                PDefine def = (PDefine)ch;
                String name = def.getName();
                document.addPair(FLD_ROOT, name, true, true);
                document.addPair(FLD_DEFINE, name, true, false);
            }
            if (ch instanceof PParamContainer) {
                PParamContainer cl = (PParamContainer)ch;
                for (PClassParam param : cl.getParams()) {
                    document.addPair(FLD_PARAM, stripDollar(param.getVariable()), false, true);
                    if (param.getDefaultValue() == null) {
                        document.addPair(FLD_REQ_PARAM, stripDollar(param.getVariable()), false, true);
                    }
                }
            }
            List<PClassRef> refs = ch.getChildrenOfType(PClassRef.class, true);
            for (PClassRef ref : refs) {
                document.addPair(FLD_CLASSREF, ref.getName(), true, false);
            }
            List<PVariableDefinition> varDefs = ch.getChildrenOfType(PVariableDefinition.class, true);
            for (PVariableDefinition vd : varDefs) {
                document.addPair(FLD_VAR, stripDollar(vd.getName()), true, true);
                document.addPair(FLD_VARREF, stripDollar(vd.getName()), true, false);
            }
            List<PVariable> vars = ch.getChildrenOfType(PVariable.class, true);
            for (PVariable v : vars) {
                document.addPair(FLD_VARREF, stripDollar(v.getName()), true, false);
            }
            List<PResource> resources = ch.getChildrenOfType(PResource.class, true);
            for (PResource r : resources) {
                document.addPair(FLD_RESOURCE, r.getResourceType(), true, false);
            }
        }
        support.addDocument(document);
    }

    private String stripDollar(String s) {
        return s.startsWith("$") ? s.substring(1) : s;
    }

}
