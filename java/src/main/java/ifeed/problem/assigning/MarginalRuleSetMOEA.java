package ifeed.problem.assigning;

import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.ConnectiveTester;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.local.params.MOEAParams;
import ifeed.mining.AbstractDataMiningAlgorithm;
import ifeed.mining.moea.*;
import ifeed.mining.moea.operators.FeatureMutation;
import ifeed.ontology.OntologyManager;
import ifeed.problem.assigning.logicOperators.generalization.single.InstrumentGeneralizer;
import ifeed.problem.assigning.logicOperators.generalization.single.OrbitGeneralizer;
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.*;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.GAVariation;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.util.TypedProperties;
import seakers.aos.aos.AOSMOEA;
import seakers.aos.creditassignment.setimprovement.SetImprovementDominance;
import seakers.aos.operator.AOSVariation;
import seakers.aos.operator.AOSVariationSI;
import seakers.aos.operatorselectors.AdaptivePursuit;
import seakers.aos.operatorselectors.OperatorSelector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MarginalRuleSetMOEA extends RuleSetMOEABase {

    private RUN_MODE mode;
    private OntologyManager ontologyManager;
    private String[] orbitList;
    private String[] instrumentList;

    public MarginalRuleSetMOEA(BaseParams params, List<AbstractArchitecture> architectures,
                               List<Integer> behavioral, List<Integer> non_behavioral){

        super(params, architectures, behavioral, non_behavioral, new FeatureFetcher(params, architectures));

        this.mode = RUN_MODE.MOEA;
        super.setRandomFeatureGenerator(new RuleSetRandomFeatureGenerator(3, super.baseFeatures));
    }

    public void setMode(RUN_MODE mode){
        this.mode = mode;
    }

    public void setOrbitList(String[] orbitList) {
        this.orbitList = orbitList;
    }

    public void setInstrumentList(String[] instrumentList){
        this.instrumentList = instrumentList;
    }

    public void setOntologyManager(OntologyManager manager){
        this.ontologyManager = manager;
    }

    public OntologyManager getOntologyManager() {
        return ontologyManager;
    }

    @Override
    public List<AbstractFilter> generateCandidates(){
        return new FeatureGenerator(super.params).generateCandidates();
    }

    public List<Connective> run(Connective root, Connective parent, double minPrecision, double minRecall){

        MarginalRuleSetMOEA base = this;

        Params params = (Params) super.params;

        // Create tester
        ConnectiveTester tester = new ConnectiveTester(root);

        // Find the parent node within the tester tree
        ConnectiveTester parentNodeTester = null;
        for(Connective node: tester.getDescendantConnectives()){
            if(this.getFeatureHandler().featureTreeEquals(parent, node)){
                parentNodeTester = (ConnectiveTester) node;
            }
        }
        parentNodeTester.setAddNewNode();

        //parameters and operators for search
        TypedProperties properties = new TypedProperties();

        //search paramaters set here
        int popSize = 300;
        int maxEvals = 4000;
        properties.setInt("maxEvaluations", maxEvals);
        properties.setInt("populationSize", popSize);

        Problem problem = new MarginalFeatureExtractionProblem(base, tester, 1, MOEAParams.numberOfObjectives);
        Initialization initialization = new FeatureExtractionInitialization(problem, popSize, "random");

        //setup for epsilon MOEA_GP
        double[] epsilonDouble = new double[]{0.01, 0.01, 1};
        archive = new EpsilonBoxDominanceArchive(epsilonDouble);

        Population outputPopulation = new Population();

        switch (mode) {

            case MOEA: //Use epsilonMOEA

                Variation mutation  = new FeatureMutation(mutationProbability, base);
                Variation crossover = new ifeed.mining.moea.operators.RuleSetType.CutAndSpliceCrossover(crossoverProbability, base);
                Variation gaVariation = new GAVariation(crossover, mutation);

                Algorithm eMOEA = new EpsilonMOEA(problem, population, archive, selection, gaVariation, initialization);

                InstrumentedSearch run = new InstrumentedSearch(eMOEA, properties, "",  "", base);
                run.setSuppressPrintout();

                Algorithm alg = run.call();
                outputPopulation = ((AbstractEvolutionaryAlgorithm) alg).getArchive();
                break;

            case AOS: // Adaptive operator selection

                if(this.instrumentList == null || this.orbitList == null){
                    throw new IllegalStateException("Orbit list and instrument list need to be specified before running MOEA_AOS with generalization operators");
                }

                String origname = "AOS_" + System.nanoTime();

                params.setOntologyManager(this.ontologyManager);
                params.setLeftSet(this.instrumentList);
                params.setRightSet(this.orbitList);

                // Define operators
                List<Variation> operators = new ArrayList<>();
                mutation  = new FeatureMutation(mutationProbability, base);
                crossover = new ifeed.mining.moea.operators.RuleSetType.CutAndSpliceCrossover(crossoverProbability, base);
                gaVariation = new GAVariation(crossover, mutation);

                operators.add(gaVariation);
//                    operators.add(new InOrbit2Present(params, base));
//                    operators.add(new SharedNotInOrbit2AbsentPlusCond(params, base));
//                    operators.add(new NotInOrbit2EmptyOrbit(params, base));
                operators.add(new GAVariation(new InstrumentGeneralizer(params, base), mutation));
                operators.add(new GAVariation(new OrbitGeneralizer(params, base), mutation));

                properties.setDouble("pmin", 0.09);

                // Create operator selector
                OperatorSelector operatorSelector = new AdaptivePursuit(operators, 0.8, 0.8, 0.03);

                // Create credit assigning
                SetImprovementDominance creditAssignment = new SetImprovementDominance(archive, 1, 0);

                // Create AOS strategy
                AOSVariation aosStrategy = new AOSVariationSI(operatorSelector, creditAssignment, popSize);

                EpsilonMOEA emoea = new EpsilonMOEA(problem, population, archive, selection, aosStrategy, initialization, comparator);

                AOSMOEA aos = new AOSMOEA(emoea, aosStrategy, true);

                run = new InstrumentedSearch(aos, properties, "", "", base);

                alg = run.call();
                outputPopulation = ((AbstractEvolutionaryAlgorithm) alg).getArchive();
                break;

            default:
                throw new IllegalArgumentException("Unsupported mode");
        }

        List<Connective> out = new ArrayList<>();

        System.out.println(outputPopulation.size());

        for(int i = 0; i < outputPopulation.size(); i++){
            FeatureTreeVariable var = (FeatureTreeVariable) outputPopulation.get(i).getVariable(0);

            Connective tempRoot = var.getRoot();
//            BitSet matches = tempRoot.getMatches();
//            double[] metrics = Utils.computeMetricsSetNaNZero(matches, super.labels, super.samples.size());

            //System.out.println(tempRoot.getName() + " | precision: " + metrics[2] + ", recall: " + metrics[3]);

//            if(metrics[2] <= minPrecision){
//                continue;
//            }else if(metrics[3] <= minRecall){
//                continue;
//            }else{
                out.add(tempRoot);
//            }
        }

        return out;
    }

    public enum RUN_MODE {
        MOEA,
        AOS;
    }
}
