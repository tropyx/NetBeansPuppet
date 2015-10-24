/*
 * Copyright (C) Tropyx Technology Pty Ltd and Michael Lindner 2013
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

package com.tropyx.nb_puppet.highlighter;

import com.tropyx.nb_puppet.lexer.PLangHierarchy;
import com.tropyx.nb_puppet.lexer.PTokenId;
import java.awt.Color;
import java.lang.ref.WeakReference;
import javax.swing.JEditorPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

public class MarkOccurrencesHighlighter implements CaretListener {

    private static final AttributeSet defaultColors =
            AttributesUtilities.createImmutable(StyleConstants.Background,
            new Color(236, 235, 163));

    private final OffsetsBag bag;

    private JTextComponent comp;
    private final WeakReference<Document> weakDoc;

    private final RequestProcessor rp;
    private final static int REFRESH_DELAY = 100;
    private RequestProcessor.Task lastRefreshTask;

    public MarkOccurrencesHighlighter(Document doc) {
        rp = new RequestProcessor(MarkOccurrencesHighlighter.class);
        bag = new OffsetsBag(doc);
        weakDoc = new WeakReference<>(doc);
        DataObject dobj = NbEditorUtilities.getDataObject(weakDoc.get());
        if (dobj != null) {
            EditorCookie pane = dobj.getLookup().lookup(EditorCookie.class);
            JEditorPane[] panes = pane.getOpenedPanes();
            if (panes != null && panes.length > 0) {
                comp = panes[0];
                comp.addCaretListener(this);
            }
        }
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        bag.clear();
        setupAutoRefresh();
    }

    public void setupAutoRefresh() {
        if (lastRefreshTask == null) {
            lastRefreshTask = rp.create(new Runnable() {
                @Override
                public void run() {
                    final Document doc = comp.getDocument();
                    final int offset = comp.getCaretPosition();
                    doc.render(new Runnable() {
                        @Override
                        public void run() {
                            TokenSequence<PTokenId> ts = PLangHierarchy.getTokenSequence(doc);
                            ts.move(offset);
                            ts.moveNext();
                            Token<PTokenId> token = ts.token();
                            if (token == null) {
                                return;
                            }
                            if (token.id() == PTokenId.VARIABLE) {
                                String variable = token.text().toString();
                                addHighlights(ts, variable);
                            } else if (token.id() == PTokenId.STRING_LITERAL) {
                                String text = token.text().toString();
                                int reloffset = offset - ts.offset();
                                int start = text.substring(0, reloffset).lastIndexOf("${");
                                if (start != -1 && start <= reloffset && text.indexOf("}", start) > reloffset) {
                                    String var = text.substring(start, text.indexOf("}", start)).replace("${", "$");
                                    addHighlights(ts, var);
                                }
                                
                            }
                        }

                        public void addHighlights(TokenSequence<PTokenId> ts, String variable) {
                            Token<PTokenId> token;
                            ts.moveStart();
                            while (ts.moveNext()) {
                                token = ts.token();
                                if (token.id() == PTokenId.VARIABLE) {
                                    if (token.text().toString().equals(variable)) {
                                        bag.addHighlight(ts.offset(), ts.offset() + token.length(), defaultColors);
                                    }
                                }
                                if (token.id() == PTokenId.STRING_LITERAL) {
                                    String txt = token.text().toString();
                                    int i = txt.indexOf(variable.replace("$", "${") + "}");
                                    while (i > 0) {
                                        bag.addHighlight(ts.offset() + i, ts.offset()  + i + variable.length() + 2, defaultColors);
                                        i = txt.indexOf(variable.replace("$", "${") + "}", i + 1);
                                    }
                                }
                            }
                        }
                    });
                }
            });
        }
        lastRefreshTask.schedule(REFRESH_DELAY);
    }

    public OffsetsBag getHighlightsBag() {
        return bag;
    }

}