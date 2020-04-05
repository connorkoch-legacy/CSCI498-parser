/**
 * A class representing an SLR "item"
 * According to the slides, an "item" is a production rule with a "progress marker"
 */
public class SLRItem implements Comparable<SLRItem> {
    public ProductionRule productionRule;

    // By using an int, we can just do ProductionRule[progressMarker],
    // as this will return the element *after* the progressMarker
    private int progressMarker;

    public SLRItem(ProductionRule productionRule) {
        this.productionRule = productionRule;
        progressMarker = 0; // The 'beginning'
    }

    /**
     * Constructor that specifies both the productionRule and the progressMarker -- useful for unit tests
     * @param productionRule -
     * @param progressMarker -
     */
    public SLRItem(ProductionRule productionRule, int progressMarker) {
        this.productionRule = productionRule;
        this.progressMarker = progressMarker;
    }

    /**
     * Copy constructor
     * @param toBeCopied -
     */
    public SLRItem(SLRItem toBeCopied) {
        productionRule = new ProductionRule(toBeCopied.productionRule);
        progressMarker = toBeCopied.progressMarker;
    }

    /**
     * Makes a "fresh start" SRLItem, the progress marker is at the beginning (A → •αBβ)
     * @param productionRule the production rule to make into an SLRItem
     * @return The SLRItem result
     */
    public static SLRItem makeFreshStart(ProductionRule productionRule) {
        return new SLRItem(productionRule);
    }

    /**
     * Used in Closure() -- returns the element *after* the progress marker.
     * @return Given A → α•Bβ, returns B, or null if the progress marker is at the end
     */
    public AlphabetCharacter elementAfterProgressMarker() {
        if (progressMarker < productionRule.rhs.size()) {
            return productionRule.rhs.get(progressMarker);
        }

        // TODO: Return an AlphabetCharacter that equals null, rather than null itself
        return null;
    }

    /**
     * Returns whether the grammar symbol X is to the *right* of the dot in this item set
     * @param grammarSymbol - X
     * @return - whether X is to the right of •
     */
    public boolean isSymbolToTheRightOfProgressMarker(AlphabetCharacter grammarSymbol) {
        if (progressMarker >= productionRule.rhs.size()) {
            return false;
        }

        // Only true if it's to the *immediate* right
        return productionRule.rhs.get(progressMarker).equals(grammarSymbol);
    }

    /**
     * Moves the progress marker past X
     * @param grammarSymbol - X
     */
    public void moveProgressMarkerToTheRightOf(AlphabetCharacter grammarSymbol) {
        // Put • to the *right* (i.e. +1 the position of) X
        for (int i = progressMarker; i < productionRule.rhs.size(); i++) {
            if (productionRule.rhs.get(i).equals(grammarSymbol)) {
                progressMarker = i + 1;
                break;
            }
        }
    }

    /**
     * Return true if the progress marker is at the end of the production rule
     * @return true if P = A → α •
     */
    public boolean isProgressMarkerAtEnd() {
        return progressMarker == productionRule.rhs.size();
    }

    /**
     * Return true if it's a lambda production rule
     * @return true if A → •λ
     */
    public boolean isLambdaProduction() {
        return productionRule.isLambdaProduction();
    }

    /**
     * get the left hand side of the production rule
     * @return production rule's LHS
     */
    public AlphabetCharacter getLHS() {
        return productionRule.lhs;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(productionRule.toString());
        result.append("\n");

        // Skip the LHS length
        for (int i = 0; i < productionRule.lhs.toString().length(); i++) {
            result.append(" ");
        }

        // Skip " ->", but NOT the trailing space! As if it's a fresh start, points to the first space after ->
        result.append("   ");

        // Add an upwards arrow where the production rule is
        // Start by skipping to the correct location
        for (int i = 0; i < progressMarker; i++) {
            // Skip over the length of the alphabetCharacter in this position
            for (int j = 0; j < productionRule.rhs.get(i).toString().length(); j++) {
                result.append(" ");
            }

            // Skip over the trailing space
            result.append(" ");
        }

        // Add the arrow. Or caret, whatever.
        result.append("^");

        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SLRItem slrItem = (SLRItem) o;
        return progressMarker == slrItem.progressMarker &&
                productionRule.equals(slrItem.productionRule);
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public int compareTo(SLRItem o) {
        return this.toString().compareTo(o.toString());
    }

}
