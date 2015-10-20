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

import org.netbeans.api.lexer.TokenId;

/**
 *
 * @author mkleint
 */
public enum PTokenId implements TokenId
{
    ERROR(null, "error"),
    IDENTIFIER(null, "identifier"),
    INT_LITERAL(null, "number"),
    LONG_LITERAL(null, "number"),
    FLOAT_LITERAL(null, "number"),
    DOUBLE_LITERAL(null, "number"),
    CHAR_LITERAL(null, "character"),
    
    
    CASE("case", Category.KEYWORD),
    CLASS("class", Category.KEYWORD),
    DEFAULT("default", Category.KEYWORD),
    DEFINE("define", Category.KEYWORD),
    ELSE("else", Category.KEYWORD),
    ELSIF("elsif", Category.KEYWORD),
    FALSE("false", Category.KEYWORD), //not sure if truly keyword, makes life easier on parsing
    IF("if", Category.KEYWORD),
    IMPORT("import", Category.KEYWORD),
    INHERITS("inherits", Category.KEYWORD),
    NODE("node", Category.KEYWORD),
    NIL("nil", Category.KEYWORD), //not sure if truly keyword, makes life easier on parsing
    TRUE("true", Category.KEYWORD),//not sure if truly keyword, makes life easier on parsing
    UNDEF("undef", Category.KEYWORD),
    UNLESS("unless", Category.KEYWORD),

    //not sure if truly keyword, makes life easier on parsing
    PRESENT("present", Category.KEYWORD),
    ABSENT("absent", Category.KEYWORD),
    
    AND("and", Category.OPERATOR),
    OR("or", Category.OPERATOR),
    IN("in", Category.OPERATOR),
    QUESTIONMARK("?", Category.OPERATOR), //really operator?
    
    OPERATOR(null, Category.OPERATOR),
    
    //built-in functions that can be used without ()
    CONTAIN("contain", Category.FUNCTION),//*
    DEBUG("debug", Category.FUNCTION), //*
    ERR("err", Category.FUNCTION), //*
    FAIL("fail", Category.FUNCTION), //*
    INFO("info", Category.FUNCTION),
    INCLUDE("include", Category.FUNCTION), //*
    NOTICE("notice", Category.FUNCTION), //*
    REALIZE("realize", Category.FUNCTION),//*
    REQUIRE("require", Category.FUNCTION), //*
    TAG("tag", Category.FUNCTION), //*
    WARNING("warning", Category.FUNCTION), //*
    
    COMMENT(null, "comment"),
    LINE_COMMENT(null, "comment"),
    WHITESPACE(null, "whitespace"),
    STRING_LITERAL(null, "string"),
    REGEXP_LITERAL(null, "regexp"),
    VARIABLE(null, "variable-declaration"),
    
    /** ( **/
    LPAREN("(", Category.SEPARATOR),
    /** ) **/
    RPAREN(")", Category.SEPARATOR),
    /** { **/
    LBRACE("{", Category.SEPARATOR),
    /** } **/
    RBRACE("}", Category.SEPARATOR),
    /** [ **/
    LBRACKET("[", Category.SEPARATOR),
    /** ] **/
    RBRACKET("]", Category.SEPARATOR),
    /** , **/
    COMMA(",", Category.SEPARATOR),
    /** : **/
    COLON(":", Category.SEPARATOR),
    /** ; **/
    SEMICOLON(";", Category.SEPARATOR),
    /** | */
    PIPE("|", Category.SEPARATOR),
    /** = **/
    EQUALS("=", Category.OPERATOR),
    /** => */
    PARAM_ASSIGN("=>", Category.OPERATOR),
    /** -> */
    ORDER_ARROW("->", Category.OPERATOR),
    /** ~> */
    NOTIF_ARROW("~>", Category.OPERATOR),
    /** &lt;| */
    LCOLLECTOR("<|", Category.OPERATOR),
    /** |> */
    RCOLLECTOR("|>", Category.OPERATOR),
    /** &lt;&lt;| */
    LEXPORTCOLLECTOR("<<|", Category.OPERATOR),
    /** |>> */
    REXPORTCOLLECTOR("|>>", Category.OPERATOR),
    /** . */
    DOT(".", Category.OPERATOR);
    
    private final String name;
    private final String category;

    PTokenId(
            String name, String category
    )
    {
        this.name = name;
        this.category = category;
    }
    
    @Override
    public String primaryCategory()
    {
        return category;
    }

    String fixedText()
    {
        return name;
    }

    public interface Category {
        public static final String KEYWORD = "keyword";
        public static final String SEPARATOR = "separator";
        public static final String OPERATOR = "operator";
        public static final String FUNCTION = "method-declaration";
    }
}
