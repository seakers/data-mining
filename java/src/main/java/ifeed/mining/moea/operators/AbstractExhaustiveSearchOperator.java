package ifeed.mining.moea.operators;

import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.local.params.BaseParams;
import ifeed.mining.moea.AbstractMOEABase;
import java.util.*;

public abstract class AbstractExhaustiveSearchOperator extends AbstractLogicOperator{

    private boolean searchFinished;
    private final int nDim;
    private Set<Integer> visitedVariables;
    private Map<Integer, Set<Integer>> visitedVariableCombinations;

    public abstract void initialize();

    public AbstractExhaustiveSearchOperator(BaseParams params, AbstractMOEABase base, int nDim){
        super(params, base);
        this.nDim = nDim;
        this.resetSearch();
    }

    public AbstractExhaustiveSearchOperator(BaseParams params, AbstractMOEABase base, LogicalConnectiveType targetLogic, int nDim){
        super(params, base, targetLogic);
        this.nDim = nDim;
        this.resetSearch();
    }

    public void setSearchFinished(){ this.searchFinished = true; }


    public boolean isSearchFinished(){
        return this.searchFinished;
    }

    public void resetSearchForGivenConstraintSetter(){
        this.searchFinished = false;
    }

    public void resetSearch(){
        this.searchFinished = false;
        if(this.nDim == 1){
            this.visitedVariables = new HashSet<>();
        }else if(this.nDim == 2){
            this.visitedVariableCombinations = new HashMap<>();
        }else{
            throw new IllegalStateException();
        }
    }

    public Set<Integer> getVisitedVariables(){
        return this.visitedVariables;
    }

    public Map<Integer, Set<Integer>> getVisitedVariableCombinations(){
        return this.visitedVariableCombinations;
    }

    public void setVisitedVariable(int i){
        if(this.nDim == 1){
            this.visitedVariables.add(i);
        }else if (this.nDim == 2){
            if(this.visitedVariableCombinations.containsKey(i)){
                this.visitedVariableCombinations.get(i).add(-1);
            }else{
                throw new IllegalStateException();
            }
        }else{
            throw new IllegalStateException();
        }
    }

    public void setVisitedVariable(int i, int j){
        Set<Integer> temp;
        if(this.visitedVariableCombinations.containsKey(i)){
            temp = this.visitedVariableCombinations.get(i);
        }else{
            temp = new HashSet<>();
            this.visitedVariableCombinations.put(i, temp);
        }
        temp.add(j);
    }

    public boolean checkIfVisited(int i){
        if(this.nDim == 1){
            return this.visitedVariables.contains(i);
        }else if (this.nDim == 2){
            if(this.visitedVariableCombinations.containsKey(i)){
                if(this.visitedVariableCombinations.get(i).contains(-1)){
                    return true;
                }
            }
            return false;
        }else{
            throw new IllegalStateException();
        }
    }

    public boolean checkIfVisited(int i, int j){
        if(this.visitedVariableCombinations.containsKey(i)){
            return this.visitedVariableCombinations.get(i).contains(j);
        }else{
            return false;
        }
    }
}
