import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class CFG {
	private Set<AlphabetCharacter> nonterminals = new HashSet<>();
	private Set<AlphabetCharacter> terminals = new HashSet<>();

	// nonterminal -> list[sequence of alphabet chars]
	private Map<AlphabetCharacter, ArrayList<ArrayList<AlphabetCharacter>>> productions = new HashMap<>();
	private Map<AlphabetCharacter, ArrayList<AlphabetCharacter>> startingRule = new HashMap<>();

	/**
	 * Builds the CFG
	 * @param inFile Name of the file to be read in
	 */
	public CFG(String inFile) {
		File file = new File(inFile);

		try {
			Scanner scanner = new Scanner(file);
			AlphabetCharacter currentLHS = null;
			ArrayList<AlphabetCharacter> currentRHS = new ArrayList<>();

			// scan by space separated tokens
			while (scanner.hasNextLine()) {
				Scanner lineParser = new Scanner(scanner.nextLine());
				boolean firstTokenInLine = true;

				// high quality input validation
				while (lineParser.hasNext()) {
					String token = lineParser.next();

					// alternation means we push a production, reset currentRHS buffer
					if (token.charAt(0) == '|') {
						if (!productions.containsKey(currentLHS)) {
							productions.put(currentLHS, new ArrayList<>());
						}

						productions.get(currentLHS).add(currentRHS);
						currentRHS = new ArrayList<>();

					// Skip the '->', as it's just a special case of alternation
					} else if(token.equals("->")) {
						continue;
					} else {
						if (firstTokenInLine) {
							if (currentLHS != null) {
								if (!productions.containsKey(currentLHS)) {
									productions.put(currentLHS, new ArrayList<>());
								}

								productions.get(currentLHS).add(currentRHS);
								currentRHS = new ArrayList<>();
							}

							currentLHS = new AlphabetCharacter(token);
						} else {
							AlphabetCharacter c = new AlphabetCharacter(token);

							if (c.isNonTerminal()) {
								nonterminals.add(c);
							}
							else if (!c.isEOF() && !c.isLambda()){
								terminals.add(c);
							}

							currentRHS.add(c);

							// TODO: Can more than 1 production rule contain $? I think *yes*, but this doesn't handle that
							if (c.isEOF()) {
								startingRule.put(currentLHS, currentRHS);
							}
						}
					}

					firstTokenInLine = false;
				}

			}

			// add anything remaining in the token buffer
			if (currentLHS != null) {
				if (!productions.containsKey(currentLHS)) {
					productions.put(currentLHS, new ArrayList<>());
				}
				productions.get(currentLHS).add(currentRHS);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the follow set
	 * DO NOT IMPLEMENT
	 */
	public Set<AlphabetCharacter> getFollowSet(AlphabetCharacter c) {
		if (c.followSet != null)
			return c.followSet;
		Set<AlphabetCharacter> followSet = new HashSet<>();
		return followSet;
	}

	/**
	 * Gets the first set 
	 * DO NOT IMPLEMENT
	 */
	public Set<AlphabetCharacter> getFirstSet(AlphabetCharacter c) {
		if (c.firstSet != null)
			return c.firstSet;
		Set<AlphabetCharacter> firstSet = new HashSet<>();
		if (!c.isNonTerminal()) {
			firstSet.add(c);
			return firstSet;
		}
		return firstSet;
	}

	@Override
	public String toString() {
		// Generate terminals
		StringBuilder out = new StringBuilder("Terminals: ");
		for (AlphabetCharacter c: terminals) {
			out.append(c).append(", ");
		}

		// Remove the last comma, and then generate non terminals
		out = new StringBuilder(out.substring(0, out.length() - 2) + "\nNon-terminals: ");
		for (AlphabetCharacter c: nonterminals) {
			out.append(c).append(", ");
		}

		// Remove the last comma, and then generate rules
		out = new StringBuilder(out.substring(0, out.length() - 2) + "\n\n");
		out.append("Grammar Rules\n");
		int ruleNumber = 1;

		for (AlphabetCharacter key: productions.keySet()) {
			for (int i = 0; i < productions.get(key).size(); i++) {
				ArrayList<AlphabetCharacter> rhs = productions.get(key).get(i);
				// Generates: #) A ->
				out.append(ruleNumber++).append(") ");
				out.append(key).append(" -> ");

				// Build the RHS
				for (AlphabetCharacter c: rhs)
					out.append(c).append(" ");

				out.append("\n");
			}
		}

		// Print the grammar start symbol / goal
		out.append("\n\nGrammar Start Symbol or Goal: ");
		for (AlphabetCharacter key : startingRule.keySet()) {
			out.append(key);
		}

		// Print the start symbol production rule
		out.append("\n\nGrammar Start Production Rule: ");

		for (AlphabetCharacter key : startingRule.keySet()) {
			out.append(key).append(" -> ");

			for (AlphabetCharacter c : startingRule.get(key)) {
				out.append(c).append(" ");
			}
		}

		return out.toString();
	}

}
