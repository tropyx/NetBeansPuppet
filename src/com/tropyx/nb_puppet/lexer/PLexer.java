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

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author mkleint
 */
public class PLexer implements Lexer<PTokenId>
{

    private static final int EOF = LexerInput.EOF;
    private final LexerInput input;

    private final TokenFactory<PTokenId> tokenFactory;

    PLexer(LexerRestartInfo<PTokenId> lri)
    {
        input = lri.input();
        tokenFactory = lri.tokenFactory();
    }

    @Override
    public Token<PTokenId> nextToken()
    {
        while (true)
        {
            int c = nextChar();
            PTokenId lookupId = null;
            switch (c)
            {
                case '0' : case '1': case '2': case '3': case '4':
                case '5' : case '6': case '7': case '8': case '9':
                    return finishNumberLiteral(nextChar(), false);
                case '#': // in single-line comment
                    while (true)
                        switch (nextChar()) {
                            case '\r': consumeNewline();
                            case '\n':
                            case EOF:
                                return token(PTokenId.LINE_COMMENT);
                        }
                case '\'': // string literal
                    while (true)
                        switch (nextChar()) {
                            case '\'': // NOI18N
                                return token(PTokenId.STRING_LITERAL);
                            case '\\':
                                nextChar(); // read escaped char
                                break;
                            case '\r': consumeNewline();
                            case '\n':
                            case EOF:
                                return tokenFactory.createToken(PTokenId.STRING_LITERAL,
                                        input.readLength(), PartType.START);
                        }
                case '"': // string literal
                    while (true)
                        switch (nextChar()) {
                            case '"': // NOI18N
                                //TODO make different from '?
                                return token(PTokenId.STRING_LITERAL);
                            case '\\':
                                nextChar(); // read escaped char
                                break;
                            case '\r': consumeNewline();
                            case '\n':
                            case EOF:
                                //TODO make different from '?
                                return tokenFactory.createToken(PTokenId.STRING_LITERAL,
                                        input.readLength(), PartType.START);
                        }

                case '$':
                    return finishVariable(c);
                    
                case '!': 
                    switch (c = nextChar())
                    {
                        case '=' : return token(PTokenId.OPERATOR);
                        case '~' : return token(PTokenId.OPERATOR);
                        default : backup(1);
                    }
                    
                    return token(PTokenId.OPERATOR);
                case '=': 
                    switch (c = nextChar())
                    {
                        case '>' : return token(PTokenId.PARAM_ASSIGN); //TODO is this really operator?
                        case '=' : return token(PTokenId.OPERATOR);
                        case '~' : return token(PTokenId.OPERATOR);
                        default : backup(1);
                    }
                    
                    return token(PTokenId.EQUALS);
                case '>' :
                    switch (c = nextChar())
                    {
                        case '=' : return token(PTokenId.OPERATOR);
                        case '>' : return token(PTokenId.OPERATOR);
                        default : backup(1);
                    }
                    
                    return token(PTokenId.OPERATOR);
                                        
                case '<' :
                    switch (c = nextChar())
                    {
                        case '=' : return token(PTokenId.OPERATOR);
                        case '<' : return token(PTokenId.OPERATOR);
                        default : backup(1);
                    }
                    
                    return token(PTokenId.OPERATOR);
                
                case '+':
                case '-':
                case '*':
                case '/':
                    return finishRegexp(c);
                case '%':
                    return token(PTokenId.OPERATOR);
    
                case '?':
                    return token(PTokenId.QUESTIONMARK); 
                    
                case 'a' :
                    switch (c = nextChar())
                    {
                        case 'n' :
                            if ((c = nextChar()) == 'd') 
                            {
                                return textOperatorOrIdentifier(PTokenId.AND);
                            }
                            break;
                        case 'l':
                            if ((c = nextChar()) == 'e' 
                             && (c = nextChar()) == 'r'
                             && (c = nextChar()) == 't')
                            {
                                return functionOrIdentifier(PTokenId.ALERT);
                            }
                            break;
                    }                            
                    return finishIdentifier(c);
                    
                case 'c' :
                    switch (c = nextChar())
                    {
                        case 'a' :
                            if ((c = nextChar()) == 's'
                             && (c = nextChar()) == 'e') 
                            {
                                return keywordOrIdentifier(PTokenId.CASE);
                            }
                            break;
                        case 'l' :
                            if ((c = nextChar()) == 'a'
                             && (c = nextChar()) == 's'
                             && (c = nextChar()) == 's') 
                            {
                                return keywordOrIdentifier(PTokenId.CLASS);
                            }
                            break;
                        case 'o': 
                            switch (c = nextChar()) 
                            {
                                case 'l' :
                                    if ((c = nextChar()) == 'l'
                                     && (c = nextChar()) == 'e'
                                     && (c = nextChar()) == 'c'
                                     && (c = nextChar()) == 't') 
                                    {
                                        return functionOrIdentifier(PTokenId.COLLECT);
                                    }
                                    break;
  
                                case 'n' :
                                    if ((c = nextChar()) == 't'
                                     && (c = nextChar()) == 'a'
                                     && (c = nextChar()) == 'i'
                                     && (c = nextChar()) == 'n') 
                                    {
                                        return functionOrIdentifier(PTokenId.CONTAIN);
                                    }
                                    break;
                            }
                            break;
                        case 'r':
                            switch (c = nextChar()) 
                            {
                                case 'i' :
                                    if ((c = nextChar()) == 't')
                                    {
                                        return functionOrIdentifier(PTokenId.CRIT);
                                    }
                                    break;
  
                                case 'e' :
                                    if ((c = nextChar()) == 'a'
                                     && (c = nextChar()) == 't'
                                     && (c = nextChar()) == 'e'
                                     && (c = nextChar()) == '_'
                                     && (c = nextChar()) == 'r'
                                     && (c = nextChar()) == 'e'
                                     && (c = nextChar()) == 's'
                                     && (c = nextChar()) == 'o'
                                     && (c = nextChar()) == 'r'
                                     && (c = nextChar()) == 'c'
                                     && (c = nextChar()) == 'e'
                                     && (c = nextChar()) == 's')
                                    {
                                        return functionOrIdentifier(PTokenId.CREATE_RESOURCES);
                                    }
                                    break;
                            }
                            
                            break;
                    }
                    return finishIdentifier(c);
                    
                        
                case 'd' : 
                    if ((c = nextChar()) == 'e') {
                        switch (c = nextChar()) {
                            case 'f':
                                switch (c = nextChar()) {
                                    case 'i':
                                        if ((c = nextChar()) == 'n'
                                         && (c = nextChar()) == 'e') {
                                            return keywordOrIdentifier(PTokenId.DEFINE);
                                        }
                                        break;
                                    case 'a':
                                        if ((c = nextChar()) == 'u'
                                         && (c = nextChar()) == 'l'
                                         && (c = nextChar()) == 't') {
                                            return keywordOrIdentifier(PTokenId.DEFAULT);
                                        }
                                        break;
                                }
                                break;
                            case 'b': 
                                if ((c = nextChar()) == 'u'
                                 && (c = nextChar()) == 'g') {
                                    return keywordOrIdentifier(PTokenId.DEBUG);
                                }
                                break;
                        }
                    }
                    return finishIdentifier(c);
                    
                case 'e' :
                    switch (c = nextChar())
                    {
                        case 'a':
                            if ((c = nextChar()) == 'c' 
                             && (c = nextChar()) == 'h') 
                            {
                                return functionOrIdentifier(PTokenId.EACH);
                            }
                            break;
                        case 'l':
                            if ((c = nextChar()) == 's') 
                            {
                                switch (c = nextChar()) {
                                    case 'e' : return keywordOrIdentifier(PTokenId.ELSE);
                                    case 'i' : 
                                        if ((c = nextChar()) == 'f') {
                                            return keywordOrIdentifier(PTokenId.ELSIF);
                                        }
                                        break;
                                }
                            }
                            break;
                        case 'm' :
                            if ((c = nextChar()) == 'e' 
                             && (c = nextChar()) == 'r'
                             && (c = nextChar()) == 'g') {
                                return functionOrIdentifier(PTokenId.EMERG);
                            }
                            break;
                        case 'p' :
                            if ((c = nextChar()) == 'p') {
                                return functionOrIdentifier(PTokenId.EPP);
                            }
                            break;
                        case 'r' :
                            if ((c = nextChar()) == 'r') {
                                return functionOrIdentifier(PTokenId.ERR);
                            }
                            break;
                        case 'x' :
                            if ((c = nextChar()) == 't' 
                             && (c = nextChar()) == 'l'
                             && (c = nextChar()) == 'o'
                             && (c = nextChar()) == 'o'
                             && (c = nextChar()) == 'k'
                             && (c = nextChar()) == 'u'
                             && (c = nextChar()) == 'p') {
                                return functionOrIdentifier(PTokenId.EXTLOOKUP);
                            }
                            break;
                    }
                    return finishIdentifier(c);
                case 'f':
                    switch (c = nextChar())
                    {
                        case 'a': 
                            if ((c = nextChar()) == 'i'
                                    && (c = nextChar()) == 'l')
                            {
                                return functionOrIdentifier(PTokenId.FAIL);
                            }
                            break;
                        case 'i':
                            if ((c = nextChar()) == 'l') {
                                switch (c = nextChar()) {
                                    case 'e':
                                        return functionOrIdentifier(PTokenId.FILE);
                                    case 't':
                                        if ((c = nextChar()) == 'e' 
                                          && (c = nextChar()) == 'r') {
                                            return functionOrIdentifier(PTokenId.FILTER);
                                        }
                                        break;
                                }
                            }
                            break;
                        case 'q':
                            if ((c = nextChar()) == 'd' 
                             && (c = nextChar()) == 'n'
                             && (c = nextChar()) == '_'
                             && (c = nextChar()) == 'r'
                             && (c = nextChar()) == 'a'
                             && (c = nextChar()) == 'n'
                             && (c = nextChar()) == 'd') {
                                return functionOrIdentifier(PTokenId.FQDN_RAND);
                            }
                            break;
                            
                    }
                    return finishIdentifier(c);
                
                case 'h' :
                    if ((c = nextChar()) == 'i'
                     && (c = nextChar()) == 'e'
                     && (c = nextChar()) == 'r'
                     && (c = nextChar()) == 'a')
                    {
                        c = nextChar();
                        if (c == '_') {
                            switch (c = nextChar()) {
                                case 'a':
                                    if ((c = nextChar()) == 'r'
                                     && (c = nextChar()) == 'r'
                                     && (c = nextChar()) == 'a'
                                     && (c = nextChar()) == 'y') {
                                        return functionOrIdentifier(PTokenId.HIERA_ARRAY);
                                    }
                                    break;
                                case 'h':
                                    if ((c = nextChar()) == 'a'
                                     && (c = nextChar()) == 's'
                                     && (c = nextChar()) == 'h') {
                                        return functionOrIdentifier(PTokenId.HIERA_HASH);
                                    }
                                    break;
                                case 'i':
                                    if ((c = nextChar()) == 'n'
                                     && (c = nextChar()) == 'c'
                                     && (c = nextChar()) == 'l'
                                     && (c = nextChar()) == 'u'
                                     && (c = nextChar()) == 'd'
                                     && (c = nextChar()) == 'e') {
                                        return functionOrIdentifier(PTokenId.HIERA_INCLUDE);
                                    }
                                    break;
                            }
                        } else {
                            //hieara itself?
                            backup(1);
                            return functionOrIdentifier(PTokenId.HIERA);
                        }
                    }
                    return finishIdentifier(c);
                    
                case 'i':
                    switch (c = nextChar())
                    {
                        case 'f':
                            return keywordOrIdentifier(PTokenId.IF);
                        case 'm':
                            if ((c = nextChar()) == 'p'
                                    && (c = nextChar()) == 'o'
                                    && (c = nextChar()) == 'r'
                                    && (c = nextChar()) == 't')
                            {
                                return keywordOrIdentifier(PTokenId.IMPORT);
                            }
                            break;
                        case 'n':
                            
                            switch (c = nextChar())
                            {
                                case 'h':
                                    if ((c = nextChar()) == 'e'
                                        && (c = nextChar()) == 'r'
                                        && (c = nextChar()) == 'i'
                                        && (c = nextChar()) == 't'
                                        && (c = nextChar()) == 's')
                                    {
                                        return keywordOrIdentifier(PTokenId.INHERITS);
                                    }
                                    break;
                                    
                                case 'c':
                                    if ((c = nextChar()) == 'l'
                                        && (c = nextChar()) == 'u'
                                        && (c = nextChar()) == 'd'
                                        && (c = nextChar()) == 'e')
                                    {
                                        return functionOrIdentifier(PTokenId.INCLUDE);
                                    }
                                    break;
                            }
                            break;
                            
                    }
                    return finishIdentifier(c);
                    
                case 'l' :
                    if ((c = nextChar()) == 'o'
                     && (c = nextChar()) == 'o' 
                     && (c = nextChar()) == 'k' 
                     && (c = nextChar()) == 'u' 
                     && (c = nextChar()) == 'p') 
                    {
                        return keywordOrIdentifier(PTokenId.LOOKUP);
                    }
                    return finishIdentifier(c);
                    
                    
                case 'm' :
                        switch (c = nextChar())
                        {
                            case 'a':
                                if ((c = nextChar()) == 'p') {
                                    return functionOrIdentifier(PTokenId.MAP);
                                }
                                return finishIdentifier(c);
                            case 'd':
                                if ((c = nextChar()) == '5') {
                                    return functionOrIdentifier(PTokenId.MD5);
                                }
                                return finishIdentifier(c);
                    
                        }
                        return finishIdentifier(c);
                case 'n' :
                    if ((c = nextChar()) == 'o') {
                        switch (c = nextChar())
                        {
                            case 't':
                                if ((c = nextChar()) == 'i'
                                 && (c = nextChar()) == 'c' 
                                 && (c = nextChar()) == 'e')
                                {
                                    return functionOrIdentifier(PTokenId.NOTICE);
                                }
                                return finishIdentifier(c);
                            case 'd':
                                if ((c = nextChar()) == 'e') 
                                {
                                    return keywordOrIdentifier(PTokenId.NODE);
                                }
                                return finishIdentifier(c);
                        }
                    }
                    return finishIdentifier(c);
                    
                case 'o' :
                    if ((c = nextChar()) == 'r')
                    {
                        return textOperatorOrIdentifier(PTokenId.OR, c);
                    }
                    return finishIdentifier(c);

                case 'r' :
                    
                    if ((c = nextChar()) == 'e') {
                        switch (c = nextChar()) {
                            case 'q' :
                                if ((c = nextChar()) == 'u'
                                        && (c = nextChar()) == 'i'
                                        && (c = nextChar()) == 'r'
                                        && (c = nextChar()) == 'e') {
                                    return functionOrIdentifier(PTokenId.REQUIRE);
                                }
                                break;
                            case 'a':
                                if ((c = nextChar()) == 'l'
                                        && (c = nextChar()) == 'i'
                                        && (c = nextChar()) == 'z'
                                        && (c = nextChar()) == 'e') {
                                    return functionOrIdentifier(PTokenId.REALIZE);
                                }
                                break;
                            case 'd':
                                if ((c = nextChar()) == 'u'
                                        && (c = nextChar()) == 'c'
                                        && (c = nextChar()) == 'e') {
                                    return functionOrIdentifier(PTokenId.REDUCE);
                                }
                                break;
                        }
                    }
                    return finishIdentifier(c);
                case 's' :
                    switch (c = nextChar()) {
                        case 'e' :
                            switch (c = nextChar())
                            {
                                case 'l':
                                    if ((c = nextChar()) == 'e'
                                       && (c = nextChar()) == 'c'
                                       && (c = nextChar()) == 't') 
                                    {  
                                        return functionOrIdentifier(PTokenId.SELECT);
                                    }
                                    break;
                                case 'a':
                                    if ((c = nextChar()) == 'r' 
                                       && (c = nextChar()) == 'c'
                                       && (c = nextChar()) == 'h') 
                                    {
                                        return functionOrIdentifier(PTokenId.SEARCH);
                                    }
                                    break;
                            }
                            break;
                        case 'h':
                            switch (c = nextChar())
                            {
                                case 'a':
                                    if ((c = nextChar()) == '1')
                                    {  
                                        return functionOrIdentifier(PTokenId.SHA1);
                                    }
                                    break;
                                case 'e':
                                    if ((c = nextChar()) == 'l' 
                                       && (c = nextChar()) == 'l'
                                       && (c = nextChar()) == 'q'
                                       && (c = nextChar()) == 'u'
                                       && (c = nextChar()) == 'o'
                                       && (c = nextChar()) == 't'
                                       && (c = nextChar()) == 'e') 
                                    {
                                        return functionOrIdentifier(PTokenId.SHELLQUOTE);
                                    }
                                    break;
                            }
                            break;
                        case 'l':
                            if ((c = nextChar()) == 'i' 
                               && (c = nextChar()) == 'c'
                               && (c = nextChar()) == 'e') 
                            {
                                return functionOrIdentifier(PTokenId.SLICE);
                            }
                            break;
                        case 'p':
                            switch (c = nextChar())
                            {
                                case 'l':
                                    if ((c = nextChar()) == 'i'
                                       && (c = nextChar()) == 't') 
                                    {  
                                        return functionOrIdentifier(PTokenId.SPLIT);
                                    }
                                    break;
                                case 'r':
                                    if ((c = nextChar()) == 'i' 
                                       && (c = nextChar()) == 'n'
                                       && (c = nextChar()) == 't'
                                       && (c = nextChar()) == 'f') 
                                    {
                                        return functionOrIdentifier(PTokenId.SPRINTF);
                                    }
                                    break;
                            }
                            break;

                            
                    }
                    return finishIdentifier(c);
                case 't' :
                    switch (c = nextChar()) {
                        case 'a' :
                            if ((c = nextChar()) == 'g') {
                                //TODO tagged
                                return functionOrIdentifier(PTokenId.TAG);
                            }
                            break;
                        case 'e' :
                            if ((c = nextChar()) == 'm'
                               && (c = nextChar()) == 'p'
                               && (c = nextChar()) == 'l'
                               && (c = nextChar()) == 'a'
                               && (c = nextChar()) == 't'
                               && (c = nextChar()) == 'e') 
                            {
                                return functionOrIdentifier(PTokenId.TEMPLATE);
                            }
                            break;
                            
                    }
                    return finishIdentifier(c);
                    
                case 'u' :
                    if ((c = nextChar()) == 'n') {
                        switch (c = nextChar()) {
                            case 'd':
                                if ((c = nextChar()) == 'e'
                                    && (c = nextChar()) == 'f')
                                {
                                    return keywordOrIdentifier(PTokenId.UNDEF);
                                }
                                break;
                                
                            case 'l':
                                if ((c = nextChar()) == 'e'
                                    && (c = nextChar()) == 's'
                                    && (c = nextChar()) == 's')
                                {
                                    return keywordOrIdentifier(PTokenId.UNLESS);
                                }
                                break;
                        }
                    }
                    return finishIdentifier(c);
                case 'v' :
                    if ((c = nextChar()) == 'e'
                     && (c = nextChar()) == 'r' 
                     && (c = nextChar()) == 's' 
                     && (c = nextChar()) == 'i' 
                     && (c = nextChar()) == 'o' 
                     && (c = nextChar()) == 'n' 
                     && (c = nextChar()) == 'c' 
                     && (c = nextChar()) == 'm' 
                     && (c = nextChar()) == 'p') 
                    {
                        return functionOrIdentifier(PTokenId.VERSIONCMP);
                    }
                    return finishIdentifier(c);
                    
                case 'w' :
                    if ((c = nextChar()) == 'a'
                     && (c = nextChar()) == 'r' 
                     && (c = nextChar()) == 'n' 
                     && (c = nextChar()) == 'i' 
                     && (c = nextChar()) == 'n' 
                     && (c = nextChar()) == 'g') 
                    {
                        return functionOrIdentifier(PTokenId.WARNING);
                    }
                    return finishIdentifier(c);

                // Rest of lowercase letters starting identifiers
                case 'b':
                case 'g':
                case 'j':
                case 'k':
                case 'p':
                case 'q':
                case 'x':
                case 'y':
                case 'z':
                // Uppercase letters starting identifiers
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '_':
                    return finishIdentifier();
                    
                case '(':
                    return token(PTokenId.LPAREN);
                case ')':
                    return token(PTokenId.RPAREN);
                case '[':
                    return token(PTokenId.LBRACKET);
                case ']':
                    return token(PTokenId.RBRACKET);
                case '{':
                    return token(PTokenId.LBRACE);
                case '}':
                    return token(PTokenId.RBRACE);
                case ',':
                    return token(PTokenId.COMMA);
                case ':':
                    return token(PTokenId.COLON);
                    
// All Character.isWhitespace(c) below 0x80 follow
                // ['\t' - '\r'] and [0x1c - ' ']
                case '\t':
                case '\n':
                case 0x0b:
                case '\f':
                case '\r':
                case 0x1c:
                case 0x1d:
                case 0x1e:
                case 0x1f:
                    return finishWhitespace();
                case ' ':
                    c = nextChar();
                    if (c == EOF || !Character.isWhitespace(c)) { // Return single space as flyweight token
                        backup(1);
                        return   input.readLength() == 1
                               ? tokenFactory.getFlyweightToken(PTokenId.WHITESPACE, " ")
                               : tokenFactory.createToken(PTokenId.WHITESPACE);
                    }
                    return finishWhitespace();

                case EOF:
                    return null;

                default:
                    if (c >= 0x80) { // lowSurr ones already handled above
                        c = translateSurrogates(c);
                        if (Character.isJavaIdentifierStart(c))
                            return finishIdentifier();
                        if (Character.isWhitespace(c))
                            return finishWhitespace();
                    }

                    // Invalid char
                    return token(PTokenId.ERROR);
            }
        }
    }

