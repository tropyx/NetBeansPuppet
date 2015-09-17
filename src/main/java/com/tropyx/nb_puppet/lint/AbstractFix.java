
package com.tropyx.nb_puppet.lint;

import java.util.Objects;
import javax.swing.text.Document;
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

}
