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
package com.tropyx.nb_puppet.parser;

import com.tropyx.nb_puppet.lexer.PTokenId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;

class PuppetParser extends Parser {

    private final AtomicBoolean cancelled = new AtomicBoolean();

    private PuppetParserResult result;

    public PuppetParser() {
    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
//        System.out.println("text" + snapshot.getText().toString());
        result = doParse(snapshot, task);
        System.out.println("AST:" + result.getRootNode().toStringRecursive());
    }

    @Override
    public Result getResult(Task task) throws ParseException {
        return result;
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void cancel(CancelReason reason, SourceModificationEvent event) {
        cancelled.set(true);
    }

    @Override
    public void cancel() {
        cancelled.set(true);
    }

    private PuppetParserResult doParse(Snapshot snapshot, Task task) {
        TokenSequence<PTokenId> ts = (TokenSequence<PTokenId>) snapshot.getTokenHierarchy().tokenSequence();
        ts.moveStart();
        final PElement root = new PElement(PElement.ROOT, null, 0 );
        Token<PTokenId> token = nextSkipWhitespaceComment(ts);
        while (token != null && ts.isValid()) {
            if (token.id() == PTokenId.CLASS) {
                parseClass(root, ts);
            } 
            else if (token.id() == PTokenId.NODE) {
                parseNode(root, ts);
            } else if (token.id() == PTokenId.DEFINE) {
                parseDefine(root, ts);
            }
            token = nextSkipWhitespaceComment(ts);
        }

        return new PuppetParserResult(snapshot, root);
    }

    private Token<PTokenId> skipWhitespaceComment(TokenSequence<PTokenId> ts) {
        while (ts.token() != null && (ts.token().id() == PTokenId.WHITESPACE || ts.token().id() == PTokenId.COMMENT))
        {
            if (!ts.moveNext()) {
                return null;
            }
        }
        return ts.token();
    }

    private Token<PTokenId> nextSkipWhitespaceComment(TokenSequence<PTokenId> ts) {
        if (!ts.moveNext()) {
            return null;
        }
        return skipWhitespaceComment(ts);
    }

    private String collectText(TokenSequence<PTokenId> ts, PTokenId... stopTokens) {
        StringBuilder name = new StringBuilder();
        Token<PTokenId> token = ts.token();
        List<PTokenId> stops = Arrays.asList(stopTokens);
        while (token != null && !stops.contains(token.id())) {
            name.append(token.text().toString());
            ts.moveNext();
            token = ts.token();
        }
        if (token == null) {
            return null;
        }
        return name.toString();
    }

    private PBlob fastForward(@NullAllowed PElement parent, TokenSequence<PTokenId> ts, PTokenId... stopTokens) {
        PBlob blob = new PBlob(parent, ts.offset());
        return fastForwardImpl(blob, ts, stopTokens);
    }

    private PBlob fastForwardImpl(@NonNull PBlob blob, TokenSequence<PTokenId> ts, PTokenId... stopTokens) {
        Token<PTokenId> token = ts.token();
        List<PTokenId> stops = Arrays.asList(stopTokens);
        int braceCount = 0;
        int bracketCount = 0;
        int parenCount = 0;
        boolean ignore = false;
        int len = 0;

        while (token != null && (ignore || !stops.contains(token.id()))) {
            len = len + token.length();
            if (null != token.id()) switch (token.id()) {
                case LBRACE:
                    braceCount++;
                    break;
                case RBRACE:
                    braceCount--;
                    break;
                case LBRACKET:
                    bracketCount++;
                    break;
                case RBRACKET:
                    bracketCount--;
                    break;
                case LPAREN:
                    parenCount++;
                    break;
                case RPAREN:
                    parenCount--;
                    break;
                case STRING_LITERAL:
                    String val = token.text().toString();
                    int off = ts.offset();
                    new PString(blob, off, val);
                    break;
                case VARIABLE:
                    val = token.text().toString();
                    off = ts.offset();
                    token = nextSkipWhitespaceComment(ts);
                    if (token != null && token.id() == PTokenId.EQUALS) {
                        //variable definition;
                        new PVariableDefinition(blob, off, val);
                    } else if (token != null) {
                        //variable usage
                        new PVariable(blob, off, val);
                        continue;
                    }
                    break;
                case INCLUDE:
                    token = nextSkipWhitespaceComment(ts);
                    if (token != null && token.id() == PTokenId.IDENTIFIER) {
                        new PClassRef(blob, ts.offset(), token.text().toString());
                    } else {
                        continue;
                    }
                    break;
                case REQUIRE:
                    token = nextSkipWhitespaceComment(ts);
                    if (token != null && token.id() == PTokenId.IDENTIFIER) {
                        new PClassRef(blob, ts.offset(), token.text().toString());
                    } else {
                        continue;
                    }
                    break;
                case IDENTIFIER:
                case CLASS:
                    if (bracketCount == 0 && parenCount == 0) {
                        val = token.text().toString();
                        boolean isClass = token.id() == PTokenId.CLASS;
                        off = ts.offset();
                        token = nextSkipWhitespaceComment(ts);
                        if (token != null && token.id() == PTokenId.LBRACE) {
                            parseResource(blob, val, ts, off);
                        } else if (isClass && token != null && token.id() == PTokenId.IDENTIFIER) {
                            String name = token.text().toString();
                            nextSkipWhitespaceComment(ts);
                            parseClassInternal(new PClass(blob, off), name, ts);
                        } else if (token != null && token.id() == PTokenId.LBRACKET && Character.isUpperCase(val.charAt(0))) {
    //                    parseReference(pc, val);
                            continue; //for now, to properly eat LBRACKET
                        } else {
                            continue;
                        }
                    }
                    break;
                case CASE:
                    parseCase(blob, ts);
                    break;
                default:
            }

            token = nextSkipWhitespaceComment(ts);
            ignore = bracketCount > 0 || braceCount > 0 || parenCount > 0;
        }
        blob.setLength(len);
        return blob;
    }

    //https://docs.puppetlabs.com/puppet/latest/reference/lang_defined_types.html
    private void parseDefine(PElement root, TokenSequence<PTokenId> ts) {
        PDefine pc = new PDefine(root, ts.offset());
        Token<PTokenId> token;
        if (null == nextSkipWhitespaceComment(ts)) {
            return;
        }
        String name = collectText(ts, PTokenId.WHITESPACE, PTokenId.LBRACE, PTokenId.LPAREN);
        if (name != null) {
            pc.setName(name);
            token = skipWhitespaceComment(ts);
            if (token != null && token.id() == PTokenId.LPAREN) {
                //params
                parseParams(pc, ts);
                token = nextSkipWhitespaceComment(ts);
            }
            if (token != null && token.id() == PTokenId.LBRACE) {
                //we are done for define
                //internals or skip to RBRACE
                ts.moveNext();
                fastForward(pc, ts, PTokenId.RBRACE);
            }
        }
    }

    //http://docs.puppetlabs.com/puppet/4.2/reference/lang_node_definitions.html
    private void parseNode(PElement root, TokenSequence<PTokenId> ts) {
        PNode pc = new PNode(root, ts.offset());
        if (null == nextSkipWhitespaceComment(ts)) {
            return;
        }
        List<String> names = new ArrayList<>();
        Token<PTokenId> token = ts.token();
        while (token != null && PTokenId.LBRACE != token.id()) {
            if (PTokenId.COMMA != token.id()) {
                String name = token.text().toString();
                if (name != null) {
                    names.add(name);
                }
            }
            token = nextSkipWhitespaceComment(ts);
        }
        pc.setNames(names.toArray(new String[0]));
        if (token != null && token.id() == PTokenId.LBRACE) {
            //we are done for node
            //internals or skip to RBRACE
            ts.moveNext();
            fastForward(pc, ts, PTokenId.RBRACE);
        }
    }

    //http://docs.puppetlabs.com/puppet/4.2/reference/lang_classes.html
    private void parseClass(PElement root, TokenSequence<PTokenId> ts) {
        int offset = ts.offset();
        if (null == nextSkipWhitespaceComment(ts)) {
            return;
        }
        String name = collectText(ts, PTokenId.WHITESPACE, PTokenId.LBRACE, PTokenId.LPAREN);
        if (name != null) {
            PClass pc = new PClass(root, offset);
            parseClassInternal(pc, name, ts);
        }
    }
    private void parseClassInternal(PClass pc, String name, TokenSequence<PTokenId> ts) {
        Token<PTokenId> token;
        pc.setName(name);
        token = skipWhitespaceComment(ts);
        if (token != null && token.id() == PTokenId.LPAREN) {
            //params
            parseParams(pc, ts);
            token = nextSkipWhitespaceComment(ts);
        }
        if (token != null && token.id() == PTokenId.INHERITS) {
            //inherits
            nextSkipWhitespaceComment(ts);
            int off = ts.offset();
            String inherit = collectText(ts, PTokenId.WHITESPACE, PTokenId.LBRACE);
            if (inherit != null) {
                pc.setInherits(new PClassRef(pc, off, inherit));
                token = nextSkipWhitespaceComment(ts);
            } else {
                token = null;
            }
        }
        if (token != null && token.id() == PTokenId.LBRACE) {
            //we are done for class
            //internals or skip to RBRACE
            ts.moveNext();
            fastForward(pc, ts, PTokenId.RBRACE);
        }
    }

    private void parseParams(PParamContainer pc, TokenSequence<PTokenId> ts) {
        Token<PTokenId> token = nextSkipWhitespaceComment(ts);
        String type = null;
        int offset = 0;
        String var = null;
        PElement def = null;
        List<PClassParam> params = new ArrayList<>();
        while (token != null && token.id() != PTokenId.RPAREN) {
            if (type == null && token.id() == PTokenId.IDENTIFIER) {
                type = token.text().toString();
                offset = ts.offset();
            }
            if (var == null && token.id() == PTokenId.VARIABLE) {
                var = token.text().toString();
                type = type != null ? type : "Any";
                offset = offset != 0 ? offset : ts.offset();
            }
            if (token.id() == PTokenId.EQUALS) {
                def = fastForward(null, ts, PTokenId.RPAREN, PTokenId.COMMA);
                token = ts.token();
                if (token.id() == PTokenId.RPAREN) {
                    break;
                }
            }
            if (token.id() == PTokenId.COMMA) {
                assert var != null && type != null : "var:" + var + " type:" + type + " for pc:" + pc.toString();
                PClassParam param = new PClassParam((PElement)pc, offset, var);
                param.setTypeType(type);
                if (def != null) {
                    def.setParent(param);
                    param.setDefaultValue(def);
                }
                params.add(param);
                type = null;
                var = null;
                def = null;
                offset = 0;
            }
            //TODO default values
            token = nextSkipWhitespaceComment(ts);
        }
        if (var != null) {
            assert type != null;
            PClassParam param = new PClassParam((PElement)pc, offset, var);
            param.setTypeType(type);
            if (def != null) {
                def.setParent(param);
                param.setDefaultValue(def);
            }
            params.add(param);
        }
        pc.setParams(params.toArray(new PClassParam[0]));
    }

    private void parseResource(PElement pc, String type, TokenSequence<PTokenId> ts, int resOff) {
        if (Character.isUpperCase(type.charAt(0))) {
            PResource resource = new PResource(pc, resOff, type);
            parseResourceAttrs(resource, ts);
        } else {
            Token<PTokenId> token = nextSkipWhitespaceComment(ts);
            if (token != null) {
                PElement title;
                if (token.id() == PTokenId.STRING_LITERAL) {
                    title = new PString(null, ts.offset(), token.text().toString());
                } else if (token.id() == PTokenId.VARIABLE) {
                    title = new PVariable(null, ts.offset(), token.text().toString());
                } else if (token.id() == PTokenId.LBRACKET) {
                    PBlob blob = new PBlob(null, ts.offset());
                    //current token in LBRACKET which we need to skip here, to actually bump into the right RBRACKET
                    ts.moveNext();
                    title = fastForwardImpl(blob, ts, PTokenId.RBRACKET);
                } else if (token.id() == PTokenId.IDENTIFIER) {
                    title = new PString(null, ts.offset(), token.text().toString()); //TODO not real string or unquoted string
                } else {
                    throw new IllegalStateException("token:" + token.text().toString() + " of type:" + token.id() + " in " + ts.toString());
                }
                token = nextSkipWhitespaceComment(ts);
                if (token != null && token.id() == PTokenId.COLON) {
                    PResource resource = new PResource(pc, resOff, type);
                    title.setParent(resource);
                    resource.setTitle(title);
                    parseResourceAttrs(resource, ts);
                }
            }
        }
    }

    private void parseResourceAttrs(PResource resource, TokenSequence<PTokenId> ts) {
        Token<PTokenId> token = nextSkipWhitespaceComment(ts);
        String attr = null;
        PElement val = null;
        int off = 0;
        while (token != null && token.id() != PTokenId.RBRACE) {
            if (attr == null && (token.id() == PTokenId.IDENTIFIER || token.id() == PTokenId.UNLESS)) {
                off = ts.offset();
                attr = token.text().toString();
            }
            if (token.id() == PTokenId.PARAM_ASSIGN) {
                nextSkipWhitespaceComment(ts);
                val = fastForward(null, ts, PTokenId.COMMA, PTokenId.RBRACE);
                token = ts.token();
                continue;
            }
            if (token.id() == PTokenId.COMMA) {
                assert attr != null && val != null : "attr:" + attr + " val:" + val + " in resource:" + resource.toString();
                PResourceAttribute param = new PResourceAttribute(resource, off, attr);
                val.setParent(param);
                param.setValue(val);
                resource.addAttribute(param);
                attr = null;
                val = null;
                off = 0;
            }
            token = nextSkipWhitespaceComment(ts);
        }
        if (attr != null) {
            assert val != null;
            PResourceAttribute param = new PResourceAttribute(resource, off, attr);
            param.setValue(val);
            resource.addAttribute(param);
        }
    }

    private void parseCase(PElement parent, TokenSequence<PTokenId> ts) {
        PCase pcase = new PCase(parent, ts.offset());
        nextSkipWhitespaceComment(ts);
        PBlob caseExpr = fastForward(pcase, ts, PTokenId.LBRACE);
        pcase.setControl(caseExpr);
        Token<PTokenId> token = ts.token();
        nextSkipWhitespaceComment(ts);
        while (token.id() != PTokenId.RBRACE) {
            PBlob cas = fastForward(pcase, ts, PTokenId.COLON);
            nextSkipWhitespaceComment(ts);
            token = ts.token();
            PBlob caseBody;
            if (token.id() == PTokenId.LBRACE) {
                nextSkipWhitespaceComment(ts);
                caseBody = fastForward(pcase, ts, PTokenId.RBRACE);
                pcase.addCase(cas, caseBody);
            } else {
                //huh? what to do here?
//                caseBody = fastForward(pcase, ts, PTokenId.RBRACE);
            }
            nextSkipWhitespaceComment(ts);
            token = ts.token();
        }
    }

}
