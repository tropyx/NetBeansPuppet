/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tropyx.nb_puppet.lexer;

import java.util.Collection;
import java.util.EnumSet;
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
    
}
