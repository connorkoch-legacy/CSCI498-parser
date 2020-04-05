/**
 * This exception is thrown by the LL(1) Parser
 */
public class LLParseException extends Exception {
    public LLParseException() {
        super();
    }

    /**
     * For an error where the terminal didn't match what we expected.
     * @param expected - the alphabetcharacter we wanted
     * @param actual - the alphabetcharacter we actually got.
     */
    public LLParseException(AlphabetCharacter expected, AlphabetCharacter actual) {
        super("Invalid terminal in input stream. Expected: " + expected + "   got: " + actual);
    }

    /**
     * For an error where a non-terminal doesn't have a ProducitonRule for the terminal given
     * @param nonTerminal - the non-terminal in question
     * @param terminal - the terminal we got
     * @param predictSetError - a dummy variable (set this to true) to have a different method signature than the method above
     */
    public LLParseException(AlphabetCharacter nonTerminal, AlphabetCharacter terminal, boolean predictSetError) {
        super("Invalid terminal in input stream. Terminal " + terminal + " is not in the predict set of: " + nonTerminal);
    }
}
