import java.util.ArrayList;
import java.util.Set;

public class ProductionRule {
    public ArrayList<AlphabetCharacter> rhs;

    public ProductionRule(ArrayList<AlphabetCharacter> rhs) {
        this.rhs = rhs;
    }

    public ProductionRule() {
        this.rhs = new ArrayList<>();
    }

    public void addCharacterToRHS(AlphabetCharacter c) {
        rhs.add(c);
    }

    /**
     * Only a lambda production if it's ONLY L -> lambda
     * @return
     */
    public boolean isLambdaProduction() {
        return rhs.size() == 1 && rhs.get(0).isLambda();
    }

    /**
     * Returns true if *anything* on the RHS is not a nonterminal
     * @return
     */
    public boolean containsTerminal() {
        for (AlphabetCharacter c : rhs) {
            if (!c.isNonTerminal()) {
                return true;
            }
        }

        return false;
    }
}
