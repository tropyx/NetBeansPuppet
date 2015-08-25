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

import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageProvider;

/**
 *
 * @author Milos Kleint
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.lexer.LanguageProvider.class)
public class PLanguageProvider extends LanguageProvider {
    public static final String MIME_TYPE = "text/x-puppet-manifest";
    
    public Language<PTokenId> findLanguage (String mimeType) {
        if (MIME_TYPE.equals (mimeType))
            return new PLangHierarchy ().language ();
        return null;
    }

    @Override
    public LanguageEmbedding<?> findLanguageEmbedding (
        Token arg0,
        LanguagePath arg1,
        InputAttributes arg2
    ) {
        return null;
    }
}


