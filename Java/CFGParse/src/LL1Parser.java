import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class LL1Parser {
    private ParseTreeNode root;
    private LL1ParsingTable parsingTable;
    private AlphabetCharacter marker;

    /**
     * Initializes this parser with the table
     * @param table
     */
    public LL1Parser(LL1ParsingTable table) {
        this.parsingTable = table;

        root = new ParseTreeNode(null, new AlphabetCharacter("ROOT"));
        marker = new AlphabetCharacter("*");
    }

    /**
     * Runs the LLTabularParsing algorithm
     * @param tokenStream - the stream of tokens
     * @param startingSymbol - the starting symbol of the grammar
     * @return the root of the parse tree
     */
    public ParseTreeNode LLTabularParsing(Queue<AlphabetCharacter> tokenStream, AlphabetCharacter startingSymbol) throws LLParseException {
        ParseTreeNode currentNode = root;
        Stack<AlphabetCharacter> kStack = new Stack<>();
        kStack.push(startingSymbol);

        while (!kStack.empty()) {
            AlphabetCharacter x = kStack.pop();

            // Check if marker first, as it'll pass the 'isTerminal()' test :/
            if (x.equals(marker)) {
                currentNode = currentNode.parent;
            } else if (x.isNonTerminal()) {
                // Throw exception if we cannot find the production rule specified.
                if (!parsingTable.doesProductionRuleExist(x, tokenStream.peek())) {
                    throw new LLParseException(x, tokenStream.peek(), true);
                }

                ProductionRule p = parsingTable.getProductionRuleOf(x, tokenStream.peek());
                kStack.push(marker);
                List<AlphabetCharacter> R = p.rhs;
                // Pushes onto the stack in reverse order
                for (int i = R.size() - 1; i >= 0; i--) {
                    kStack.push(R.get(i));
                }

                ParseTreeNode n = new ParseTreeNode(currentNode, x);
                currentNode.addChild(n);
                currentNode = n;
            } else if (x.isTerminal() || x.isEOF()) {
                if (x.isTerminal()) {
                    // If x does *not* match the token at the top of the stream, then ParseError.
                    if (x != tokenStream.peek()) {
                        throw new LLParseException(x, tokenStream.peek());
                    }

                    tokenStream.remove();
                }

                ParseTreeNode n = new ParseTreeNode(currentNode, x);
                currentNode.addChild(n);
            }
        }

        // Cur now points to Root, which as a single child: the start
        return root.children.get(0);
    }
}
