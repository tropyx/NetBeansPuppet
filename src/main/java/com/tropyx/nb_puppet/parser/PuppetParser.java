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
package com.tropyx.nb_puppet.parser;

import com.tropyx.nb_puppet.lexer.PTokenId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeListener;
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
        System.out.println("text" + snapshot.getText().toString());
        result = doParse(snapshot, task);
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
        final PElement root = new PElement(PElement.ROOT, null);
        Token<PTokenId> token = nextSkipWhitespace(ts);
        while (token != null && ts.isValid()) {
            if (token.id() == PTokenId.CLASS) {
                parseClass(root, ts);
            } 
            else if (token.id() == PTokenId.NODE) {
                //http://docs.puppetlabs.com/puppet/4.2/reference/lang_node_definitions.html
            } else if (token.id() == PTokenId.DEFINE) {
                //http://docs.puppetlabs.com/puppet/4.2/reference/lang_defined_types.html
            }
            token = nextSkipWhitespace(ts);
        }

        return new PuppetParserResult(snapshot, root);
    }

    private Token<PTokenId> skipWhitespace(TokenSequence<PTokenId> ts) {
        while (ts.token() != null && ts.token().id() == PTokenId.WHITESPACE)
        {
            if (!ts.moveNext()) {
                return null;
            }
        }
        return ts.token();
    }

    private Token<PTokenId> nextSkipWhitespace(TokenSequence<PTokenId> ts) {
        if (!ts.moveNext()) {
            return null;
        }
        return skipWhitespace(ts);
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

    private String stripString(String toString) {
        return toString.substring(1, toString.length() - 1);
    }

    //http://docs.puppetlabs.com/puppet/4.2/reference/lang_classes.html
    private void parseClass(PElement root, TokenSequence<PTokenId> ts) {
        PClass pc = new PClass(root);
        Token<PTokenId> token;
        if (null == nextSkipWhitespace(ts)) {
            return;
        }
        String name = collectText(ts, PTokenId.WHITESPACE, PTokenId.LBRACE, PTokenId.LPAREN);
        if (name != null) {
            pc.setName(name);
            token = skipWhitespace(ts);
            if (token != null && token.id() == PTokenId.LPAREN) {
                //params
                parseClassParams(pc, ts);
                token = nextSkipWhitespace(ts);
            }
            if (token != null && token.id() == PTokenId.INHERITS) {
                //inherits
                nextSkipWhitespace(ts);
                String inherit = collectText(ts, PTokenId.WHITESPACE, PTokenId.LBRACE);
                if (inherit != null) {
                    PClassRef ref = new PClassRef(pc);
                    ref.setName(inherit);
                    pc.setInherits(ref);
                    token = nextSkipWhitespace(ts);
                } else {
                    token = null;
                }
            }
            if (token != null && token.id() == PTokenId.LBRACE) {
                //we are done for class
                //internals or skip to RBRACE
            }
        }
    }

    private void parseClassParams(PClass pc, TokenSequence<PTokenId> ts) {
        Token<PTokenId> token = nextSkipWhitespace(ts);
        String type = null;
        String var = null;
        String def = null;
        List<PClassParam> params = new ArrayList<>();
        while (token != null && token.id() != PTokenId.RPAREN) {
            if (type == null && token.id() == PTokenId.IDENTIFIER) {
                type = token.text().toString();
            }
            if (var == null && token.id() == PTokenId.VARIABLE) {
                var = token.text().toString();
                type = type != null ? type : "Any";
            }
            if (token.id() == PTokenId.EQUALS) {
            }
            if (token.id() == PTokenId.COMMA) {
                assert var != null && type != null;
                PClassParam param = new PClassParam(null, var);
                param.setTypeType(type);
                params.add(param);
                type = null;
                var = null;
            }
            //TODO default values
            token = nextSkipWhitespace(ts);
        }
        if (var != null) {
            assert type != null;
            PClassParam param = new PClassParam(null, var);
            param.setTypeType(type);
            params.add(param);
        }
        pc.setParams(params.toArray(new PClassParam[0]));
    }

}
