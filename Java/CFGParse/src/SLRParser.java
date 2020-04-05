import java.util.*;

public class SLRParser {
    private CFG grammar;
    public ArrayList<Set<SLRItem>> canonicalItemSets;

    // SLR Action Table
    // Rows are state numbers, columns are X ∈ N union Σ$
    public Map<Integer, Map<AlphabetCharacter, SLRAction>> slrActionTable;

    /**
     * Constructor. Just requires a CFG.
     * @param grammar - the CFG for this SLRParser
     */
    public SLRParser(CFG grammar) {
        this.grammar = grammar;

        // Construct the canonical sets
        setsOfItemsConstruction();

        // Construct the action table
        constructSLRActionTable(canonicalItemSets);
    }

    /**
     * Creates the canonicalItemSets by following the Dragon Book's construction algorithm
     */
    public void setsOfItemsConstruction() {
        // TODO: We need to do this for the augmented grammar S' -> S, to handle multiple starting rules
        Set<SLRItem> startingItemSet = new HashSet<>();
        startingItemSet.add(SLRItem.makeFreshStart(grammar.getProductionsOf(grammar.getStartingSymbol()).get(0)));

        Set<SLRItem> response = itemSetClosure(startingItemSet);
        canonicalItemSets = new ArrayList<>();
        canonicalItemSets.add(response);

        Set<AlphabetCharacter> allGrammarSymbols = grammar.getAllGrammarSymbols();
        int oldSize;

        // Sets-of-Items Construction
        do {
            oldSize = canonicalItemSets.size();
            ArrayList<Set<SLRItem>> setToAvoidConcurrentModificationException = new ArrayList<>();

            for (Set<SLRItem> I : canonicalItemSets) {
                for (AlphabetCharacter x : allGrammarSymbols) {
                    Set<SLRItem> goToX = gotoItem(I, x);

                    // Skip empty
                    if (goToX.isEmpty() || canonicalItemSets.contains(goToX)) {
                        continue;
                    }

                    setToAvoidConcurrentModificationException.add(goToX);
                }
            }

            canonicalItemSets.addAll(setToAvoidConcurrentModificationException);
        } while(canonicalItemSets.size() != oldSize);
    }

    /**
     * Compute's an itemSets closure
     * @param itemSet - the item set we care about
     * @return - it's closure
     */
    public Set<SLRItem> itemSetClosure(Set<SLRItem> itemSet) {
        Set<SLRItem> closure = new HashSet<>(itemSet);
        int oldSize;

        do {
            oldSize = closure.size();
            Set<SLRItem> setToAvoidConcurrentModificationException = new HashSet<>();

            // For each item in the itemset
            for (SLRItem item : closure) {
                // Given A → α•Bβ, get B
                AlphabetCharacter B = item.elementAfterProgressMarker();
                if (B == null) { // null -> progress marker is at end, skip
                    continue;
                }

                // Get all the production rules of B
                ArrayList<ProductionRule> BsProductionRules = grammar.getProductionsOf(B);
                if (BsProductionRules == null) { // null -> B is not a non-terminal, skip
                    continue;
                }

                // Add all the fresh starts
                for (ProductionRule p : BsProductionRules) {
                    setToAvoidConcurrentModificationException.add(SLRItem.makeFreshStart(p));
                }
            }

            closure.addAll(setToAvoidConcurrentModificationException);
        } while (oldSize != closure.size());

        return closure;
    }

    /**
     * Runs the 'GoTo(I, X)' algorithm for SLR
     * @param itemSet - I
     * @param grammarSymbol - X
     * @return the closure of K'
     */
    public Set<SLRItem> gotoItem(Set<SLRItem> itemSet, AlphabetCharacter grammarSymbol) {
        Set<SLRItem> K = new HashSet<>();

        // K = { k ∈ I | X is to the right of • in k }
        for (SLRItem k : itemSet) {
            if (k.isSymbolToTheRightOfProgressMarker(grammarSymbol)) {
                K.add(new SLRItem(k));
            }
        }

        Set<SLRItem> Kprime = new HashSet<>(K);

        // K′ = { k ∈ K | with • progressed past X }
        for (SLRItem k : Kprime) {
            k.moveProgressMarkerToTheRightOf(grammarSymbol);
        }

        return itemSetClosure(Kprime);
    }

    /**
     * Generates a constructed SLR table
     * @param itemSetsOfG (rammar) - the canonical item sets
     */
    public void constructSLRActionTable(ArrayList<Set<SLRItem>> itemSetsOfG) {
        slrActionTable = new TreeMap<>();
        Set<AlphabetCharacter> allGrammarSymbols = grammar.getAllGrammarSymbols();

        for (int i = 0; i < itemSetsOfG.size(); i++) {
            // Create the inner map if it isn't already there
            if (!slrActionTable.containsKey(i)) {
                slrActionTable.put(i, new HashMap<>());
            }

            // Case #1: GoTo()s
            for (AlphabetCharacter x : allGrammarSymbols) {
                // if this character is "up next"
                for (SLRItem item : itemSetsOfG.get(i)) {
                    if (item.elementAfterProgressMarker() != null && item.elementAfterProgressMarker().equals(x)) {
                        // generate GoTo item set and index
                        Set<SLRItem> goToSet = gotoItem(itemSetsOfG.get(i), x);
                        int goToIndex = itemSetsOfG.indexOf(goToSet);

                        // create an SLRAction and put it in table
                        SLRAction currentAction = SLRAction.createShiftAndGoTo(goToIndex, x);
                        slrActionTable.get(i).put(x, currentAction);
                    }
                }
            }

            // Case #2: ReduceWith()
            for (SLRItem item : itemSetsOfG.get(i)) {
                if (!(item.isProgressMarkerAtEnd() || item.isLambdaProduction())) {
                    continue;
                }

                // generate followset
                Set<AlphabetCharacter> followSet = grammar.deriveFollowSetOfNonTerminal(item.getLHS(), new HashSet<>());
                for (AlphabetCharacter f : followSet) {
                    // create an SLRAction and put it in table
                    SLRAction currentAction = SLRAction.createReduceWith(item.productionRule, f);
                    slrActionTable.get(i).put(f, currentAction);
                }
            }

            // Case 3: ReduceWithAndAccept()
            for (SLRItem item : itemSetsOfG.get(i)) {
                // Only 1 of the rules has to be S → π$• to accept on all
                if (item.getLHS().equals(grammar.getStartingSymbol()) && item.isProgressMarkerAtEnd()) {
                    for (AlphabetCharacter x : allGrammarSymbols) {
                        slrActionTable.get(i).put(x, SLRAction.createReduceWithAndAccept(item.productionRule, x));
                    }

                    break;
                }
            }
        }
    }
}
