package ifeed.problem.assigning.logicOperators.generalization.single.marginalEA;

import ifeed.Utils;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.MarginalRuleSetMOEA;
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.logicOperators.generalization.single.NotInOrbit2EmptyOrbit;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotInOrbit2EmptyOrbitWithMEA extends NotInOrbit2EmptyOrbit {

    MarginalRuleSetMOEA ruleSetMOEA;

    public NotInOrbit2EmptyOrbitWithMEA(BaseParams params, MarginalRuleSetMOEA base){
        super(params, base);
        this.ruleSetMOEA = base;
    }

    public void apply(Connective root,
                         Connective parent,
                         AbstractFilter constraintSetterAbstract,
                         Set<AbstractFilter> matchingFilters,
                         Map<AbstractFilter, Literal> nodes
    ){
        Params params = (Params) super.params;

        double[] metrics = Utils.computeMetricsSetNaNZero(root.getMatches(), super.base.getLabels(), super.base.getSamples().size());
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
                metrics = Utils.computeMetricsSetNaNZero(feat.getMatches(), base.getLabels(), base.getSamples().size());

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
