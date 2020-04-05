import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CFGTest {
    /**
     * A great utility: returns the string representation of a set
     * @param givenSet set to make into a string
     * @return the result
     */
    private String createStringFromSet(Set<AlphabetCharacter> givenSet) {
        StringBuilder result = new StringBuilder("{");
        for (AlphabetCharacter i : givenSet) {
            result.append(i).append(", ");
        }

        // Remove the trailing ", " at the end of the set, if non-empty
        if (givenSet.size() > 0) {
            result.delete(result.length() - 2, result.length());
        }
        result.append("}");

        return result.toString();
    }

    /**
     * Tests that the starting symbol is parsed correctly. Regardless of what it is, or the whitespace in the file
     * before it.
     */
    @Test
    void testStartSymbol() throws Exception {
        AlphabetCharacter S = new AlphabetCharacter("S");
        AlphabetCharacter Start = new AlphabetCharacter("START");

        // First several tests: 'S' is the start symbol
        CFG cfg = new CFG("derives_first_follow_example1.cfg");
        assertEquals(S, cfg.getStartingSymbol());

        cfg = new CFG("biglanguage.cfg");
        assertEquals(S, cfg.getStartingSymbol());

        cfg = new CFG("predict-set-test1.cfg");
        assertEquals(S, cfg.getStartingSymbol());

        // Next several: 'START' is the start symbol
        cfg = new CFG("postfix-grammar.cfg");
        assertEquals(Start, cfg.getStartingSymbol());

        cfg = new CFG("predict-set-test0.cfg");
        assertEquals(Start, cfg.getStartingSymbol());
    }

    /**
     * Tests derivesToLambda()
     *  <p>This verifies that derivesToLambda() is what's expected for all test files. Yes, it technically requires
     *      that toString() works on AlphabetCharacter too...</p>
     * @throws Exception
     */
    @Test
    void testDerivesToLambda() throws Exception {
        // Test #1: derives_first_follow_example1.cfg
        CFG cfg = new CFG("derives_first_follow_example1.cfg");
        String expected = "{A, B, C, D}";
        assertEquals(expected, createStringFromSet(cfg.getDerivesToLambdaSet()));

        // Test #2: derives_first_follow_example2.cfg
        cfg = new CFG("derives_first_follow_example2.cfg");
        expected = "{A, B, C}";
        assertEquals(expected, createStringFromSet(cfg.getDerivesToLambdaSet()));
    }

    /**
     * Tests firstSetOf() for derives_first_follow_example1 & derives_first_follow_example2
     * @throws Exception
     */
    @Test
    void testFirstSets() throws Exception {
        // Test #1: derives_first_follow_example1.cfg
        CFG cfg = new CFG("derives_first_follow_example1.cfg");

        // Verify non-terminals
        Set<AlphabetCharacter> nonTerminals = cfg.getNonTerminals();
        assertEquals("{A, B, C, D, S}", createStringFromSet(nonTerminals));

        // Verify firstSets()
        Map<String, String> expectedResults = new HashMap<>();
        expectedResults.put("S", "{$, a, b, d, g, h}");
        expectedResults.put("A", "{d, g, h}");
        expectedResults.put("B", "{g}");
        expectedResults.put("C", "{h}");
        expectedResults.put("D", "{a, b, d, g, h}");

        for (AlphabetCharacter l: nonTerminals) {
            assertEquals(expectedResults.get(l.toString()), createStringFromSet(cfg.firstSetOf(l)));
        }

        // Test #2: derives_first_follow_example2.cfg
        cfg = new CFG("derives_first_follow_example2.cfg");

        // Verify non-terminals
        nonTerminals = cfg.getNonTerminals();
        assertEquals("{A, B, C, S}", createStringFromSet(nonTerminals));

        // Verify firstSets()
        expectedResults = new HashMap<>();
        expectedResults.put("S", "{z}");
        expectedResults.put("A", "{e, z}");
        expectedResults.put("B", "{e, z}");
        expectedResults.put("C", "{e}");

        for (AlphabetCharacter l: nonTerminals) {
            assertEquals(expectedResults.get(l.toString()), createStringFromSet(cfg.firstSetOf(l)));
        }
    }

    @Test
    void testFollowSets() throws Exception {
        // Test #1: derives_first_follow_example1.cfg
        CFG cfg = new CFG("derives_first_follow_example1.cfg");
        Set<AlphabetCharacter> nonTerminals = cfg.getNonTerminals();

        // Verify firstSets()
        Map<String, String> expectedResults = new HashMap<>();
        expectedResults.put("S", "{}");
        expectedResults.put("A", "{$, g, h}");
        expectedResults.put("B", "{$, a, g, h}");
        expectedResults.put("C", "{$, b, g, h}");
        expectedResults.put("D", "{$}");

        for (AlphabetCharacter l: nonTerminals) {
            assertEquals(expectedResults.get(l.toString()), createStringFromSet(cfg.deriveFollowSetOfNonTerminal(l, new HashSet<>())));
        }

        // Test #2: derives_first_follow_example2.cfg
        cfg = new CFG("derives_first_follow_example2.cfg");
        nonTerminals = cfg.getNonTerminals();

        // Verify firstSets()
        expectedResults = new HashMap<>();
        expectedResults.put("S", "{}");
        expectedResults.put("A", "{e, f, z}");
        expectedResults.put("B", "{e, f, z}");
        expectedResults.put("C", "{e, f, z}");

        for (AlphabetCharacter l: nonTerminals) {
            assertEquals(expectedResults.get(l.toString()), createStringFromSet(cfg.deriveFollowSetOfNonTerminal(l, new HashSet<>())));
        }
    }

    @Test
    void testParseTable() throws Exception {
        CFG cfg = new CFG("fisher-5-2-predict-set-example.cfg");
        LL1ParsingTable actual = cfg.generateParsingTable();

        /* a b c d q $
        S  1 1 1   1 1
        A  4 5 5   5 5
        C      2 3
        B    6 7 7 7 7
        Q      9   8 9
         */
        // TODO: Complete this unit test
    }
}