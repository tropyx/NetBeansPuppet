
package com.tropyx.nb_puppet.lint;

import com.tropyx.nb_puppet.lexer.PLangHierarchy;
import com.tropyx.nb_puppet.lexer.PTokenId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.openide.util.Exceptions;

class ArrowAlignmentFix extends AbstractFix {

    public ArrowAlignmentFix(Document document, int startindex, int endindex) {
        super(document, startindex, endindex, "Arrow alignment - re-align");
    }

    @Override
    public ChangeInfo implement() throws Exception {
        final List<DocumentChange> toReplace = new ArrayList<>();
        document.render(new Runnable() {
            @Override
            public void run() {
                TokenSequence<PTokenId> ts = PLangHierarchy.getTokenSequence(document);
                ts.move(startindex);
                ts.moveNext();
                Token<PTokenId> token = ts.token();

                // when it's not a value -> do nothing.
                while (token != null && token.id() != PTokenId.RBRACE) {
                    ts.moveNext();
                    token = ts.token();
                }
                if (token != null) {
                    final List<WS> ws = new ArrayList<>();
                    int maxWsLine = 0;
                    WS max = null;
                    while (token != null && token.id() != PTokenId.LBRACE) {
                        if (token.id() == PTokenId.PARAM_ASSIGN) {
                            ts.movePrevious();
                            token = ts.token();
                            try {
                                if (token != null && token.id() == PTokenId.WHITESPACE) {
                                    int len = token.text().length();
                                    int off = ts.offset();
                                    int rowStart = Utilities.getRowStart((BaseDocument)document, off);
                                    final WS ws1 = new WS(ts.offset(), len, rowStart);
                                    if (maxWsLine < off - rowStart) {
                                        maxWsLine = off - rowStart;
                                        max = ws1;
                                    }
                                    ws.add(ws1);
                                }
                            } catch (BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }

                        }
                        ts.movePrevious();
                        token = ts.token();
                    }
                    final WS fMax = max;
                    toReplace.add(new DocumentChange() {
                        @Override
                        public void run() throws Exception {
                            int lineWhitespaceEnd = (fMax.offset - fMax.rowStart) + 1;
                            for (WS w : ws) {
                                int wsEnd = (w.offset - w.rowStart) + w.len;
                                if (wsEnd > lineWhitespaceEnd) {
                                    document.remove(w.offset, wsEnd - lineWhitespaceEnd);
                                } else if (wsEnd < lineWhitespaceEnd) {
                                    document.insertString(w.offset, new String(new char[lineWhitespaceEnd - wsEnd]).replace('\0', ' '), null);
                                }
                            }
                        }
                    });
                }
            }
        });
        Collections.reverse(toReplace);
        for (DocumentChange change : toReplace) {
            change.run();
        }
        return new ChangeInfo();
    }

    @Override
    protected DocumentChange changeForToken(Token<PTokenId> token, TokenSequence<PTokenId> ts) {
        throw new UnsupportedOperationException("Not supported");
    }

    private class WS {
        final int offset;
        final int len;
        final int rowStart;

        public WS(int offset, int len, int rowStart) {
            this.offset = offset;
            this.len = len;
            this.rowStart = rowStart;
        }

    }

}
