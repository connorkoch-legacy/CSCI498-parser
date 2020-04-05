import java.util.*;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a CFG to be used in an LL(1) parser.
 */
public class CFG {
	// These sets are filled after the constructor is called
	private Set<AlphabetCharacter> nonTerminals = new TreeSet<>();
	private Set<AlphabetCharacter> terminals = new TreeSet<>();
	private Set<AlphabetCharacter> derivesToLambdaSet = new TreeSet<>();

	// nonterminal -> list[production rules]
	private Map<AlphabetCharacter, ArrayList<ProductionRule>> productions = new HashMap<>();
	private AlphabetCharacter startingSymbol = null;

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
			String line = scanner.nextLine();
			// Skip spurious empty lines (thx Keith)
			if (line.length() < 1) {
				continue;
			}

			// If this is a proper file, there's 2 cases here:
			// a) [lhs] -> [rhs]
			// b)       | [rhs], which uses the previously parsed LHS
			Pattern case1 = Pattern.compile("(?<LHS>.+) -> (?<RHS>.+)");
			Pattern case2 = Pattern.compile("(?: +)\\| (?<RHS>.+)");
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
				// Add all space-delimited characters on the rightSideString to the appropriate set and to production object
				String[] tokens = m.group("RHS").split(" ");
				for (String token : tokens) {
					AlphabetCharacter c = new AlphabetCharacter(token);

					if (c.isNonTerminal()) {
						nonTerminals.add(c);
					} else if (!c.isEOF() && !c.isLambda()) {
						terminals.add(c);
					}

					currentRHS.addCharacterToRHS(c);
				}
			}

			// Add the productionrule to the map
			if (!productions.containsKey(currentLHS)) {
				productions.put(currentLHS, new ArrayList<>());
			}

			currentRHS.lhs = currentLHS;
			productions.get(currentLHS).add(currentRHS);
			nonTerminals.add(currentLHS);

			// The starting symbol is always the first one in the file
			if (startingSymbol == null) {
				startingSymbol = currentLHS;
			}
		}

		// Generate the derivesToLambda set
		generateDerivesToLambdaSet();
	}

	/**
	 * Returns all the production rules where nonTerminal is on the LHS. Used in SLRParser
	 * @param nonTerminal - the LHS
	 * @return - a list of the RHSs
	 */
	public ArrayList<ProductionRule> getProductionsOf(AlphabetCharacter nonTerminal) {
		return productions.get(nonTerminal);
	}

	/**
	 * Returns all grammar symbols (but lambda?)
	 * @return
	 */
	public Set<AlphabetCharacter> getAllGrammarSymbols() {
		Set<AlphabetCharacter> result = new HashSet<>();
		result.addAll(nonTerminals);
		result.addAll(terminals);
		result.add(new AlphabetCharacter("$"));
		return result;
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
	 * Prints the predictSet() of every production rule
	 */
	public void printAllPredictSets() {
		for (Map.Entry<AlphabetCharacter, ArrayList<ProductionRule>> entry : productions.entrySet()) {
			for (ProductionRule p : entry.getValue()) {
				StringBuilder result = new StringBuilder("Predict(" + entry.getKey() + " " + p.toString().trim() + ") = {");
				Set<AlphabetCharacter> predictSet = getPredictSetOfProductionRule(entry.getKey(), p);

				for (AlphabetCharacter c : predictSet) {
					result.append(c).append(", ");
				}

				// Remove the trailing ", " at the end of the set if it's there
				if (!predictSet.isEmpty()) {
					result.delete(result.length() - 2, result.length());
				}
				result.append("}");

				System.out.println(result);
			}
		}
	}

	/**
	 * Tests whether all the predict sets of this CFG are disjoint
	 * @return true if they are, false otherwise
	 */
	public boolean arePredictSetsDisjoint() {
		for (Map.Entry<AlphabetCharacter, ArrayList<ProductionRule>> entry : productions.entrySet()) {
			Set<AlphabetCharacter> predictSets = new TreeSet<>();
			for (ProductionRule p : entry.getValue()) {
				Set<AlphabetCharacter> tempSet = getPredictSetOfProductionRule(entry.getKey(), p);

				// If disjoint, union. Else, return false.
				if (Collections.disjoint(tempSet, predictSets)) {
					predictSets.addAll(tempSet);
				} else {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Returns what nonterminals derive to lambda. Used in testing
	 * @return derivesToLambdaSet
	 */
	public Set<AlphabetCharacter> getDerivesToLambdaSet() {
		return derivesToLambdaSet;
	}

	/**
	 * Returns all non-terminals.
	 * @return the non-terminals
	 */
	public Set<AlphabetCharacter> getNonTerminals() {
		return nonTerminals;
	}

	/**
	 * Returns the start symbol of this CFG, usually "S" or "START"
	 * @return - the start symbol
	 */
	public AlphabetCharacter getStartingSymbol() {
		return startingSymbol;
	}

	/**
	 * Generates and returns the LL1ParsingTable of this grammar (if possible)
	 * @return - the result
	 */
	public LL1ParsingTable generateParsingTable() throws Exception {
		LL1ParsingTable result = new LL1ParsingTable();

		// For every non-terminal, look at every production rule
		for (Map.Entry<AlphabetCharacter, ArrayList<ProductionRule>> entry : productions.entrySet()) {
			AlphabetCharacter nonTerminal = entry.getKey();

			// For every production rule, generate the predict set
			for (ProductionRule p : entry.getValue()) {
				Set<AlphabetCharacter> predictSet = getPredictSetOfProductionRule(entry.getKey(), p);

				// For every terminal in the predict set, add to the LL1 table.
				for (AlphabetCharacter terminal : predictSet) {
					result.addProductionRule(nonTerminal, terminal, p);
				}
			}
		}

		return result;
	}

	/**
	 * Returns the predictSet() of a LHS -> ProductionRule
	 * @param LHS - the non-terminal on the left-hand side of the production rule
	 * @param p - the ProductionRule that represents the right-hand side
	 * @return the result
	 */
	private Set<AlphabetCharacter> getPredictSetOfProductionRule(AlphabetCharacter LHS, ProductionRule p) {
		Set<AlphabetCharacter> result = deriveFirstSetOfProductionRule(p.rhs, new HashSet<>());

		if (entireRuleDerivesToLambda(p)) {
			result.addAll(deriveFollowSetOfNonTerminal(LHS, new HashSet<>()));
		}

		return result;
	}

	/**
	 * Calls derivesToLambda? on all non-terminals
	 */
	private void generateDerivesToLambdaSet() {
		for (AlphabetCharacter l : productions.keySet()) {
			if (this.derivesToLambda(l, new Stack<>())) {
				derivesToLambdaSet.add(l);
			}
		}
	}

	/**
	 * Creates the first set of AlphabetCharacter l by calling deriveFirstSet() on all production rules
	 * @param l - Alphabet character to find the firstSet of
	 * @return the resulting set
	 */
	public Set<AlphabetCharacter> firstSetOf(AlphabetCharacter l) {
		if (!productions.containsKey(l)) {
			return new TreeSet<>();
		}

		Set<AlphabetCharacter> firstSet = new TreeSet<>();
		for (ProductionRule rhs : productions.get(l)) {
			firstSet.addAll(deriveFirstSetOfProductionRule(rhs.rhs, new TreeSet<>()));
		}

		return firstSet;
	}

	/**
	 * Implements the derivesToLambda procedure given in Keith's pseudocode
	 * @param l - AlphabetCharacter asked about
	 * @param charStack - An empty stack
	 * @return yes or no
	 */
	private boolean derivesToLambda(AlphabetCharacter l, Stack<Pair<ProductionRule, AlphabetCharacter>> charStack) {
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

		// for each ProductionRule *p* with A on RHS
		for (Map.Entry<AlphabetCharacter, ArrayList<ProductionRule>> entry : productions.entrySet()) {
			ArrayList<ProductionRule> rhs = entry.getValue();

			for (ProductionRule p : rhs) {
				if (!p.rhs.contains(A)) {
					continue;
				}

				// for each instance of A on rhs of p
				for (int i = 0; i < p.rhs.size(); i++) {
					AlphabetCharacter c = p.rhs.get(i);
					if (!c.equals(A)) {
						continue;
					}

					// Deep copy over to xBeta everything to the right of this instance of A
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
		// If this is a lambda production, then duh
		if (p.isLambdaProduction()) {
			return true;
		}

		// If there's anything except a non-terminal, this is false.
		if (p.containsTerminalOr$()) {
			return false;
		}

		for (AlphabetCharacter l : p.rhs) {
			if (!derivesToLambdaSet.contains(l)) {
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
		for (AlphabetCharacter c: nonTerminals) {
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
		out.append("\n\nGrammar Start Symbol or Goal: ").append(startingSymbol);
		return out.toString();
	}
}
