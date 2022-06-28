package ifeed.feature.logic;

import java.util.BitSet;

public abstract class Formula {

    protected FormulaWithChildren parent;
    protected boolean negation = false;
    protected BitSet matches;
    protected Integer weight = null;
    protected WeightType weightType;

    public abstract String getName();
    public abstract BitSet getMatches();

    public FormulaWithChildren getParent(){
        return this.parent;
    }

    public void setParent(FormulaWithChildren parent){
        this.parent = parent;
    }

    public void removeParent(){ this.parent = null; }

    public void setNegation(boolean input){
        this.negation = input;
    }

    public void applyNegation(){ // toggle
        this.negation = this.negation == false;
    }

    public boolean getNegation(){ return this.negation; }

    public int getWeight(){
        if(this.weight == null){
            this.weight = 0;
        }
        return this.weight;
    }

    public void setWeight(int w){
        this.weight = w;
    }

    public void addWeight(){
        if(this.weight == null){
            this.weight = 0;
        }
        this.weight += 1;
    }

    public void setWeightType(WeightType type){
        this.weightType = type;
    }

    public boolean checkWeightType(WeightType type){
        if (this.weightType == type) {
            return true;

        } else if (this.weightType == WeightType.DEFAULT) {
            this.weightType = type;
            return true;

        }else{
            return false;
        }
    }

    public abstract Formula copy();

    public enum WeightType{
        DEFAULT,
        LOOSENESS
    }

    @Override
    public String toString(){
        return this.getName();
    }
}

