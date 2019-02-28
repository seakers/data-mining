package ifeed.mining.moea;

import ifeed.feature.logic.Formula;
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

//        // Maximize the coverage of each clause in DNF
//        Connective dnfRoot = base.getFeatureHandler().convertToDNF(root);
//        int sampleSize = base.getArchitectures().size();
//        int[] coverageCounter = new int[sampleSize];
//        int num_clauses = dnfRoot.getChildNodes().size();
//        int cnt_S = this.base.getLabels().cardinality();
//        for(Formula node: dnfRoot.getChildNodes()){
//            BitSet matches = (BitSet) node.getMatches().clone();
//            matches.and(this.base.getLabels());
//
//            for(int i = 0; i < sampleSize; i++){
//                if(matches.get(i)){
//                    coverageCounter[i] += 1;
//                }
//            }
//            clauseCumulativeCoverage += (double) matches.cardinality() / cnt_S;
//        }
//        root = dnfRoot;

        BitSet featureMatches = root.getMatches();
        double[] metrics = Utils.computeMetricsSetNaNZero(featureMatches, this.base.getLabels(), this.base.getPopulation().size());
        double precision = metrics[2];
        double recall = metrics[3];
        double complexity = tree.getRoot().getDescendantLiterals(true).size();

        // Three objective
        solution.setObjective(0, - precision);
        solution.setObjective(1, - recall); // negative because MOEAFramework assumes minimization problems
//        solution.setObjective(1, - clauseCumulativeCoverage);
        solution.setObjective(2, complexity);

        double[] objectives = new double[3];
        objectives[0] = precision;
        objectives[0] = recall;
//        objectives[1] = clauseCumulativeCoverage;
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
