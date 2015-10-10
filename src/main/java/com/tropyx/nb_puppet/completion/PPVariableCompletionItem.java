
package com.tropyx.nb_puppet.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

public class PPVariableCompletionItem implements CompletionItem {
    private final String prefix;
    private final String varName;
    private final int caretOffset;
    private final String className;
    private final String currentClassName;
    private final String inherits;
    private final boolean inString;
    
    @StaticResource
    private static final String ICON = "com/tropyx/nb_puppet/resources/puppet_icon.gif";

    public PPVariableCompletionItem(String prefix, String varName, int caretOffset, 
            String className, String currentClassName, String inherits, boolean inString) {
        this.prefix = prefix;
        this.varName = varName;
        this.caretOffset = caretOffset;
        this.className = className;
        this.currentClassName = currentClassName;
        this.inherits = inherits;
        this.inString = inString;
    }

    @Override
    public void defaultAction(JTextComponent component) {
        String text;
        if (currentClassName.equals(className) || inherits.equals(className)) {
            text = (inString ? "{" : "$" ) + varName + (inString ? "}" : " ");
        } else {
            text = (inString ? "{" : "$" ) + className + "::" + varName + (inString ? "}" : " ");
        }

        try {
            component.getDocument().remove(caretOffset - prefix.length(), prefix.length());
            component.getDocument().insertString(caretOffset - prefix.length(), text, null);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void processKeyEvent(KeyEvent evt) {
    }

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(varName, className, g, defaultFont);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(ImageUtilities.loadImageIcon(ICON, true), varName, "<font color='!controlShadow'>" + className + "</font>", g, defaultFont, defaultColor, width, height, selected);
    }

    @Override
    public CompletionTask createDocumentationTask() {
        return null;
    }

    @Override
    public CompletionTask createToolTipTask() {
        return null;
    }

    @Override
    public boolean instantSubstitution(JTextComponent component) {
        return false;
    }

    @Override
    public int getSortPriority() {
        if (currentClassName.equals(className)) {
            return -10;
        }
        if (inherits.equals(className)) {
            return -5;
        }
        String[] s = currentClassName.split("::");
        String moduleName = s[0];
        if (className.startsWith(moduleName + "::")) {
            return -3;
        }
        return 0;
    }

    @Override
    public CharSequence getSortText() {
        return className + "::" + varName;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return prefix;
    }

}
