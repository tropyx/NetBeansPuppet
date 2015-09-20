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
        ts.moveNext();
        Token<PTokenId> token = ts.token();
        // when it's not a value -> do nothing.
        if (token == null) {

            return null;
        }
        final PNode root = new PNode(PNode.ROOT, null);
        if (token.id() == PTokenId.IDENTIFIER || token.id() == PTokenId.FILE) {
            String identifier = token.text().toString();
            token = nextSkipWhitespace(ts);
            if (token != null) {
                if (token.id() == PTokenId.LBRACE) {
                    //resource?
                    processResource(ts, root, identifier);
                }
            } else {
                //just identifier
            }
        }
        return new PuppetParserResult(snapshot, root);
    }

    private Token<PTokenId> nextSkipWhitespace(TokenSequence<PTokenId> ts) {
        ts.moveNext();
        while (ts.token() != null && ts.token().id() == PTokenId.WHITESPACE)
        {
            ts.moveNext();
        }
        return ts.token();
    }

    private void processResource(TokenSequence<PTokenId> ts, PNode root, String resourceIdentifier) {
        ResourcePNode nd = new ResourcePNode(root);
        nd.setResourceType(resourceIdentifier);
        parseResourceTitle(nextSkipWhitespace(ts), ts, nd);
        while (ts.token().id() != PTokenId.RBRACE ) {
            parseResourceParam(nextSkipWhitespace(ts), ts, nd);
        }
    }

    private String stripString(String toString) {
        return toString.substring(1, toString.length() - 1);
    }

    private void parseResourceParam(Token<PTokenId> next, TokenSequence<PTokenId> ts, ResourcePNode nd) {
        String paramName = null;
        while (ts.token().id() != PTokenId.RBRACE || ts.token().id() != PTokenId.COMMA) {
            if (paramName == null && ts.token().id() == PTokenId.IDENTIFIER) {
                
            }
        }        
    }

    private void parseResourceTitle(Token<PTokenId> nextSkipWhitespace, TokenSequence<PTokenId> ts, ResourcePNode nd) {
    }

}
