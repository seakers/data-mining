package ifeed.mining.moea.operators;

import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.local.params.BaseParams;
import ifeed.mining.moea.AbstractMOEABase;
import java.util.*;

public abstract class AbstractGeneralizationOperator extends AbstractLogicOperator{

    private boolean exhaustiveSearchFinished;
    private Set<Integer> restrictedVariableSet;
    private Map<Integer, Set<Integer>> restrictedVariableCombination;

    public abstract void initialize();

    public AbstractGeneralizationOperator(BaseParams params, AbstractMOEABase base){
        super(params, base);
        this.restrictedVariableSet = new HashSet<>();
        this.restrictedVariableCombination = new HashMap<>();
        this.exhaustiveSearchFinished = false;
    }

    public AbstractGeneralizationOperator(BaseParams params, AbstractMOEABase base, LogicalConnectiveType targetLogic){
        super(params, base, targetLogic);
        this.restrictedVariableSet = new HashSet<>();
        this.restrictedVariableCombination = new HashMap<>();
        this.exhaustiveSearchFinished = false;
    }

    public void addVariableRestriction(int var){
        restrictedVariableSet.add(var);
    }

    public void addVariableRestriction(int var1, int var2){
        if(!restrictedVariableCombination.containsKey(var1)){
            restrictedVariableCombination.put(var1, new HashSet<>());
        }
        restrictedVariableCombination.get(var1).add(var2);
    }

    public Set<Integer> getRestrictedVariables(){
        return this.restrictedVariableSet;
    }

    public Map<Integer, Set<Integer>> getRestrictedVariableCombination(){
        return this.restrictedVariableCombination;
    }

    public Set<Integer> getRestrictedVariableCombination(int var){
        if(!this.restrictedVariableCombination.containsKey(var)){
            return new HashSet<>();
        }else{
            return restrictedVariableCombination.get(var);
        }
    }

    public void setExhaustiveSearchFinished(){
        this.exhaustiveSearchFinished = true;
    }

    public void setExhaustiveSearchFinished(int var){
        if(!restrictedVariableCombination.containsKey(var)){
            restrictedVariableCombination.put(var, new HashSet<>());
        }
        restrictedVariableCombination.get(var).add(-1);
    }

    public boolean isExhaustiveSearchFinished(){
        return this.exhaustiveSearchFinished;
    }

    public boolean isExhaustiveSearchFinished(int var){
        if(restrictedVariableCombination.containsKey(var)){
            if(restrictedVariableCombination.get(var).contains(-1)){
                return true;
            }
        }
        return false;
    }

    public void reset(){
        this.restrictedVariableSet = new HashSet<>();
        this.restrictedVariableCombination = new HashMap<>();
        this.exhaustiveSearchFinished = false;
    }
}
