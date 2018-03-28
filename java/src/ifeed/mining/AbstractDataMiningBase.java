/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.mining;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.filter.Filter;
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
            double cnt_all= (double) this.non_behavioral.size() + this.behavioral.size();
            double cnt_S= (double) this.behavioral.size();
            double cnt_F;
            double cnt_SF;

            for(Filter cand: candidate_features){

                BitSet matches = new BitSet(size);
                double support;
                double lift=0.0;
                double fconfidence=0.0;
                double rconfidence;

                cnt_F=0.0;
                cnt_SF=0.0;

                int i=0;

                for(AbstractArchitecture a: architectures){

                    if(cand.apply(a)){
                        matches.set(i);
                        cnt_F++;
                        if(this.behavioral.contains(a.getID())){
                            cnt_SF++;
                        }
                    }
                    i++;
                }

                support = cnt_SF/cnt_all;

                if(cnt_F!=0){
                    lift = (cnt_SF/cnt_S) / (cnt_F/cnt_all);
                    fconfidence = (cnt_SF)/(cnt_F);   // confidence (feature -> selection)
                }
                rconfidence = (cnt_SF)/(cnt_S);   // confidence (selection -> feature)

                Feature feature = new Feature(cand.toString(), matches, support, lift, fconfidence, rconfidence);

                evaluated_features.add(feature);
            }

        }catch(Exception e){
            System.out.println("Exe in evaluating the base features: " + e.getMessage());
        }

        return evaluated_features;
    }
}
