package ifeed.mining.moea;

import ifeed.local.params.MOEAParams;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.Utils;

import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;

import java.util.BitSet;
import java.lang.Math.*;

/**
 *
 * @author hsbang
 */

/**
 * Defines the evaluation function
 */
public class FeatureExtractionProblem extends AbstractProblem {

    public MOEABase base;

    public FeatureExtractionProblem(MOEABase base, int numberOfVariables, int numberOfObjectives){
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
        double[] metrics = Utils.computeMetricsSetNaNZero(featureMatches, this.base.getLabels(), this.base.getPopulation().size());
        double coverage = metrics[2];
        double specificity = metrics[3];
        double complexity = tree.getRoot().getDescendantLiterals(true).size();

        // Set two confidences as objectives

        // Bi-objective
        //solution.setObjective(0, - Math.sqrt(coverage * specificity));
        //solution.setObjective(1, complexity);

        // Three objective
        solution.setObjective(0, - coverage); // negative because MOEAFramework assumes minimization problems
        solution.setObjective(1, - specificity);
        solution.setObjective(2, complexity);

        double[] objectives = new double[3];
        objectives[0] = coverage;
        objectives[1] = specificity;
        objectives[2] = complexity;

        if(base.isSaveResult()){
            base.recordFeature( "", root.getMatches(), objectives );
        }

        //System.out.println("Number of literals: " + tree.getRoot().getNumOfDescendantLiterals());
        //System.out.println(tree.getRoot().getName() + ": " + metrics[2] + ", " + metrics[3]);
        // TODO: Add another objective, which accounts for the complexity
    }

    @Override
    public Solution newSolution(){
        FeatureTreeVariable featureTree = new FeatureTreeVariable(this.base, new Connective(LogicalConnectiveType.AND));
        return new FeatureTreeSolution(featureTree, MOEAParams.numberOfObjectives);
    }
}
