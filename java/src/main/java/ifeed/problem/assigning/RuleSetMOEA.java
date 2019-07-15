package ifeed.problem.assigning;

import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.feature.logic.Connective;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.local.params.MOEAParams;
import ifeed.mining.AbstractDataMiningAlgorithm;
import ifeed.mining.moea.*;
import ifeed.ontology.OntologyManager;
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.*;
import org.moeaframework.core.operator.GAVariation;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class RuleSetMOEA extends RuleSetMOEABase implements AbstractDataMiningAlgorithm {

    private OntologyManager ontologyManager;
    private boolean useGeneralizedVariables;
    private String[] orbitList;
    private String[] instrumentList;

    public RuleSetMOEA(BaseParams params, List<AbstractArchitecture> architectures,
                       List<Integer> behavioral, List<Integer> non_behavioral){

        super(params, architectures, behavioral, non_behavioral, new FeatureFetcher(params, architectures));
        this.useGeneralizedVariables = false;
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

    public void setUseGeneralizedVariables(){
        this.useGeneralizedVariables = true;
        super.resetBaseFeatures();
    }

    @Override
    public List<AbstractFilter> generateCandidates(){
        if(this.useGeneralizedVariables){
            System.out.println("Using generalized variables in the search");
            return new FeatureGenerator(super.params).generateCandidates();
        }else{
            return new FeatureGenerator(super.params).generateCandidates();
        }
    }

    @Override
    public List<Feature> run(){
        this.init();

        RuleSetMOEA base = this;

        Variation mutation  = new ifeed.mining.moea.operators.RuleSetType.RuleSetFeatureMutation(mutationProbability, base);
        Variation crossover = new ifeed.mining.moea.operators.RuleSetType.CutAndSpliceCrossover(crossoverProbability, base);
//                    Variation ifThenGen = new ifeed.mining.moea.operators.RuleSetType.GenerateIfThenStatement(ifThenGenProbability, base);
        Variation gaVariation = new GAVariation(crossover, mutation);
//                    Variation compoundVariation = new CompoundVariation(gaVariation, ifThenGen);

        problem = new FeatureExtractionProblemWithCoverageCount(base, 1, MOEAParams.numberOfObjectives);
        initialization = new FeatureExtractionInitialization(problem, popSize, "random");

        Algorithm eMOEA = new EpsilonMOEA(problem, population, archive, selection, gaVariation, initialization);
        InstrumentedSearch search = new InstrumentedSearch(eMOEA, properties, this.projectPath + File.separator + "results",  "", base);
        Algorithm alg = search.call();
        Population outputPopulation = ((AbstractEvolutionaryAlgorithm) alg).getArchive();

        List<Feature> out = new ArrayList<>();
        for(int i = 0; i < outputPopulation.size(); i++){
            FeatureTreeVariable var = (FeatureTreeVariable) outputPopulation.get(i).getVariable(0);
            Connective root = var.getRoot();
            BitSet matches = root.getMatches();
            double[] metrics = Utils.computeMetrics(matches, base.getLabels(), base.getSamples().size(), 0.0);
            Feature thisFeature = new Feature(root.getName(), root.getMatches(), metrics[0], metrics[1], metrics[2], metrics[3], root.getDescendantLiterals().size());
            out.add(thisFeature);
        }
        return out;
    }

}
