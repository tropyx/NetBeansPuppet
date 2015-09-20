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

import java.util.List;
import javax.swing.text.BadLocationException;
import org.junit.Test;
import org.netbeans.editor.BaseDocument;
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

    @Test
    public void testSimpleClassParse() throws Exception {
        PuppetParserResult result = doParse("class aaa { }");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa", c.getName());
    }

    @Test
    public void testSimpleClassParse2() throws Exception {
        PuppetParserResult result = doParse("class aaa::param { }");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa::param", c.getName());
    }

    @Test
    public void testInheritedClassParse() throws Exception {
        PuppetParserResult result = doParse("class aaa inherits aaa::params { }");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa", c.getName());
        assertEquals("aaa::params", c.getInherits().getName());
    }

    @Test
    public void testInheritedClassWithEmptyParamsParse() throws Exception {
        PuppetParserResult result = doParse("class aaa() inherits aaa::params { }");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa", c.getName());
        assertEquals("aaa::params", c.getInherits().getName());
    }

    @Test
    public void testClassWithSingleParamParse() throws Exception {
        PuppetParserResult result = doParse("class aaa ( $bb = '' ) { }");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa", c.getName());
        assertNotNull(c.getParams());
        assertEquals(1, c.getParams().length);
        PClassParam p = c.getParams()[0];
        assertEquals("$bb", p.getVariable().getName());
        assertEquals("Any", p.getTypeType());
    }

    @Test
    public void testClassWithMultiParamsParse() throws Exception {
        PuppetParserResult result = doParse("class aaa ( $bb = '',Regexp $cc = /aaa/, $dd=$aa::aa,) { }");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa", c.getName());
        assertNotNull(c.getParams());
        assertEquals(3, c.getParams().length);
        PClassParam p = c.getParams()[0];
        assertEquals("$bb", p.getVariable().getName());
        assertEquals("Any", p.getTypeType());
        p = c.getParams()[1];
        assertEquals("$cc", p.getVariable().getName());
        assertEquals("Regexp", p.getTypeType());
        p = c.getParams()[2];
        assertEquals("$dd", p.getVariable().getName());
        assertEquals("Any", p.getTypeType());
    }

    private PClass assertAndGetClassElement(PuppetParserResult result) {
        PElement nd = result.getRootNode();
        assertNotNull(nd);
        List<PElement> children = nd.getChildren();
        assertNotNull(children);
        assertEquals(1, children.size());
        PElement ch = children.get(0);
        assertEquals(PElement.CLASS, ch.getType());
        PClass c = (PClass)ch;
        return c;
    }

    private PuppetParserResult doParse(String string) throws ParseException, BadLocationException {
        BaseDocument bd = new BaseDocument(false, "text/x-puppet-manifest");
        bd.insertString(0, string, null);

        Snapshot snap = Source.create(bd).createSnapshot();
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
