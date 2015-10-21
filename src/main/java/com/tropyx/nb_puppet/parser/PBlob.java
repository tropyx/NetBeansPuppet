
package com.tropyx.nb_puppet.parser;

public class PBlob extends PElement {

    private int endOffset;

    public PBlob(PElement parent, int offset) {
        super(BLOB, parent, offset);
    }

    @Override
    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int length) {
        this.endOffset = length;
    }

    
}
