package com.tropyx.nb_puppet.parser;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mkleint
 */
public class PStringTest {

    public PStringTest() {
    }

    @Test
    public void testVariables() {
        PString v = new PString(null, 0, "\"aaa${bbb::ccc}/${ddd::eee}fff\"");
        List<PVariable> ch = v.getChildrenOfType(PVariable.class, true);
        assertEquals(2, ch.size());
        assertEquals("$bbb::ccc", ch.get(0).getName());
        assertEquals("$ddd::eee", ch.get(1).getName());
    }


}
