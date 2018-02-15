/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS;

import ifeed_dm.*;
import ifeed_dm.binaryInput.BinaryInputArchitecture;
import ifeed_dm.binaryInput.BinaryInputDataMining;
import ifeed_dm.logic.Connective;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

//import ifeed_dm.MRMR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;


/**
 *
 * @author bang
 */


public class EOSSDataMining extends BinaryInputDataMining{
    
    BitSet labels;
    
    public EOSSDataMining(List<Integer> behavioral, List<Integer> non_behavioral, List<BinaryInputArchitecture> architectures, double supp, double conf, double lift, Set<Integer> restrictedInstrumentSet) {
        this(behavioral, non_behavioral, architectures, supp, conf, lift);
        super.candidateGenerator = new EOSSFeatureGenerator(restrictedInstrumentSet);           
    }    
    
        
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
        
        System.out.println("General data mining run initiated");
        
        List<BaseFeature> baseFeatures = super.generateBaseFeatures(true); 
        
        //writeToFile(baseFeatures);
    
        System.out.println("...[EOSSDataMining] The number of candidate features: " + baseFeatures.size());

        // Run Apriori algorithm
        Apriori ap = new Apriori(super.population.size(), baseFeatures, labels);
        
        ap.run(super.support_threshold, super.confidence_threshold, DataMiningParams.maxLength);

        //List<BinaryInputFeature> extracted_features = ap.getTopFeatures(DataMiningParams.max_number_of_features_before_mRMR, DataMiningParams.metric);
        
        FeatureComparator comparator1 = new FeatureComparator(FeatureMetric.FCONFIDENCE);
        FeatureComparator comparator2 = new FeatureComparator(FeatureMetric.RCONFIDENCE);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));
        
        List<Feature> extracted_features = ap.exportFeatures();
        
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
    

    public void writeToFile(List<BaseFeature> baseFeatures){
    
        File file = new File("/Users/bang/workspace/FeatureExtractionGA/data/baseFeatures");
        File file2 = new File("/Users/bang/workspace/FeatureExtractionGA/data/featureNames");
        File file3 = new File("/Users/bang/workspace/FeatureExtractionGA/data/labels");
        
        try{
                    
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            
            BufferedWriter featureNameWriter = new BufferedWriter(new FileWriter(file2));
            
            String printRow = "";

            for(int j=0;j<baseFeatures.size();j++){

                BitSet bs = baseFeatures.get(j).getMatches();
                int nbits = bs.size();

                final StringBuilder buffer = new StringBuilder(nbits);
                IntStream.range(0, nbits).mapToObj(i -> bs.get(i) ? '1' : '0').forEach(buffer::append);

                writer.write(buffer.toString() + "\n");
                featureNameWriter.write(baseFeatures.get(j).getName() + "\n");
            }
            
            System.out.println("Done");
            writer.close();
            featureNameWriter.close();
        
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
        
        try{
                    
            BufferedWriter writer = new BufferedWriter(new FileWriter(file3));
            String printRow = "";

            BitSet bs = this.labels;
            int nbits = bs.size();

            final StringBuilder buffer = new StringBuilder(nbits);
            IntStream.range(0, nbits).mapToObj(i -> bs.get(i) ? '1' : '0').forEach(buffer::append);

            writer.write(buffer.toString() + "\n");
            
            System.out.println("Done");
            writer.close();
        
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }



    public List<Feature> runLocalSearch(Connective root){
        List<BaseFeature> baseFeatures = super.generateBaseFeatures(false);
        return this.runLocalSearch(root, baseFeatures);
    }


    /**
     * Runs local search that extends a given feature
     *
     * @param root
     *
     * */
    public List<Feature> runLocalSearch(Connective root, List<BaseFeature> baseFeatures){

        long t0 = System.currentTimeMillis();

        System.out.println("Local search initiated");

        List<Feature> extracted_features;

//        try{

            List<Feature> minedFeatures = new ArrayList<>();

            // Add a base feature to the given feature, replacing the placeholder
            for(BaseFeature feature:baseFeatures){

                // Define which feature will be add to the current placeholder location
                root.setPlaceholder(feature.getName(), feature.getMatches());

                BitSet matches = root.getMatches();

                double[] metrics = Utils.computeMetrics(matches,this.labels,super.population.size());

                if(Double.isNaN(metrics[0])){
                    continue;
                }

                String name = root.getName();

                BaseFeature newFeature = new BaseFeature(name, matches, metrics[0], metrics[1], metrics[2], metrics[3]);

                minedFeatures.add(newFeature);
            }

            FeatureComparator comparator1 = new FeatureComparator(FeatureMetric.FCONFIDENCE);
            FeatureComparator comparator2 = new FeatureComparator(FeatureMetric.RCONFIDENCE);
            List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));

            extracted_features = Utils.getFeatureFuzzyParetoFront(minedFeatures,comparators,0);

            long t1 = System.currentTimeMillis();
            System.out.println("...[EOSSDataMining] Total features found: " + minedFeatures.size() + ", Pareto front: " + extracted_features.size());
            System.out.println("...[EOSSDataMining] Total data mining time : " + String.valueOf(t1 - t0) + " msec");

//        }catch(Exception e){
//
//            if(root.getLogic() == LogicOperator.AND){
//                System.out.println("root logic: AND");
//            }else{
//                System.out.println("root logic: OR");
//            }
//            System.out.println("root getName: " + root.getName());
//
//            e.printStackTrace();
//            //System.out.println(e.getMessage());
//
//        }

        return extracted_features;
    }
    

    /**
     * Extends a given feature using a local search. This method can only add new features using conjunction at the
     * outermost level. It does not directly compute the matching samples of the initial feature.
     *
     */
    public List<Feature> runLocalSearch(String featureName, List<Integer> archsWithFeature){
        
        BitSet matches = new BitSet(super.architectures.size());
        
        for (int i = 0; i < super.architectures.size(); i++) {
            
            BinaryInputArchitecture a = super.architectures.get(i);
            if (archsWithFeature.contains(a.getID())){
                matches.set(i);
            }
        }
        
        BaseFeature feature = new BaseFeature(featureName, matches);
        return runLocalSearch(feature);
    }

    /**
     * Extends a given feature using a local search. This method can only add new features using conjunction.
     *
      * @param feature Feature to extend
     * @return
     */
    public List<Feature> runLocalSearch(BaseFeature feature){
        
        long t0 = System.currentTimeMillis();
                
        System.out.println("Local search initiated");
        
        List<BaseFeature> baseFeatures = super.generateBaseFeatures(false);

        System.out.println("...[EOSSDataMining] The number of candidate features: " + baseFeatures.size());                
        System.out.println("...[EOSSDataMining] Local search root feature name: " + feature.getName());

        baseFeatures.add(feature);
                
        Apriori ap = new Apriori(super.population.size(), baseFeatures, labels);
        ap.run(baseFeatures.size()-1, super.support_threshold, super.confidence_threshold, DataMiningParams.maxLength);
        
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