/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS;

import ifeed_dm.Apriori;
import ifeed_dm.BinaryInputFeature;
import ifeed_dm.BinaryInputFilter;
import ifeed_dm.BinaryInputArchitecture;
import ifeed_dm.DataMining;
import ifeed_dm.DataMiningParams;
import ifeed_dm.MRMR;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
/**
 *
 * @author bang
 */
public class EOSSDataMining extends DataMining{
    
        
    public EOSSDataMining(List<Integer> behavioral, List<Integer> non_behavioral, List<BinaryInputArchitecture> architectures, double supp, double conf, double lift) {
        super(behavioral, non_behavioral, architectures, supp, conf, lift); 
        
        super.candidateGenerator = new EOSSFeatureGenerator();
        
    }
    
    
    @Override
    public List<BinaryInputFeature> run(){
        
        long t0 = System.currentTimeMillis();
        
        List<BinaryInputFilter> candidate_features = super.candidateGenerator.generateCandidates();

        List<BinaryInputFeature> primitive_features = getPrimitiveFeatures(candidate_features);
        
        BitSet labels = new BitSet(super.population.size());
        for (int i = 0; i < super.population.size(); i++) {
            if (super.behavioral.contains(super.population.get(i))) {
                labels.set(i, true);
            }
        }
        
        Apriori ap = new Apriori(super.population.size(), primitive_features);
                
        ap.run(labels, super.support_threshold, super.confidence_threshold, DataMiningParams.maxLength);

        List<BinaryInputFeature> extracted_features = ap.getTopFeatures(DataMiningParams.max_number_of_features_before_mRMR, DataMiningParams.metric);
        

        if (DataMiningParams.run_mRMR) {
            
//            System.out.println("...[DrivingFeatures] Number of features before mRMR: " + drivingFeatures.size() + ", with max confidence of " + drivingFeatures.get(0).getFConfidence());
//            
//            MRMR mRMR = new MRMR();
//            this.drivingFeatures = mRMR.minRedundancyMaxRelevance( population.size(), getDataMat(this.drivingFeatures), this.labels, this.drivingFeatures, topN);
        }
        

        long t1 = System.currentTimeMillis();
        System.out.println("...[DrivingFeature] Total data mining time : " + String.valueOf(t1 - t0) + " msec");
        
        return extracted_features;    

    }
    

    
    public List<BinaryInputFeature> getPrimitiveFeatures(List<BinaryInputFilter> candidate_features){
        
        long t0 = System.currentTimeMillis();
        
        
        ArrayList<BinaryInputFeature> evaluated_features = new ArrayList<>();
        
        int size = super.population.size();

        try {
            
            double cnt_all= (double) super.non_behavioral.size() + super.behavioral.size();
            double cnt_S= (double) super.behavioral.size();
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
 
                for(int ind:super.behavioral){
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

                BinaryInputFeature feature = new BinaryInputFeature(cand.toString(), matches, support, lift, fconfidence, rconfidence);    
                
                evaluated_features.add(feature);
            }
            
            
            int iter = 0;
            ArrayList<Integer> addedFeatureIndices = new ArrayList<>();
            double[] bounds = new double[2];
            bounds[0] = 0;
            bounds[1] = (double) super.behavioral.size() / super.population.size();
            
            int minRuleNum = DataMiningParams.minRuleNum;
            int maxRuleNum = DataMiningParams.maxRuleNum;
            int maxIter = DataMiningParams.maxIter;
            double adaptSupp = (double) super.behavioral.size() / super.population.size() * 0.5;
            
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
                    
                    for (int i = 0; i < evaluated_features.size(); i++) {
                        BinaryInputFeature feature = evaluated_features.get(i);
                        
                        if (feature.getSupport() > adaptSupp) {
                            
                            addedFeatureIndices.add(i);
                            
                            if (addedFeatureIndices.size() > maxRuleNum && iter < maxIter) {
                                break;
                            } else if ((candidate_features.size() - (i + 1)) + addedFeatureIndices.size() < minRuleNum) {
                                break;
                            }
                        }
                    }
                    System.out.println("...[DrivingFeatures] number of preset rules found: " + addedFeatureIndices.size() + " with treshold: " + adaptSupp);
                }
                System.out.println("...[DrivingFeatures] preset features extracted in " + iter + " steps with size: " + addedFeatureIndices.size());
            } else {
                for (int i = 0; i < evaluated_features.size(); i++) {
                    BinaryInputFeature feature = evaluated_features.get(i);
                    if (feature.getSupport() > this.getSupportThreshold() && feature.getFConfidence() > this.getConfidenceThreshold() 
                            && feature.getRConfidence() > this.getConfidenceThreshold() && feature.getLift() >  this.getLiftThreshold()) {
                        
                        
                        addedFeatureIndices.add(i);
                    }
                }
            }

            ArrayList<BinaryInputFeature> tempFeatureList = new ArrayList<>();
            
            for (int ind : addedFeatureIndices) {
                tempFeatureList.add(evaluated_features.get(ind));
            }
            
            evaluated_features = tempFeatureList;
            

            long t1 = System.currentTimeMillis();
            System.out.println("...[DrivingFeatures] preset feature evaluation done in: " + String.valueOf(t1 - t0) + " msec");
            
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return evaluated_features;
    }


}