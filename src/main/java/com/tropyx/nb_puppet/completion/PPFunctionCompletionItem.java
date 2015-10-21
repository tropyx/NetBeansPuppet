
package com.tropyx.nb_puppet.completion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.ImageUtilities;

public class PPFunctionCompletionItem implements CompletionItem {
    private final String prefix;
    private final String value;
    private final int caretOffset;
    private final String rightText;
    
    @StaticResource
    public static final String ICON = "com/tropyx/nb_puppet/resources/function.png";

    public PPFunctionCompletionItem(String prefix, String value, int caretOffset, String rightText) {
        this.prefix = prefix;
        this.value = value;
        this.caretOffset = caretOffset;
        this.rightText = rightText;
    }
    public PPFunctionCompletionItem(String prefix, String value, int caretOffset) {
        this(prefix, value, caretOffset, "");
    }
    

    @Override
    public void defaultAction(JTextComponent component) {
        String text = value.substring(prefix.length()) + "(${cursor})";
        CodeTemplateManager ctm = CodeTemplateManager.get(component.getDocument());
        ctm.createTemporary(text).insert(component);
    }

    @Override
    public void processKeyEvent(KeyEvent evt) {
    }

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        return CompletionUtilities.getPreferredWidth(value, rightText, g, defaultFont);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(ImageUtilities.loadImageIcon(ICON, true), value, rightText, g, defaultFont, defaultColor, width, height, selected);
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
        return 20;
    }

    @Override
    public CharSequence getSortText() {
        return value;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return prefix;
    }

}
