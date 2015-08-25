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
    
    
    CASE("case", "keyword"),
    CLASS("class", "keyword"),
    DEFAULT("default", "keyword"),
    DEFINE("define", "keyword"),
    ELSE("else", "keyword"),
    ELSIF("elsif", "keyword"),
    //false
    IF("if", "keyword"),
    IMPORT("import", "keyword"),
    INHERITS("inherits", "keyword"),
    NODE("node", "keyword"),
    //true
    UNDEF("undef", "keyword"),
    UNLESS("unless", "keyword"),
    
    AND("and", "operator"),
    OR("or", "operator"),
    IN("in", "operator"),
    QUESTIONMARK("?", "operator"), //really operator?
    

    
    OPERATOR(null, "operator"),
//    INCLUDE("include", "keyword"),
//    REQUIRE("require", "keyword"),
    
    ALERT("alert", "method-declaration"), //*
    COLLECT("collect", "method-declaration"),//*
    CONTAIN("contain", "method-declaration"),//*
    CREATE_RESOURCES("create_resources", "method-declaration"),//*
    CRIT("crit", "method-declaration"),//*
    DEBUG("debug", "method-declaration"), //*
    DEFINED("defined", "method-declaration"),
    EACH("each", "method-declaration"), //*
    EMERG("emerg", "method-declaration"), //*
    EPP("epp", "method-declaration"), //*
    ERR("err", "method-declaration"), //*
    EXTLOOKUP("extlookup", "method-declaration"),//*
    FAIL("fail", "method-declaration"), //*
    FILE("file", "method-declaration"),//*
    FILTER("filter", "method-declaration"),//*
    FQDN_RAND("fqdn_rand", "method-declaration"), //*
    GENERATE("generate", "method-declaration"),
    HIERA("hiera", "method-declaration"),//*
    HIERA_ARRAY("hiera_array", "method-declaration"),//*
    HIERA_HASH("hiera_hash", "method-declaration"),//*
    HIERA_INCLUDE("hiera_include", "method-declaration"),//*
    INCLUDE("include", "method-declaration"), //*
    INFO("info", "method-declaration"),
    INLINE_EPP("inline_epp", "method-declaration"),
    INLINE_TEMPLATE("inline_template", "method-declaration"),
    LOOKUP("lookup", "method-declaration"), //*
    MAP("map", "method-declaration"), //*
    MD5("md5", "method-declaration"), //*
    NOTICE("notice", "method-declaration"), //*
    REALIZE("realize", "method-declaration"),//*
    REDUCE("reduce", "method-declaration"),//*
    REQSUBST("regsubst", "method-declaration"),
    REQUIRE("require", "method-declaration"), //*
    SEARCH("search", "method-declaration"),//*
    SELECT("select", "method-declaration"),//*
    SHA1("sha1", "method-declaration"),//*
    SHELLQUOTE("shellquote", "method-declaration"),//*
    SLICE("slice", "method-declaration"),//*
    SPLIT("split", "method-declaration"),//*
    SPRINTF("sprintf", "method-declaration"),//*
    TAG("tag", "method-declaration"), //*
    TAGGED("tagged", "method-declaration"),
    TEMPLATE("template", "method-declaration"), //*
    VERSIONCMP("versioncmp", "method-declaration"),//*
    WARNING("warning", "method-declaration"), //*
    
    COMMENT(null, "comment"),
    LINE_COMMENT(null, "comment"),
    WHITESPACE(null, "whitespace"),
    STRING_LITERAL(null, "string"),
    REGEXP_LITERAL(null, "regexp"),
    VARIABLE(null, "variable-declaration"),
    
    /** ( **/
    LPAREN("(", "separator"),
    /** ) **/
    RPAREN(")", "separator"),
    /** { **/
    LBRACE("{", "separator"),
    /** } **/
    RBRACE("}", "separator"),
    /** [ **/
    LBRACKET("[", "separator"),
    /** ] **/
    RBRACKET("]", "separator"),
    /** , **/
    COMMA(",", "separator"), 
    /** : **/
    COLON(":", "separator"),
    EQUALS("=", "operator"),
    /** => */
    PARAM_ASSIGN("=>", "operator"),
    /** -> */
    ORDER_ARROW("->", "operator"),
    /** ~> */
    NOTIF_ARROW("~>", "operator"),
    /** <| */
    LCOLLECTOR("<|", "operator"),
    /** |> */
    RCOLLECTOR("|>", "operator"),
    /** <<| */
    LEXPORTCOLLECTOR("<<|", "operator"),
    /** |>> */
    REXPORTCOLLECTOR("|>>", "operator");
    
    
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
}
