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

class DoubleQuotedStringsFix extends AbstractFix {

    DoubleQuotedStringsFix(Document document, int startindex, int endindex) {
        super(document, startindex, endindex, "Double Quoted Strings - replace with single quotes");
    }


    @Override
    public ChangeInfo implement() throws Exception {
        final List<Integer> toReplace = new ArrayList<>();
        document.render(new Runnable() {
            @Override
            public void run() {
                TokenHierarchy th = TokenHierarchy.get(document);
                TokenSequence<PTokenId> ts = th.tokenSequence();
                ts.move(startindex);
                ts.moveNext();
                Token<PTokenId> token = ts.token();
                // when it's not a value -> do nothing.
                while (token != null && token.offset(th) < endindex) {
                    if (token.id() == PTokenId.STRING_LITERAL) {
                        String txt = token.text().toString();
                        if (txt.startsWith("\"") && txt.endsWith("\"")) {
                            int start = ts.offset();
                            int end = start + token.length() - 1;
                            toReplace.add(start);
                            toReplace.add(end);
                        }
                    }
                    ts.moveNext();
                    token = ts.token();
                }
            }
        });
        Collections.reverse(toReplace);
        for (int offset : toReplace) {
            document.remove(offset, 1);
            document.insertString(offset, "'", null);
        }
        return new ChangeInfo();
    }
}
