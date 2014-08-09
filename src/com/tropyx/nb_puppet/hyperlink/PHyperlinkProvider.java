/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tropyx.nb_puppet.hyperlink;

import com.tropyx.nb_puppet.lexer.PLanguageProvider;
import com.tropyx.nb_puppet.lexer.PTokenId;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
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
            return "Jump to definition";
        }
        return null;
    }

    Tuple getTuple(final Document doc, final int offset) {
        final String[] fValue = new String[1];
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
                    //whitespace is clear, command and identifier are here for this case..
                        // require groovy::config, groovy::install
                    } while (doOneMore);
                } else if (token.id() == PTokenId.STRING_LITERAL) {
                    fTokenOff[0] = xml.offset();
                    fValue[0] = token.text().toString();
                    if (matchChains(createStringChains(), xml)) {
                        fAssociatedID[0] = xml.token().id();
                        if (token.id() == PTokenId.IDENTIFIER && !xml.token().text().toString().equals("Class")) {
                            fAssociatedID[0] = null;
                        }
                    }

                } else if (token.id() == PTokenId.VARIABLE) {
                    fTokenOff[0] = xml.offset();
                    fValue[0] = token.text().toString();
                    if (fValue[0].indexOf("::") != fValue[0].lastIndexOf("::") //heuristics, want XXX::YYY::variable name only 
                            && !fValue[0].startsWith("$::")) {
                        fAssociatedID[0] = token.id();
                    }
                }
            }
        });
        if (fAssociatedID[0] != null) {
            return new Tuple(fValue[0], fAssociatedID[0], fTokenOff[0]);
        }
        return null;

    }

    private boolean matchChains(DecisionNode node, TokenSequence<PTokenId> xml) {

        boolean doOneMore;
        
        do {
            doOneMore = false;
            xml.movePrevious();
            Token<PTokenId> token = xml.token();
            if (token.id() == PTokenId.WHITESPACE) {
                doOneMore = true;
            } else {
                for (DecisionNode ch : node.children) {
                    if (ch.id.equals(token.id())) {
                        if (ch.children.length == 0) {
                            //we are at the end.
                            return true;
                        } else {
                            node = ch;
                            doOneMore = true;
                        }
                    }
                }
            } 
            // template ('sss/ss.rbm')
        } while (doOneMore);
        return false;
    }
    
    private DecisionNode createStringChains() {
        return new DecisionNode(PTokenId.STRING_LITERAL,
                new DecisionNode[]{
                    new DecisionNode(PTokenId.LPAREN,
                            new DecisionNode[]{
                                new DecisionNode(PTokenId.TEMPLATE, new DecisionNode[0])
                            }),
                    new DecisionNode(PTokenId.LBRACKET,
                            new DecisionNode[]{
                                new DecisionNode(PTokenId.IDENTIFIER, new DecisionNode[0])
                            })
                }
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
        } else if (tup.associatedId == PTokenId.VARIABLE) {
            //substring removes $
            String[] splitValue = path.substring(1).split("\\:\\:");
            if (splitValue.length > 2) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < splitValue.length - 1; i++) {
                    sb.append(splitValue[i]);
                    if (i == 0) {
                        sb.append("/manifests/");
                    } else if (i == splitValue.length - 2) {
                        sb.append(".pp");
                    } else {
                        sb.append("/");
                    }
                }
                variableName = "$" + splitValue[splitValue.length -1];
                path = sb.toString();
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
                        if (opened && variableName != null) {
                            EditorCookie editc = dobj.getLookup().lookup(EditorCookie.class);
                            final int[] foffset = new int[1];
                            foffset[0] = -1;
                            BaseDocument bd = null;
                            try {
                                final StyledDocument targetdoc = editc.openDocument();
                                bd = (BaseDocument) targetdoc;
                                final String fVariableName = variableName;
                                doc.render(new Runnable() {

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
                                }
                            } catch (IOException | BadLocationException ex) {
                                Exceptions.printStackTrace(ex);
                            }

                        }
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                }
            }
        }
    }

    private FileObject findFile(FileObject fo, String path) {
        FileObject manifests = fo.getParent();
        if ("nodes".equals(manifests.getName()) && manifests.getParent() != null) {
            manifests = manifests.getParent();
        }
        if ("manifests".equals(manifests.getName()) && manifests.getParent() != null) {
            FileObject entireSearchSpace = null;
            if (manifests.getFileObject("site.pp") == null) {
                //in modules/xxx/manifests
                FileObject moduleDir = manifests.getParent();
                if (moduleDir.getParent() != null) {
                    FileObject modulesParentDir = moduleDir.getParent();
                    FileObject res = modulesParentDir.getFileObject(path);
                    if (res != null) {
                        return res;
                    }
                    entireSearchSpace = modulesParentDir.getParent();
                }
            } else {
                //in manifests/site.pp related dir
                entireSearchSpace = manifests.getParent();
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
        }
        return null;
    }

    private static class DecisionNode {
        final DecisionNode[] children;
        final PTokenId id;

        public DecisionNode(PTokenId id, DecisionNode[] children) {
            this.children = children;
            this.id = id;
        }
    }

    private final class Tuple {
        final String value;
        final int tokenOffset;
        final PTokenId associatedId;


        private Tuple(String value, PTokenId pTokenId, int offset)
        {
            this.value = value;
            this.associatedId = pTokenId;
            this.tokenOffset = offset;
        }

    }
}
