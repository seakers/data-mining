/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.mining.arm;

import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.CandidateFeatureGenerator;
import ifeed.feature.FeatureComparator;
import ifeed.feature.FeatureMetric;
import ifeed.local.DataMiningParams;
import ifeed.feature.Feature;
import ifeed.filter.Filter;

import java.util.*;

/**
 *
 * @author bang
 */

public abstract class DataMining {

    /**
     * Thresholds used for association rule mining
     */
    protected double support_threshold;
    protected double confidence_threshold;
    protected double lift_threshold;
    protected double [] thresholds;

    protected List<AbstractArchitecture> architectures;
    protected List<Integer> behavioral;
    protected List<Integer> non_behavioral;
    protected List<Integer> population;

    protected CandidateFeatureGenerator candidateGenerator;
    protected BitSet labels;

    public DataMining(CandidateFeatureGenerator candidateGenerator, List<Integer> behavioral, List<Integer> non_behavioral, List<AbstractArchitecture> architectures,
                      double supp, double conf, double lift){

        // Initialize threshold values
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

        // Set label
        this.labels = new BitSet(this.architectures.size());
        for (int i = 0; i < this.architectures.size(); i++) {
            AbstractArchitecture a = this.architectures.get(i);
            if (this.behavioral.contains(a.getID())) {
                this.labels.set(i);
            }
        }
    }

    public List<AbstractArchitecture> getArchitectures(){return this.architectures;}
    public List<Integer> getBehavioral(){return this.behavioral;}
    public List<Integer> getNon_behavioral(){return this.non_behavioral;}
    public List<Integer> getPopulation(){return this.population;}

    public double getSupportThreshold(){return this.support_threshold;}
    public double getConfidenceThreshold(){return this.confidence_threshold;}
    public double getLiftThreshold(){return this.lift_threshold;}


    /**
     * Runs data mining using association rule mining
     * @return List of features extracted using association rule mining
     */
    public List<Feature> run(){

        long t0 = System.currentTimeMillis();

        System.out.println("General data mining run initiated");

        List<Feature> baseFeatures = this.generateBaseFeatures(true);

        System.out.println("...[EOSSDataMining] The number of candidate features: " + baseFeatures.size());

        // Run Apriori algorithm
        Apriori ap = new Apriori(this.population.size(), baseFeatures, labels);

        ap.run(this.support_threshold, this.confidence_threshold, DataMiningParams.maxLength);

        FeatureComparator comparator1 = new FeatureComparator(FeatureMetric.FCONFIDENCE);
        FeatureComparator comparator2 = new FeatureComparator(FeatureMetric.RCONFIDENCE);
        List<Comparator> comparators = new ArrayList<>(Arrays.asList(comparator1,comparator2));

        List<Feature> extracted_features = ap.exportFeatures();

        if (DataMiningParams.run_mRMR) {
//            System.out.println("...[DrivingFeatures] Number of features before mRMR: " + drivingFeatures.size() + ", with max confidence of " + drivingFeatures.get(0).getFConfidence());
//            MRMR mRMR = new MRMR();
//            this.drivingFeatures = mRMR.minRedundancyMaxRelevance( population.size(), getDataMat(this.drivingFeatures), this.labels, this.drivingFeatures, topN);
        }

        extracted_features = Utils.getTopFeatures(extracted_features, DataMiningParams.max_number_of_features_before_mRMR, FeatureMetric.DISTANCE2UP);

        long t1 = System.currentTimeMillis();
        System.out.println("...[EOSSDataMining] Total features found: " + extracted_features.size());
        System.out.println("...[EOSSDataMining] Total data mining time : " + String.valueOf(t1 - t0) + " msec");

        return extracted_features;
    }

    public List<Feature> generateBaseFeatures(boolean adjustRuleNum){

        List<Filter> candidates = this.candidateGenerator.generateCandidates();
        List<Feature> evaluatedFeatures = evaluateBaseFeatures(candidates);

        if(adjustRuleNum){
            evaluatedFeatures = adjustBaseFeatureSize(evaluatedFeatures);
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

}
