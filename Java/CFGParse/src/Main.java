public class Main {
	public static void main(String[] args) throws Exception {
		// IMPORTANT: the filename for the CFG is taken as a command-line argument
		CFG grammar = new CFG(args[0]);
		System.out.println(grammar);

		grammar.printDerivesToLambda();
		grammar.printAllFirstSets();
		grammar.printAllFollowSets();
//		Set<AlphabetCharacter> first = grammar.deriveFirstSet()
	}
}
