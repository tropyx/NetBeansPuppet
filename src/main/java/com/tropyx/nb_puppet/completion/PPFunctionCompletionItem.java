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
package com.tropyx.nb_puppet.completion;

import com.tropyx.nb_puppet.PPConstants;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.text.JTextComponent;
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
        CompletionUtilities.renderHtml(ImageUtilities.loadImageIcon(PPConstants.FUNCTION_ICON, true), value, rightText, g, defaultFont, defaultColor, width, height, selected);
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
