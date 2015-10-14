
package com.tropyx.nb_puppet.refactoring;

import com.tropyx.nb_puppet.lexer.PLanguageProvider;
import com.tropyx.nb_puppet.parser.PuppetParserResult;
import java.awt.Component;
import java.awt.EventQueue;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

@ServiceProvider(service = ActionsImplementationProvider.class)
public class PPActionsImplementationProvider extends ActionsImplementationProvider {

    private static final RequestProcessor RP = new RequestProcessor(PPActionsImplementationProvider.class);
    private static final Logger LOG = Logger.getLogger(PPActionsImplementationProvider.class.getName());

    @Override
    public void doFindUsages(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (ec != null) {
            StyledDocument doc = ec.getDocument();
            FileObject fo = NbEditorUtilities.getFileObject(doc);
            if (fo != null && PLanguageProvider.MIME_TYPE.equals(fo.getMIMEType())) {

                RP.post(new TextComponentTask(ec) {
                    //editor element context
                    @Override
                    protected RefactoringUI createRefactoringUI(PPElementContext context) {
                        return new WhereUsedUI(context);
                    }
                });
            }
        }
    }

    @Override
    public boolean canFindUsages(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        if (ec != null) {
            StyledDocument doc = ec.getDocument();
            FileObject fo = NbEditorUtilities.getFileObject(doc);
            if (fo != null && PLanguageProvider.MIME_TYPE.equals(fo.getMIMEType())) {
                return true;
            }
        }
        return false;
    }

    private static class WhereUsedUI implements RefactoringUI {

        private final CustomRefactoringPanel panel = new CustomRefactoringPanel() {

                @Override
                public void initialize() {
                }

                @Override
                public Component getComponent() {
                    return new JPanel();
                }
            };
        private final Lookup lookup;
        private final WhereUsedQuery refactoring;

        public WhereUsedUI(PPElementContext context) {
            this.lookup = Lookups.fixed(context);
            this.refactoring = new WhereUsedQuery(lookup);
        }

        @Override
        public String getName() {
            //TODO not all will be variables
            return PPWhereUsedQueryPlugin.getVariableName(lookup.lookup(PPElementContext.class));
        }

        @Override
        public String getDescription() {
            return "Usages of "  + getName();
        }

        @Override
        public boolean isQuery() {
            return true;
        }

        @Override
        public CustomRefactoringPanel getPanel(ChangeListener parent) {
            return panel;
        }

        @Override
        public Problem setParameters() {
            return refactoring.checkParameters();
        }

        @Override
        public Problem checkParameters() {
            return refactoring.fastCheckParameters();
        }

        @Override
        public boolean hasParameters() {
            return false;
        }

        @Override
        public AbstractRefactoring getRefactoring() {
            return refactoring;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }
    }

    private static abstract class TextComponentTask extends UserTask implements Runnable {

        private final Document document;
        private final int caretOffset;
        private final int selectionStart;
        private final int selectionEnd;
        private RefactoringUI ui;
        private final CloneableEditorSupport cloneableEditor;

        public TextComponentTask(EditorCookie ec) {
            JTextComponent textC = ec.getOpenedPanes()[0];
            this.document = textC.getDocument();
            this.caretOffset = textC.getCaretPosition();
            this.selectionStart = textC.getSelectionStart();
            this.selectionEnd = textC.getSelectionEnd();
            this.cloneableEditor = (CloneableEditorSupport)ec;
        }

        @Override
        public void run(ResultIterator ri) throws ParseException {
            Parser.Result pr = ri.getParserResult();
            if (pr instanceof PuppetParserResult) {
                PuppetParserResult result = (PuppetParserResult) pr;
                if (result.getRootNode() != null) {
                    //the parser result seems to be quite ok,
                    //in case of serious parse issue the parse root is null
                    PPElementContext context = new PPElementContext(cloneableEditor, document, result.getRootNode(), caretOffset, selectionStart, selectionEnd);
                    ui = context.isRefactoringAllowed() ? createRefactoringUI(context) : null;
                }
            }
        }

        //runs in RequestProcessor
        @Override
        public final void run() {
            try {
                Source source = Source.create(document);
                ParserManager.parse(Collections.singleton(source), this);
            } catch (ParseException e) {
                LOG.log(Level.WARNING, null, e);
                return;
            }

            //switch to EDT
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    TopComponent activetc = TopComponent.getRegistry().getActivated();

                    if (ui != null) {
                        UI.openRefactoringUI(ui, activetc);
                    } else {
                        JOptionPane.showMessageDialog(null, "Cannot refactor");//NOI18N
                    }
                }
            });
        }

        protected abstract RefactoringUI createRefactoringUI(PPElementContext context);
    }

}
