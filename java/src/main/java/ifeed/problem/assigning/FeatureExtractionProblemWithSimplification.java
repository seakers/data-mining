package ifeed.problem.assigning;

import ifeed.Utils;
import ifeed.feature.FeatureExpressionHandler;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.mining.moea.FeatureExtractionProblem;
import ifeed.mining.moea.FeatureTreeSolution;
import ifeed.mining.moea.FeatureTreeVariable;
import org.moeaframework.core.Solution;

import java.util.BitSet;

public class FeatureExtractionProblemWithSimplification extends FeatureExtractionProblem {

    private FeatureExpressionHandler expressionHandler;

    public FeatureExtractionProblemWithSimplification(AbstractMOEABase base,
                                                      int numberOfVariables,
                                                      int numberOfObjectives,
                                                      FeatureExpressionHandler expressionHandler){

        super(base, numberOfVariables, numberOfObjectives);
        this.expressionHandler = expressionHandler;
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
        FeatureSimplifier simplifier = new FeatureSimplifier(this.base.getParams(), (FeatureFetcher) this.base.getFeatureFetcher(), expressionHandler);
        simplifier.simplify(root);

        super.evaluate(solution);
    }
}
