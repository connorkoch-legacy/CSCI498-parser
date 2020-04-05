import java.util.HashMap;
import java.util.Map;

/**
 * Represents an LL(1) table
 */
public class LL1ParsingTable {
    // Maps non terminals -> [ map of terminal -> production rule that triggered it, if it exists ]
    private Map<AlphabetCharacter, Map<AlphabetCharacter, ProductionRule>> ll1Table;

    /**
     * Initializes the table
     */
    public LL1ParsingTable() {
        ll1Table = new HashMap<>();
    }

    /**
     * Adds a production rule for this nonTerminal/terminal combination
     * @param nonTerminal -
     * @param terminal -
     * @param productionRule -
     */
    public void addProductionRule(AlphabetCharacter nonTerminal, AlphabetCharacter terminal, ProductionRule productionRule) throws Exception {
        // Initialize as appropriately if needed
        if (!ll1Table.containsKey(nonTerminal)) {
            ll1Table.put(nonTerminal, new HashMap<>());
        }

        if (ll1Table.get(nonTerminal).containsKey(terminal)) {
            // TODO: LL1 Conflict? There's already a production rule for this terminal
            throw new Exception("Conflict in LL1Table at non-terminal: " + nonTerminal + " and terminal: " + terminal);
        }

        ll1Table.get(nonTerminal).put(terminal, productionRule);
    }

    /**
     * Gets the production rule of the terminal/nonterminal combo
     * @param nonTerminal -
     * @param terminal -
     * @return -
     */
    public ProductionRule getProductionRuleOf(AlphabetCharacter nonTerminal, AlphabetCharacter terminal) {
        return ll1Table.get(nonTerminal).get(terminal);
    }

    /**
     * Checks if there's a production rule for nonTerminal -> terminal
     *  <p>Or, more formally, whether terminal is in a predict set for nonTerminal</p>
     * @param nonTerminal - the nonterminal in question
     * @param terminal - ditto
     * @return - true if the production rule exists, false otherwise.
     */
    public boolean doesProductionRuleExist(AlphabetCharacter nonTerminal, AlphabetCharacter terminal) {
        return ll1Table.containsKey(nonTerminal) && ll1Table.get(nonTerminal).containsKey(terminal);
    }
}
