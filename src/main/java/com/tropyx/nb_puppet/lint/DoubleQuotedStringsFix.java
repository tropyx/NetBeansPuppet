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
    protected DocumentChange changeForToken(Token<PTokenId> token, TokenSequence<PTokenId> ts) {
        if (token.id() == PTokenId.STRING_LITERAL) {
            String txt = token.text().toString();
            if (txt.startsWith("\"") && txt.endsWith("\"")) {
                final int start = ts.offset();
                final int end = start + token.length() - 1;
                return new DocumentChange() {
                    @Override
                    public void run() throws Exception {
                        document.remove(start, 1);
                        document.insertString(start, "'", null);
                        document.remove(end, 1);
                        document.insertString(end, "'", null);
                    }
                };
            }
        }
        return null;
    }
}
