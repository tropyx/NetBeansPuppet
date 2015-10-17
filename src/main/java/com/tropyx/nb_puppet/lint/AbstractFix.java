
package com.tropyx.nb_puppet.lint;

import com.tropyx.nb_puppet.lexer.PLangHierarchy;
import com.tropyx.nb_puppet.lexer.PTokenId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;

public abstract class AbstractFix implements Fix {
    protected final Document document;
    protected final int endindex;
    protected final int startindex;
    private final String text;

    public AbstractFix(Document document, int startindex, int endindex, String text) {
        this.document = document;
        this.endindex = endindex;
        this.startindex = startindex;
        this.text = text;
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.getText());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return getClass() == obj.getClass();
    }

    @Override
    public String getText() {
        return text;
    }

    protected abstract DocumentChange changeForToken(Token<PTokenId> token, TokenSequence<PTokenId> ts);

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
                while (token != null && ts.offset() <= endindex) {
                    DocumentChange ch = changeForToken(token, ts);
                    if (ch != null) {
                        toReplace.add(ch);
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



    protected interface DocumentChange {
        public void run() throws Exception;
    }

}
