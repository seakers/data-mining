package ifeed_dm.logic;

import java.util.BitSet;
import java.util.StringJoiner;

public abstract class Formula {

    protected Connective parent;
    protected StringJoiner name;
    protected BitSet matches;

    public Formula(Connective parent){
        this.parent = parent;

    }

    public abstract String getName();
    public abstract BitSet getMatches();

    public void setParent(Connective parent){
        this.parent = parent;
    }

}
