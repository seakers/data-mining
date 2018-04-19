package ifeed.problem.eoss.logicOperators.generalization;

import ifeed.feature.logic.LogicOperator;
import ifeed.feature.logic.Connective;
import ifeed.mining.moea.MOEABase;

import org.moeaframework.core.Variation;
import org.moeaframework.core.Solution;

public class inOrbit2Present implements LogicOperator, Variation{

    protected double probability;
    protected MOEABase base;

    public inOrbit2Present(double probability, MOEABase base) {
        this.probability = probability;
        this.base = base;
    }

    @Override
    public Solution[] evolve(Solution[] parents){
        return parents;
    }

    @Override
    public boolean checkApplicability(Connective a){
        return false;
    }

    @Override
    public void apply(Connective a){

    }

    @Override
    public int getArity(){
        return 0;
    }

}
