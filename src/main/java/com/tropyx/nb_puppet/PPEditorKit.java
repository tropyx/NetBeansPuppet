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
import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.TextAction;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorKit;

public class PPEditorKit extends NbEditorKit {

    @Override
    public String getContentType() {
        return PLanguageProvider.MIME_TYPE;
    }

    @Override
    public Document createDefaultDocument() {
        Document doc = super.createDefaultDocument();
        return doc;
    }    
    @Override
    protected void initDocument(BaseDocument doc) {
        super.initDocument(doc);
        Language<PTokenId> language = getLanguage();
        doc.putProperty(Language.class, language);
        doc.putProperty(InputAttributes.class, getLexerAttributes(language, doc));
    }

    protected Language<PTokenId> getLanguage() {
        return new PLangHierarchy().language();
    }
    
    protected final InputAttributes getLexerAttributes(Language<?> language, BaseDocument doc) {
        InputAttributes lexerAttrs = new InputAttributes();
        return lexerAttrs;
    }

    
    protected Action getCommentAction() {
        return new CommentAction("#"); 
    }

    protected Action getUncommentAction() {
        return new UncommentAction("#"); 
    }

    protected Action getToggleCommentAction() {
        return new ToggleCommentAction("#"); 
    }
    
    @Override 
    protected Action[] createActions() {
        Action[] superActions = super.createActions();
        Action[] ccActions = new Action[]{
            getToggleCommentAction(),
            getCommentAction(),
            getUncommentAction(),
        };
        ccActions = TextAction.augmentList(superActions, ccActions);

        return ccActions;
    }
    
}
