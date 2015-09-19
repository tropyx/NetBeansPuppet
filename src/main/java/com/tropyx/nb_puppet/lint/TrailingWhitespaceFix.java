
package com.tropyx.nb_puppet.lint;

import com.tropyx.nb_puppet.lexer.PTokenId;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;

class TrailingWhitespaceFix extends AbstractFix {

    public TrailingWhitespaceFix(Document document, int startindex, int endindex) {
        super(document, startindex, endindex, "Trailing whitespace - remove");
    }

    @Override
    protected DocumentChange changeForToken(Token<PTokenId> token, TokenSequence<PTokenId> ts) {
        if (token.id() == PTokenId.WHITESPACE) {
            String text = token.text().toString();
            final int index = text.indexOf("\n");
            final int start = ts.offset();
            if (text.indexOf("\n") > 0) {
                return new DocumentChange() {
                    @Override
                    public void run() throws Exception {
                        document.remove(start, index);
                    }
                };
            }
        }
        return null;
    }

}
