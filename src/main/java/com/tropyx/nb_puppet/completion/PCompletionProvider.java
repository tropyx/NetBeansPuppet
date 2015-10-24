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

import com.tropyx.nb_puppet.PPConstants;
import com.tropyx.nb_puppet.indexer.PPIndexer;
import com.tropyx.nb_puppet.indexer.PPIndexerFactory;
import com.tropyx.nb_puppet.lexer.PLangHierarchy;
import com.tropyx.nb_puppet.lexer.PTokenId;
import com.tropyx.nb_puppet.parser.PClass;
import com.tropyx.nb_puppet.parser.PClassParam;
import com.tropyx.nb_puppet.parser.PElement;
import com.tropyx.nb_puppet.parser.PuppetParserResult;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.editor.completion.CompletionProvider;
import static org.netbeans.spi.editor.completion.CompletionProvider.COMPLETION_QUERY_TYPE;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.util.Exceptions;

@MimeRegistration(service = CompletionProvider.class, mimeType = PPConstants.MIME_TYPE)
public class PCompletionProvider implements CompletionProvider {

    @Override
    public CompletionTask createTask(final int queryType, JTextComponent component) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE && queryType != CompletionProvider.COMPLETION_ALL_QUERY_TYPE) return null;
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(final CompletionResultSet completionResultSet, final Document document, final int caretOffset) {
                final boolean[] completeClasses = new boolean[1];
                final boolean[] completeVariables = new boolean[1];
                final boolean[] completeVariablesInString = new boolean[1];
                final boolean[] completeFunctions = new boolean[1];
                final boolean[] completeResources = new boolean[1];
                final String[] prefix = new String[1];
                
//                runWithParserResult(document, new ParseResultRunnable() {
//                    @Override
//                    public void run(PElement rootNode) {
//                        if (rootNode == null) {
//                            return;
//                        }
//                        System.out.println("tn1:" + Thread.currentThread().getName());
//                        System.out.println("path:" + rootNode.getChildAtOffset(caretOffset).toStringToRoot());
//                    }
//                });
//                System.out.println("here");
//                System.out.println("tn2:" + Thread.currentThread().getName());

                document.render(new Runnable() {

                    @Override
                    public void run() {
                        TokenSequence<PTokenId> ts = PLangHierarchy.getTokenSequence(document);
                        ts.move(caretOffset);
                        ts.moveNext();
                        Token<PTokenId> token = ts.token();
                        String pref = null;
                        if (token != null) {
                            if (token.id() == PTokenId.WHITESPACE) {
                                if (ts.offset() == caretOffset) {
                                    ts.movePrevious();
                                    token = ts.token();
                                } else {
                                    pref = "";
                                    completeFunctions[0] = true;
                                    completeResources[0] = true;
                                }
                            }
                            if (token.id() == PTokenId.IDENTIFIER) {
                                try {
                                    pref = document.getText(ts.offset(), caretOffset - ts.offset());
                                    completeFunctions[0] = true;
                                    completeResources[0] = true;
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                            if (token.id() == PTokenId.VARIABLE) {
                                try {
                                    pref = document.getText(ts.offset(), caretOffset - ts.offset());
                                } catch (BadLocationException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                                completeVariables[0] = true;
                            }
                            if (token.id() == PTokenId.STRING_LITERAL) {
                                int currentInText = caretOffset - ts.offset();
                                String text = token.text().toString().substring(0, currentInText);
                                int start = text.lastIndexOf("${");
                                if (start != -1 && start < currentInText && text.indexOf("}", start) == -1) {
                                    pref = text.substring(start, text.length()).replace("${", "$");
                                    completeVariables[0] = true;
                                    completeVariablesInString[0] = true;
                                }
                            }
                            if (pref != null) {
                                prefix[0] = pref;
                                ts.movePrevious();
                                token = ts.token();
                                if (token.id() == PTokenId.WHITESPACE) {
                                    ts.movePrevious();
                                    token = ts.token();
                                }
                                if (token.id() == PTokenId.INCLUDE || token.id() == PTokenId.REQUIRE || token.id() == PTokenId.INHERITS) {
                                    completeClasses[0] = true;
                                }
                            }
                        }
                    }
                });
                if (completeFunctions[0]) {
                    completeFunctions(prefix[0], completionResultSet, caretOffset);
                }
                if (completeResources[0]) {
                    completeResources(prefix[0], completionResultSet, caretOffset, document, queryType);
                }
                if (completeClasses[0]) {
                    boolean thisProjectOnly = checkAndMarkQueryType(queryType, completionResultSet);
                    try {
                        QuerySupport qs = PPIndexerFactory.getQuerySupportFor(document, !thisProjectOnly);
                        for (IndexResult res : qs.query(PPIndexer.FLD_CLASS, "" + prefix[0], QuerySupport.Kind.PREFIX, PPIndexer.FLD_ROOT)) {
                            completionResultSet.addItem(new PPCompletionItem(prefix[0], res.getValue(PPIndexer.FLD_ROOT), caretOffset));
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    completionResultSet.finish();
                    return;
                }
                if (completeVariables[0]) {
                    final boolean thisProjectOnly = checkAndMarkQueryType(queryType, completionResultSet);
                    runWithParserResult(document, new ParseResultRunnable() {
                        @Override
                        public void run(PElement rootNode) {
                            if (rootNode == null) {
                                completionResultSet.finish();
                                return;
                            }
                            String pref = prefix[0].substring(1);
                            //if completing from same class or class we inherit, use simple name, otherwise
                            //use the full name
                            String currentName = "";
                            String inherits = "";
                            List<PClass> clazzes = rootNode.getChildrenOfType(PClass.class, false);
                            if (!clazzes.isEmpty()) {
                                PClass ppclazz = clazzes.get(0);
                                currentName = ppclazz.getName();
                                inherits = ppclazz.getInherits() != null ? ppclazz.getInherits().getName() : "";
                                for (PClassParam param : ppclazz.getParams()) {
                                    if (param.getVariable().startsWith("$" + pref)) {
                                        completionResultSet.addItem(new PPVariableCompletionItem(prefix[0], param.getVariable().substring(1), caretOffset, currentName, currentName, inherits, completeVariablesInString[0]));
                                    }
                                }
                            }
                            try {
                                QuerySupport qs = PPIndexerFactory.getQuerySupportFor(document, !thisProjectOnly);
                                QuerySupport.Query.Factory qf = qs.getQueryFactory();
                                //TODO how to query just aaa::params::a|
                                // we would need to split on :: and do exact match on class and prefix on var name
                                QuerySupport.Query query =
                                    qf.or(
                                        qf.field(PPIndexer.FLD_VAR, "" + pref, QuerySupport.Kind.PREFIX),
                                        qf.field(PPIndexer.FLD_ROOT, "" + pref, QuerySupport.Kind.PREFIX)
                                    );
                                for (IndexResult res : query.execute(PPIndexer.FLD_VAR, PPIndexer.FLD_ROOT)) {
                                    String clazz = res.getValue(PPIndexer.FLD_ROOT);
                                    for (String val : new HashSet<>(Arrays.asList(res.getValues(PPIndexer.FLD_VAR)))) {
                                        if (val.startsWith(pref) || clazz.startsWith(pref)) {
                                            completionResultSet.addItem(new PPVariableCompletionItem(prefix[0], val, caretOffset, clazz, currentName, inherits, completeVariablesInString[0]));
                                        }
                                    }
                                }
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            for (String glob : GlobalVarsFromPalette.get()) {
                                if (glob.startsWith(pref)) {
                                    completionResultSet.addItem(new PPVariableCompletionItem(prefix[0], glob, caretOffset, "", currentName, inherits, completeVariablesInString[0]));
                                }
                            }
                            completionResultSet.finish();
                        }
                    });
                    return;
                }
                completionResultSet.finish();
            }

        }, component);
    }

    private void completeFunctions(String prefix, CompletionResultSet completionResultSet, int offset) {
        for (PTokenId token : PTokenId.values()) {
            if (PTokenId.Category.FUNCTION.equals(token.primaryCategory())) {
                String name = token.name().toLowerCase(Locale.ENGLISH);
                if (name.startsWith(prefix)) {
                    completionResultSet.addItem(new PPFunctionCompletionItem(prefix, name, offset));
                }
            }
        }
        for (String builtin : BuiltInFunctions.get()) {
            completionResultSet.addItem(new PPFunctionCompletionItem(prefix, builtin, offset));
        }
    }

    private void completeResources(String prefix, CompletionResultSet completionResultSet, int caretOffset, Document document, int queryType) {
        try {
            boolean thisProjectOnly = checkAndMarkQueryType(queryType, completionResultSet);
            QuerySupport qs = PPIndexerFactory.getQuerySupportFor(document, !thisProjectOnly);
            for (IndexResult res : qs.query(PPIndexer.FLD_DEFINE, prefix, QuerySupport.Kind.PREFIX, PPIndexer.FLD_ROOT, PPIndexer.FLD_REQ_PARAM)) {
                String def = res.getValue(PPIndexer.FLD_ROOT);
                completionResultSet.addItem(new PPResourceCompletionItem(prefix, def, caretOffset, res.getValues(PPIndexer.FLD_REQ_PARAM)));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }


    private boolean checkAndMarkQueryType(int queryType, CompletionResultSet completionResultSet) {
        final boolean thisProjectOnly = queryType == COMPLETION_QUERY_TYPE;
        if (thisProjectOnly) {
            completionResultSet.setHasAdditionalItems(true);
            completionResultSet.setHasAdditionalItemsText("Results from open projects");
        }
        return thisProjectOnly;
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    public static void runWithParserResult(final Document document, final ParseResultRunnable runnable) {
        try {
            Source source = Source.create(document);
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result result = resultIterator.getParserResult();
                    if (result instanceof PuppetParserResult) {
                        PuppetParserResult ppresult = (PuppetParserResult)result;
                        runnable.run(ppresult.getRootNode());
                    } else {
                        runnable.run(null);
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public interface ParseResultRunnable {
        void run(PElement rootNode);
    }
}
