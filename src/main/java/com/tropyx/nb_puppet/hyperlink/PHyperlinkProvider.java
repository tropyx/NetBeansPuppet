
package com.tropyx.nb_puppet.hyperlink;

import com.tropyx.nb_puppet.PPConstants;
import com.tropyx.nb_puppet.PuppetProject;
import com.tropyx.nb_puppet.completion.PCompletionProvider;
import com.tropyx.nb_puppet.parser.PBlob;
import com.tropyx.nb_puppet.parser.PClass;
import com.tropyx.nb_puppet.parser.PClassRef;
import com.tropyx.nb_puppet.parser.PElement;
import com.tropyx.nb_puppet.parser.PFunction;
import com.tropyx.nb_puppet.parser.PIdentifier;
import com.tropyx.nb_puppet.parser.PString;
import com.tropyx.nb_puppet.parser.PVariable;
import com.tropyx.nb_puppet.parser.PVariableDefinition;
import com.tropyx.nb_puppet.refactoring.PPWhereUsedQueryPlugin;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.EditCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.Pair;

/**
 *
 * @author mkleint
 */
@MimeRegistration(mimeType = PPConstants.MIME_TYPE, service = HyperlinkProviderExt.class)
public class PHyperlinkProvider implements HyperlinkProviderExt {

    private static final int TYPE_VARIABLE = 1;
    private static final int TYPE_CLASS_REF = 2;
    private static final int TYPE_TEMPLATE = 3;


