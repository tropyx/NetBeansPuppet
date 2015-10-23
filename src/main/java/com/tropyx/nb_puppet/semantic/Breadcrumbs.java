
package com.tropyx.nb_puppet.semantic;

import com.tropyx.nb_puppet.PPConstants;
import com.tropyx.nb_puppet.completion.PPFunctionCompletionItem;
import com.tropyx.nb_puppet.completion.PPResourceCompletionItem;
import com.tropyx.nb_puppet.hyperlink.PHyperlinkProvider;
import com.tropyx.nb_puppet.lexer.PLanguageProvider;
import com.tropyx.nb_puppet.parser.PClass;
import com.tropyx.nb_puppet.parser.PCondition;
import com.tropyx.nb_puppet.parser.PDefine;
import com.tropyx.nb_puppet.parser.PElement;
import com.tropyx.nb_puppet.parser.PFunction;
import com.tropyx.nb_puppet.parser.PResource;
import com.tropyx.nb_puppet.parser.PuppetParserResult;
import java.awt.Image;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsController;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsElement;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class Breadcrumbs extends ParserResultTask<PuppetParserResult> {

    @Override
    public void run(PuppetParserResult result, SchedulerEvent event) {
        final Document doc = result.getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return;
        }
        if (!BreadcrumbsController.areBreadCrumsEnabled(doc)) {
            return;
        }
        int caret;
        if (event instanceof CursorMovedSchedulerEvent) {
            caret = ((CursorMovedSchedulerEvent) event).getCaretOffset();
        } else {
            //WTF here..
            JTextComponent c = EditorRegistry.focusedComponent();
            if (c != null && c.getDocument() == doc) {
                caret = c.getCaretPosition();
            } else {
                return;
            }
        }
        PElement root = result.getRootNode();
        PElement child = root.getChildAtOffset(caret);
        child = !isSuitable(child) ? computeSuitableParent(child) : child;
        BreadcrumbsElement el = new Element((BaseDocument)doc, child, Collections.<BreadcrumbsElement>emptyList());
        BreadcrumbsController.setBreadcrumbs(doc, el);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return BreadcrumbsController.BREADCRUMBS_SCHEDULER;
    }

    @Override
    public void cancel() {

    }

    private static boolean isSuitable(PElement child) {
        switch (child.getType()) {
            case PElement.CASE:
            case PElement.CLASS:
            case PElement.CONDITION:
            case PElement.DEFINE:
            case PElement.NODE:
            case PElement.RESOURCE:
            case PElement.FUNCTION:
                return true;
            default:
                return false;
        }
    }

    private static PElement computeSuitableParent(PElement curr) {
        PElement parent = curr.getParent();
        while (parent != null) {
            if (isSuitable(parent)) {
                return parent;
            } else {
                parent = parent.getParent();
            }
        }
        return null;
    }



    @MimeRegistration(mimeType = PPConstants.MIME_TYPE, service = TaskFactory.class)
    public static class Factory extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singleton(new Breadcrumbs());
        }
    }

    private static class Element implements BreadcrumbsElement, Openable {
        private final BreadcrumbsElement parent;
        private final List<BreadcrumbsElement> children;
        private final String name;
        private final Image icon;
        private final int offset;
        private final BaseDocument doc;

        public Element(BaseDocument doc, PElement current, List<BreadcrumbsElement> children) {
            this.children = children;
            this.doc = doc;
            this.icon = computeIcon(current);
            this.name = computeName(current);
            if (current != null) {
                this.offset = current.getOffset();
                PElement suitableParent = computeSuitableParent(current);
                this.parent = new Element(doc, suitableParent, Collections.<BreadcrumbsElement>singletonList(this));
            } else {
                this.parent = null;
                this.offset = 0;
            }
        }

        @Override
        public String getHtmlDisplayName() {
            return name;
        }

        @Override
        public Image getIcon(int type) {
            return icon;
        }
        
        private Image computeIcon(PElement current) {
            if (current == null) {
                return BreadcrumbsController.NO_ICON;
            }
            switch (current.getType()) {
                case PElement.RESOURCE:
                    return ImageUtilities.loadImage(PPConstants.RESOURCE_ICON);
                case PElement.FUNCTION:
                    return ImageUtilities.loadImage(PPConstants.FUNCTION_ICON);
                default:
                    return BreadcrumbsController.NO_ICON;
            }

        }

        @Override
        public Image getOpenedIcon(int type) {
            return icon;
        }

        @Override
        public List<BreadcrumbsElement> getChildren() {
            return children;
        }

        @Override
        public Lookup getLookup() {
            return Lookups.singleton(this);
        }

        @Override
        public void open() {
            PHyperlinkProvider.showAtOffset(doc, offset);
        }

        @Override
        public BreadcrumbsElement getParent() {
            return parent;
        }

        private String computeName(PElement current) {
            if (current == null) {
                return "";
            }
            switch (current.getType()) {
                case PElement.CASE:
                    return "case";
                case PElement.CLASS:
                    return ((PClass)current).getName();
                case PElement.CONDITION:
                    PCondition c = ((PCondition)current);
                    return "if";
                case PElement.DEFINE:
                    return ((PDefine)current).getName();
                case PElement.NODE:
                    return "node";
                case PElement.RESOURCE:
                    PResource r = (PResource)current;
                    return r.getResourceType();
                case PElement.FUNCTION:
                    PFunction f = (PFunction)current;
                    return f.getName();
                default:
                    return "";
            }
        }


    }


}
