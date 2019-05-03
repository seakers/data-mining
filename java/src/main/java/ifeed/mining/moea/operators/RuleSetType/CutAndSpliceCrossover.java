package ifeed.mining.moea.operators.RuleSetType;

import ifeed.feature.FeatureExpressionHandler;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Formula;
import ifeed.feature.logic.Literal;
import ifeed.feature.logic.LogicalConnectiveType;
import ifeed.local.params.MOEAParams;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.mining.moea.FeatureTreeSolution;
import ifeed.mining.moea.FeatureTreeVariable;
import ifeed.mining.moea.GPMOEABase;
import ifeed.mining.moea.operators.AbstractFeatureCrossover;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CutAndSpliceCrossover extends AbstractFeatureCrossover{

    public CutAndSpliceCrossover(double probability, AbstractMOEABase base){
        super(probability, base);
    }

    @Override
    public Solution[] evolve(Solution[] parents){

        if( PRNG.nextDouble() > super.probability){
            return parents;
        }

        Solution[] out = new Solution[2];
        FeatureTreeVariable tree1 = (FeatureTreeVariable) parents[0].getVariable(0);
        FeatureTreeVariable tree2 = (FeatureTreeVariable) parents[1].getVariable(0);

        // Copy the root nodes
        Connective root1 = tree1.getRoot().copy();
        Connective root2 = tree2.getRoot().copy();

        // Make feature trees follow CNF
        if((!root1.getConnectiveChildren().isEmpty() || root1.getLogic() != LogicalConnectiveType.AND) ||
                !root2.getConnectiveChildren().isEmpty() || root2.getLogic() != LogicalConnectiveType.AND){
            throw new IllegalStateException("To use " + this.getClass().getName() + " operator, all input features should be a conjunctive clause.");
        }

        Connective parent1 = root1;
        Connective parent2 = root2;

        while(true){
            int cut1 = PRNG.nextInt(root1.getChildNodes().size());
            int cut2 = PRNG.nextInt(root2.getChildNodes().size());
            cutAndSplice(parent1, parent2, cut1, cut2);
            if(!parent1.getChildNodes().isEmpty() && !parent2.getChildNodes().isEmpty()){
                break;
            }
        }

        FeatureTreeVariable newTree1 = new FeatureTreeVariable(this.base, root1);
        FeatureTreeVariable newTree2 = new FeatureTreeVariable(this.base, root2);
        Solution sol1 = new FeatureTreeSolution(newTree1, MOEAParams.numberOfObjectives);
        Solution sol2 = new FeatureTreeSolution(newTree2, MOEAParams.numberOfObjectives);
        out[0] = sol1;
        out[1] = sol2;
        return out;
    }

    public void cutAndSplice(Connective parent1, Connective parent2, int cut1, int cut2) {

        List<Formula> nodes1 = parent1.getChildNodes();
        List<Formula> nodes2 = parent2.getChildNodes();

        List<Formula> subList1 = new ArrayList<>();
        List<Formula> subList2 = new ArrayList<>();
        for(int i = cut1; i < nodes1.size(); i++){
            subList1.add(nodes1.get(i));
        }
        for(int i = cut2; i < nodes2.size(); i++){
            subList2.add(nodes2.get(i));
        }

        nodes1.subList(cut1, parent1.getChildNodes().size()).clear();
        nodes2.subList(cut2, parent2.getChildNodes().size()).clear();
        nodes1.addAll(subList2);
        nodes2.addAll(subList1);

        parent1.removeNodes();
        parent2.removeNodes();
        parent1.addNodes(nodes1);
        parent2.addNodes(nodes2);
    }
}
