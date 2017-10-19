/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS;

import ifeed_dm.BinaryInputFeature;
import ifeed_dm.BinaryInputFilter;
import ifeed_dm.BinaryInputArchitecture;
import ifeed_dm.DataMining;
import ifeed_dm.CandidateFeatureGenerator;
import ifeed_dm.DrivingFeature;
import ifeed_dm.PrimitiveFeature;
import ifeed_dm.DataMiningParams;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
/**
 *
 * @author bang
 */
public class EOSSDataMining extends DataMining{
        
    private CandidateFeatureGenerator candidateGenerator;
    
    
    public EOSSDataMining(List<Integer> behavioral, List<Integer> non_behavioral, List<BinaryInputArchitecture> architectures, double supp, double conf, double lift) {
        super(behavioral, non_behavioral, architectures, supp, conf, lift); 
        
        candidateGenerator = new EOSSFeatureGenerator();
        
    }

    
    public List<BinaryInputFeature> getPrimitiveFeatures(List<BinaryInputFilter> candidates){
        
        List<BinaryInputFilter> candidate_features = candidateGenerator.generateCandidates();
        
        ArrayList<BinaryInputFeature> evaluated_features = new ArrayList<>();
        
        List<BinaryInputArchitecture> architectures = super.getArchitectures();
        List<Integer> population = super.getPopulation();
        List<Integer> behavioral = super.getBehavioral();
        List<Integer> non_behavioral = super.getNon_behavioral();
        
        int size = population.size();

        try {
            
            double cnt_all= (double) non_behavioral.size() + behavioral.size();
            double cnt_S= (double) behavioral.size();
            double cnt_F;
            double cnt_SF;  
            
            for(BinaryInputFilter cand: candidate_features){

                BitSet matches = new BitSet(size);
                double support;
                double lift=0.0;
                double fconfidence=0.0;
                double rconfidence;
                
                double[] metrics = {0,0,0,0};
                
                cnt_F=0.0;
                cnt_SF=0.0;
 
                for(int ind:behavioral){
                    BinaryInputArchitecture a = architectures.get(ind);
                    if(cand.apply(a.getInputs())){
                        matches.set(ind);
                        cnt_F++;
                        cnt_SF++;
                    }
                }
                
                for(int ind:non_behavioral){
                    BinaryInputArchitecture a = architectures.get(ind);
                    if(cand.apply(a.getInputs())){
                        matches.set(ind);
                        cnt_F++;
                    }
                }
                
                support = cnt_SF/cnt_all;
                if(cnt_F!=0){
                    lift = (cnt_SF/cnt_S) / (cnt_F/cnt_all);
                    fconfidence = (cnt_SF)/(cnt_F);   // confidence (feature -> selection)
                }
                rconfidence = (cnt_SF)/(cnt_S);   // confidence (selection -> feature)

                PrimitiveFeature feature = new PrimitiveFeature(matches, support, lift, fconfidence, rconfidence);    
                
                evaluated_features.add(feature);
            }
            
            
            int iter = 0;
            ArrayList<Integer> addedFeatureIndices = new ArrayList<>();
            double[] bounds = new double[2];
            bounds[0] = 0;
            bounds[1] = (double) behavioral.size() / population.size();
            
            int minRuleNum = DataMiningParams.minRuleNum;
            int maxRuleNum = DataMiningParams.maxRuleNum;
            int maxIter = DataMiningParams.maxIter;
            double adaptSupp = 0.5;
                    
            boolean apriori = true;
            if (apriori) {
                while (addedFeatureIndices.size() < minRuleNum || addedFeatureIndices.size() > maxRuleNum) {

                    iter++;
                    if (iter > maxIter) {
                        break;
                    } else if (iter > 1) {
                        // max supp threshold is support_S
                        // min supp threshold is 0
                        double a;
                        if (addedFeatureIndices.size() > maxRuleNum) { // Too many rules -> increase threshold
                            bounds[0] = adaptSupp;
                            a = bounds[1];
                        } else { // too few rules -> decrease threshold
                            bounds[1] = adaptSupp;
                            a = bounds[0];
                        }
                        // Bisection
                        adaptSupp = (double) (adaptSupp + a) * 0.5;
                    }
                    
                    addedFeatureIndices = new ArrayList<>();
                    
                    for (int i = 0; i < featureData_name.size(); i++) {
                        double[] metrics = featureData_metrics.get(i);
                        if (metrics[0] > adaptSupp) {
                            addedFeatureIndices.add(i);
                            if (addedFeatureIndices.size() > this.maxRuleNum && iter < maxIter) {
                                break;
                            } else if ((candidate_features.size() - (i + 1)) + addedFeatureIndices.size() < this.minRuleNum) {
                                break;
                            }
                        }
                    }
                    System.out.println("...[DrivingFeatures] number of preset rules found: " + addedFeatureIndices.size() + " with treshold: " + this.adaptSupp);
                }
                System.out.println("...[DrivingFeatures] preset features extracted in " + iter + " steps with size: " + addedFeatureIndices.size());
            } else {
                for (int i = 0; i < featureData_name.size(); i++) {
                    double[] metrics = featureData_metrics.get(i);
                    if (metrics[0] > thresholds[0] && metrics[1] > thresholds[1] && metrics[2] > thresholds[2] && metrics[3] > thresholds[2]) {
                        addedFeatureIndices.add(i);
                    }
                }
            }

            for (int ind : addedFeatureIndices) {
                BitSet bs = new BitSet(population.size());
                for (int j = 0; j < population.size(); j++) {

                    if(featureData_satList.get(ind)[j] > 0.0001){
                        bs.set(j);
                    }
                }
                this.presetDrivingFeatures.add(new DrivingFeature(featureData_exp.get(ind),bs));
                presetDrivingFeatures_satList.add(featureData_satList.get(ind));
            }

            long t1 = System.currentTimeMillis();
            System.out.println("...[DrivingFeatures] preset feature evaluation done in: " + String.valueOf(t1 - t0) + " msec");

            //if(apriori) return getDrivingFeatures();
            return this.presetDrivingFeatures;
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return evaluated_features;
    }

    
    
    
    
    
}
