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
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.codetemplates.api.CodeTemplateManager;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

public class PPResourceCompletionItem implements CompletionItem {
    private final String prefix;
    private final String value;
    private final int caretOffset;
    private final String rightText;
    
    private String[] reqParams;

    private PPResourceCompletionItem(String prefix, String value, int caretOffset, String rightText) {
        this.prefix = prefix;
        this.value = value;
        this.caretOffset = caretOffset;
        this.rightText = rightText;
    }

    public PPResourceCompletionItem(String prefix, String value, int caretOffset, String[] reqParams) {
        this(prefix, value, caretOffset, "");
        this.reqParams = reqParams;
    }
    

    @Override
    public void defaultAction(JTextComponent component) {
        String indent = "  ";
        try {
            int start = Utilities.getRowStart(component, caretOffset);
            StringBuilder sb = new StringBuilder();
            int count = caretOffset - prefix.length() - start;
            for (int i = 0; i < count; i++) {
                sb.append(" ");
            }
            indent = sb.toString();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(value.substring(prefix.length())).append(" { '${title}':\n");
        for (String param : reqParams) {
            sb.append(indent).append("  ").append(param).append(" => ${").append(param).append("},\n");
        }
        sb.append(indent).append("  ${cursor}\n").append(indent).append("}");
        String text = sb.toString();
        CodeTemplateManager ctm = CodeTemplateManager.get(component.getDocument());
        ctm.createTemporary(text).insert(component);
        Completion.get().hideAll();
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
        CompletionUtilities.renderHtml(ImageUtilities.loadImageIcon(PPConstants.RESOURCE_ICON, true), value, rightText, g, defaultFont, defaultColor, width, height, selected);
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
        return 10;
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