    int previousLength = -1;
    int currentLength = -1;

    public int nextChar()
    {
        previousLength = currentLength;

        int backupReadLength = input.readLength();
        int c = input.read();

        if (c != '\\')
        {
            currentLength = 1;
            return c;
        }

        boolean wasU = false;
        int first;

        while ((first = input.read()) == 'u')
        {
            wasU = true;
        }

        if (!wasU)
        {
            input.backup(input.readLengthEOF() - backupReadLength);
            currentLength = 1;
            return input.read();
        }

        int second = input.read();
        int third = input.read();
        int fourth = input.read();

        if (fourth == LexerInput.EOF)
        {
            //TODO: broken unicode
            input.backup(input.readLengthEOF() - backupReadLength);
            currentLength = 1;
            return input.read();
        }

        first = Character.digit(first, 16);
        second = Character.digit(second, 16);
        third = Character.digit(third, 16);
        fourth = Character.digit(fourth, 16);

        if (first == (-1) || second == (-1) || third == (-1) || fourth == (-1))
        {
            //TODO: broken unicode
            input.backup(input.readLengthEOF() - backupReadLength);
            currentLength = 1;
            return input.read();
        }

        currentLength = input.readLength() - backupReadLength;
        return ((first * 16 + second) * 16 + third) * 16 + fourth;
    }

