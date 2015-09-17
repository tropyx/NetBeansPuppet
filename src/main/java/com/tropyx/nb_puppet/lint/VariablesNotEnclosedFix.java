
package com.tropyx.nb_puppet.lint;

import javax.swing.text.Document;
import org.netbeans.spi.editor.hints.ChangeInfo;

class VariablesNotEnclosedFix extends AbstractFix {

    public VariablesNotEnclosedFix(Document document, int startindex, int endindex) {
        super(document, startindex, endindex, "Variable not enclosed - surround with {}");
    }
    @Override
    public ChangeInfo implement() throws Exception {
        return new ChangeInfo();
    }

}
