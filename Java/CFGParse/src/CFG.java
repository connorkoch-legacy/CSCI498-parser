import java.util.*;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CFG {
	private Set<AlphabetCharacter> nonterminals = new HashSet<>();
	private Set<AlphabetCharacter> terminals = new HashSet<>();
	private Set<AlphabetCharacter> derivesToLambdaSet = new HashSet<>();

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
				AlphabetCharacter c = new AlphabetCharacter("lambda");
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
	 * Calls derivesToLambda? on all non-terminals
	 */
	public void generateDerivesToLambdaSet() {
		for (AlphabetCharacter l : productions.keySet()) {
			if (this.derivesToLambda(l, new Stack<>())) {
				derivesToLambdaSet.add(l);
			}
		}
	}

	/**
	 * Creates the first set of AlphabetCharacter l by calling deriveFirstSet() on all production rules
	 * @param l
	 * @return
	 */
	public Set<AlphabetCharacter> firstSetOf(AlphabetCharacter l) {
		if (!productions.keySet().contains(l)) {
			return new HashSet<>();
		}

		Set<AlphabetCharacter> firstSet = new HashSet<>();
		for (ProductionRule rhs : productions.get(l)) {
			firstSet.addAll(deriveFirstSetOfProductionRule(rhs.rhs, new HashSet<>()));
		}

		return firstSet;
	}

	/**
	 * Useful for debugging. Prints the derivesToLambda set
	 */
	public void printDerivesToLambda() {
		System.out.print("Derives to Lambda: ");
		for (AlphabetCharacter l : derivesToLambdaSet) {
			System.out.print(l + " ");
		}
		System.out.println();
	}

	/**
	 * Prints the first set of every LHS. Good for debugging.
	 */
	public void printAllFirstSets() {
		for (AlphabetCharacter l : productions.keySet()) {
			StringBuilder result = new StringBuilder("First(" + l + ") = {");
			Set<AlphabetCharacter> firstSet = firstSetOf(l);
			for (AlphabetCharacter c : firstSet) {
				result.append(c).append(", ");
			}

			// Remove the trailing ", " at the end of the set
			result.delete(result.length() - 2, result.length());
			result.append("}");

			System.out.println(result);
		}

		System.out.println();
	}

	/**
	 * Derives the followSet of every non-terminal and prints the result. Useful for debugging
	 */
	public void printAllFollowSets() {
		for (AlphabetCharacter l : productions.keySet()) {
			StringBuilder result = new StringBuilder("Follow(" + l + ") = {");
			Set<AlphabetCharacter> followSet = deriveFollowSetOfNonTerminal(l, new HashSet<>());
			for (AlphabetCharacter c : followSet) {
				result.append(c).append(", ");
			}

			// Remove the trailing ", " at the end of the set if it's there
			if (!followSet.isEmpty()) {
				result.delete(result.length() - 2, result.length());
			}
			result.append("}");

			System.out.println(result);
		}
	}

	/**
	 * Implements the derivesToLambda procedure given in Keith's pseudocode
	 * @param l - AlphabetCharacter asked about
	 * @param charStack - An empty stack
	 * @return yes or no
	 */
	public boolean derivesToLambda(AlphabetCharacter l, Stack<Pair<ProductionRule, AlphabetCharacter>> charStack) {
		for (ProductionRule p : productions.get(l)) {
			if (p.isLambdaProduction()) {
				return true;
			}

			if (p.containsTerminal()) {
				continue;
			}

			boolean allDeriveToLambda = true;
			for (AlphabetCharacter xi : p.rhs) {
				Pair<ProductionRule, AlphabetCharacter> thisPair = new Pair<>(p, xi);
				if (charStack.contains(thisPair)) {
					continue;
				}

				charStack.push(thisPair);
				allDeriveToLambda = derivesToLambda(xi, charStack);
				charStack.pop();

				if (!allDeriveToLambda) {
					break;
				}
			}

			if (allDeriveToLambda) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Implements deriveFirstSet() of his follow code.
	 * 	To find the firstSet of a non-terminal, call this function every production rule of said character
	 * @param xBeta - the RHS of a production rule.
	 * @param visitedSet - just pass an empty set on first call. This is how we track what we've visited in this recursive function
	 * @return The first set of this xBeta
	 */
	private Set<AlphabetCharacter> deriveFirstSetOfProductionRule(ArrayList<AlphabetCharacter> xBeta, Set<AlphabetCharacter> visitedSet) {
		//xBeta is a valid sequence of grammar elements and visitedSet is an empty set
		// If xBeta is empty, return an empty set
		if (xBeta.isEmpty()) {
			return new TreeSet<>();
		}

		AlphabetCharacter x = xBeta.get(0); // x is first symbol in sequence of grammar elements

		// Deep-copy xBeta to beta, so that we don't change the original ArrayList passed in.
		ArrayList<AlphabetCharacter> beta = new ArrayList<>(xBeta.size());
		for (AlphabetCharacter gamma : xBeta) {
			beta.add(new AlphabetCharacter(gamma.label));
		}
        beta.remove(0);

        if (!x.isNonTerminal()) { //if x is a terminal then return x
            Set<AlphabetCharacter> xSet = new TreeSet<>();

            // Only add to set if *not* lambda
            if (!x.isLambda()) {
				xSet.add(x);
			}
            return xSet;
        }

        Set<AlphabetCharacter> F = new TreeSet<>();

        if (!visitedSet.contains(x)) { //if x is not in set visitedSet
            visitedSet.add(x);
            for (ProductionRule p : productions.get(x)) {
                ArrayList<AlphabetCharacter> rightHandSide = p.rhs; //let rightHandSide be the RHS of p
                F.addAll(deriveFirstSetOfProductionRule(rightHandSide, visitedSet));
            }
        }

        if (derivesToLambda(x, new Stack<>())) {
			F.addAll(deriveFirstSetOfProductionRule(beta, visitedSet));
        }

        return F;
    }

	/**
	 * Derives the followSet of a nonterminal A
	 * @param A - a nonterminal
	 * @param visitedSet - initially an empty set.
	 * @return the followSet of nonterminal A
	 */
	public Set<AlphabetCharacter> deriveFollowSetOfNonTerminal(AlphabetCharacter A, Set<AlphabetCharacter> visitedSet) {
		if (visitedSet.contains(A)) {
			return new TreeSet<>();
		}

		visitedSet.add(A);
		Set<AlphabetCharacter> resultingSet = new TreeSet<>();

		// for each p with A on RHS
		for (Map.Entry<AlphabetCharacter, ArrayList<ProductionRule>> entry : productions.entrySet()) {
			ArrayList<ProductionRule> rhs = entry.getValue();
			for (ProductionRule p : rhs) {
				if (!p.rhs.contains(A)) {
					continue;
				}

				// for each instance of A on rhs
				for (int i = 0; i < p.rhs.size(); i++) {
					AlphabetCharacter c = p.rhs.get(i);
					if (!c.equals(A)) {
						continue;
					}

					// Deep copy over to xBeta
					ArrayList<AlphabetCharacter> xBeta = new ArrayList<>();
					for (int j = i + 1; j < p.rhs.size(); j++) {
						xBeta.add(new AlphabetCharacter(p.rhs.get(j)));
					}

					if (!xBeta.isEmpty()) {
						resultingSet.addAll(deriveFirstSetOfProductionRule(xBeta, new HashSet<>()));
					}

					ProductionRule temp = new ProductionRule(xBeta);
					if (xBeta.isEmpty() || (!temp.containsTerminalOr$() && entireRuleDerivesToLambda(temp))) {
						resultingSet.addAll(deriveFollowSetOfNonTerminal(entry.getKey(), visitedSet));
					}
				}
			}
		}

		return resultingSet;
	}

	/**
	 * Checks if the entire rule can become lambda
	 * 	I don't like this, as I think this should be in ProductionRule (and this needlessly entwines the two classes)
	 * @param p The production rule in question
	 * @return result
	 */
	private boolean entireRuleDerivesToLambda(ProductionRule p) {
		// If there's anything except a non-terminal, this is false.
		if (p.containsTerminalOr$()) {
			return false;
		}


		for (AlphabetCharacter l : p.rhs) {
			if (!derivesToLambda(l, new Stack<>())) {
				return false;
			}
		}

		return true;
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
