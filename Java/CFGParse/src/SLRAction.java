import java.util.Objects;

/**
 * The inner-most element of the action table
 */
public class SLRAction {
    public enum SLRActionEnum {
        ShiftAndGoTo, ReduceWith, ReduceWithAndAccept;
    }

    public SLRActionEnum action;
    public int goToItemSet;
    public ProductionRule productionRuleReducedWith;
    public AlphabetCharacter column; // needed to have unique hashcodes for same action

    /**
     * Constructor to be used with ReduceWith and ReduceWithAndAccept SLRActions
     * @param action - the enum
     * @param productionRuleReducedWith - P
     * @param column - X
     */
    public SLRAction(SLRActionEnum action, ProductionRule productionRuleReducedWith, AlphabetCharacter column) {
        this.action = action;
        this.productionRuleReducedWith = productionRuleReducedWith;
        this.column = column;
    }

    /**
     * Constructor to be used with GoTo() SLRActions
     * @param action - the enum
     * @param goToItemSet - the index in the ArrayList of canonical item sets
     * @param column - the grammar symbol of this action
     */
    public SLRAction(SLRActionEnum action, int goToItemSet, AlphabetCharacter column) {
        this.action = action;
        this.goToItemSet = goToItemSet;
        this.column = column;
    }

    /**
     * Calls the appropriate constructor to make this a ShiftAndGoTo
     * @param goToItemSet - the item set to GoTo
     * @return - the SLRAction
     */
    public static SLRAction createShiftAndGoTo(int goToItemSet, AlphabetCharacter column) {
        return new SLRAction(SLRActionEnum.ShiftAndGoTo, goToItemSet, column);
    }

    /**
     * Called to create a ReduceWith SLRAction
     * @param P - the production rule to reduce with
     * @param column - X
     * @return - the created SLRAction
     */
    public static SLRAction createReduceWith(ProductionRule P, AlphabetCharacter column) {
        return new SLRAction(SLRActionEnum.ReduceWith, P, column);
    }

    /**
     * Called to create a ReduceWithAndAccept SLRAction
     * @param P - the production rule
     * @param column - X
     * @return - the created SLRAction
     */
    public static SLRAction createReduceWithAndAccept(ProductionRule P, AlphabetCharacter column) {
        return new SLRAction(SLRActionEnum.ReduceWithAndAccept, P, column);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        switch (this.action) {
            case ShiftAndGoTo:
                result.append("ShiftAndGoTo(").append(goToItemSet).append(") at column ").append(column);
                break;

            case ReduceWith:
                result.append("ReduceWith(").append(productionRuleReducedWith).append(") at column").append(column);
                break;

            case ReduceWithAndAccept:
                result.append("ReduceWithAndAccept(").append(productionRuleReducedWith).append(") at column").append(column);
                break;
        }

        return result.toString();
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SLRAction slrAction = (SLRAction) o;
        return goToItemSet == slrAction.goToItemSet &&
                action == slrAction.action &&
                Objects.equals(productionRuleReducedWith, slrAction.productionRuleReducedWith) &&
                column.equals(slrAction.column);
    }
}
