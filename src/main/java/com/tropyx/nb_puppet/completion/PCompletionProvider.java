
package com.tropyx.nb_puppet.completion;

import com.tropyx.nb_puppet.indexer.PPIndexer;
import com.tropyx.nb_puppet.indexer.PPIndexerFactory;
import com.tropyx.nb_puppet.lexer.PLanguageProvider;
import com.tropyx.nb_puppet.lexer.PTokenId;
import com.tropyx.nb_puppet.parser.PClass;
import com.tropyx.nb_puppet.parser.PElement;
import com.tropyx.nb_puppet.parser.PuppetParserResult;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
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

@MimeRegistration(service = CompletionProvider.class, mimeType = PLanguageProvider.MIME_TYPE)
public class PCompletionProvider implements CompletionProvider {

    @Override
    public CompletionTask createTask(final int queryType, JTextComponent component) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE && queryType != CompletionProvider.COMPLETION_ALL_QUERY_TYPE) return null;
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            @Override
            protected void query(final CompletionResultSet completionResultSet, final Document document, final int caretOffset) {
                final boolean[] completeClasses = new boolean[1];
                final boolean[] completeVariables = new boolean[1];
                final String[] prefix = new String[1];
                document.render(new Runnable() {

                    @Override
                    public void run() {
                        TokenHierarchy th = TokenHierarchy.get(document);
                        @SuppressWarnings("unchecked")
                        TokenSequence<PTokenId> ts = th.tokenSequence();
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
                                }
                            }
                            if (token.id() == PTokenId.IDENTIFIER) {
                                try {
                                    pref = document.getText(ts.offset(), caretOffset - ts.offset());
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
                                System.out.println("prefix=" + pref);
                            }
                        }
                    }
                });
                if (completeClasses[0]) {
                    boolean thisProjectOnly = queryType == COMPLETION_QUERY_TYPE;
                    try {
                        QuerySupport qs = PPIndexerFactory.getQuerySupportFor(document, !thisProjectOnly);
                        for (IndexResult res : qs.query(PPIndexer.FLD_CLASS, "" + prefix[0], QuerySupport.Kind.PREFIX, PPIndexer.FLD_CLASS)) {
                            completionResultSet.addItem(new PPCompletionItem(prefix[0], res.getValue(PPIndexer.FLD_CLASS), caretOffset));
                        }
                        if (thisProjectOnly) {
                            completionResultSet.setHasAdditionalItems(true);
                            completionResultSet.setHasAdditionalItemsText("Results from open projects");
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    completionResultSet.finish();
                    return;
                }
                if (completeVariables[0]) {
                    final boolean thisProjectOnly = queryType == COMPLETION_QUERY_TYPE;
                    runWithParserResult(document, new ParseResultRunnable() {
                        @Override
                        public void run(PElement rootNode) {
                            if (rootNode == null) {
                                completionResultSet.finish();
                                return;
                            }
                            //if completing from same class or class we inherit, use simple name, otherwise
                            //use the full name
                            String currentName = "";
                            String inherits = "";
                            List<PClass> clazzes = rootNode.getChildrenOfType(PClass.class, false);
                            if (!clazzes.isEmpty()) {
                                PClass ppclazz = clazzes.get(0);
                                currentName = ppclazz.getName();
                                inherits = ppclazz.getInherits() != null ? ppclazz.getInherits().getName() : "";
                            }
                            try {
                                QuerySupport qs = PPIndexerFactory.getQuerySupportFor(document, !thisProjectOnly);
                                QuerySupport.Query.Factory qf = qs.getQueryFactory();
                                String pref = prefix[0].substring(1);
                                //TODO how to query just aaa::params::a|
                                // we would need to split on :: and do exact match on class and prefix on var name
                                QuerySupport.Query query =
                                    qf.or(
                                        qf.field(PPIndexer.FLD_VAR, "" + pref, QuerySupport.Kind.PREFIX),
                                        qf.field(PPIndexer.FLD_CLASS, "" + pref, QuerySupport.Kind.PREFIX));
                                for (IndexResult res : query.execute(PPIndexer.FLD_VAR, PPIndexer.FLD_CLASS)) {
                                    String clazz = res.getValue(PPIndexer.FLD_CLASS);
                                    for (String val : new HashSet<>(Arrays.asList(res.getValues(PPIndexer.FLD_VAR)))) {
                                        if (val.startsWith(pref) || clazz.startsWith(pref)) {
                                            completionResultSet.addItem(new PPVariableCompletionItem(prefix[0], val, caretOffset, clazz, currentName, inherits));
                                        }
                                    }
                                }
                                if (thisProjectOnly) {
                                    completionResultSet.setHasAdditionalItems(true);
                                    completionResultSet.setHasAdditionalItemsText("Results from open projects");
                                }
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
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

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    void runWithParserResult(final Document document, final ParseResultRunnable runnable) {
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

    interface ParseResultRunnable {
        void run(PElement rootNode);
    }
}
