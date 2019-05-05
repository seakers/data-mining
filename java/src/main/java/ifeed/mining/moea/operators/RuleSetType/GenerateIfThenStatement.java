//package ifeed.mining.moea.operators.RuleSetType;
//
//import ifeed.feature.Feature;
//import ifeed.feature.logic.*;
//import ifeed.local.params.MOEAParams;
//import ifeed.mining.moea.AbstractMOEABase;
//import ifeed.mining.moea.FeatureTreeSolution;
//import ifeed.mining.moea.FeatureTreeVariable;
//import org.moeaframework.core.PRNG;
//import org.moeaframework.core.Solution;
//import org.moeaframework.core.Variation;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
//public class GenerateIfThenStatement implements Variation{
//
//    private double probability;
//    AbstractMOEABase base;
//
//    public GenerateIfThenStatement(double probability, AbstractMOEABase base){
//        this.probability = probability;
//        this.base = base;
//    }
//
//    @Override
//    public Solution[] evolve(Solution[] parents){
//
//        if( PRNG.nextDouble() > this.probability){
//            return parents;
//        }
//
//        Solution[] out = new Solution[1];
//        FeatureTreeVariable tree = (FeatureTreeVariable) parents[0].getVariable(0);
//        Connective root = tree.getRoot().copy();
//
//        if(root.getLiteralChildren().size() < 2){
//            // No change
//
//        }else{
//            Literal conditional = null;
//            Literal consequent = null;
//
//            List<Literal> literals = root.getLiteralChildren();
//            int conditionalIndex = PRNG.nextInt(literals.size());
//            conditional = literals.get(conditionalIndex);
//
//            while(consequent == null){
//                int consequentIndex = PRNG.nextInt(literals.size());
//                if(consequentIndex != conditionalIndex){
//                    consequent = literals.get(consequentIndex);
//                }
//            }
//            root.removeLiteral(conditional);
//            root.removeLiteral(consequent);
//
//            List<Literal> conditionalList = new ArrayList<>();
//            conditionalList.add(conditional);
//            List<Literal> consequentList = new ArrayList<>();
//            consequentList.add(consequent);
//
//            IfThenStatement ifThen = new IfThenStatement(conditionalList, consequentList);
//            root.addNode(ifThen);
//        }
//
//        base.getFeatureHandler().repairFeatureTreeStructure(root);
//        FeatureTreeVariable newTree = new FeatureTreeVariable(this.base, root);
//        Solution sol = new FeatureTreeSolution(newTree, MOEAParams.numberOfObjectives);
//        out[0] = sol;
//        return out;
//    }
//
//    @Override
//    public int getArity(){
//        return 1;
//    }
//}
