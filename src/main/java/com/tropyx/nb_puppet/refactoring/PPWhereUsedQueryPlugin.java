
package com.tropyx.nb_puppet.refactoring;

import com.tropyx.nb_puppet.indexer.PPIndexer;
import com.tropyx.nb_puppet.indexer.PPIndexerFactory;
import com.tropyx.nb_puppet.parser.PClass;
import com.tropyx.nb_puppet.parser.PElement;
import com.tropyx.nb_puppet.parser.PFunction;
import com.tropyx.nb_puppet.parser.PVariable;
import com.tropyx.nb_puppet.parser.PVariableDefinition;
import com.tropyx.nb_puppet.parser.PuppetParserResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Query;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;

class PPWhereUsedQueryPlugin implements RefactoringPlugin {
    private final WhereUsedQuery refactoring;
    private boolean cancelled = false;

    public PPWhereUsedQueryPlugin(WhereUsedQuery refactoring) {
        this.refactoring = refactoring;
    }

    @Override
    public Problem preCheck() {
        return null;
    }

    public static String getDisplayName(PPElementContext context) {
        String toRet = getVariableName(context);
        if (toRet != null) {
            return toRet;
        }
        toRet = getFunctionName(context);
        if (toRet != null) {
            return toRet + "()";
        }
        return null;
    }

    private static String getVariableName(PPElementContext context) {
        final PElement caretNode = context.getCaretNode();
        if (caretNode.getType() == PElement.VARIABLE) {
            return  ((PVariable)caretNode).getName();
        } else if (caretNode.getType() == PElement.VARIABLE_DEFINITION) {
            return  ((PVariableDefinition)caretNode).getName();
        }
        return null;
    }

    private static String getFunctionName(PPElementContext context) {
        final PElement caretNode = context.getCaretNode();
        if (caretNode.getType() == PElement.FUNCTION) {
            return ((PFunction)caretNode).getName();
        }
        return null;
    }

