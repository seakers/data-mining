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
import ifeed_dm.Feature;
import ifeed_dm.Utils;

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
    
    BitSet labels;
        
    public EOSSDataMining(List<Integer> behavioral, List<Integer> non_behavioral, List<BinaryInputArchitecture> architectures, double supp, double conf, double lift) {
        super(behavioral, non_behavioral, architectures, supp, conf, lift); 
        
        super.candidateGenerator = new EOSSFeatureGenerator();     
        
        // Set label
        this.labels = new BitSet(super.architectures.size());
        for (int i = 0; i < super.architectures.size(); i++) {
            BinaryInputArchitecture a = super.architectures.get(i);
            if (super.behavioral.contains(a.getID())) {
                this.labels.set(i);
            }
        }        
    }

    
    
    
    @Override
    public List<Feature> run(){
        
        long t0 = System.currentTimeMillis();
        
        List<BinaryInputFeature> baseFeatures = super.generateBaseFeatures(true); 

        System.out.println("General data mining run initiated");
        System.out.println("...[EOSSDataMining] The number of candidate features: " + baseFeatures.size());

        // Run Apriori algorithm
        Apriori ap = new Apriori(super.population.size(), baseFeatures, labels);
        
        ap.run(super.support_threshold, super.confidence_threshold, DataMiningParams.maxLength);

        //List<BinaryInputFeature> extracted_features = ap.getTopFeatures(DataMiningParams.max_number_of_features_before_mRMR, DataMiningParams.metric);
        
        FeatureComparator comparator1 = new FeatureComparator(FeatureMetric.FCONFIDENCE);
        FeatureComparator comparator2 = new FeatureComparator(FeatureMetric.RCONFIDENCE);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
        
        List<Feature> mined_features = ap.exportFeatures();
        
        List<Feature> extracted_features = Utils.getFeatureFuzzyParetoFront(mined_features,comparators,3);

        extracted_features = Utils.getTopFeatures(extracted_features, DataMiningParams.max_number_of_features_before_mRMR);

        
        if (DataMiningParams.run_mRMR) {
            
//            System.out.println("...[DrivingFeatures] Number of features before mRMR: " + drivingFeatures.size() + ", with max confidence of " + drivingFeatures.get(0).getFConfidence());
//            
//            MRMR mRMR = new MRMR();
//            this.drivingFeatures = mRMR.minRedundancyMaxRelevance( population.size(), getDataMat(this.drivingFeatures), this.labels, this.drivingFeatures, topN);
        }
        

        long t1 = System.currentTimeMillis();
        System.out.println("...[EOSSDataMining] Total features found: " + extracted_features.size());
        System.out.println("...[EOSSDataMining] Total data mining time : " + String.valueOf(t1 - t0) + " msec");
        
        return extracted_features;    

    }
    
    

    
    public List<Feature> runLocalSearch(String featureExpression){
        
        long t0 = System.currentTimeMillis();
        
        List<BinaryInputFeature> baseFeatures = super.generateBaseFeatures(false); 
        
        System.out.println("Local search initiated");
        System.out.println("...[EOSSDataMining] The number of candidate features: " + baseFeatures.size());
        
        EOSSFilterExpressionHandler filterExpressionHandler = new EOSSFilterExpressionHandler(super.architectures.size(), baseFeatures);
        
        FeatureTreeNode root = filterExpressionHandler.generateFeatureTree(featureExpression);

        List<Feature> minedFeatures = new ArrayList<>();
        
        int i=0;
        // Add a base feature to the given feature, replacing the placeholder
        for(BinaryInputFeature feature:baseFeatures){
                                    
            root.setPlaceholderFeature(feature.getMatches(), feature.getName());
                        
            BitSet matches = root.getMatches();
                    
            double[] metrics = Utils.computeMetrics(matches,this.labels,super.population.size());
            
            String name = root.getName();
            
            BinaryInputFeature newFeature = new BinaryInputFeature(name, matches, metrics[0], metrics[1], metrics[2], metrics[3]);
            
            minedFeatures.add(newFeature);
            
        }
        
        FeatureComparator comparator1 = new FeatureComparator(FeatureMetric.FCONFIDENCE);
        FeatureComparator comparator2 = new FeatureComparator(FeatureMetric.RCONFIDENCE);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));        
        
        List<Feature> extracted_features = Utils.getFeatureFuzzyParetoFront(minedFeatures,comparators,0);
        
        long t1 = System.currentTimeMillis();
        System.out.println("...[EOSSDataMining] Total features found: " + minedFeatures.size() + ", Pareto front: " + extracted_features.size());
        System.out.println("...[EOSSDataMining] Total data mining time : " + String.valueOf(t1 - t0) + " msec");
        
        return extracted_features;

    }
    
    

    public List<Feature> runLocalSearch(String featureName, List<Integer> archsWithFeature){
        
        BitSet matches = new BitSet(super.architectures.size());
        
        for (int i = 0; i < super.architectures.size(); i++) {
            
            BinaryInputArchitecture a = super.architectures.get(i);
            if (archsWithFeature.contains(a.getID())){
                matches.set(i);
            }
        }
        
        BinaryInputFeature feature = new BinaryInputFeature(featureName, matches);  
        
        return runLocalSearch(feature);
    }

    
    public List<Feature> runLocalSearch(BinaryInputFeature feature){
        
        long t0 = System.currentTimeMillis();
                
        List<BinaryInputFeature> baseFeatures = super.generateBaseFeatures(false); 
        
        System.out.println("Local search initiated");
        System.out.println("...[EOSSDataMining] The number of candidate features: " + baseFeatures.size());                
        System.out.println("...[EOSSDataMining] Local search root feature name: " + feature.getName());
        
        baseFeatures.add(feature);
                
        Apriori ap = new Apriori(super.population.size(), baseFeatures, labels);
        ap.run(baseFeatures.size()-1,super.support_threshold, super.confidence_threshold, DataMiningParams.maxLength);
        
        List<Feature> mined_features = ap.exportFeatures();
        
        FeatureComparator comparator1 = new FeatureComparator(FeatureMetric.FCONFIDENCE);
        FeatureComparator comparator2 = new FeatureComparator(FeatureMetric.RCONFIDENCE);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
        
        List<Feature> extracted_features = Utils.getFeatureFuzzyParetoFront(mined_features,comparators,0);
        
        extracted_features = Utils.getTopFeatures(extracted_features, DataMiningParams.max_number_of_features_before_mRMR);
        
        
        long t1 = System.currentTimeMillis();
        
        System.out.println("...[EOSSDataMining] Total features found: " + mined_features.size() + ", Pareto front: " + extracted_features.size());
        System.out.println("...[EOSSDataMining] Total data mining time : " + String.valueOf(t1 - t0) + " msec");
        
        return extracted_features;    
        
    }
    

}