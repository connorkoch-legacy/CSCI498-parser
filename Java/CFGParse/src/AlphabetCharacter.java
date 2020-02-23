import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

public class AlphabetCharacter {
	String label;
	public Set<AlphabetCharacter> firstSet = null;
	public Set<AlphabetCharacter> followSet = null;
	
	public AlphabetCharacter(String label) {
		this.label = label;

		firstSet = new HashSet<>();
		followSet = new HashSet<>();
	}

	public static boolean isTerminalToken(String token) {
		// terminals === not uppercase. So if we convert the token to lowercase, and it's the same, then it's a terminal!
		return token.toLowerCase().equals(token);
	}

	public boolean equals(String a) {
		return label.equals(a);
	}

	public boolean isNonTerminal() {
		return !isTerminalToken(label) && !isLambda() && !isEOF();
	}

	public boolean isLambda() {
		return "lambda".equals(label);
	}
	
	public boolean isEOF() {
		return "$".equals(label);
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public String toString() {
		return label;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AlphabetCharacter that = (AlphabetCharacter) o;
		return Objects.equals(label, that.label) &&
				Objects.equals(firstSet, that.firstSet) &&
				Objects.equals(followSet, that.followSet);
	}
}
