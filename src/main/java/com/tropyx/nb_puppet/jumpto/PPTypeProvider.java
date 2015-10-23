
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
        Collection<FileObject> roots = getIndexerRoots(context);
        if (cancelled.get()) return;
        try {
            String text = context.getText();
            boolean prependSep = false;
            if (SearchType.CAMEL_CASE == type) {
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

    private QuerySupport.Kind searchType2Kind(SearchType type) {
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

    public Collection<FileObject> getIndexerRoots(Context context) {
        Collection<FileObject> roots;
        if (context.getProject() == null) {
            roots = GlobalPathRegistry.getDefault().getSourceRoots();
        } else {
            ClassPath cp = ClassPath.getClassPath(context.getProject().getProjectDirectory(), ClassPath.SOURCE);
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
        private final Project project;
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
            return ProjectUtils.getInformation(project).getDisplayName();
        }

        @Override
        public Icon getProjectIcon() {
            return ProjectUtils.getInformation(project).getIcon();
        }

        @Override
        public FileObject getFileObject() {
            return fileObject;
        }

        @Override
        public int getOffset() {
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
