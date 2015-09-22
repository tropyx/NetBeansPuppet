
package com.tropyx.nb_puppet.parser;

public class PBlob extends PElement {

    private int length;

    public PBlob(PElement parent, int offset) {
        super(BLOB, parent, offset);
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    
}
