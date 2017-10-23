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
import ifeed_dm.FeatureComparator;
import ifeed_dm.FeatureMetric;
//import ifeed_dm.MRMR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
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
        
        System.out.println("...[DrivingFeatures] The number of candidate features: " + candidate_features.size());

        List<BinaryInputFeature> primitive_features = getBaseFeatures(candidate_features);      
        
        BitSet labels = new BitSet(super.architectures.size());
        for (int i = 0; i < super.architectures.size(); i++) {
            BinaryInputArchitecture a = super.architectures.get(i);
            if (super.behavioral.contains(a.getID())) {
                labels.set(i);
            }
        }
        
        Apriori ap = new Apriori(super.population.size(), primitive_features, labels);
                
        ap.run(super.support_threshold, super.confidence_threshold, DataMiningParams.maxLength);

        //List<BinaryInputFeature> extracted_features = ap.getTopFeatures(DataMiningParams.max_number_of_features_before_mRMR, DataMiningParams.metric);
        
        FeatureComparator comparator1 = new FeatureComparator(FeatureMetric.FCONFIDENCE);
        FeatureComparator comparator2 = new FeatureComparator(FeatureMetric.RCONFIDENCE);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
        
        List<BinaryInputFeature> extracted_features = ap.getFuzzyParetoFront(comparators,2, DataMiningParams.max_number_of_features_before_mRMR);
        
        

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
    
    
    
    public List<BinaryInputFeature> run_local_search(String featureName, List<Integer> archsWithFeature){
        
        long t0 = System.currentTimeMillis();
        
        List<BinaryInputFilter> candidate_features = super.candidateGenerator.generateCandidates();
        
        System.out.println("...[DrivingFeatures] The number of candidate features: " + candidate_features.size());
        
        List<BinaryInputFeature> primitive_features = getBaseFeatures(candidate_features);     
        
        BitSet labels = new BitSet(super.architectures.size());
        BitSet matches = new BitSet(super.architectures.size());
        
        for (int i = 0; i < super.architectures.size(); i++) {
            BinaryInputArchitecture a = super.architectures.get(i);
            if (super.behavioral.contains(a.getID())) {
                labels.set(i);
            }
            if (archsWithFeature.contains(a.getID())){
                matches.set(i);
            }
        }
        
        System.out.println("...[]DrivingFeatures] Root feature name: " + featureName);
        
        BinaryInputFeature feature = new BinaryInputFeature(featureName, matches);  
        primitive_features.add(feature);
                
        Apriori ap = new Apriori(super.population.size(), primitive_features, labels);
        ap.run(primitive_features.size()-1,super.support_threshold, super.confidence_threshold, DataMiningParams.maxLength);
        
        
        FeatureComparator comparator1 = new FeatureComparator(FeatureMetric.FCONFIDENCE);
        FeatureComparator comparator2 = new FeatureComparator(FeatureMetric.RCONFIDENCE);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
        
        List<BinaryInputFeature> extracted_features = ap.getFuzzyParetoFront(comparators, 0, DataMiningParams.max_number_of_features_before_mRMR);

        long t1 = System.currentTimeMillis();
        System.out.println("...[DrivingFeature] Total data mining time : " + String.valueOf(t1 - t0) + " msec");
        
        return extracted_features;    
    }
    
    
   
    
    public List<BinaryInputFeature> getBaseFeatures(List<BinaryInputFilter> candidate_features){
        
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
                
                cnt_F=0.0;
                cnt_SF=0.0;
                
                int i=0;

                for(BinaryInputArchitecture a: architectures){
                    
                    if(cand.apply(a.getInputs())){
                        matches.set(i);
                        cnt_F++;
                        if(super.behavioral.contains(a.getID())){
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
                
                BinaryInputFeature feature = new BinaryInputFeature(cand.toString(), matches, support, lift, fconfidence, rconfidence);    
                               
                evaluated_features.add(feature);
            }
            
            
            if (DataMiningParams.runApriori) {
                
                if(DataMiningParams.limitRuleNum){
                    
                    int iter = 0;
                    ArrayList<Integer> addedFeatureIndices = new ArrayList<>();

                    double[] bounds = new double[2];
                    bounds[0] = 0;
                    bounds[1] = (double) super.behavioral.size() / super.population.size();

                    int minRuleNum = DataMiningParams.minRuleNum;
                    int maxRuleNum = DataMiningParams.maxRuleNum;
                    int maxIter = DataMiningParams.maxIter;
                    double adaptSupp = (double) super.behavioral.size() / super.population.size() * 0.5; // 1/2 of the maximum possible support

                                
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

                        for (int i = 0; i < evaluated_features.size(); i++) {
                            // For each feature
                            BinaryInputFeature feature = evaluated_features.get(i);

                            // Check if each feature has the minimum support and count the number
                            if (feature.getSupport() > adaptSupp) {

                                addedFeatureIndices.add(i);

                                if (addedFeatureIndices.size() > maxRuleNum) {
                                    break;
                                } else if ((candidate_features.size() - (i + 1)) + addedFeatureIndices.size() < minRuleNum) {
                                    break;
                                }
                            }
                        }
                    }
                    
                    super.support_threshold = adaptSupp;
                    System.out.println("...[DrivingFeatures] Adjusting the support threshold... in " + iter + " steps with rule size: " + addedFeatureIndices.size());
                    
                    ArrayList<BinaryInputFeature> tempFeatureList = new ArrayList<>();

                    for (int ind : addedFeatureIndices) {
                        tempFeatureList.add(evaluated_features.get(ind));
                    }

                    evaluated_features = tempFeatureList;
                    
                    
                }else{
                
                    ArrayList<BinaryInputFeature> tempFeatureList = new ArrayList<>();

                    for (BinaryInputFeature feature:evaluated_features) {

                        if (feature.getSupport() > super.support_threshold) {
                            tempFeatureList.add(feature);
                        }
                    }
                    
                    evaluated_features = tempFeatureList;
                }

            } else {
                
                ArrayList<BinaryInputFeature> tempFeatureList = new ArrayList<>();

                for (BinaryInputFeature feature:evaluated_features) {
                    
                    if (feature.getSupport() > super.support_threshold && feature.getFConfidence() > super.confidence_threshold
                            && feature.getRConfidence() > super.confidence_threshold && feature.getLift() >  super.lift_threshold) {
                        
                        tempFeatureList.add(feature);
                    }
                }
                
                evaluated_features = tempFeatureList;
            }

            long t1 = System.currentTimeMillis();
            
            System.out.println("...[DrivingFeatures] Number of preset rules found: " + evaluated_features.size() + " with treshold: " + super.support_threshold);
            System.out.println("...[DrivingFeatures] Preset feature evaluation done in: " + String.valueOf(t1 - t0) + " msec");
            
            
        }catch(Exception e){
            e.printStackTrace();
        }
        
        return evaluated_features;
    }


}