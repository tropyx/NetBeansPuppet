package com.tropyx.nb_puppet.refactoring;

import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.util.Lookup;

/**
 * @author Milos Kleint
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class)
public class PPRefactoringFactory implements RefactoringPluginFactory {
    
    public PPRefactoringFactory() { }

    @Override
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        Lookup look = refactoring.getRefactoringSource();
        PPElementContext context = look.lookup(PPElementContext.class);
        
        if (refactoring instanceof WhereUsedQuery) {
            if (context != null) {
                return new PPWhereUsedQueryPlugin((WhereUsedQuery)refactoring);
            }
        }
        return null;
    }
}
