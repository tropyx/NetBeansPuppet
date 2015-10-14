
package com.tropyx.nb_puppet.refactoring;

import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class PPWhereUsedElement extends SimpleRefactoringElementImplementation{
    private final PositionBounds bounds;
    private final FileObject file;
    private final String text;

    public PPWhereUsedElement(String text, FileObject file, PositionBounds bounds) {
        this.text = text;
        this.file = file;
        this.bounds = bounds;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getDisplayText() {
        return text;
    }

    @Override
    public void performChange() {
    }

    @Override
    public Lookup getLookup() {
        return Lookups.singleton(file);
    }

    @Override
    public FileObject getParentFile() {
        return file;
    }

    @Override
    public PositionBounds getPosition() {
        return bounds;
    }

}
