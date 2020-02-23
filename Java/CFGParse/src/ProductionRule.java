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
}
