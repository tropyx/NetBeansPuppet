
package com.tropyx.nb_puppet.lint;

import com.tropyx.nb_puppet.lexer.PTokenId;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;

class VariablesNotEnclosedFix extends AbstractFix {

    public VariablesNotEnclosedFix(Document document, int startindex, int endindex) {
        super(document, startindex, endindex, "Variable not enclosed - surround with {}");
    }

    @Override
    protected DocumentChange changeForToken(Token<PTokenId> token, TokenSequence<PTokenId> ts) {
        return null;
    }

}
