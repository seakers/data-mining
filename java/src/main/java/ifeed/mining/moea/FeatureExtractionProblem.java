package ifeed.mining.moea;

import ifeed.local.MOEAParams;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.LogicOperator;
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

    public MOEABase base;

    public FeatureExtractionProblem(int numberOfVariables, int numberOfObjectives, MOEABase base){
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

        BitSet featureMatches = tree.getRoot().getMatches();
        double[] metrics = Utils.computeMetricsSetNaNZero(featureMatches, this.base.getLabels(), this.base.getPopulation().size());

        double coverage;
        double specificity;

        // Set two confidences as objectives
        solution.setObjective(0, - metrics[2]); // negative because MOEAFramework assumes minimization problems
        solution.setObjective(1, - metrics[3]);
        solution.setObjective(2, tree.getRoot().getDescendantLiterals(true).size());

        //System.out.println("Number of literals: " + tree.getRoot().getNumOfDescendantLiterals());
        //System.out.println(tree.getRoot().getName() + ": " + metrics[2] + ", " + metrics[3]);
        // TODO: Add another objective, which accounts for the complexity
    }

    @Override
    public Solution newSolution(){
        FeatureTreeVariable featureTree = new FeatureTreeVariable(new Connective(LogicOperator.AND), this.base);
        return new FeatureTreeSolution(featureTree, MOEAParams.numberOfObjectives);
    }
}
