/*
 * Copyright (C) 2015 mkleint
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
package com.tropyx.nb_puppet.jumpto;

import com.tropyx.nb_puppet.PPConstants;
import com.tropyx.nb_puppet.hyperlink.PHyperlinkProvider;
import com.tropyx.nb_puppet.indexer.PPIndexer;
import com.tropyx.nb_puppet.indexer.PPIndexerFactory;
import com.tropyx.nb_puppet.refactoring.PPWhereUsedQueryPlugin;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.text.StyledDocument;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.netbeans.spi.jumpto.symbol.SymbolProvider;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SymbolProvider.class)
public class PPSymbolProvider implements SymbolProvider{
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    @Override
    public String name() {
        return "puppet";
    }

    @Override
    public String getDisplayName() {
        return "Puppet";
    }

    @Override
    public void computeSymbolNames(Context context, Result result) {
                cancelled.set(false);

        SearchType type = context.getSearchType();
        Collection<FileObject> roots = PPTypeProvider.getIndexerRoots(context.getProject());
        if (cancelled.get()) return;
        try {
            String text = context.getText();
            if (SearchType.CASE_INSENSITIVE_EXACT_NAME == type) {
                text = text.toLowerCase();
            }
            if (SearchType.CASE_INSENSITIVE_REGEXP == type  || SearchType.REGEXP == type) {
                text = text.replace("*", ".*").replace("?", ".?");
            }
            QuerySupport qs = QuerySupport.forRoots(PPIndexerFactory.INDEXER_TYPE, PPIndexerFactory.INDEXER_VERSION, roots.toArray(new FileObject[0]));
            if (cancelled.get()) return;
            for ( IndexResult r :qs.query(PPIndexer.FLD_VAR, text, PPTypeProvider.searchType2Kind(type), PPIndexer.FLD_ROOT, PPIndexer.FLD_VAR)) {
                if (cancelled.get()) return;
                final String root = r.getValue(PPIndexer.FLD_ROOT);
                for (String v : r.getValues(PPIndexer.FLD_VAR)) {
                    if (cancelled.get()) return;
                    if (matches(v, type, text)) {
                        result.addResult(new PPSymbolProvider.SymbolDescriptorImpl(v, root, r.getFile(), 0));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    @Override
    public void cancel() {
        cancelled.compareAndSet(false, true);
    }

    @Override
    public void cleanup() {
    }

    private boolean matches(String v, SearchType type, String text) {
        switch (type) {
            case CASE_INSENSITIVE_EXACT_NAME:
                return v.toLowerCase().equals(text.toLowerCase());
            case CASE_INSENSITIVE_PREFIX:
                return v.toLowerCase().startsWith(text.toLowerCase());
            case EXACT_NAME:
                return v.equals(text);
            case PREFIX:
                return v.startsWith(text);
            case REGEXP:
                return Pattern.matches(text, v);
            case CASE_INSENSITIVE_REGEXP:
                return Pattern.matches(text.toLowerCase(), v.toLowerCase());
            case CAMEL_CASE:
        }
        return false;
    }

    private static class SymbolDescriptorImpl extends SymbolDescriptor {
        private final String var;
        private final String root;
        private final FileObject file;
        private final int offset;
        private final @NullAllowed Project project;

        public SymbolDescriptorImpl(String var, String root, FileObject file, int offset) {
            this.var = var;
            this.root = root;
            this.file = file;
            this.offset = offset;
            this.project = FileOwnerQuery.getOwner(file);
        }

        @Override
        public Icon getIcon() {
            return ImageUtilities.loadImageIcon(PPConstants.VARIABLE_ICON, true);
        }

        @Override
        public String getSymbolName() {
            return var;
        }

        @Override
        public String getOwnerName() {
            return root;
        }

        @Override
        public String getProjectName() {
            if (project != null) {
                return ProjectUtils.getInformation(project).getDisplayName();
            }
            return null;
        }

        @Override
        public Icon getProjectIcon() {
            if (project != null) {
                return ProjectUtils.getInformation(project).getIcon();
            }
            return null;
        }

        @Override
        public FileObject getFileObject() {
            return file;
        }

        @Override
        public int getOffset() {
            //TODO
            return offset;
        }

        @Override
        public void open() {
            CloneableEditorSupport ces = PPWhereUsedQueryPlugin.getEditorSupport(file);
            if (ces != null) {
                try {
                    StyledDocument doc = ces.openDocument();
                    PHyperlinkProvider.showAtOffset((BaseDocument)doc, offset);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

}
