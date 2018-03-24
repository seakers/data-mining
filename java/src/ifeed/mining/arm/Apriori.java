/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ifeed.mining.arm;


import java.util.*;

import ifeed.feature.Feature;
import org.hipparchus.util.Combinations;

/**
 *
 * @author Hitomi
 */
public class Apriori {

    /**
     * The base features that are combined to create the Hasse diagram in the
     * Apriori algorithm. Each BitSet corresponds to a feature and contains the
     * binary vector of the observations that match the feature
     *
     */
    
    /**
     * The features given to the Apriori algorithm
     *
     */
    private final ArrayList<Feature> baseFeatures;

    /**
     * The features found by the Apriori algorithm that exceed the necessary
     * support and confidence thresholds
     */
    private ArrayList<AprioriFeature> minedFeatures;

    /**
     * The number of observations in the data
     */
    private final int numberOfObservations;

    /**
     * The threshold for support
     */
    private double supportThreshold;
    
    
    private BitSet labels;
    

    
    /**
     * A constructor to initialize the apriori algorithm
     *
     * @param numberOfObservations the number of observations in the data
     * @param features the base driving features to combine with Apriori
     * @param labels a BitSet containing information about which observations
     * are behavioral (1) and which are not (0).
     */
    public Apriori(int numberOfObservations, List<Feature> features, BitSet labels) {
        
        this.numberOfObservations = numberOfObservations;

        this.baseFeatures = new ArrayList<>(features);
        
        this.labels = labels;
        
    }

    
    /**
     * Runs the Apriori algorithm to identify features and compound features
     * that surpass the support and confidence thresholds
     *
     * @param supportThreshold The threshold for support
     * @param fConfidenceThreshold The threshold for forward confidence
     * @param maxLength the maximum length of a compound feature
     */
    
    public void run(double supportThreshold, double fConfidenceThreshold, int maxLength){
        run(null, supportThreshold, fConfidenceThreshold, maxLength);
    }

    /**
     * Runs the Apriori algorithm to identify features and compound features
     * that surpass the support and confidence thresholds
     *
     * @param constraintFeatureIndex Index of the feature that will be included in all compound features that are generated
     * @param supportThreshold The threshold for support
     * @param fConfidenceThreshold The threshold for forward confidence
     * @param maxLength the maximum length of a compound feature
     */

    public void run(Integer constraintFeatureIndex, double supportThreshold, double fConfidenceThreshold, int maxLength) {
        
        this.supportThreshold = supportThreshold;

        long t0 = System.currentTimeMillis();

        System.out.println("...[Apriori] size of the input matrix: " + numberOfObservations + " X " + baseFeatures.size());

        // Define the initial set of features
        minedFeatures = new ArrayList<>();
        
        // Define front. front is the set of features whose length is L and passes significant test
        ArrayList<BitSet> front = new ArrayList();
        
        
        int i=0;
        for (Feature feature:baseFeatures) {
            
            if(feature.getSupport() > supportThreshold){
                
                BitSet featureCombo = new BitSet(baseFeatures.size());
                featureCombo.set(i);
                
                if (constraintFeatureIndex==null){
                    // Unconstrained case
                    if (feature.getFConfidence() > fConfidenceThreshold) {   
                        //only add feature to output list if it passes support and confidence thresholds
                        minedFeatures.add(new AprioriFeature(featureCombo,feature.getMatches(),feature.getSupport(),feature.getLift(),feature.getFConfidence(),feature.getRConfidence()));
                    }    
                }else{
                    featureCombo.set(constraintFeatureIndex);                
                }
                front.add(featureCombo);
            }

            i++;
        }
        
        int currentLength = 2;
        // While there are features still left to explore
        while (front.size() > 0) {
            
            if (currentLength - 1 == maxLength) {
                break;
            }
            
            
            // Candidates to form the frontier with length L+1
            // Updated front with new instance only containing the L+1 combinations of features
            ArrayList<BitSet> candidates;
            
            if(constraintFeatureIndex!=null && currentLength==2){
                candidates = new ArrayList<>(front);
            }else{
                candidates = join(front, baseFeatures.size());
            }
            
            front.clear();

            System.out.println("...[Apriori] number of candidates (length " + currentLength + "): " + candidates.size());

            for (BitSet featureCombo : candidates) {
                
                BitSet matches = getMatches(featureCombo);
                
                // Check if it passes minimum support threshold
                double[] metrics = computeMetrics(matches, labels);
                
                
                if (metrics[0] > supportThreshold) {
                    // Add all features whose support is above threshold, add to candidates
                    front.add(featureCombo);

                    if (metrics[2] > fConfidenceThreshold) {
                        // If the metric is above the threshold, current feature is statistically significant
                        minedFeatures.add(new AprioriFeature(featureCombo, matches, metrics[0], metrics[1], metrics[2], metrics[3]));
                    }
                }
            }
            
            currentLength = currentLength + 1;
        }

        long t1 = System.currentTimeMillis();
        System.out.println("...[Apriori] evaluation done in: " + String.valueOf(t1 - t0) + " msec, with " + minedFeatures.size() + " features found");
    }
    
    
    
    

