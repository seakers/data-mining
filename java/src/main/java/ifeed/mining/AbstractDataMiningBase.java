/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.mining;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.filter.Filter;
import ifeed.Utils;
import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;

/**
 *
 * @author bang
 */

public abstract class AbstractDataMiningBase {

    protected List<AbstractArchitecture> architectures;
    protected List<Integer> behavioral;
    protected List<Integer> non_behavioral;
    protected List<Integer> population;
    protected BitSet labels;

    public AbstractDataMiningBase(List<AbstractArchitecture> architectures,
                                  List<Integer> behavioral, List<Integer> non_behavioral){

        this.architectures = architectures;
        this.behavioral = behavioral;
        this.non_behavioral = non_behavioral;

        this.population = new ArrayList<>();
        this.population.addAll(this.behavioral);
        this.population.addAll(this.non_behavioral);

        // Set label
        this.labels = new BitSet(this.architectures.size());
        for (int i = 0; i < this.architectures.size(); i++) {
            AbstractArchitecture a = this.architectures.get(i);
            if (this.behavioral.contains(a.getID())) {
                this.labels.set(i);
            }
        }
    }

    public abstract List<Filter> generateCandidates();
    public List<AbstractArchitecture> getArchitectures(){return this.architectures;}
    public List<Integer> getBehavioral(){return this.behavioral;}
    public List<Integer> getNon_behavioral(){return this.non_behavioral;}
    public List<Integer> getPopulation(){return this.population;}
    public BitSet getLabels(){ return this.labels; }

    public List<Feature> generateBaseFeatures(){
        List<Filter> candidates = this.generateCandidates();
        return this.evaluateBaseFeatures(candidates);
    }

    public List<Feature> evaluateBaseFeatures(List<Filter> candidate_features){

        ArrayList<Feature> evaluated_features = new ArrayList<>();
        int size = this.population.size();

        try {
            for(Filter cand: candidate_features){

                BitSet matches = new BitSet(size);
                int i=0;
                for(AbstractArchitecture a: architectures){
                    if(cand.apply(a)){
                        matches.set(i);
                    }
                    i++;
                }

                double[] metrics = Utils.computeMetricsSetNaNZero(matches, this.labels, this.architectures.size());
                Feature feature = new Feature(cand.toString(), matches, metrics[0], metrics[1], metrics[2], metrics[3]);
                evaluated_features.add(feature);
            }

        }catch(Exception e){
            System.out.println("Exc in evaluating the base features: " + e.getMessage());
            e.printStackTrace();
        }

        return evaluated_features;
    }
}
