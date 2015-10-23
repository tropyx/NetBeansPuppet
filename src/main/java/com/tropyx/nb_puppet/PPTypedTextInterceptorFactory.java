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

import com.tropyx.nb_puppet.lexer.PLangHierarchy;
import com.tropyx.nb_puppet.lexer.PLanguageProvider;
import com.tropyx.nb_puppet.lexer.PTokenId;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

@MimeRegistration(mimeType = PPConstants.MIME_TYPE, service = TypedTextInterceptor.Factory.class)
public class PPTypedTextInterceptorFactory implements TypedTextInterceptor.Factory {

    @Override
    public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
        return new PPTypedTextInterceptor();
    }

    private static class PPTypedTextInterceptor implements TypedTextInterceptor {

        public PPTypedTextInterceptor() {
        }

        @Override
        public boolean beforeInsert(Context context) throws BadLocationException {
            return false;
        }

        @Override
        public void insert(MutableContext context) throws BadLocationException {
            int caretOffset = context.getOffset();
            BaseDocument document = (BaseDocument) context.getDocument();
            TokenSequence<PTokenId> ts = PLangHierarchy.getTokenSequence(document);

            if (ts == null) {
                return;
            }

            ts.move(caretOffset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return;
            }
            Token<PTokenId> token = ts.token();
            if (token.id() == PTokenId.STRING_LITERAL) {
                if (token.text().toString().startsWith("\"")) {
                    if ("$".equals(context.getText())) {
                        context.setText("${}", 2);
                    }
                }
            } else if (token.id() == PTokenId.VARIABLE) {
//                if (":".equals(context.getText())) {
//                    context.setText("::", 2);
//                }
            } else {
                String previousChar = caretOffset > 0 ? document.getText(caretOffset -1, 1) : "";
                String prevPreviousChar = caretOffset > 1 ? document.getText(caretOffset -2, 1) : "";
                if ("|".equals(context.getText())) {
                    if (prevPreviousChar.equals("<") && previousChar.equals("<")) {
                        context.setText("| |>>", 1);
                        return;
                    }
                    if (previousChar.equals("<")) {
                        context.setText("| |>", 1);
                        return;
                    }
                }

                if (null != context.getText()) switch (context.getText()) {
                    case "'":
                        context.setText("''", 1);
                        break;
                    case "\"":
                        context.setText("\"\"", 1);
                        break;
                    case "[":
                        context.setText("[]", 1);
                        break;
                    case "{":
                        context.setText("{}", 1);
                        break;
                    case "(":
                        context.setText("()", 1);
                        break;
                    default:
                }
            }
        }

        @Override
        public void afterInsert(Context context) throws BadLocationException {
        }

        @Override
        public void cancelled(Context context) {
        }
    }

}
