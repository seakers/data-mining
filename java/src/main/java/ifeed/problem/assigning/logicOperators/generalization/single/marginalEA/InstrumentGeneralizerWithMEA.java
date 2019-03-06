package ifeed.problem.assigning.logicOperators.generalization.single.marginalEA;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import ifeed.Utils;
import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFinder;
import ifeed.local.params.BaseParams;
import ifeed.mining.moea.AbstractMOEABase;
import ifeed.mining.moea.operators.AbstractLogicOperator;
import ifeed.problem.assigning.MarginalRuleSetMOEA;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.InOrbit;
import ifeed.problem.assigning.filters.NotInOrbit;
import ifeed.problem.assigning.filters.Separate;
import ifeed.problem.assigning.filters.Together;
import ifeed.problem.assigning.logicOperators.generalization.single.InstrumentGeneralizer;

import java.util.*;

public class InstrumentGeneralizerWithMEA extends InstrumentGeneralizer {

    MarginalRuleSetMOEA ruleSetMOEA;

    public InstrumentGeneralizerWithMEA(BaseParams params, MarginalRuleSetMOEA base) {
        super(params, base);
        this.ruleSetMOEA = base;
    }

    @Override
    public void apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){
        Params params = (Params) super.params;

        double[] metrics = Utils.computeMetricsSetNaNZero(root.getMatches(), super.base.getLabels(), super.base.getPopulation().size());
        double minPrecision = metrics[2];
        double minRecall = metrics[3];

        // Apply generalization
        super.apply(root, parent, constraintSetterAbstract, matchingFilters, nodes);

        // Add conditions using EA
        List<Connective> out = this.ruleSetMOEA.run(root, parent, minPrecision, minRecall);

        if(!out.isEmpty()){

            // Find the best feature
            Connective bestFeatureTree = out.get(0);
            double minDistance2UP = Double.MAX_VALUE;

            for(Connective feat: out){
                metrics = Utils.computeMetricsSetNaNZero(feat.getMatches(), base.getLabels(), base.getPopulation().size());

                double distance2UP = Math.sqrt(Math.pow(1-metrics[2], 2) + Math.pow(1-metrics[3], 2));

                if(distance2UP < minDistance2UP){
                    bestFeatureTree = feat;
                }
            }

            parent.addNode(bestFeatureTree);

            System.out.println(bestFeatureTree.getName() + " added");

        }else{
            System.out.println("No improvement found using EA");
        }
    }
}
