
package com.tropyx.nb_puppet.completion;

import com.tropyx.nb_puppet.indexer.PPIndexer;
import com.tropyx.nb_puppet.indexer.PPIndexerFactory;
import com.tropyx.nb_puppet.lexer.PLanguageProvider;
import com.tropyx.nb_puppet.lexer.PTokenId;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
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
            protected void query(CompletionResultSet completionResultSet, final Document document, final int caretOffset) {
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
                }
                if (completeVariables[0]) {
                    boolean thisProjectOnly = queryType == COMPLETION_QUERY_TYPE;
                    try {
                        QuerySupport qs = PPIndexerFactory.getQuerySupportFor(document, !thisProjectOnly);
                        for (IndexResult res : qs.query(PPIndexer.FLD_VAR, "" + prefix[0], QuerySupport.Kind.PREFIX, PPIndexer.FLD_VAR, PPIndexer.FLD_CLASS)) {
                            String clazz = res.getValue(PPIndexer.FLD_CLASS);
                            for (String val : new HashSet<>(Arrays.asList(res.getValues(PPIndexer.FLD_VAR)))) {
                                completionResultSet.addItem(new PPCompletionItem(prefix[0], val, caretOffset, clazz));
                            }
                        }
                        if (thisProjectOnly) {
                            completionResultSet.setHasAdditionalItems(true);
                            completionResultSet.setHasAdditionalItemsText("Results from open projects");
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                }
                completionResultSet.finish();
            }

        }, component);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

}
