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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Icon;
import javax.swing.text.StyledDocument;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = TypeProvider.class)
public class PPTypeProvider implements TypeProvider {

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
    public void computeTypeNames(Context context, Result result) {
        cancelled.set(false);

        SearchType type = context.getSearchType();
        Collection<FileObject> roots = getIndexerRoots(context.getProject());
        if (cancelled.get()) return;
        try {
            String text = context.getText();
            if (SearchType.CAMEL_CASE == type) {
                boolean prependSep = false;
                StringBuilder sb = new StringBuilder();
                for (char ch : text.toCharArray()) {
                    if (Character.isUpperCase(ch)) {
                        if (prependSep) {
                            sb.append("*\\:\\:");
                        } else {
                            prependSep = true;
                        }
                    }
                    sb.append(Character.toLowerCase(ch));
                }
                sb.append("*");
                text = sb.toString();
                type = SearchType.CASE_INSENSITIVE_REGEXP;
            }
            if (SearchType.CASE_INSENSITIVE_EXACT_NAME == type) {
                text = text.toLowerCase();
            }
            if (SearchType.CASE_INSENSITIVE_REGEXP == type  || SearchType.REGEXP == type) {
                text = text.replace("*", ".*").replace("?", ".?");
            }
            QuerySupport qs = QuerySupport.forRoots(PPIndexerFactory.INDEXER_TYPE, PPIndexerFactory.INDEXER_VERSION, roots.toArray(new FileObject[0]));
            if (cancelled.get()) return;
            for ( IndexResult r :qs.query(PPIndexer.FLD_ROOT, text, searchType2Kind(type), PPIndexer.FLD_ROOT)) {
                if (cancelled.get()) return;
                result.addResult(new TypeDescriptorImpl(r.getValue(PPIndexer.FLD_ROOT), r.getFile(), 0));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    static QuerySupport.Kind searchType2Kind(SearchType type) {
        switch (type) {
            case CAMEL_CASE : return QuerySupport.Kind.CAMEL_CASE;
            case PREFIX : return QuerySupport.Kind.PREFIX;
            case EXACT_NAME : return QuerySupport.Kind.EXACT;
            case REGEXP : return QuerySupport.Kind.REGEXP;
            case CASE_INSENSITIVE_EXACT_NAME : return QuerySupport.Kind.EXACT;
            case CASE_INSENSITIVE_PREFIX : return QuerySupport.Kind.CASE_INSENSITIVE_PREFIX;
            case CASE_INSENSITIVE_REGEXP : return QuerySupport.Kind.CASE_INSENSITIVE_REGEXP;
            default: return QuerySupport.Kind.EXACT;
        }
    }

    static Collection<FileObject> getIndexerRoots(Project prj) {
        Collection<FileObject> roots;
        if (prj == null) {
            roots = GlobalPathRegistry.getDefault().getSourceRoots();
        } else {
            ClassPath cp = ClassPath.getClassPath(prj.getProjectDirectory(), ClassPath.SOURCE);
            if (cp != null) {
                roots = Arrays.asList(cp.getRoots());
            } else {
                roots = Collections.emptyList();
            }
        }
        return roots;
    }

    @Override
    public void cancel() {
        cancelled.compareAndSet(false, true);
    }

    @Override
    public void cleanup() {
    }

    private static class TypeDescriptorImpl extends TypeDescriptor {
        private final String typeName;
        private final @NullAllowed Project project;
        private final FileObject fileObject;
        private final int offset;

        public TypeDescriptorImpl(String typeName, FileObject fileObject, int offset) {
            this.typeName = typeName;
            this.fileObject = fileObject;
            this.project = FileOwnerQuery.getOwner(fileObject);
            this.offset = offset;
        }

        @Override
        public String getSimpleName() {
            return typeName;
        }

        @Override
        public String getOuterName() {
            return "";
        }

        @Override
        public String getTypeName() {
            return typeName;
        }

        @Override
        public String getContextName() {
            return "";
        }

        @Override
        public Icon getIcon() {
            return ImageUtilities.loadImageIcon(PPConstants.ICON_PUPPET_FILE, true);
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
            return fileObject;
        }

        @Override
        public int getOffset() {
            //TODO
            return offset;
        }

        @Override
        public void open() {
            CloneableEditorSupport ces = PPWhereUsedQueryPlugin.getEditorSupport(fileObject);
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
