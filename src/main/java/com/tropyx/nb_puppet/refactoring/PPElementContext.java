
package com.tropyx.nb_puppet.refactoring;

import com.tropyx.nb_puppet.parser.PElement;
import javax.swing.text.Document;
import org.openide.text.CloneableEditorSupport;

public class PPElementContext {
    private final int caretOffset;
    private final PElement rootNode;
    private final Document document;
    private final CloneableEditorSupport editorSupport;

    PPElementContext(CloneableEditorSupport editorSupport, Document doc, PElement rootNode, int caretOffset, int selectionStart, int selectionEnd) {
        this.rootNode = rootNode;
        this.caretOffset = caretOffset;
        this.document = doc;
        this.editorSupport = editorSupport;
    }

    public int getCaretOffset() {
        return caretOffset;
    }

    public PElement getRootNode() {
        return rootNode;
    }

    public PElement getCaretNode() {
        return rootNode.getChildAtOffset(caretOffset);
    }

    boolean isRefactoringAllowed() {
        return true;
    }

    Document getDocument() {
        return document;
    }

    public CloneableEditorSupport getEditorSupport() {
        return editorSupport;
    }

}