    @Override
    public Problem prepare(final RefactoringElementsBag elements) {
        if (cancelled) {
            return null;
        }

        PPElementContext context = refactoring.getRefactoringSource().lookup(PPElementContext.class);
        String var = getVariableName(context);
        if (var != null) {
            String clazzDefineName = getCurrentName(context);
            var = var.substring(1);
            try {
                QuerySupport qs = PPIndexerFactory.getQuerySupportFor(context.getDocument(), true);
                List<String> names = new ArrayList<>();
                Query q = collectVariableCandidateNames(qs, clazzDefineName, var, names);
                System.out.println("q:" + q.toString());
                for (IndexResult res : q.execute(PPIndexer.FLD_ROOT)) {
                    FileObject file = res.getFile();
                    findVariableLocations(elements, file, names);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
        String func = getFunctionName(context);
        if (func != null) {
            try {
                QuerySupport qs = PPIndexerFactory.getQuerySupportFor(context.getDocument(), true);
                for (IndexResult res : qs.query(PPIndexer.FLD_FUNCTION, func, QuerySupport.Kind.EXACT, PPIndexer.FLD_ROOT)) {
                    FileObject file = res.getFile();
                    findFunctionLocations(elements, file, func);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
        return new Problem(true, "Where Used only works on variables and functions");
    }


    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    public Problem checkParameters() {
        return null;
    }

    @Override
    public void cancelRequest() {
        cancelled = true;
    }

    private String getCurrentName(PPElementContext context) {
        List<PClass> clz = context.getRootNode().getChildrenOfType(PClass.class, false);
        if (clz.isEmpty()) {
            return null;
        }
        return clz.get(0).getName();
    }

    private Query collectVariableCandidateNames(QuerySupport qs, String clazzDefineName, String var, List<String> names) throws IOException {
        Query.Factory qf = qs.getQueryFactory();
        if (var.startsWith("::")) {
            names.add(var);
            names.add(var.substring(2));
            return qf.or(
                    qf.field(PPIndexer.FLD_VARREF, var, QuerySupport.Kind.EXACT),
                    qf.field(PPIndexer.FLD_VARREF, var.substring(2), QuerySupport.Kind.EXACT)
            );
        }
        if (!var.contains("::")) {
            //local
            List<Query> q = new ArrayList<>();
            names.add(var);
            q.add(qf.and(
                        qf.field(PPIndexer.FLD_VARREF, var, QuerySupport.Kind.EXACT),
                        qf.field(PPIndexer.FLD_ROOT, clazzDefineName, QuerySupport.Kind.EXACT)
                    ));
            names.add("::" + var);
            q.add(qf.field(PPIndexer.FLD_VARREF, "::" + var, QuerySupport.Kind.EXACT)); //what if is global?
            names.add(clazzDefineName + "::" + var);
            q.add(qf.field(PPIndexer.FLD_VARREF, clazzDefineName + "::" + var, QuerySupport.Kind.EXACT));
            for (IndexResult r : qs.query(PPIndexer.FLD_INHERIT, clazzDefineName, QuerySupport.Kind.EXACT, PPIndexer.FLD_ROOT)) {
                String c = r.getValue(PPIndexer.FLD_ROOT);
                if (c != null) {
                    names.add(c + "::" + var);
                    q.add(qf.field(PPIndexer.FLD_VARREF, c + "::" + var, QuerySupport.Kind.EXACT));
                }
            }
            return qf.or(q.toArray(new Query[0]));
        } else {
            String clz = var.substring(0, var.lastIndexOf("::"));
            String var0 = var.substring(var.lastIndexOf("::") + 2);
            List<Query> q = new ArrayList<>();
            names.add(var);
            q.add(qf.field(PPIndexer.FLD_VARREF, var, QuerySupport.Kind.EXACT));
            names.add(var0);
            q.add(qf.and(
                qf.field(PPIndexer.FLD_VARREF, var0, QuerySupport.Kind.EXACT),
                qf.field(PPIndexer.FLD_ROOT, clz, QuerySupport.Kind.EXACT)
            ));
            for (IndexResult r : qs.query(PPIndexer.FLD_ROOT, clz, QuerySupport.Kind.EXACT, PPIndexer.FLD_INHERIT)) {
                String i = r.getValue(PPIndexer.FLD_INHERIT);
                if (i != null) {
                    names.add(i + "::" + var0);
                    q.add(qf.field(PPIndexer.FLD_VARREF, i + "::" + var0, QuerySupport.Kind.EXACT));
                    q.add(qf.and(
                        qf.field(PPIndexer.FLD_VARREF, var0, QuerySupport.Kind.EXACT),
                        qf.field(PPIndexer.FLD_ROOT, i, QuerySupport.Kind.EXACT)
                    ));
                }
            }
            return qf.or(q.toArray(new Query[0]));
        }
    }

    private void findVariableLocations(final RefactoringElementsBag elements, final FileObject file, final List<String> names) {
        try {
            Source source = Source.create(file);
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result pr = resultIterator.getParserResult();
                    if (pr instanceof PuppetParserResult) {
                        PuppetParserResult result = (PuppetParserResult) pr;
                        if (result.getRootNode() != null) {
                            for (PVariable var : result.getRootNode().getChildrenOfType(PVariable.class, true)) {
                                if (names.contains(var.getName().substring(1))) {
                                    String line = createHightlightTextLine(file, var.getOffset(), var.getName().length());
                                    elements.add(refactoring, new PPWhereUsedElement(line.trim(), file, boundsForElement(file, var, var.getName())));
                                }
                            }
                            for (PVariableDefinition var : result.getRootNode().getChildrenOfType(PVariableDefinition.class, true)) {
                                if (names.contains(var.getName().substring(1))) {
                                    String line = createHightlightTextLine(file, var.getOffset(), var.getName().length());
                                    elements.add(refactoring, new PPWhereUsedElement(line.trim(), file, boundsForElement(file, var, var.getName())));
                                }
                            }
                        }
                    }
                }
            });
        } catch (ParseException e) {
        }
    }
    
    private static CloneableEditorSupport getEditorSupport(FileObject file) {
        try {
            DataObject dob = DataObject.find(file);
            return dob.getLookup().lookup(CloneableEditorSupport.class);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private void findFunctionLocations(final RefactoringElementsBag elements, final FileObject file, final String func) {
      try {
            Source source = Source.create(file);
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Parser.Result pr = resultIterator.getParserResult();
                    if (pr instanceof PuppetParserResult) {
                        PuppetParserResult result = (PuppetParserResult) pr;
                        if (result.getRootNode() != null) {
                            for (PFunction function : result.getRootNode().getChildrenOfType(PFunction.class, true)) {
                                if (func.equals(function.getName())) {
                                    String line = createHightlightTextLine(file, function.getOffset(), func.length());
                                    elements.add(refactoring, new PPWhereUsedElement(line.trim(), file, boundsForElement(file, function, func)));
                                }
                            }
                        }
                    }
                }
            });
        } catch (ParseException e) {
        }
    }

    public String createHightlightTextLine(FileObject file, int offset, int length) throws IOException, BadLocationException {
        CloneableEditorSupport es = getEditorSupport(file);
        int rowStart = Utilities.getRowStart((BaseDocument)es.openDocument(), offset);
        int rowEnd = Utilities.getRowEnd((BaseDocument)es.getDocument(), offset);
        String line = es.getDocument().getText(rowStart, offset - rowStart) +
                "<b>" + es.getDocument().getText(offset,  length) +
                "</b>" + es.getDocument().getText(offset + length, rowEnd - (offset + length));
        return line;
    }

    public PositionBounds boundsForElement(FileObject file, PElement var, String text) {
        CloneableEditorSupport es = getEditorSupport(file);
        PositionRef start = es.createPositionRef(var.getOffset(), Position.Bias.Forward);
        PositionRef end = es.createPositionRef(var.getOffset() + text.length(), Position.Bias.Backward);
        final PositionBounds positionBounds = new PositionBounds(start, end);
        return positionBounds;
    }
}
