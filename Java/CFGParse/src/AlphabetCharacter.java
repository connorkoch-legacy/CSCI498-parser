public class AlphabetCharacter implements Comparable<AlphabetCharacter> {
	String label;

	public AlphabetCharacter(String label) {
		this.label = label.trim();
	}

	public AlphabetCharacter(AlphabetCharacter toBeCloned) {
		this(toBeCloned.label);
	}

	public static boolean isTerminalToken(String token) {
		// terminals === not uppercase. So if we convert the token to lowercase, and it's the same, then it's a terminal!
		return token.toLowerCase().equals(token);
	}

	/**
	 * Whether this is strictly a *terminal* character (lowercase)
	 * @return see above
	 */
	public boolean isTerminal() {
		return isTerminalToken(this.label);
	}

	/**
	 * Strictly a non-terminal
	 * @return see above
	 */
	public boolean isNonTerminal() {
		return !isTerminalToken(label) && !isLambda() && !isEOF();
	}

	/**
	 * Is λ?
	 * @return == "lambda"
	 */
	public boolean isLambda() {
		return "lambda".equals(label);
	}
	
	public boolean isEOF() {
		return "$".equals(label);
	}

	public boolean equals(String a) {
		return label.equals(a);
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
		return label.equals(that.label);
	}

	@Override
	public int compareTo(AlphabetCharacter o) {
		return this.label.compareTo(o.label); // compare based on label
	}
}
