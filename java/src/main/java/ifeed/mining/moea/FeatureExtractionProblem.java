package ifeed.mining.moea;

import ifeed.feature.logic.Literal;
import ifeed.local.params.MOEAParams;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.Utils;

import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;

import java.util.BitSet;

/**
 *
 * @author hsbang
 */

/**
 * Defines the evaluation function
 */
public class FeatureExtractionProblem extends AbstractProblem {

    public AbstractMOEABase base;

    public FeatureExtractionProblem(AbstractMOEABase base, int numberOfVariables, int numberOfObjectives){
        super(numberOfVariables, numberOfObjectives);
        this.base = base;
    }

    @Override
    public void evaluate(Solution solution){

        FeatureTreeVariable tree;

        if (solution instanceof FeatureTreeSolution) {
            FeatureTreeSolution feature = (FeatureTreeSolution) solution;
            tree = (FeatureTreeVariable) feature.getVariable(0);

        }else{
            throw new IllegalArgumentException("Wrong solution type: " + solution.getClass().getName());
        }

        Connective root = tree.getRoot();

        BitSet featureMatches = root.getMatches();
        double[] metrics = Utils.computeMetricsSetNaNZero(featureMatches, this.base.getLabels(), this.base.getSamples().size());
        double precision = metrics[2];
        double recall = metrics[3];
        double complexity = tree.getRoot().getDescendantLiterals().size();

        // Three objective
        solution.setObjective(0, - precision);
        solution.setObjective(1, - recall); // negative because MOEAFramework assumes minimization problems
        solution.setObjective(2, complexity);

        double[] objectives = new double[3];
        objectives[0] = precision;
        objectives[1] = recall;
        objectives[2] = complexity;

        if(base.isSaveResult()){
            base.recordFeature( "", root.getMatches(), objectives );
        }
    }

    @Override
    public Solution newSolution(){
        FeatureTreeVariable featureTree = new FeatureTreeVariable(this.base, new Connective(LogicalConnectiveType.AND));
        return new FeatureTreeSolution(featureTree, MOEAParams.numberOfObjectives);
    }
}
