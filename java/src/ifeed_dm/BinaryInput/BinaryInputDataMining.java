/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.binaryInput;

import ifeed_dm.DataMiningParams;
import ifeed_dm.Feature;
import ifeed_dm.Filter;

import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;

/**
 *
 * @author bang
 */

public abstract class BinaryInputDataMining {
    
    protected double support_threshold;
    protected double confidence_threshold;
    protected double lift_threshold;
    protected double [] thresholds;
    
    protected List<BinaryInputArchitecture> architectures;
    
    protected List<Integer> behavioral;
    protected List<Integer> non_behavioral;
    protected List<Integer> population;
    
    protected BinaryInputCandidateFeatureGenerator candidateGenerator;
    
    
    public BinaryInputDataMining(List<Integer> behavioral, List<Integer> non_behavioral, List<BinaryInputArchitecture> architectures,
                            double supp, double conf, double lift){
            
        this.support_threshold = supp;
        this.confidence_threshold = conf;
        this.lift_threshold = lift;
        
        this.thresholds = new double[3];
        thresholds[0] = support_threshold;
        thresholds[1] = lift_threshold;
        thresholds[2] = confidence_threshold;

        this.architectures = architectures;
        this.behavioral = behavioral;
        this.non_behavioral = non_behavioral;

        this.population = new ArrayList<>();
        this.population.addAll(this.behavioral);
        this.population.addAll(this.non_behavioral);
    }
    
    
    public abstract List<Feature> run();
    
    
    public List<Feature> generateBaseFeatures(boolean adjustRuleNum){
        
        List<Filter> candidates = this.candidateGenerator.generateCandidates();
        List<Feature> evaluatedFeatures = evaluateBaseFeatures(candidates);
        
        if(adjustRuleNum){
            evaluatedFeatures= adjustBaseFeatureSize(evaluatedFeatures);
        }
        
        System.out.println("...[DataMining] Number of base features generated: " + evaluatedFeatures.size());
        return evaluatedFeatures;
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

                for(BinaryInputArchitecture a: architectures){
                    
                    if(cand.apply(a.getInputs())){
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
    
    
    
    public List<Feature> adjustBaseFeatureSize(List<Feature> baseFeatures){
    
        ArrayList<Feature> reducedFeatureList = new ArrayList<>();
        
        if(DataMiningParams.hardLimitRuleNum){

            int iter = 0;
            ArrayList<Integer> addedFeatureIndices = new ArrayList<>();

            double[] bounds = new double[2];
            bounds[0] = 0;
            bounds[1] = (double) this.behavioral.size() / this.population.size();

            int minRuleNum = DataMiningParams.minRuleNum;
            int maxRuleNum = DataMiningParams.maxRuleNum;
            int maxIter = DataMiningParams.maxIter;
            double adaptSupp = (double) this.behavioral.size() / this.population.size() * 0.5; // 1/2 of the maximum possible support


            while (addedFeatureIndices.size() < minRuleNum || addedFeatureIndices.size() > maxRuleNum) {

                iter++;
                if (iter > maxIter) {
                    break;
                } else if (iter > 1) {
                    // max supp threshold is support_S
                    // min supp threshold is 0

                    double a;
                    if (addedFeatureIndices.size() > maxRuleNum) { // Too many rules -> increase threshold
                        bounds[0] = adaptSupp; // Set the minimum bound to the current level
                        a = bounds[1];
                    } else { // too few rules -> decrease threshold
                        bounds[1] = adaptSupp;
                        a = bounds[0];
                    }
                    // Bisection
                    adaptSupp = (double) (adaptSupp + a) * 0.5;
                }

                addedFeatureIndices = new ArrayList<>();

                for (int i = 0; i < baseFeatures.size(); i++) {
                    // For each feature
                    Feature feature = baseFeatures.get(i);

                    // Check if each feature has the minimum support and count the number
                    if (feature.getSupport() > adaptSupp) {

                        addedFeatureIndices.add(i);

                        if (addedFeatureIndices.size() > maxRuleNum) {
                            break;
                        } else if ((baseFeatures.size() - (i + 1)) + addedFeatureIndices.size() < minRuleNum) {
                            break;
                        }
                    }
                }
            }

            this.support_threshold = adaptSupp;
            System.out.println("...[DrivingFeatures] Adjusting the support threshold... in " + iter + " steps with rule size: " + addedFeatureIndices.size());

            for (int ind : addedFeatureIndices) {
                reducedFeatureList.add(baseFeatures.get(ind));
            }

        }else{
            for (Feature feature:baseFeatures) {

                if (feature.getSupport() > this.support_threshold) {
                    reducedFeatureList.add(feature);
                }
            }

        }

        return reducedFeatureList;
    }
    
   
    
    public List<BinaryInputArchitecture> getArchitectures(){return this.architectures;}
    public List<Integer> getBehavioral(){return this.behavioral;}
    public List<Integer> getNon_behavioral(){return this.non_behavioral;}
    public List<Integer> getPopulation(){return this.population;}
    public double getSupportThreshold(){return this.support_threshold;}
    public double getConfidenceThreshold(){return this.confidence_threshold;}
    public double getLiftThreshold(){return this.lift_threshold;}
}
