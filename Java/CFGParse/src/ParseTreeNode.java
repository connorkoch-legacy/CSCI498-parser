import java.util.ArrayList;
import java.util.List;

public class ParseTreeNode {
    public ParseTreeNode parent;
    public List<ParseTreeNode> children;
    public AlphabetCharacter data;

    /**
     * Initializes a new parseTree node
     * @param parent - the parent of this node
     * @param data - the 'label' of this node
     */
    public ParseTreeNode(ParseTreeNode parent, AlphabetCharacter data) {
        this.parent = parent;
        this.data = data;

        children = new ArrayList<>();
    }

    /**
     * Adds a child to the current tree
     * @param child
     */
    public void addChild(ParseTreeNode child) {
        children.add(child);
    }
}
