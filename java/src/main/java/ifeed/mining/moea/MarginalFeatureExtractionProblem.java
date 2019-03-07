package ifeed.mining.moea;

import ifeed.Utils;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.ConnectiveTester;
import org.moeaframework.core.Solution;
import java.util.BitSet;

/**
 *
 * @author hsbang
 */

/**
 * Defines the evaluation function
 */
public class MarginalFeatureExtractionProblem extends FeatureExtractionProblem {

    ConnectiveTester root;

    public MarginalFeatureExtractionProblem(AbstractMOEABase base, ConnectiveTester root, int numberOfVariables, int numberOfObjectives){
        super(base, numberOfVariables, numberOfObjectives);
        this.root = root;
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

        Connective subtree = tree.getRoot();

        // Add subtree to the whole tree
        this.root.setNewNode(subtree);

        try{
            BitSet featureMatches = this.root.getMatches();
            double[] metrics = Utils.computeMetricsSetNaNZero(featureMatches, this.base.getLabels(), this.base.getPopulation().size());
            double precision = metrics[2];
            double recall = metrics[3];
            double complexity = tree.getRoot().getDescendantLiterals(true).size();

            //System.out.println(this.root.getName() + " | cardinality: " + featureMatches.cardinality() +", precision: " + metrics[2] + ", recall: " + metrics[3]);

            // Three objective
            solution.setObjective(0, - precision);
            solution.setObjective(1, - recall); // negative because MOEAFramework assumes minimization problems
            solution.setObjective(2, complexity);

        }catch (IllegalStateException e){
            System.out.println(this.root.getName());
            throw e;
        }
    }
}