    public BitSet getMatches(BitSet featureIndices){
        
        int ind = featureIndices.nextSetBit(0);
        BitSet matches = (BitSet) baseFeatures.get(ind).getMatches().clone();

        //find feature indices
        for (int j = featureIndices.nextSetBit(ind + 1); j != -1; j = featureIndices.nextSetBit(j + 1)) {
            matches.and(baseFeatures.get(j).getMatches());
        }

        return matches;
    }
    
    
    
    public List<Feature> exportFeatures(){
        return exportFeatures(this.minedFeatures);
    }
    
    public List<Feature> exportFeatures(List<AprioriFeature> apFeatures){

        ArrayList<Feature> out = new ArrayList<>(apFeatures.size());

        for (AprioriFeature apFeature:apFeatures) {

            //build the binary array taht is 1 for each solution matching the feature
            StringBuilder sb = new StringBuilder();
            BitSet featureCombo = apFeature.getFeatureIndices();

            int ind = featureCombo.nextSetBit(0);
            sb.append(baseFeatures.get(ind).getName());

            //find feature indices
            for (int j = featureCombo.nextSetBit(ind + 1); j != -1; j = featureCombo.nextSetBit(j + 1)) {
                sb.append("&&");
                sb.append(baseFeatures.get(j).getName());
            }

            out.add(new Feature(sb.toString(), apFeature.getMatches(),
                    apFeature.getSupport(), apFeature.getLift(),
                    apFeature.getFConfidence(), apFeature.getRConfidence()));
        }
        return out;
    }
    
    
    
    
    

    
    

    /**
     * Joins the features together using the Apriori algorithm. Ensures that
     * duplicate feature are not generated and that features that are subsets of
     * features that were previously filtered out aren't generated. Ordering of
     * the bitset in the arraylist of the front is important. It should be
     * ordered such that 10000 (A) comes before 010000 (B) or 11010 (ABD) comes
     * before 00111 (CDE)
     *
     * Example1: if AB and BC both surpass the support threshold, ABC is only
     * generated once
     *
     * Example2: if AB was already filtered out but BC surpasses the support
     * threshold, ABC should not and will not be generated
     *
     * @param front is an arraylist of bitsets corresponding to which features
     * are being combined. For example in a set of {A, B C, D, E} 10001 is
     * equivalent to AE
     * @param numberOfFeatures the maximum number of features being considered
     * in the entire Apriori algorithm
     * @return the next front of potential feature combinations. These need to
     * be tested against the support threshold
     */
    private ArrayList<BitSet> join(ArrayList<BitSet> front, int numberOfFeatures) {
        
        ArrayList<BitSet> candidates = new ArrayList<>();

        //The new candidates must be checked against the current front to make 
        //sure that each length L subset in the new candidates must already
        //exist in the front to make sure that ABC never gets added if AB, AB,
        //or BC is missing from the front
        HashSet<BitSet> frontSet = new HashSet<>(front);

        for (int i = 0; i < front.size(); i++) {
            BitSet f1 = front.get(i);
            int lastSetIndex1 = f1.previousSetBit(numberOfFeatures - 1);
            for (int j = i + 1; j < front.size(); j++) {
                BitSet f2 = front.get(j);
                int lastSetIndex2 = f1.previousSetBit(numberOfFeatures - 1);

                //check to see that all the bits leading up to the minimum of the last set bits are equal
                //That is AB (11000) and AC (10100) should be combined but not AB (11000) and BC (01100)
                //AB and AC are combined because the first bits are equal
                //AB and BC are not combined because the first bits are not equal
                int index = Math.min(lastSetIndex1, lastSetIndex2);
                if (f1.get(0, index).equals(f2.get(0, index))) {
                    BitSet copy = (BitSet) f1.clone();
                    copy.or(f2);

                    if (checkSubsets(copy, frontSet, numberOfFeatures)) {
                        candidates.add(copy);
                    }
                } else {
                    //once AB is being considered against BC, the inner loop should break
                    //since the input front is assumed to be ordered, any set after BC is also incompatible with AB
                    break;
                }
            }
        }
        return candidates;
    }

