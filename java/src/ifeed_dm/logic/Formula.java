package ifeed_dm.logic;

import java.util.BitSet;
import java.util.StringJoiner;

public abstract class Formula {

    protected StringJoiner name;
    protected boolean negation = false;
    protected BitSet matches;

    public abstract String getName();
    public abstract BitSet getMatches();

    public void setNegation(boolean input){
        this.negation = input;
    }

    public void toggleNegation(){ // toggle
        this.negation = this.negation == false;
    }

    public boolean getNegation(){ return this.negation; }

    public abstract Formula copy();
}
