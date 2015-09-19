
package com.tropyx.nb_puppet.lint;

import com.tropyx.nb_puppet.lexer.PTokenId;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;

class OnlyVariableStringFix extends AbstractFix {

    public OnlyVariableStringFix(Document document, int startindex, int endindex) {
        super(document, startindex, endindex, "Only variable string - remove quotes");
    }

    @Override
    protected DocumentChange changeForToken(Token<PTokenId> token, TokenSequence<PTokenId> ts) {
        if (token.id() == PTokenId.STRING_LITERAL) {
            String txt = token.text().toString();
            if (txt.startsWith("\"${") && txt.endsWith("}\"")) {
                final int start = ts.offset();
                final int end = start + token.length() - 1;
                return new DocumentChange() {
                    @Override
                    public void run() throws Exception {
                        document.remove(end - 1, 2);
                        document.remove(start, 3);
                        document.insertString(start, "$", null);
                    }
                };
            } else if (txt.startsWith("\"$")) {
                final int start = ts.offset();
                final int end = start + token.length() - 1;
                return new DocumentChange() {
                    @Override
                    public void run() throws Exception {
                        document.remove(end, 1);
                        document.remove(start, 2);
                        document.insertString(start, "$", null);
                    }
                };
            }
        }
        return null;
    }

}
