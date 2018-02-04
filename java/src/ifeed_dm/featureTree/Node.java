package ifeed_dm.featureTree;

import java.util.BitSet;
import java.util.StringJoiner;

public abstract class Node {

    protected LogicNode parent;
    protected StringJoiner name;
    protected BitSet matches;

    public Node(LogicNode parent){
        this.parent = parent;

    }

    public abstract String getName();
    public abstract BitSet getMatches();

    public void setParent(LogicNode parent){
        this.parent = parent;
    }

}
