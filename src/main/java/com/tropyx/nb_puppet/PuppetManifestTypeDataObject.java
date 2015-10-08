/*
 * Copyright (C) Tropyx Technology Pty Ltd and Michael Lindner 2013
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tropyx.nb_puppet;

import java.io.IOException;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.TopComponent;

@Messages({
    "LBL_PuppetManifestType_LOADER=Files of PuppetManifestType"
})
@MIMEResolver.ExtensionRegistration(
    displayName = "#LBL_PuppetManifestType_LOADER",
mimeType = "text/x-puppet-manifest",
extension = {"pp"})
@DataObject.Registration(
    mimeType = "text/x-puppet-manifest",
iconBase = "com/tropyx/nb_puppet/resources/puppet16.png",
displayName = "#LBL_PuppetManifestType_LOADER",
position = 300)
@ActionReferences({
    @ActionReference(
        path = "Loaders/text/x-puppet-manifest/Actions",
    id =
    @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
    position = 100,
    separatorAfter = 200),
    @ActionReference(
        path = "Loaders/text/x-puppet-manifest/Actions",
    id =
    @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
    position = 300),
    @ActionReference(
        path = "Loaders/text/x-puppet-manifest/Actions",
    id =
    @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
    position = 400,
    separatorAfter = 500),
    @ActionReference(
        path = "Loaders/text/x-puppet-manifest/Actions",
    id =
    @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
    position = 600),
    @ActionReference(
        path = "Loaders/text/x-puppet-manifest/Actions",
    id =
    @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
    position = 700,
    separatorAfter = 800),
    @ActionReference(
        path = "Loaders/text/x-puppet-manifest/Actions",
    id =
    @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
    position = 900,
    separatorAfter = 1000),
    @ActionReference(
        path = "Loaders/text/x-puppet-manifest/Actions",
    id =
    @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
    position = 1100,
    separatorAfter = 1200),
    @ActionReference(
        path = "Loaders/text/x-puppet-manifest/Actions",
    id =
    @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
    position = 1300),
    @ActionReference(
        path = "Loaders/text/x-puppet-manifest/Actions",
    id =
    @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
    position = 1400)
})
public class PuppetManifestTypeDataObject extends MultiDataObject {

    public PuppetManifestTypeDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
//        registerEditor("text/x-puppet-manifest", true);
        CookieSet cookies = getCookieSet();
        cookies.add(new PPManifestDataEditor());
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @MultiViewElement.Registration(
        displayName = "#LBL_PuppetManifestType_EDITOR",
    iconBase = "com/tropyx/nb_puppet/resources/puppet16.png",
    mimeType = "text/x-puppet-manifest",
    persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
    preferredID = "PuppetManifestType",
    position = 1000)
    @Messages("LBL_PuppetManifestType_EDITOR=Source")
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }
    
    private static class AtlPlgEnv extends DataEditorSupport.Env {

        private static final long serialVersionUID = 1L;

        AtlPlgEnv(MultiDataObject d) {
            super(d);
        }

        protected @Override
        FileObject getFile() {
            return getDataObject().getPrimaryFile();
        }

        protected @Override
        FileLock takeLock() throws IOException {
            return ((MultiDataObject) getDataObject()).getPrimaryEntry().takeLock();
        }

        public @Override
        CloneableOpenSupport findCloneableOpenSupport() {
            return getDataObject().getLookup().lookup(PPManifestDataEditor.class);
        }

    }
    private class PPManifestDataEditor extends DataEditorSupport implements EditorCookie.Observable, OpenCookie, EditCookie, PrintCookie, CloseCookie {

        private String cachedTitleSuffix = null;
        private final Object TITLE_LOCK = new Object();
        private final SaveCookie save = new SaveCookie() {
            public @Override void save() throws IOException {
                saveDocument();
//                PuppetManifestTypeDataObject.this.resetDocument();
            }
            @Override public String toString() {
                return getPrimaryFile().getNameExt();
            }
        };

        @Override
        protected StyledDocument createStyledDocument(EditorKit kit) {
            return super.createStyledDocument(kit); 
        }

        private final FileChangeListener listener = new FileChangeAdapter() {
            public @Override void fileChanged(FileEvent fe) {
                synchronized (TITLE_LOCK) {
                    cachedTitleSuffix = null;
                }
                updateTitles();
            }
        };

        PPManifestDataEditor() {
            super(PuppetManifestTypeDataObject.this, null, new AtlPlgEnv(PuppetManifestTypeDataObject.this));
            getPrimaryFile().addFileChangeListener(FileUtil.weakFileChangeListener(listener, getPrimaryFile()));
        }

        @Override
        protected CloneableEditorSupport.Pane createPane() {
            return (CloneableEditorSupport.Pane) MultiViews.createCloneableMultiView("text/x-puppet-manifest", getDataObject());
        }

        protected @Override
        boolean notifyModified() {
            if (!super.notifyModified()) {
                return false;
            }
            if (getLookup().lookup(SaveCookie.class) == null) {
                getCookieSet().add(save);
                setModified(true);
            }
            return true;
        }

        protected @Override
        void notifyUnmodified() {
            super.notifyUnmodified();
            if (getLookup().lookup(SaveCookie.class) == save) {
                getCookieSet().remove(save);
                setModified(false);
            }
        }

        protected @Override
        String messageName() {
            String titleSuffix = null;
            synchronized (TITLE_LOCK) {
                if (cachedTitleSuffix == null) {
                    cachedTitleSuffix = annotateWithKey(getPrimaryFile());
                }
                titleSuffix = cachedTitleSuffix;
            }
            return super.messageName() + titleSuffix;
        }

        protected @Override
        String messageHtmlName() {
            String titleSuffix = null;
            synchronized (TITLE_LOCK) {
                if (cachedTitleSuffix == null) {
                    cachedTitleSuffix = annotateWithKey(getPrimaryFile());
                }
                titleSuffix = cachedTitleSuffix;
            }
            return super.messageHtmlName() + titleSuffix;
        }

        protected @Override
        boolean asynchronousOpen() {
            return true;
        }

    }

    static String annotateWithKey(FileObject primaryFile) {
        FileObject parent = primaryFile.getParent();
        while (parent != null && !"manifests".equals(parent.getNameExt())) {
            if (ProjectManager.getDefault().isProject(parent)) {
                break;
            }
            parent = parent.getParent();
        }
        if (parent != null && "manifests".equals(parent.getNameExt())) {
            FileObject pp = parent.getParent();
            if (pp != null) {
                return " [" + pp.getNameExt() + "]";
            }
        }
        return "";
    }

}
