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


