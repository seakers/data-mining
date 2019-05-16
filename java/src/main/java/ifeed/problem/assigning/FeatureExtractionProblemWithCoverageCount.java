package ifeed.problem.assigning;

import ifeed.Utils;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.mining.moea.FeatureExtractionProblem;
import ifeed.mining.moea.FeatureTreeSolution;
import ifeed.mining.moea.FeatureTreeVariable;
import org.moeaframework.core.Solution;

import java.util.BitSet;

public class FeatureExtractionProblemWithCoverageCount extends FeatureExtractionProblem {

    public FeatureExtractionProblemWithCoverageCount(AbstractMOEABase base, int numberOfVariables, int numberOfObjectives){
        super(base, numberOfVariables, numberOfObjectives);
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

        FeatureSimplifier simplifier = new FeatureSimplifier(this.base.getParams(), (FeatureFetcher) this.base.getFeatureFetcher());
        simplifier.simplify(root);

        BitSet featureMatches = root.getMatches();
        double[] metrics = Utils.computeMetricsSetNaNZero(featureMatches, this.base.getLabels(), this.base.getSamples().size());
        double precision = metrics[2];
        double recall = metrics[3];
        double complexity = tree.getRoot().getDescendantLiterals().size();

        // Use coverage as an objective (number of times each sample is covered)
        Connective cnfRoot;
        if(this.base.getFeatureHandler().isCNF(root)){
            cnfRoot = root.copy();
        }else{
            cnfRoot = this.base.getFeatureHandler().convertToCNF(root.copy());
        }
        BitSet out = (BitSet)featureMatches.clone();
        out.and(this.base.getLabels());
        int[] setBitIndices = new int[out.cardinality()];
        int currentIndex = 0;
        for (int i = out.nextSetBit(0); i >= 0; i = out.nextSetBit(i+1)) {
            setBitIndices[currentIndex++] = i;
        }
        int[] coverage = new int[out.cardinality()];
        for(Connective branch: cnfRoot.getConnectiveChildren()){
            BitSet matches = branch.getMatches();
            for(int i = 0; i < setBitIndices.length; i++){
                int index = setBitIndices[i];
                if(matches.get(index)){
                    coverage[i]++;
                }
            }
        }
        for(Literal literal: cnfRoot.getLiteralChildren()){
            BitSet matches = literal.getMatches();
            for(int i = 0; i < setBitIndices.length; i++){
                int index = setBitIndices[i];
                if(matches.get(index)){
                    coverage[i]++;
                }
            }
        }
        int coverageCount = 0;
        int cap = 20;
        for(int count: coverage){
            if(count > cap){
                coverageCount += cap;
            }else{
                coverageCount += count;
            }
        }

        // Three objective
        solution.setObjective(0, - precision);
//        solution.setObjective(1, - recall); // negative because MOEAFramework assumes minimization problems
        solution.setObjective(1, - coverageCount);
        solution.setObjective(2, complexity);

        double[] objectives = new double[3];
        objectives[0] = precision;
//        objectives[1] = recall;
        objectives[1] = coverageCount;
        objectives[2] = complexity;

        if(base.isSaveResult()){
            base.recordFeature( "", root.getMatches(), objectives );
        }
    }
}