    /**
     * The new candidates must be checked against the current front to make sure
     * that each length L subset in the new candidates must already exist in the
     * front to make sure that ABC never gets added if AB, AB, or BC is missing
     * from the front
     *
     * @param bs the length L bit set
     * @param toCheck a set of bit sets of length L-1 to check all subsets of L
     * against
     * @param numberOfFeatures the number of features
     * @return true if all subsets of the given bit set are in the set of bit
     * sets
     */
    private boolean checkSubsets(BitSet bs, HashSet<BitSet> toCheck, int numberOfFeatures) {
        
        // the indices that are set in the bitset
        int[] setIndices = new int[bs.cardinality()];
        int count = 0;
        for (int i = bs.nextSetBit(0); i != -1; i = bs.nextSetBit(i + 1)) {
            setIndices[count] = i;
            count++;
        }

        //create all combinations of n choose k
        Combinations subsets = new Combinations(bs.cardinality(), bs.cardinality() - 1);
        Iterator<int[]> iter = subsets.iterator();
        while (iter.hasNext()) {
            BitSet subBitSet = new BitSet(numberOfFeatures);
            int[] subsetIndices = iter.next();
            for (int i = 0; i < subsetIndices.length; i++) {
                subBitSet.set(setIndices[subsetIndices[i]], true);
            }

            if (!toCheck.contains(subBitSet)) {
                return false;
            }
        }
        return true;
    }


    
    /**
     * Computes the metrics of a feature. The feature is represented as the
     * bitset that specifies which base features define it. If the support
     * threshold is not met, then the other metrics are not computed.
     *
     * @param feature the bit set specifying which base features define it
     * @param labels the behavioral/non-behavioral labeling
     * @return a 4-tuple containing support, lift, fcondfidence, and
     * rconfidence. If the support threshold is not met, all metrics will be NaN
     */
    private double[] computeMetrics(BitSet feature, BitSet labels) {
        double[] out = new double[4];

        BitSet copyMatches = (BitSet) feature.clone();
        copyMatches.and(labels);
        double cnt_SF = (double) copyMatches.cardinality();
        out[0] = cnt_SF / (double) numberOfObservations; //support

        // Check if it passes minimum support threshold
        if (out[0] > supportThreshold) {
            //compute the confidence and lift
            double cnt_S = (double) labels.cardinality();
            double cnt_F = (double) feature.cardinality();
            out[1] = (cnt_SF / cnt_S) / (cnt_F / (double) numberOfObservations); //lift
            out[2] = (cnt_SF) / (cnt_F);   // confidence (feature -> selection)
            out[3] = (cnt_SF) / (cnt_S);   // confidence (selection -> feature)
        } else {
            Arrays.fill(out, Double.NaN);
        } 
        return out;
    }       


    
    
    
    public List<AprioriFeature> getMinedFeatures(){
        return this.minedFeatures;
    }

    
    
    private class AprioriFeature extends Feature {
        
        private final BitSet featureIndices;
        
        public AprioriFeature(BitSet featureIndices, BitSet matches, double support, double lift, double fconfidence, double rconfidence) {
            super(null, matches,support,lift,fconfidence,rconfidence);
            this.featureIndices = featureIndices;
        }
   
        public BitSet getFeatureIndices(){
            return this.featureIndices;
        }
    }
   
}
