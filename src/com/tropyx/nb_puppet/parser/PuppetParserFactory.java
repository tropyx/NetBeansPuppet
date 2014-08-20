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

import java.util.Collection;
import java.util.Collections;
import javax.swing.text.Document;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;

public class PuppetParserFactory extends ParserFactory {

    public PuppetParserFactory() {
        
    }
    
    @Override
    public Parser createParser(Collection<Snapshot> clctn) {
        return new PuppetParser();
    }

    public static PNode parse(Document sourceFo) throws ParseException {
        Source source = Source.create(sourceFo);
        final PNode[] root = new PNode[1];
        UserTask task = new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                root[0] = ((PuppetParserResult)resultIterator.getParserResult()).getRootNode();
            }
        };
        ParserManager.parse(Collections.singleton(source), task);
        return root[0];
    }

}
