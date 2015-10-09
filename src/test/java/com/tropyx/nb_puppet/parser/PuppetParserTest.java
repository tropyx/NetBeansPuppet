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
        assertEquals("$bb", p.getVariable());
        assertEquals("Any", p.getTypeType());
    }

    @Test
    public void testClassParamsRandomThis() throws Exception {
        PuppetParserResult result = doParse("class java::defaultversion($version = $java::params::recommended_version){ $bbb = $ccc }");
        PClass c = assertAndGetClassElement(result);
        assertEquals("java::defaultversion", c.getName());
        assertNotNull(c.getParams());
        assertEquals(1, c.getParams().length);
        PClassParam p = c.getParams()[0];
        assertEquals("$version", p.getVariable());
    }

    @Test
    public void testClassWithMultiParamsParse() throws Exception {
        PuppetParserResult result = doParse("class aaa ( $bb = '',Regexp $cc = /aaa/, $dd=$aa::aa,) { }");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa", c.getName());
        assertNotNull(c.getParams());
        assertEquals(3, c.getParams().length);
        PClassParam p = c.getParams()[0];
        assertEquals("$bb", p.getVariable());
        assertEquals("Any", p.getTypeType());
        p = c.getParams()[1];
        assertEquals("$cc", p.getVariable());
        assertEquals("Regexp", p.getTypeType());
        p = c.getParams()[2];
        assertEquals("$dd", p.getVariable());
        assertEquals("Any", p.getTypeType());
    }

    @Test
    public void testClassWithBlobParamParse() throws Exception {
        PuppetParserResult result = doParse("class aaa ( $bb = { aa => 'aa', cc => 'cc' }, $dd = 'dd,' ) { }");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa", c.getName());
        assertNotNull(c.getParams());
        assertEquals(2, c.getParams().length);
        PClassParam p = c.getParams()[0];
        assertEquals("$bb", p.getVariable());
        assertEquals("Any", p.getTypeType());
        p = c.getParams()[1];
        assertEquals("$dd", p.getVariable());
        assertEquals("Any", p.getTypeType());
    }


    @Test
    public void testClassIncludeParse() throws Exception {
        PuppetParserResult result = doParse(
                "class aaa::param { "
             +  " include bbb::param"
             + " }");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa::param", c.getName());
        final List<PClassRef> refs = c.getChildrenOfType(PClassRef.class, true);
        assertEquals(1, refs.size());
        assertEquals("bbb::param", refs.get(0).getName());
    }

    @Test
    public void testClassIncludesParse() throws Exception {
        PuppetParserResult result = doParse(
                "class aaa::param { "
             +  " include bbb::param"
             +  " include ccc::param"
             + " }");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa::param", c.getName());
        final List<PClassRef> refs = c.getChildrenOfType(PClassRef.class, true);
        assertEquals(2, refs.size());
        assertEquals("bbb::param", refs.get(0).getName());
        assertEquals("ccc::param", refs.get(1).getName());
    }

    @Test
    public void testClassBracingParse() throws Exception {
        PuppetParserResult result = doParse(
                "class aaa::param { "
             +  " {  } "
             +  " { { } }"
             + " }"
             + "   include bbb:param");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa::param", c.getName());
    }

    @Test
    public void testSimpleResourceParse() throws Exception {
        PuppetParserResult result = doParse(
                "class aaa::install { "
             +  " file { \"fff\":"
              + " ensure => present, "
             +  " path => \'aaaa\',"
             + " }"
             + " }");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa::install", c.getName());
        PResource res = c.getChildrenOfType(PResource.class, true).get(0);
        assertEquals("file", res.getResourceType());
        assertNotNull(res.getTitle());
        assertEquals(PString.STRING, res.getTitle().getType());
        assertEquals(2, res.getAtributes().size());
        assertEquals("ensure", res.getAtributes().get(0).getName());
//        assertEquals("present", res.getAtributes().get(0).getValue());
    }

    @Test
    public void testSimpleResourceParse2() throws Exception {
        PuppetParserResult result = doParse(
                "class aaa::install { "
             +  " file { $aaa::params::fff :"
              + " ensure => present, "
             +  " path => \'aaaa\',"
              + " foo => 644"
             + " }"
             + " }");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa::install", c.getName());
        PResource res = c.getChildrenOfType(PResource.class, true).get(0);
        assertEquals("file", res.getResourceType());
        assertNotNull(res.getTitle());
        assertEquals(PString.VARIABLE, res.getTitle().getType());
        assertEquals("$aaa::params::fff", ((PVariable)res.getTitle()).getName());
        assertEquals(3, res.getAtributes().size());
        assertEquals("foo", res.getAtributes().get(2).getName());
//        assertEquals("644", res.getAtributes().get(2).getValue());
    }

 @Test
    public void testArrayResourceTitleParse() throws Exception {
        PuppetParserResult result = doParse(
                "class aaa::install { "
             +  " file { [$aaa::params::fff, 'aaa']:"
              + " ensure => present, "
             +  " path => \'aaaa\',"
              + " foo => 644"
             + " }"
             + " }");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa::install", c.getName());
        PResource res = c.getChildrenOfType(PResource.class, true).get(0);
        assertEquals("file", res.getResourceType());
        assertNotNull(res.getTitle());
        assertEquals(PString.BLOB, res.getTitle().getType());
        assertEquals(3, res.getAtributes().size());
        assertEquals("foo", res.getAtributes().get(2).getName());
        List<PVariable> vars = res.getTitle().getChildrenOfType(PVariable.class, true);
        assertEquals(1, vars.size());
        List<PString> strings = res.getTitle().getChildrenOfType(PString.class, true);
        assertEquals(1, strings.size());
    }
    
    @Test
    public void testResourceWithUnlessParse() throws Exception {
        PuppetParserResult result = doParse(
                "class aaa::install { "
             +  " file { \"fff\":"
              + " unless => \"aaa\", "
             +  " path => \'aaaa\',"
             + " }"
             + " }");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa::install", c.getName());
        PResource res = c.getChildrenOfType(PResource.class, true).get(0);
        assertEquals("file", res.getResourceType());
        assertNotNull(res.getTitle());
        assertEquals(PString.STRING, res.getTitle().getType());
        assertEquals(2, res.getAtributes().size());
        assertEquals("unless", res.getAtributes().get(0).getName());
        assertEquals("path", res.getAtributes().get(1).getName());
    }
    
    @Test
    public void testResourceWithResourceReferences() throws Exception {
        PuppetParserResult result = doParse(
                "class aaa::install { "
             +  " file { \"fff\":"
              + " before => Yumrepo['epel','epel-source','epel-debuginfo','epel-testing','epel-testing-source','epel-testing-debuginfo'], "
             +  " path => \'aaaa\',"
             + " }"
             + " }");
        PClass c = assertAndGetClassElement(result);
        PResource res = c.getChildrenOfType(PResource.class, true).get(0);
        assertEquals("file", res.getResourceType());
        assertNotNull(res.getTitle());
        assertEquals(PString.STRING, res.getTitle().getType());
        assertEquals(2, res.getAtributes().size());
        assertEquals("before", res.getAtributes().get(0).getName());
        assertEquals("path", res.getAtributes().get(1).getName());
        
    }

    @Test
    public void testDefaultResourceParse() throws Exception {
        PuppetParserResult result = doParse(
               "class aaa::install { "
             + " File { "
             + "  notify => Service[$bamboo_agent::service_name], "
             + " }"
             + "}");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa::install", c.getName());
        PResource res = c.getChildrenOfType(PResource.class, true).get(0);
        assertEquals("File", res.getResourceType());
        assertNull(res.getTitle());
        assertEquals(1, res.getAtributes().size());
        assertEquals("notify", res.getAtributes().get(0).getName());
    }


    @Test
    public void testResourceInConditionParse() throws Exception {
        PuppetParserResult result = doParse(
               "class aaa::install { "
             + " if $fff {"
             + "   file { $aaa::params::fff :"
             + "     ensure => present, "
             + "     path => \'aaaa\',"
             + "     foo => 644"
             + "   }"
             + " }"
             + "}");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa::install", c.getName());
        PResource res = c.getChildrenOfType(PResource.class, true).get(0);
        assertEquals("file", res.getResourceType());
        assertNotNull(res.getTitle());
        assertEquals(PString.VARIABLE, res.getTitle().getType());
        assertEquals("$aaa::params::fff", ((PVariable)res.getTitle()).getName());
        assertEquals(3, res.getAtributes().size());
        assertEquals("foo", res.getAtributes().get(2).getName());
    }


    @Test
    public void testClassVariableAssignmentParse() throws Exception {
        PuppetParserResult result = doParse(
                "class aaa::param { "
             +  " $aaa::fff::ss = $bbb\n"
             +  " $aaa::fff = 666\n"
             + " }");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa::param", c.getName());
        List<PVariable> vars = c.getChildrenOfType(PVariable.class, true);
        assertEquals(1, vars.size());
        assertEquals("$bbb", vars.get(0).getName());
        List<PVariableDefinition> varDefs = c.getChildrenOfType(PVariableDefinition.class, true);
        assertEquals(2, varDefs.size());
        assertEquals("$aaa::fff::ss", varDefs.get(0).getName());
        assertEquals("$aaa::fff", varDefs.get(1).getName());

    }
    @Test
    public void testClassVariableAssignment2Parse() throws Exception {
        PuppetParserResult result = doParse(
                "class aaa::param { "
             +  " $aaa = { aaa => 'aaa' , bbb => 'bbb' }\n"
             +  " $aaa::fff::ss = hiera('aaa')\n"
             +  " $aaa::fff = [ 'aaa', 'bbb' ]\n"
             + " }");
        PClass c = assertAndGetClassElement(result);
        assertEquals("aaa::param", c.getName());
        List<PVariableDefinition> varDefs = c.getChildrenOfType(PVariableDefinition.class, true);
        assertEquals(3, varDefs.size());
        assertEquals("$aaa", varDefs.get(0).getName());
        assertEquals("$aaa::fff::ss", varDefs.get(1).getName());
        assertEquals("$aaa::fff", varDefs.get(2).getName());

    }

    @Test
    public void testClassInClass() throws Exception {
        PuppetParserResult result = doParse(
                "class serial { "
             + "   class console {}"
             + " }");
        PClass c = assertAndGetClassElement(result);
        assertEquals("serial", c.getName());
        List<PClass> cc = c.getChildrenOfType(PClass.class, true);
        assertEquals(1, cc.size());
        assertEquals("console", cc.get(0).getName());

    }

    @Test
    public void testSimpleNodeParse() throws Exception {
        PuppetParserResult result = doParse(
                "node 'aaa' { "
             + " }");
        PNode nd = assertAndGetNodeElement(result);
        assertEquals(1, nd.getNames().length);
        assertEquals("'aaa'", nd.getNames()[0]);
    }

    @Test
    public void testMultiNodeParse() throws Exception {
        PuppetParserResult result = doParse(
                "node 'aaa', 'bbb', 'ccc' { "
             + " }");
        PNode nd = assertAndGetNodeElement(result);
        assertEquals(3, nd.getNames().length);
        assertEquals("'aaa'", nd.getNames()[0]);
        assertEquals("'bbb'", nd.getNames()[1]);
        assertEquals("'ccc'", nd.getNames()[2]);
    }

    @Test
    public void testRegexpNodeParse() throws Exception {
        PuppetParserResult result = doParse(
                "node /aaa/ { "
             + " }");
        PNode nd = assertAndGetNodeElement(result);
        assertEquals(1, nd.getNames().length);
        assertEquals("/aaa/", nd.getNames()[0]);
    }

    //doesn't fail with nil, false, or true (now keywords) but would fail with any random identifier
    //before the { et the end of the condition
    @Test
    public void testConditionNotAStartOfResource() throws Exception {
        PuppetParserResult result = doParse(
               "class aaa { \n"
             + " if $public_html_symlink == true and $public_html_target_dir == nil {\n"
             + "    fail('fail')\n"
             + "  }"
             + " }");
        PClass nd = assertAndGetClassElement(result);
        List<PCondition> conds = nd.getChildrenOfType(PCondition.class, true);
        assertEquals(1, conds.size());
        PCondition c = conds.get(0);
        assertEquals(2, c.getCondition().getChildrenOfType(PVariable.class, true).size());
        assertEquals(1, c.getConsequence().getChildrenOfType(PString.class, true).size());
    }


    @Test
    public void testConditionElse() throws Exception {
        PuppetParserResult result = doParse(
               "class aaa { \n"
             + " if $public_html_symlink {\n"
             + "    fail('fail')\n"
             + "  } else { $bbb = 'ccc' }"
             + " }");
        PClass nd = assertAndGetClassElement(result);
        List<PCondition> conds = nd.getChildrenOfType(PCondition.class, true);
        assertEquals(1, conds.size());
        PCondition c = conds.get(0);
        assertEquals(1, c.getCondition().getChildrenOfType(PVariable.class, true).size());
        assertEquals(1, c.getConsequence().getChildrenOfType(PString.class, true).size());
        assertEquals(1, c.getOtherwise().getChildrenOfType(PString.class, true).size());
        assertEquals(1, c.getOtherwise().getChildrenOfType(PVariableDefinition.class, true).size());
    }
    @Test
    public void testConditionElseIf() throws Exception {
        PuppetParserResult result = doParse(
               "class aaa { \n"
             + " if $public_html_symlink {\n"
             + "    fail('fail')\n"
             + "  } elsif function($aa) { $bbb = 'ccc'"
             + "  } else { $ccc = 'ddd'}"
             + " }");
        PClass nd = assertAndGetClassElement(result);
        List<PCondition> conds = nd.getChildrenOfType(PCondition.class, true);
        assertEquals(2, conds.size()); //2 because of nested
        PCondition c = conds.get(0);
        assertEquals(1, c.getCondition().getChildrenOfType(PVariable.class, true).size());
        assertEquals(1, c.getConsequence().getChildrenOfType(PString.class, true).size());
        final List<PCondition> nested = c.getChildrenOfType(PCondition.class, true);
        assertEquals(1, nested.size());
        PCondition cc = nested.get(0);
        assertEquals(1, cc.getCondition().getChildrenOfType(PVariable.class, true).size());
        assertEquals(1, cc.getConsequence().getChildrenOfType(PString.class, true).size());
        assertEquals(1, cc.getConsequence().getChildrenOfType(PVariableDefinition.class, true).size());
        assertEquals(1, cc.getOtherwise().getChildrenOfType(PVariableDefinition.class, true).size());
    }

    @Test
    public void testCondition() throws Exception {
        PuppetParserResult result = doParse(
               "class aaa { \n"
             + " if contains($a, { 'd', 'e'}) {\n"
             + "    fail('fail')\n"
             + " }");
        PClass nd = assertAndGetClassElement(result);
        List<PCondition> conds = nd.getChildrenOfType(PCondition.class, true);
        assertEquals(1, conds.size());
        PCondition c = conds.get(0);
        assertEquals(1, c.getCondition().getChildrenOfType(PVariable.class, true).size());
        assertEquals(1, c.getConsequence().getChildrenOfType(PString.class, true).size());
    }

    @Test
    public void testCase() throws Exception {
        PuppetParserResult result = doParse(
               "class aaa { \n"
             + "  case $exp {"
             + "   'aaa': { $aaa='xx' }"
             + "   'bbb': { $bbb='xx' }"
             + "    default:{fail('ddd')}"
             + "  }"
             + " }");
        PClass nd = assertAndGetClassElement(result);
        List<PCase> cs = nd.getChildrenOfType(PCase.class, true);
        assertEquals(1, cs.size());
        PCase cs1 = cs.get(0);
        assertEquals(3, cs1.getCases().size());
    }


    @Test
    public void testNestedCase() throws Exception {
        PuppetParserResult result = doParse(
               "class aaa { \n"
             + "  case $exp {"
             + "   'aaa': {  "
                       + "case ($exp2 == true) { "
                       + "   true : {$ccc='xx'}"
                       + "   false: { $ddd='xx'}"
                       + "}"
                + " }"
             + "   'bbb': { $bbb='xx' }"
             + "    default:{fail('ddd')}"
             + "  }"
             + " }");
        PClass nd = assertAndGetClassElement(result);
        List<PCase> cs = nd.getChildrenOfType(PCase.class, true);
        assertEquals(2, cs.size());
        PCase cs1 = cs.get(0);
        assertEquals(3, cs1.getCases().size());
        PCase cs2 = cs.get(1);
        assertEquals(2, cs2.getCases().size());

    }
    @Test
    public void testCaseWithNestedIf() throws Exception {
        PuppetParserResult result = doParse(
               "class aaa { \n"
             + "  case $exp {"
             + "   'aaa': {  "
                       + "if ($exp2 == true) { "
                       + "   fail ('xxx')"
                       + "}"
                + " }"
             + "    default:{"
                       + "fail('ddd')"
                 + "}"
             + "  }"
             + " }");
        PClass nd = assertAndGetClassElement(result);
        List<PCase> cs = nd.getChildrenOfType(PCase.class, true);
        assertEquals(1, cs.size());
        PCase cs1 = cs.get(0);
        assertEquals(2, cs1.getCases().size());
        List<PCondition> conds = cs1.getChildrenOfType(PCondition.class, true);
        assertEquals(1, conds.size());
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

    private PNode assertAndGetNodeElement(PuppetParserResult result) {
        PElement nd = result.getRootNode();
        assertNotNull(nd);
        List<PElement> children = nd.getChildren();
        assertNotNull(children);
        assertEquals(1, children.size());
        PElement ch = children.get(0);
        assertEquals(PElement.NODE, ch.getType());
        PNode c = (PNode)ch;
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
