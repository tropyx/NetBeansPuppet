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

import com.tropyx.nb_puppet.lexer.PLanguageProvider;
import com.tropyx.nb_puppet.semantic.SemanticColoring;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;

@MimeRegistration(mimeType = PLanguageProvider.MIME_TYPE, service = HighlightsLayerFactory.class)
public class MarkOccurrencesHighlightsLayerFactory implements HighlightsLayerFactory {

    public static MarkOccurrencesHighlighter getMarkOccurrencesHighlighter(Document doc) {
        MarkOccurrencesHighlighter highlighter =
               (MarkOccurrencesHighlighter) doc.getProperty(MarkOccurrencesHighlighter.class);
        if (highlighter == null) {
            doc.putProperty(MarkOccurrencesHighlighter.class,
               highlighter = new MarkOccurrencesHighlighter(doc));
        }
        return highlighter;
    }

    @Override
    public HighlightsLayer[] createLayers(Context context) {
        return new HighlightsLayer[] {
            HighlightsLayer.create(SemanticColoring.class.getName() + "-2", ZOrder.SYNTAX_RACK.forPosition(1500), false,
                    SemanticColoring.getSemanticHighlightsBag(context.getDocument())),
            HighlightsLayer.create(MarkOccurrencesHighlighter.class.getName(), ZOrder.CARET_RACK.forPosition(2000), true,
                            getMarkOccurrencesHighlighter(context.getDocument()).getHighlightsBag())
        };
    }

}