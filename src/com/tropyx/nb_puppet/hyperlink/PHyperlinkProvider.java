
package com.tropyx.nb_puppet.hyperlink;

import com.tropyx.nb_puppet.PuppetProject;
import com.tropyx.nb_puppet.lexer.PLanguageProvider;
import com.tropyx.nb_puppet.lexer.PTokenId;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.Pair;

/**
 *
 * @author mkleint
 */
@MimeRegistration(mimeType = PLanguageProvider.MIME_TYPE, service = HyperlinkProviderExt.class)
public class PHyperlinkProvider implements HyperlinkProviderExt {

    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return Collections.singleton(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public boolean isHyperlinkPoint(final Document doc, final int offset, HyperlinkType type) {
        Tuple tup = getTuple(doc, offset);
        if (tup != null) {
            return true;
        }
        return false;
    }

    @Override
    public int[] getHyperlinkSpan(final Document doc, final int offset, HyperlinkType type) {
        Tuple tup = getTuple(doc, offset);
        if (tup != null) {
            if (tup.stringSpan != null) {
                return new int[] {
                    tup.stringSpan.spanStart, tup.stringSpan.spanEnd
                };
            }
            return new int[]{
                tup.tokenOffset, tup.tokenOffset + tup.value.length()
            };
        }
        return new int[0];
    }

    @Override
    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        Tuple tup = getTuple(doc, offset);
        if (tup != null) {
            performJump(tup, doc);
        }
    }

