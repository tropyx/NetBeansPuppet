/*
 * Copyright (C) 2014 mkleint
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

package com.tropyx.nb_puppet.lexer;

import java.util.Collection;
import java.util.EnumSet;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author mkleint
 */
public class PLangHierarchy extends LanguageHierarchy<PTokenId>
{

    @Override
    protected Collection<PTokenId> createTokenIds()
    {
        return EnumSet.allOf (PTokenId.class);
    }

    @Override
    protected Lexer<PTokenId> createLexer(LexerRestartInfo<PTokenId> lri)
    {
        return new PLexer(lri);
    }

    @Override
    protected String mimeType()
    {
        return PLanguageProvider.MIME_TYPE;
    }

    public static TokenSequence<PTokenId> getTokenSequence(Document document) {
        TokenHierarchy th = TokenHierarchy.get(document);
        @SuppressWarnings("unchecked")
        TokenSequence<PTokenId> ts = th.tokenSequence();
        return ts;
    }
    
}
