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
package com.tropyx.nb_puppet;

import com.tropyx.nb_puppet.lexer.PLanguageProvider;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;

@MimeRegistration(mimeType = PPConstants.MIME_TYPE, service = TypedBreakInterceptor.Factory.class)
public class PPTypedBreakInterceptorFactory implements TypedBreakInterceptor.Factory {

    @Override
    public TypedBreakInterceptor createTypedBreakInterceptor(MimePath mimePath) {
        return new PPTypedBreakInterceptor();
    }

    private static class PPTypedBreakInterceptor implements TypedBreakInterceptor {

        public PPTypedBreakInterceptor() {
        }

        @Override
        public boolean beforeInsert(Context context) throws BadLocationException {
            return false;
        }

        @Override
        public void insert(MutableContext context) throws BadLocationException {
            BaseDocument doc = (BaseDocument) context.getDocument();
            int offset = context.getBreakInsertOffset();
            int indent = Utilities.getRowIndent(doc, offset);
            if (indent < 0) {
                return;
            }
            StringBuilder sb = new StringBuilder("\n");
            for (int i = 0; i < indent; i++) {
                sb.append(" ");
            }
            
            context.setText(sb.toString(), 0, indent + 1);
        }

        @Override
        public void afterInsert(Context context) throws BadLocationException {
        }

        @Override
        public void cancelled(Context context) {
        }
    }

}