    public void backup(int howMany)
    {
        switch (howMany)
        {
            case 1:
                assert currentLength != (-1);
                input.backup(currentLength);
                currentLength = previousLength;
                previousLength = (-1);
                break;
            case 2:
                assert currentLength != (-1) && previousLength != (-1);
                input.backup(currentLength + previousLength);
                currentLength = previousLength = (-1);
                break;
            default:
                assert false : howMany;
        }
    }

    private Token<PTokenId> finishIdentifier(int c)
    {
        while (true)
        {
            if (c == EOF || !isIdentifierChar(c = translateSurrogates(c)))
            {
                // For surrogate 2 chars must be backed up
                backup((c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 2 : 1);
                return tokenFactory.createToken(PTokenId.IDENTIFIER);
            }
            c = nextChar();
        }
    }
    
    private boolean isIdentifierChar(int c) {
        return Character.isJavaIdentifierPart(c) || c == ':';
    }
    
    
    private boolean isVariableChar(int c) {
        return Character.isJavaIdentifierPart(c) || c == ':';
    }
    
    private Token<PTokenId> finishVariable(int c)
    {
        int lastC = c;
        while (true)
        {
            if (c == EOF || !isVariableChar(c = translateSurrogates(c)))
            {
                // For surrogate 2 chars must be backed up
                backup((c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 2 : 1);
                if (lastC == ':') {
                    backup(1);
                }
                return tokenFactory.createToken(PTokenId.VARIABLE);
            }
            lastC = c;
            c = nextChar();
        }
    }
    

    private Token<PTokenId> keywordOrIdentifier(PTokenId keywordId)
    {
        return keywordOrIdentifier(keywordId, nextChar());
    }

    private Token<PTokenId> keywordOrIdentifier(PTokenId keywordId, int c)
    {
        // Check whether the given char is non-ident and if so then return keyword
        if (c == EOF || !Character.isJavaIdentifierPart(c = translateSurrogates(c)))
        {
            // For surrogate 2 chars must be backed up
            backup((c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 2 : 1);
            return token(keywordId);
        } else // c is identifier part
        {
            return finishIdentifier();
        }
    }
    
    private Token<PTokenId> functionOrIdentifier(PTokenId functionid)
    {
        return functionOrIdentifier(functionid, nextChar());
    }
    

    private Token<PTokenId> functionOrIdentifier(PTokenId functionId, int c)
    {
        // Check whether the given char is non-ident and if so then return keyword
        if (c == EOF || !Character.isJavaIdentifierPart(c = translateSurrogates(c)))
        {
            int count = 0;
            int backupPoint = input.readLength();
            while (true)
            {
                // There should be no surrogates possible for whitespace
                // so do not call translateSurrogates()
                if (c == EOF || !Character.isWhitespace(c))
                {
                    if (c == '{' || c == '=') {
                        input.backup(input.readLength() - backupPoint);
                        return finishIdentifier();
                    }
                    break;
                }
                count = count + (c >= Character.MIN_SUPPLEMENTARY_CODE_POINT ? 2 : 1);
                c = nextChar();
            }
            
            input.backup(input.readLength() - backupPoint);
            return token(functionId);
        } else // c is identifier part
        {
            return finishIdentifier();
        }
    }
    
    private Token<PTokenId> textOperatorOrIdentifier(PTokenId textOpId)
    {
        return textOperatorOrIdentifier(textOpId, nextChar());
    }
    
    
    private Token<PTokenId> textOperatorOrIdentifier(PTokenId textOpId, int c)
    {
        // Check whether the given char is non-ident and if so then return keyword
        if (c == EOF || !Character.isJavaIdentifierPart(c = translateSurrogates(c)))
        {
            // For surrogate 2 chars must be backed up
            backup((c >= Character.MIN_SUPPLEMENTARY_CODE_POINT) ? 2 : 1);
            return token(textOpId);
        } else // c is identifier part
        {
            return finishIdentifier();
        }
    }
    
    
    private Token<PTokenId> finishWhitespace()
    {
        while (true)
        {
            int c = nextChar();
            // There should be no surrogates possible for whitespace
            // so do not call translateSurrogates()
            if (c == EOF || !Character.isWhitespace(c))
            {
                backup(1);
                return tokenFactory.createToken(PTokenId.WHITESPACE);
            }
        }
    }

    private Token<PTokenId> finishIdentifier()
    {
        return finishIdentifier(nextChar());
    }

    private int translateSurrogates(int c)
    {
        if (Character.isHighSurrogate((char) c))
        {
            int lowSurr = nextChar();
            if (lowSurr != EOF && Character.isLowSurrogate((char) lowSurr))
            {
                // c and lowSurr form the integer unicode char.
                c = Character.toCodePoint((char) c, (char) lowSurr);
            } else
            {
                // Otherwise it's error: Low surrogate does not follow the high one.
                // Leave the original character unchanged.
                // As the surrogates do not belong to any
                // specific unicode category the lexer should finally
                // categorize them as a lexical error.
                backup(1);
            }
        }
        return c;
    }

    private Token<PTokenId> token(PTokenId id)
    {
        String fixedText = id.fixedText();
        return (fixedText != null && fixedText.length() == input.readLength())
                ? tokenFactory.getFlyweightToken(id, fixedText)
                : tokenFactory.createToken(id);
    }
    
private Token<PTokenId> finishNumberLiteral(int c, boolean inFraction) {
        boolean afterDigit = true;
        while (true) {
            switch (c) {
//                case '.':
//                    if (!inFraction) {
//                        inFraction = true;
//                        afterDigit = false;
//                    } else { // two dots in the literal
//                        return token(PTokenId.FLOAT_LITERAL_INVALID);
//                    }
//                    break;
//                case 'l': case 'L': // 0l or 0L
//                    return token(PTokenId.LONG_LITERAL);
//                case 'd': case 'D':
//                    return token(PTokenId.DOUBLE_LITERAL);
//                case 'f': case 'F':
//                    return token(PTokenId.FLOAT_LITERAL);
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    afterDigit = true;
                    break;
//                case 'e': case 'E': // exponent part
//                    return finishFloatExponent();
//                case '_':
//                    if (this.version >= 7 && afterDigit) {
//                        int cc = nextChar();
//                        backup(1);
//                        if (cc >= '0' && cc <= '9' || cc == '_') {
//                            break;
//                        }
//                    }
                default:
                    backup(1);
                    return token(inFraction ? PTokenId.DOUBLE_LITERAL
                            : PTokenId.INT_LITERAL);
            }
            c = nextChar();
        }
    }   

    public void consumeNewline() {
        if (nextChar() != '\n') backup(1);
    }


    @Override
    public Object state()
    {
        return null;
    }

    @Override
    public void release()
    {
    }

    private Token<PTokenId> finishRegexp(int c) {
        boolean escaped = false;
        while (true) {
            c = nextChar();
            switch (c) {
                case EOF:
                    return null;
                case '\\' : 
                    escaped = true; 
                    break;
                case '/' : 
                    if (escaped) {
                        escaped = false;
                    } else {
                        return token(PTokenId.REGEXP_LITERAL);
                    }
                    break;
                default:
                    escaped = false;
            }
        }
    }

}
