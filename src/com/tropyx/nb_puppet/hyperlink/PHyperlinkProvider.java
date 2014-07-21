/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tropyx.nb_puppet.hyperlink;

import com.tropyx.nb_puppet.lexer.PLanguageProvider;
import com.tropyx.nb_puppet.lexer.PTokenId;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
@MimeRegistration(mimeType=PLanguageProvider.MIME_TYPE, service=HyperlinkProviderExt.class)
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
            return new int[] {
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
                //attributes
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
                }
            }
        });
        if (fAssociatedID[0] != null) {
            return new Tuple(fValue[0], fAssociatedID[0], fTokenOff[0]);
        }
        return null;
        
    }

    private void performJump(Tuple tup, Document doc) {
        String[] splitValue = tup.value.split("\\:\\:");
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
            DataObject dObject = NbEditorUtilities.getDataObject(doc);
            if (dObject != null) {
                FileObject fo = dObject.getPrimaryFile();
                FileObject res = findFile(fo, module, file);
                        if (res != null) {
                            try {
                                DataObject dobj = DataObject.find(res);
                                EditCookie ec = dobj.getLookup().lookup(EditCookie.class);
                                if (ec != null) {
                                    ec.edit();
                                    return;
                                }
                                OpenCookie oc = dobj.getLookup().lookup(OpenCookie.class);
                                if (oc != null) {
                                    oc.open();
                                    return;
                                }
                            } catch (DataObjectNotFoundException ex) {
                                Exceptions.printStackTrace(ex);
                            }

                        }
                    }
        }
    }
    
    private FileObject findFile(FileObject fo, String module, String file) {
        FileObject manifests = fo.getParent();
        String path = module + "/manifests/" + file;
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
