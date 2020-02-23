import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CFG {
	private Set<AlphabetCharacter> nonterminals = new HashSet<>();
	private Set<AlphabetCharacter> terminals = new HashSet<>();

	// nonterminal -> list[sequence of alphabet chars]
	private Map<AlphabetCharacter, ArrayList<ProductionRule>> productions = new HashMap<>();
	private Map<AlphabetCharacter, ProductionRule> startingRule = new HashMap<>();

	/**
	 * Builds the CFG
	 * @param inFile Name of the file to be read in
	 */
	public CFG(String inFile) throws Exception {
		File file = new File(inFile);
		Scanner scanner = new Scanner(file);
		AlphabetCharacter currentLHS = null;

		// Read file 1 line at a time
		while (scanner.hasNextLine()) {
			// If this is a proper file, there's 2 cases here:
			// a) [lhs] -> [rhs]
			// b)       | [rhs], which uses the previously parsed LHS
			Pattern case1 = Pattern.compile("(?<LHS>.) -> (?<RHS>.+)");
			Pattern case2 = Pattern.compile("(?: +)\\| (?<RHS>.+)");
			String line = scanner.nextLine();
			ProductionRule currentRHS = new ProductionRule();
			Matcher m = case1.matcher(line);

			// If the first line matches, then extract the two sides and move on
			if (m.matches()) {
				currentLHS = new AlphabetCharacter(m.group("LHS"));
			} else {
				// Else, hope its an alternation line
				m = case2.matcher(line);

				if (!m.matches()) {
					throw new Exception("Invalid line: " + line);
				}
			}

			// Treat the string "lambda" as 1 character.
			if (m.group("RHS").equals("lambda")) {
				AlphabetCharacter c = new AlphabetCharacter(m.group("RHS"));
				currentRHS.addCharacterToRHS(c);
			} else {
				// Add all characters on the rightSideString to the appropriate set and to production object
				for (int i = 0; i < m.group("RHS").length(); i++) {
					String token = m.group("RHS").substring(i, i + 1);
					if (token.equals(" ")) { // Skip spaces
						continue;
					}
					AlphabetCharacter c = new AlphabetCharacter(token);

					if (c.isNonTerminal()) {
						nonterminals.add(c);
					}
					else if (!c.isEOF() && !c.isLambda()){
						terminals.add(c);
					}

					currentRHS.addCharacterToRHS(c);

					// Log the starting rule
					if (c.isEOF()) {
						startingRule.put(currentLHS, currentRHS);
					}
				}
			}

			// Add the productionrule to the map
			if (!productions.containsKey(currentLHS)) {
				productions.put(currentLHS, new ArrayList<>());
			}

			productions.get(currentLHS).add(currentRHS);
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

//	public boolean derivesToLambda(AlphabetCharacter l, Stack<>)

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
				ProductionRule rhs = productions.get(key).get(i);
				// Generates: #) A ->
				out.append(ruleNumber++).append(") ");
				out.append(key).append(" -> ");

				// Build the RHS
				for (AlphabetCharacter c: rhs.rhs)
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

			for (AlphabetCharacter c : startingRule.get(key).rhs) {
				out.append(c).append(" ");
			}
		}

		return out.toString();
	}

}