    @Override
    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        Tuple tup = getTuple(doc, offset);
        if (tup != null) {
            if (tup.associatedId.equals(PTokenId.VARIABLE) || tup.stringSpan != null) {
                Pair<String, String> v = getPathAndVariable(tup.stringSpan != null ? tup.stringSpan.value : tup.value);
                DataObject dObject = NbEditorUtilities.getDataObject(doc);
                if (dObject != null && v != null) {
                    FileObject fo = dObject.getPrimaryFile();
                    FileObject res = findFile(fo, v.first());
                    if (res != null) {
                        try {
                            DataObject dobj = DataObject.find(res);
                            String value = guessVariableValue(dobj, v.second());
                            if (value != null) {
                                return value;
                            }
                        } catch (DataObjectNotFoundException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
            return "Jump to definition";
        }
        return null;
    }

    Tuple getTuple(final Document doc, final int offset) {
        final String[] fValue = new String[1];
        final StringSpan[] fSpan = new StringSpan[1];
        final int[] fTokenOff = new int[1];
        final PTokenId[] fAssociatedID = new PTokenId[1];
        doc.render(new Runnable() {

            @Override
            public void run() {
                TokenHierarchy th = TokenHierarchy.get(doc);
                TokenSequence<PTokenId> xml = th.tokenSequence();
                xml.move(offset);
                xml.moveNext();
                Token<PTokenId> token = xml.token();
                // when it's not a value -> do nothing.
                if (token == null) {

                    return;
                }
                if (token.id() == PTokenId.IDENTIFIER) {
                    fTokenOff[0] = xml.offset();
                    fValue[0] = token.text().toString();
                    boolean allowOneIdentifier = false;
                    boolean doOneMore;
                    boolean dolink = true;
                    do {
                        doOneMore = false;
                        xml.movePrevious();
                        token = xml.token();
                        if (token.id() == PTokenId.WHITESPACE) {
                            doOneMore = true;
                        }
                        else if (token.id() == PTokenId.COMMA) {
                            allowOneIdentifier = true;
                            doOneMore = true;
                        }
                        else if (allowOneIdentifier && token.id() == PTokenId.IDENTIFIER) {
                            allowOneIdentifier = false;
                            doOneMore = true;
                        }
                        else if (token.id() == PTokenId.REQUIRE) {
                            fAssociatedID[0] = token.id();
                        }
                        else if (token.id() == PTokenId.INCLUDE) {
                            fAssociatedID[0] = token.id();
                        }
                        else if (token.id() == PTokenId.INHERITS) {
                            fAssociatedID[0] = token.id();
                        }
                        else if (token.id() == PTokenId.DEFINE || token.id() == PTokenId.CLASS) {
                            dolink = false;
                        }
                    //whitespace is clear, command and identifier are here for this case..
                        // require groovy::config, groovy::install
                    } while (doOneMore);
                    if (dolink && fAssociatedID[0] == null && fValue[0].contains("::")) {
                        fAssociatedID[0] = PTokenId.IDENTIFIER;
                    }
                } else if (token.id() == PTokenId.STRING_LITERAL) {
                    fTokenOff[0] = xml.offset();
                    fValue[0] = token.text().toString();
                    if (fValue[0].startsWith("\"")) {
                        StringSpan span = findProperty(fValue[0], fTokenOff[0], offset);
                        if (span != null) {
                            fSpan[0] = span; 
                            fAssociatedID[0] = xml.token().id();
                        }
                    }
                    if (matchChains(createStringChains(), xml, true)) {
                        fAssociatedID[0] = xml.token().id();
                        if (token.id() == PTokenId.IDENTIFIER && !xml.token().text().toString().equals("Class")) {
                            fAssociatedID[0] = null;
                        }
                    }

                } else if (token.id() == PTokenId.VARIABLE) {
                    fTokenOff[0] = xml.offset();
                    fValue[0] = token.text().toString();
                    if (fValue[0].contains("::") 
                            && !fValue[0].startsWith("$::")) {
                        fAssociatedID[0] = token.id();
                    }
                }
            }
        });
        if (fAssociatedID[0] != null) {
            return new Tuple(fValue[0], fAssociatedID[0], fTokenOff[0], fSpan[0]);
        }
        return null;

    }

    private boolean matchChains(DecisionNode node, TokenSequence<PTokenId> xml, boolean moveBack) {
        return matchChainsRes(node, xml, moveBack) != null;
    }
    private Token<PTokenId> matchChainsRes(DecisionNode node, TokenSequence<PTokenId> xml, boolean moveBack) {
        boolean doOneMore;
        do {
            doOneMore = false;
            boolean didMove = moveBack ? xml.movePrevious() : xml.moveNext();
            if (!didMove) {
                return null;
            }
            Token<PTokenId> token = xml.token();
            if (token.id() == PTokenId.WHITESPACE) {
                doOneMore = true;
            } else {
                DecisionNode currN = node;
                for (DecisionNode ch : currN.children) {
                    if (ch.id.equals(token.id())) {
                        if (ch.children.length == 0) {
                            //we are at the end.
                            return token;
                        } else {
                            node = ch;
                            doOneMore = true;
                        }
                    }
                }
                if (currN.ignores(token.id())) {
                    doOneMore = true;
                }
            } 
            // template ('sss/ss.rbm')
        } while (doOneMore);
        return null;
    }
    
    private DecisionNode createStringChains() {
        return of(PTokenId.STRING_LITERAL,
                    of(PTokenId.LPAREN,
                            of(PTokenId.TEMPLATE)
                       ),
                    of(PTokenId.LBRACKET,
                            of(PTokenId.IDENTIFIER)
                    )
        );
    }

    private void performJump(Tuple tup, Document doc) {
        String path = tup.value;
        if (path.startsWith("'")) {
            path = path.substring(1);
        }
        if (path.endsWith("'")) {
            path = path.substring(0, path.length() - 1);
        }
        String variableName = null;
        if (tup.associatedId == PTokenId.TEMPLATE) {
            path = path.replaceFirst("\\/", "/templates/");
//            path = path.replace("'", "");
        } else if (tup.associatedId == PTokenId.VARIABLE || tup.stringSpan != null) {
            //substring removes $
            Pair<String, String> pair = getPathAndVariable(tup.stringSpan != null ? tup.stringSpan.value : path);
            if (pair != null) {
                path = pair.first();
                variableName = pair.second();
            }
        } else {
            String[] splitValue = path.split("\\:\\:");
            if (splitValue.length > 0) {
                String module = splitValue[0];
                String file;
                if (splitValue.length > 1) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < splitValue.length - 1; i++) {
                        sb.append(splitValue[i]).append("/");
                    }
                    file = sb.append(splitValue[splitValue.length - 1]).append(".pp").toString();
                } else {
                    file = "init.pp";
                }
                path = module + "/manifests/" + file;
            }
        }
        if (path != null) {
            DataObject dObject = NbEditorUtilities.getDataObject(doc);
            if (dObject != null) {
                FileObject fo = dObject.getPrimaryFile();
                FileObject res = findFile(fo, path);
                if (res != null) {
                    try {
                        DataObject dobj = DataObject.find(res);
                        boolean opened = openDataObject(dobj);
                        if (opened && variableName != null) {
                            cursorToVariableDefinition(dobj, variableName);
                        }
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                }
            }
        }
    }

    private void cursorToVariableDefinition(DataObject dobj, String variableName) throws IndexOutOfBoundsException {
        EditorCookie editc = dobj.getLookup().lookup(EditorCookie.class);
        final int[] foffset = new int[1];
        final String[] fInherit = new String[1];
        foffset[0] = -1;
        BaseDocument bd = null;
        try {
            final StyledDocument targetdoc = editc.openDocument();
            bd = (BaseDocument) targetdoc;
            final String fVariableName = variableName;
            targetdoc.render(new Runnable() {
                
                @Override
                public void run() {
                    TokenHierarchy th = TokenHierarchy.get(targetdoc);
                    TokenSequence<PTokenId> xml = th.tokenSequence();
                    xml.move(0);
                    while (xml.moveNext()) {
                        Token<PTokenId> token = xml.token();
                        // when it's not a value -> do nothing.
                        if (token == null) {
                            return;
                        }
                        if (fInherit[0] == null && token.id() == PTokenId.CLASS) {
                            Token<PTokenId> tk = matchChainsRes(of(PTokenId.CLASS, of(PTokenId.IDENTIFIER, of(PTokenId.INHERITS, of(PTokenId.IDENTIFIER)))), xml, false);
                            if (tk != null) {
                                fInherit[0] = tk.text().toString();
                            }
                        }
                        if (token.id() == PTokenId.VARIABLE) {
                            if (fVariableName.equals(token.text().toString())) {
                                foffset[0] = token.offset(th);
                            }
                        }
                    }
                }
            });
            if (foffset[0] != -1) {
                int line = Utilities.getLineOffset(bd, foffset[0]);
                int row = Utilities.getRowIndent(bd, foffset[0]);
                LineCookie lc = dobj.getLookup().lookup(LineCookie.class);
                lc.getLineSet().getOriginal(line).show(Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FOCUS, row);
            } else {
                if (fInherit[0] != null) {
                    performJump(new Tuple("$" + fInherit[0] + "::" + variableName.replace("$", ""), PTokenId.VARIABLE, -1, null), bd);
                }
            }
        } catch (IOException | BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private boolean openDataObject(DataObject dobj) {
        EditCookie ec = dobj.getLookup().lookup(EditCookie.class);
        boolean opened = false;
        if (ec != null) {
            ec.edit();
            opened = true;
        } else {
            OpenCookie oc = dobj.getLookup().lookup(OpenCookie.class);
            if (oc != null) {
                oc.open();
                opened = true;
            }
        }
        return opened;
    }

    private FileObject findFile(FileObject fo, String path) {
        Project prj = FileOwnerQuery.getOwner(fo);
        if (prj == null) {
            return null; //just assume we always have a project now.
        }
        PuppetProject pp = prj.getLookup().lookup(PuppetProject.class);
        if (pp == null) {
            return null; //and it should be a puppet project
        }

        FileObject entireSearchSpace = null;
        if (pp.isModule()) {
            //in modules/xxx/manifests
            FileObject modulesParentDir = prj.getProjectDirectory().getParent();
            if (modulesParentDir != null) {
                FileObject res = modulesParentDir.getFileObject(path);
                if (res != null) {
                    return res;
                }
                entireSearchSpace = modulesParentDir.getParent();
            }
        } else {
            //in manifests/site.pp related dir
            entireSearchSpace = prj.getProjectDirectory();
        }
        if (entireSearchSpace != null) {
            //now lets try a different
            Enumeration<? extends FileObject> en = entireSearchSpace.getFolders(false);
            while (en.hasMoreElements()) {
                FileObject candidate = en.nextElement();
                FileObject res = candidate.getFileObject(path);
                if (res != null) {
                    return res;
                }
            }
        }
        //if the complex setup failed, we might be in individual module only
        // but the project root folder is named differently and doesn't match
        // IMPORTANT: needs to be the last check because we strip information here
        // and should be really desperate, there's a chance in mismatch
        if (pp.isModule()) {
            String shorterPath = path.substring(path.indexOf("/"));
            return prj.getProjectDirectory().getFileObject(shorterPath);
        }
        return null;
    }
    
    /**
     * 
     * @param path first is path, second variable name
     * @return 
     */
    Pair<String, String> getPathAndVariable(String path) {
        path = path.replace("$", "").replace("{", "").replace("}", "");
        String[] splitValue = path.split("\\:\\:");
        if (splitValue.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < splitValue.length - 1; i++) {
                sb.append(splitValue[i]);
                if (i == 0) {
                    sb.append("/manifests/");
                    if (splitValue.length == 2) {
                        sb.append("init.pp");
                    }
                } else if (i == splitValue.length - 2) {
                    sb.append(".pp");
                } else {
                    sb.append("/");
                }
            }
            String variableName = "$" + splitValue[splitValue.length - 1];
            if (variableName.endsWith(":")) {
                //$java::params::all_versions:
                variableName = variableName.substring(0, variableName.length() - 1);
            }
            path = sb.toString();
            return Pair.of(path, variableName);
        }
        return null;
    }
    
    private String guessVariableValue(DataObject dobj, String variableName) throws IndexOutOfBoundsException {
        EditorCookie editc = dobj.getLookup().lookup(EditorCookie.class);
        try {
            final StyledDocument targetdoc = editc.openDocument();
            final String fVariableName = variableName;
            final String[] fValue = new String[1]; 
            targetdoc.render(new Runnable() {
                
                @Override
                public void run() {
                    TokenHierarchy th = TokenHierarchy.get(targetdoc);
                    TokenSequence<PTokenId> xml = th.tokenSequence();
                    xml.moveStart();
                    while (xml.moveNext()) {
                        Token<PTokenId> token = xml.token();
                        // when it's not a value -> do nothing.
                        if (token == null) {
                            return;
                        }
                        if (token.id() == PTokenId.VARIABLE) {
                            int startOffset = token.offset(th);
                            if (fVariableName.equals(token.text().toString())) {
                                if (matchChains(getVariableValueChain(), xml, false)) {
                                    try {
                                        int len = xml.offset() - startOffset + xml.token().length();
                                        fValue[0] = targetdoc.getText(startOffset, len);
                                    } catch (BadLocationException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            }
                        }
                    }
                }

            });
            return fValue[0];
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    private DecisionNode getVariableValueChain() {
        return of(PTokenId.VARIABLE, 
                    of(PTokenId.EQUALS, 
                        of(PTokenId.STRING_LITERAL),
                        of(PTokenId.VARIABLE),
                        of(PTokenId.LBRACKET, 
                            of(PTokenId.RBRACKET)
                        ).ignoreAll(),
                        of(PTokenId.EXTLOOKUP, 
                            of(PTokenId.RPAREN)
                        ).ignoreAll()
                    )
                );
    }
    
    DecisionNode of(PTokenId id, DecisionNode... children) {
        return new DecisionNode(id, children);
    }
    

    private static class DecisionNode {
        final DecisionNode[] children;
        final PTokenId id;
        private List<String> ignores = Collections.emptyList();
        boolean ignoreAll = false;

        public DecisionNode(PTokenId id, DecisionNode[] children) {
            this.children = children;
            this.id = id;
        }
        
        public boolean ignores(PTokenId id) {
            for (DecisionNode ch : children) {
                if (ch.id.equals(id)) {
                    return false;
                }
            }
            return ignoreAll || ignores.contains(id.toString());
        } 
        
        public DecisionNode ignore(String... ids) {
            ignores = Arrays.asList(ids);
            return this;
        }
        
        public DecisionNode ignoreAll() {
            ignoreAll = true;
            return this;
        }

        @Override
        public String toString() {
            return "DecisionNode{" + "id=" + id + '}';
        }
        
    }

    private final class Tuple {
        final String value;
        final int tokenOffset;
        final PTokenId associatedId;
        final StringSpan stringSpan;


        private Tuple(String value, PTokenId pTokenId, int offset, StringSpan stringSpan)
        {
            this.value = value;
            this.associatedId = pTokenId;
            this.tokenOffset = offset;
            this.stringSpan = stringSpan;
        }

    }
    
    private static class StringSpan {
        final int spanStart;
        final int spanEnd;
        final String value;
        public StringSpan(String val, int start, int end) {
            this.value = val;
            this.spanStart = start;
            this.spanEnd = end; 
        }
    } 
    
    
    private StringSpan findProperty(String textToken, int tokenOffset, int currentOffset) {
        if (textToken == null) {
            return null;
        }
        int ff = currentOffset - tokenOffset;

        if (ff > -1 && ff < textToken.length()) {
            String before = textToken.substring(0, ff);
            String after = textToken.substring(ff, textToken.length());
            int bo = before.lastIndexOf("${");
            int bc = before.lastIndexOf("}");
            int ao = after.indexOf("${");
            int ac = after.indexOf("}");
            if (bo > bc && ac > -1 && (ac < ao || ao == -1)) { //case where currentOffset is on property
                return new StringSpan(textToken.substring(bo, before.length() + ac + 1), tokenOffset + bo, tokenOffset + ff + ac + 1);
            }
         
            if (before.length() == 0 && ao == 0 && ac > 0) { //case where currentOffset is at beginning
                return new StringSpan(textToken.substring(0, ac + 1), tokenOffset, tokenOffset +  ac + 1);
            }
            
        }
        return null;
    }
    
}