    @Override
    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return Collections.singleton(HyperlinkType.GO_TO_DECLARATION);
    }

    @Override
    public boolean isHyperlinkPoint(final Document doc, final int offset, HyperlinkType type) {
        Tuple tup = getTuple(doc, offset);
        return tup != null;
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
            if (tup.associatedType == TYPE_VARIABLE) {
                //TODO
            }
            return "Jump to definition";
        }
        return null;
    }

    Tuple getTuple(final Document doc, final int offset) {
        final String[] fValue = new String[1];
        final int[] fTokenOff = new int[1];
        final int[] fAssociatedType = new int[1];
        PCompletionProvider.runWithParserResult(doc, new PCompletionProvider.ParseResultRunnable() {

            @Override
            public void run(PElement rootNode) {
                if (rootNode == null) {
                    return;
                }
                PElement currentNode = rootNode.getChildAtOffset(offset);
                if (currentNode.isType(PElement.VARIABLE) || currentNode.isType(PElement.VARIABLE_DEFINITION)) {
                    fTokenOff[0] = currentNode.getOffset();
                    fValue[0] = currentNode.isType(PElement.VARIABLE) ? ((PVariable)currentNode).getName() : ((PVariableDefinition)currentNode).getName();
                    if (!fValue[0].startsWith("$::")) {
                        fAssociatedType[0] = TYPE_VARIABLE;
                    }
                } else if (currentNode.isType(PElement.IDENTIFIER)) {
                    PElement parent = currentNode.getParentIgnore(PBlob.class);
                    if (parent != null) {
                        if (parent.getType() == PElement.CLASS_REF) {
                            fTokenOff[0] = currentNode.getOffset();
                            fAssociatedType[0] = TYPE_CLASS_REF;
                            fValue[0] = ((PIdentifier)currentNode).getName();
                        }
                    }
                } else if (currentNode.isType(PElement.STRING)) {
                    PElement parent = currentNode.getParentIgnore(PBlob.class);
                    if (parent != null) {
                        if (parent.getType() == PElement.FUNCTION) {
                            PFunction f = (PFunction)parent;
                            if ("template".equals(f.getName())) {
                                fTokenOff[0] = currentNode.getOffset() + 1;
                                fAssociatedType[0] = TYPE_TEMPLATE;
                                fValue[0] = ((PString)currentNode).getValue();
                            }
                        }
                    }
                }
            }
        });

        if (fAssociatedType[0] != 0) {
            return new Tuple(fValue[0], fAssociatedType[0], fTokenOff[0]);
        }
        return null;

    }

    private void performJump(Tuple tup, Document doc) {
        String path = tup.value;
        if (path.startsWith("'")) {
            path = path.substring(1);
        }
        if (path.endsWith("'")) {
            path = path.substring(0, path.length() - 1);
        }
        
        if (tup.associatedType == TYPE_TEMPLATE) {
            path = path.replaceFirst("\\/", "/templates/");
            openDocument(doc, path, true);
        } else if (tup.associatedType == TYPE_VARIABLE) {
            //substring removes $
            Pair<String, String> pair = getPathAndVariable(path);
            if (pair != null) {
                path = pair.first();
                Document targetDoc;
                if (path == null) {
                    targetDoc = doc;
                } else {
                    //TODO open document but not editor view only once found, open view
                    targetDoc = openDocument(doc, path, false);
                }
                final String variableName = pair.second();
                if (targetDoc != null) {
                    final BaseDocument bd = (BaseDocument)targetDoc;
                    final boolean[] found = new boolean[1];
                    final String[] inherits = new String[1];
                    PCompletionProvider.runWithParserResult(targetDoc, new PCompletionProvider.ParseResultRunnable() {
                            @Override
                            public void run(PElement rootNode) {
                                for (PVariableDefinition def : rootNode.getChildrenOfType(PVariableDefinition.class, true)) {
                                    if (variableName.equals(def.getName())) {
                                        showAtOffset(bd, def.getOffset());
                                        found[0] = true;
                                        break; //first one only
                                    }
                                }
                                if (!found[0]) {
                                    //TODO open document but not editor view only once found, open view
                                    List<PClass> clazz = rootNode.getChildrenOfType(PClass.class, false);
                                    if (clazz.size() > 0) {
                                        PClassRef ref = clazz.get(0).getInherits();
                                        if (ref != null) {
                                            inherits[0] = ref.getName();
                                        }
                                    }
                                }
                            }
                    });
                    if (!found[0] && inherits[0] != null) {
                        Tuple newTup = new Tuple(inherits[0] + "::" + variableName, tup.associatedType, tup.tokenOffset);
                        performJump(newTup, doc);
                    }
                }
            }
        } else if (tup.associatedType == TYPE_CLASS_REF) {
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
                //TODO try the index first to find the right file without path translations?
                //what if we have 2 checkouts of the same module but different versions or what not
                Document targetDoc = openDocument(doc, module + "/manifests/" + file, true);
                if (targetDoc != null) {
                    final String fPath = path;
                    final BaseDocument bd = (BaseDocument)targetDoc;
                    PCompletionProvider.runWithParserResult(targetDoc, new PCompletionProvider.ParseResultRunnable() {
                            @Override
                            public void run(PElement rootNode) {
                                for (PClass clz :rootNode.getChildrenOfType(PClass.class, true)) {
                                    if (fPath.equals(clz.getName())) {
                                        PIdentifier ident = clz.getChildrenOfType(PIdentifier.class, false).get(0);
                                        // first direct identifier is the class name
                                        System.out.println("identifier offset:" + ident.getOffset());
                                        showAtOffset(bd, ident.getOffset());
                                        break;
                                    }
                                }
                            }
                    });
                }
            }
        }
    }

    public static void showAtOffset(BaseDocument bd, int offset) throws IndexOutOfBoundsException {
        try {
            int line = Utilities.getLineOffset(bd, offset);
            int row = Utilities.getRowStart(bd, offset);
            LineCookie lc = NbEditorUtilities.getDataObject(bd).getLookup().lookup(LineCookie.class);
            lc.getLineSet().getOriginal(line).show(Line.ShowOpenType.REUSE, Line.ShowVisibilityType.FOCUS, offset - row);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private Document openDocument(Document currentDoc, String path, boolean openInEditor) {
        FileObject fo = NbEditorUtilities.getFileObject(currentDoc);
        if (fo != null) {
            FileObject res = findFile(fo, path);
            if (res != null) {
                try {
                    if (openInEditor) {
                        DataObject dobj = DataObject.find(res);
                        openDataObject(dobj);
                    }
                    CloneableEditorSupport ces = PPWhereUsedQueryPlugin.getEditorSupport(res);
                    if (ces != null) {
                        try {
                            return ces.openDocument();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return null;
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
            String shorterPath = path.contains("/") ? path.substring(path.indexOf("/")) : path;
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
        return Pair.of(null, "$" + path);
    }
    
    private final class Tuple {
        final String value;
        final int tokenOffset;
        final int associatedType;

        private Tuple(String value, int type, int offset)
        {
            this.value = value;
            this.associatedType = type;
            this.tokenOffset = offset;
        }
    }
}
