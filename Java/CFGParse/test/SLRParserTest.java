import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SLRParserTest {

    /**
     * Tests our sets-of-items construction implementation against Hellman's provided example
     * @throws Exception
     */
    @Test
    void testCanonicalItemSets() throws Exception {
        CFG cfg = new CFG("postfix-grammar.cfg");
        SLRParser slrParser = new SLRParser(cfg);
        ArrayList<Set<SLRItem>> canonicalItemSets = slrParser.canonicalItemSets;

        // Build the expected item sets.
        ArrayList<Set<SLRItem>> expected = new ArrayList<>();
        ProductionRule start = cfg.getProductionsOf(new AlphabetCharacter("START")).get(0);
        ProductionRule e1 = cfg.getProductionsOf(new AlphabetCharacter("E")).get(0);
        ProductionRule e2 = cfg.getProductionsOf(new AlphabetCharacter("E")).get(1);

        // State 0: START -> . E $ ; E -> . plus E E ; E -> . num
        Set<SLRItem> state0 = new HashSet<>();
        state0.add(new SLRItem(start));
        state0.add(new SLRItem(e1));
        state0.add(new SLRItem(e2));
        expected.add(state0);

        // State 1: E -> num .
        Set<SLRItem> state1 = new HashSet<>();
        state1.add(new SLRItem(e2, 1));
        expected.add(state1);

        // State 2: E -> . plus E E ; E -> plus . E E ; E -> . num
        Set<SLRItem> state2 = new HashSet<>();
        state2.add(new SLRItem(e1));
        state2.add(new SLRItem(e1, 1));
        state2.add(new SLRItem(e2));
        expected.add(state2);

        // State 3: START -> E . $
        Set<SLRItem> state3 = new HashSet<>();
        state3.add(new SLRItem(start, 1));
        expected.add(state3);

        // State 4: E -> . plus E E ; E -> plus E . E ; E -> . num
        Set<SLRItem> state4 = new HashSet<>();
        state4.add(new SLRItem(e1));
        state4.add(new SLRItem(e1, 2));
        state4.add(new SLRItem(e2));
        expected.add(state4);

        // State 5: START -> E $ .
        Set<SLRItem> state5 = new HashSet<>();
        state5.add(new SLRItem(start, 2));
        expected.add(state5);

        // State 6: E -> plus E E .
        Set<SLRItem> state6 = new HashSet<>();
        state6.add(new SLRItem(e1, 3));
        expected.add(state6);

        // State numbers might not match, which means we have to go through this hack
        assertEquals(new HashSet<>(expected), new HashSet<>(canonicalItemSets));
        // Since the cast to a set destroys duplicates, check original cardinality too
        assertEquals(expected.size(), canonicalItemSets.size());
    }

    @Test
    void testSLRActionTable() throws Exception {
        CFG cfg = new CFG("postfix-grammar.cfg");
        SLRParser slrParser = new SLRParser(cfg);
    }

    @Test
    void testGoTo_0_E() throws Exception {
        CFG cfg = new CFG("postfix-grammar.cfg");
        SLRParser slrParser = new SLRParser(cfg);

        Set<SLRItem> itemSet0 = new HashSet<>();
        itemSet0.add(SLRItem.makeFreshStart(cfg.getProductionsOf(new AlphabetCharacter("START")).get(0)));
        itemSet0.add(SLRItem.makeFreshStart(cfg.getProductionsOf(new AlphabetCharacter("E")).get(0)));
        itemSet0.add(SLRItem.makeFreshStart(cfg.getProductionsOf(new AlphabetCharacter("E")).get(1)));

        Set<SLRItem> expected = new HashSet<>();

        // Build the production rule
        AlphabetCharacter lhs = new AlphabetCharacter("START");
        ArrayList<AlphabetCharacter> rhs = new ArrayList<>();
        rhs.add(new AlphabetCharacter("E"));
        rhs.add(new AlphabetCharacter("$"));

        // Add to the set
        expected.add(new SLRItem(new ProductionRule(lhs, rhs), 1));

        Set<SLRItem> goto_0_E = slrParser.gotoItem(itemSet0, new AlphabetCharacter("E"));
        assertEquals(expected, goto_0_E);
    }

    @Test
    void testGoTo_0_num() throws Exception {
        // Goto(0,num) = 1
        CFG cfg = new CFG("postfix-grammar.cfg");
        SLRParser slrParser = new SLRParser(cfg);

        Set<SLRItem> itemSet0 = new HashSet<>();
        itemSet0.add(SLRItem.makeFreshStart(cfg.getProductionsOf(new AlphabetCharacter("START")).get(0)));
        itemSet0.add(SLRItem.makeFreshStart(cfg.getProductionsOf(new AlphabetCharacter("E")).get(0)));
        itemSet0.add(SLRItem.makeFreshStart(cfg.getProductionsOf(new AlphabetCharacter("E")).get(1)));

        // Build the expected set
        Set<SLRItem> expected = new HashSet<>();

        // Build the production rule
        AlphabetCharacter lhs = new AlphabetCharacter("E");
        ArrayList<AlphabetCharacter> rhs = new ArrayList<>();
        rhs.add(new AlphabetCharacter("num"));

        // Add to the set
        expected.add(new SLRItem(new ProductionRule(lhs, rhs), 1));

        // Test
        Set<SLRItem> goto_0_num = slrParser.gotoItem(itemSet0, new AlphabetCharacter("num"));
        assertEquals(expected, goto_0_num);
    }
}