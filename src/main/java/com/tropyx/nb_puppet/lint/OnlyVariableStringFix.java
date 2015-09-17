
package com.tropyx.nb_puppet.lint;

import com.tropyx.nb_puppet.lexer.PTokenId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.hints.ChangeInfo;

class OnlyVariableStringFix extends AbstractFix {

    public OnlyVariableStringFix(Document document, int startindex, int endindex) {
        super(document, startindex, endindex, "Only variable string - remove quotes");
    }
    @Override
    public ChangeInfo implement() throws Exception {
        final List<DocumentChange> toReplace = new ArrayList<>();
        document.render(new Runnable() {
            @Override
            public void run() {
                TokenHierarchy th = TokenHierarchy.get(document);
                TokenSequence<PTokenId> ts = th.tokenSequence();
                ts.move(startindex);
                ts.moveNext();
                Token<PTokenId> token = ts.token();
                // when it's not a value -> do nothing.
                while (token != null && token.offset(th) <= endindex) {
                    if (token.id() == PTokenId.STRING_LITERAL) {
                        String txt = token.text().toString();
                        if (txt.startsWith("\"${") && txt.endsWith("}\"")) {
                            final int start = ts.offset();
                            final int end = start + token.length() - 1;
                            toReplace.add(new DocumentChange() {
                                @Override
                                public void run() throws Exception {
                                    document.remove(end - 1, 2);
                                    document.remove(start, 3);
                                    document.insertString(start, "$", null);
                                }
                            });
                        } else if (txt.startsWith("\"$")) {
                            final int start = ts.offset();
                            final int end = start + token.length() - 1;
                            toReplace.add(new DocumentChange() {
                                @Override
                                public void run() throws Exception {
                                    document.remove(end, 1);
                                    document.remove(start, 2);
                                    document.insertString(start, "$", null);
                                }
                            });
                        }
                    }
                    ts.moveNext();
                    token = ts.token();
                }
            }
        });
        Collections.reverse(toReplace);
        for (DocumentChange change : toReplace) {
            change.run();
        }
        return new ChangeInfo();
    }

    private interface DocumentChange {
        public void run() throws Exception;
    }
}
