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

import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;

/**
 *
 * @author mkleint
 */
public class PuppetParserTest extends NbTestCase {

    public PuppetParserTest(String name) {
        super(name);
    }

    /**
     * Test of parse method, of class PuppetParser.
     */
//    @Test
    public void testParse() throws Exception {
        Document doc = new PlainDocument();
        doc.putProperty("mimeType", "text/x-puppet-manifest");
        doc.insertString(0, "file { 'title' : p1 => 'v'}", null);
        PuppetParserResult result = doParse(doc);
        result.getRootNode();
    }

    public PuppetParserResult doParse(Document doc) throws ParseException {
        Snapshot snap = Source.create(doc).createSnapshot();
        PuppetParser pp = new PuppetParser();
        UserTask ut = new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
            }
        };
        pp.parse(snap, ut, null);
        return (PuppetParserResult) pp.getResult(ut);
    }

    
}
