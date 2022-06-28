/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ifeed.mining.arm;


import java.io.*;
import java.util.*;

import ifeed.architecture.AbstractArchitecture;
import ifeed.feature.Feature;
import ifeed.local.params.ARMParams;
import ifeed.local.params.BaseParams;

/**
 *
 * @author Hitomi
 */
public abstract class AbstractApriori extends AbstractAssociationRuleMining{

    /**
     * The base features that are combined to create the Hasse diagram in the
     * AbstractApriori algorithm. Each BitSet corresponds to a feature and contains the
     * binary vector of the observations that match the feature
     *
     */
    
    /**
     * The features given to the AbstractApriori algorithm
     *
     */
    private List<Feature> baseFeatures;

    /**
     * The features found by the AbstractApriori algorithm that exceed the necessary
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

    private String path;
    private String dirname;
    private String filename;

    private int maxCandidateSize = 10000; // 10k
    private int maxFrontSize = 50000; // 50k

    private int maxFeatureLength;


    public AbstractApriori(BaseParams params,
                                         int maxFeatureLength,
                                         List<AbstractArchitecture> architectures,
                                         List<Integer> behavioral,
                                         List<Integer> non_behavioral,
                                         double supp, double conf, double lift){

        super(params, architectures, behavioral, non_behavioral, supp, conf, lift);

        this.maxFeatureLength = maxFeatureLength;

        this.baseFeatures = super.generateBaseFeatures();

        this.numberOfObservations = this.architectures.size();

        this.path =  System.getProperty("user.dir");
        //this.filename = this.path + File.separator + "temp" + File.separator + new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date());

        this.dirname = this.path + File.separator + "temp" + File.separator;

    }

    @Override
    public List<Feature> run(){
        long t0 = System.currentTimeMillis();
        System.out.println("Association rule mining");
        System.out.println("...["+ this.getClass().getSimpleName() + "] supp: " + support_threshold +
                ", conf: " + confidence_threshold + ", lift: " + lift_threshold + "");

        System.out.println("...[" + this.getClass().getSimpleName() + "] The number of candidate features: " + baseFeatures.size());

        if(ARMParams.adjustRuleSize){
            baseFeatures = adjustBaseFeatureSize(baseFeatures);
            System.out.println("...[" + this.getClass().getSimpleName() + "] Adjusted support threshold: " + this.support_threshold);
        }

        // Run AbstractApriori algorithm
        this.run(this.support_threshold, this.confidence_threshold, this.lift_threshold, maxFeatureLength);

        List<Feature> extracted_features = this.exportFeatures();

        if (ARMParams.run_mRMR) {
//            System.out.println("...[DrivingFeatures] Number of features before mRMR: " + drivingFeatures.size() + ", with max confidence of " + drivingFeatures.get(0).getPrecision());
//            MRMR mRMR = new MRMR();
//            this.drivingFeatures = mRMR.minRedundancyMaxRelevance( samples.size(), getDataMat(this.drivingFeatures), this.labels, this.drivingFeatures, topN);
        }

        long t1 = System.currentTimeMillis();
        System.out.println("...["+ this.getClass().getSimpleName() +"] Total features found: " + extracted_features.size());
        System.out.println("...["+ this.getClass().getSimpleName() +"] Total data mining time : " + String.valueOf(t1 - t0) + " msec");
        return extracted_features;
    }

    /**
     * Runs the AbstractApriori algorithm to identify features and compound features
     * that surpass the support and confidence thresholds
     *
     * @param supportThreshold The threshold for support
     * @param confidenceThreshold The threshold for forward confidence
     * @param maxLength the maximum length of a compound feature
     */

    public void run(double supportThreshold, double confidenceThreshold, double liftThreshold, int maxLength) {

        this.supportThreshold = supportThreshold;

        long t0 = System.currentTimeMillis();

        System.out.println("...["+ this.getClass().getSimpleName() +"] Size of the input matrix: " + numberOfObservations + " X " + baseFeatures.size());

        // Define the initial set of features
        minedFeatures = new ArrayList<>();
        
        // Define front. front is the set of features whose length is L and passes significance test
        ArrayList<BitSet> front = new ArrayList();

        int i = 0;
        for (Feature feature: baseFeatures) {

            if(feature.getSupport() > supportThreshold){
                
                BitSet featureCombo = new BitSet(baseFeatures.size());
                featureCombo.set(i);

                if (feature.getPrecision() > confidenceThreshold) {
                    //only add feature to output list if it passes support and confidence thresholds
                    if(feature.getLift() > liftThreshold) {
                        minedFeatures.add(new AprioriFeature(featureCombo,feature.getMatches(),feature.getSupport(),feature.getLift(),feature.getPrecision(),feature.getRecall()));
                    }
                }

                front.add(featureCombo);
            }
            i++;
        }

        // Number of files that contain features included in the front
        int numRecordedFronts = 0;

        // Current feature length
        int currentLength = 2;

        // While there are features still left to explore
        while (front.size() > 0 || numRecordedFronts > 0) {
            
            if (currentLength - 1 == maxLength) {
                break;
            }

            System.out.println("...[" + this.getClass().getSimpleName() + "] Generating candidates of length " + currentLength + ", front size: " + front.size());
            
            // Candidates to form the frontier with length L+1
            // Updated front with new instance only containing the L+1 combinations of features
            ArrayList<BitSet> candidates;

            if(currentLength == 2){

                // Generate all candidates
                candidates = new ArrayList<>();
                for(int j = 0; j < front.size(); j++){
                    candidates.addAll(this.joinFeature(front, j, baseFeatures.size()));
                }
                System.out.println("...[" + this.getClass().getSimpleName() + "] Candidate size: " + candidates.size());

                // Get the next front, and save all features whose confidence is above the threshold
                front = computeFrequentItems(candidates, supportThreshold, confidenceThreshold, liftThreshold);

            } else {
                // Depending on the memory size, two different types of splits are possible.
                // 1. Candidate split: Instead of generating and keeping all candidates in the buffer, create
                //     a subset of candidates, and use them to compute the frequent itemsets.
                // 2. Front split: Instead of using the whole front to generate candidates, only load a part of the front.
                //     Each subset of the front need to contain all featureCombos whose L-1 items overlap

                // All subsets of the front loaded and used to generate candidates
                boolean allFrontCovered = false;

                ArrayList<BitSet> currentFront;

                // Index of the front subset
                int frontSubsetIndex = 0;
                List<BitSet> carryover = new ArrayList<>();

                System.out.println("length: " + currentLength);


                while(!allFrontCovered){ // While all subsets of the front are not covered completely

                    boolean nextFrontDataFound = false;
                    int cnt = 0;
                    while(true){
                        String nextFrontData = this.dirname + Integer.toString(this.hashFrontSubset(supportThreshold, confidenceThreshold, maxFrontSize, currentLength + 1, cnt));
                        File f = new File(nextFrontData);
                        if(f.exists() && !f.isDirectory()) {
                            if(cnt == 0){
                                nextFrontDataFound = true;
                                numRecordedFronts = 0;
                                System.out.println("Skipping the calculation of length " + currentLength + " features, as the front is already stored in the disk.");
                            }
                            numRecordedFronts++;

                        } else {
                            break;
                        }
                        cnt++;
                    }
                    if(nextFrontDataFound){
                        System.out.println(Integer.toString(numRecordedFronts) + " front subsets found");
                        break;
                    }

                    if(numRecordedFronts == 0){ // There is only one set, so the front is fully covered
                        currentFront = front;
                        allFrontCovered = true;

                    }else{ // There exist multiple subsets of the front that need to be loaded separately
                        currentFront = new ArrayList<>(carryover);
                        // FeatureCombos carried over from the last subset
                        carryover = new ArrayList<>();

                        while(true){
                            boolean splitHere = false;

                            // Read a single subset of the front
                            int hashed = this.hashFrontSubset(supportThreshold, confidenceThreshold, maxFrontSize, currentLength, frontSubsetIndex);
                            List<BitSet> frontSubset = this.readFeatureCombo(this.dirname + Integer.toString(hashed));
//                            File file = new File(this.dirname + Integer.toString(hashed));
//                            file.delete();

                            if(frontSubsetIndex == numRecordedFronts - 1){ // All subsets were read
                                allFrontCovered = true;
                            }else{
                                frontSubsetIndex += 1;
                                System.out.println("Reading " + frontSubsetIndex + "-th subset out of " + numRecordedFronts);
                            }

                            // Last featureCombo
                            BitSet f2 = frontSubset.get(frontSubset.size() - 1);
                            int lastSetIndex2 = f2.previousSetBit(baseFeatures.size() - 1);

                            // Iterate over all featureCombos in the current front subset
                            for(int a = 0; a < frontSubset.size() - 1; a++){

                                BitSet f1 = frontSubset.get(a);
                                int lastSetIndex1 = f1.previousSetBit(baseFeatures.size() - 1);
                                int lastSetIndex = Math.min(lastSetIndex1, lastSetIndex2);

                                // If a featureCombo shares L-1 items with the last featureCombo, then carry it over to the next iteration
                                // This is done to ensure that all featureCombos sharing L-1 items are loaded
                                if (f1.get(0, lastSetIndex).equals(f2.get(0, lastSetIndex))) {
                                    carryover.add(f1);
                                }else{
                                    // If a featureCombo does not share L-1 items, make a split here
                                    splitHere = true;
                                    currentFront.add(f1);
                                }
                            }

                            // Carry the last featureCombo to the next iteration
                            carryover.add(f2);

                            if(splitHere){
                                break;
                            }else if(allFrontCovered){
                                numRecordedFronts = 0;
                                currentFront.addAll(carryover);
                                break;
                            }
                        }
                    }

                    ArrayList<BitSet> tempFront = new ArrayList<>(); // New front (of length L+1)
                    int featureIndex = 0; // Index of the feature where the candidate-generation process is stopped

                    while(true){ // While all features in the current front subset are not covered
                        // (For each split of candidate generation process)

                        // Create a new list of candidates (the size must be smaller than maxCandidateSize)
                        candidates = new ArrayList<>();
                        for(int j = featureIndex; j < currentFront.size(); j++){
                            candidates.addAll(this.joinFeature(currentFront, j, baseFeatures.size()));

                            // If the size of a candidate set gets too big, compute the frequent itemset only using the current candidate set
                            if(candidates.size() > this.maxCandidateSize || j == currentFront.size() - 1){
                                featureIndex = j;
                                break;
                            }
                        }
                        System.out.println("...[" + this.getClass().getSimpleName() + "] Candidate size: " + candidates.size() + " (" + featureIndex + "/" + currentFront.size() + ")");

                        // Use the current candidate set fo compute the frequent itemset of length L+1
                        ArrayList<BitSet> computedFront = computeFrequentItems(candidates, supportThreshold, confidenceThreshold, liftThreshold);

                        // Add all new candidates to the front
                        tempFront.addAll(computedFront);
                        System.out.println("...[" + this.getClass().getSimpleName() + "] Front size: " + tempFront.size() + ", minedFeaturesSize: " + minedFeatures.size());

//                         If all features in the current subset of the front is covered
                        if(featureIndex == currentFront.size() - 1){
                            break;
                        }

                        // If the size of the next front gets too big, record the current front in a disk and start a new one
                        if(tempFront.size() > this.maxFrontSize){
                            System.out.println("Recording front of size: " + tempFront.size());
                            int hashed = this.hashFrontSubset(supportThreshold, confidenceThreshold, maxFrontSize, currentLength, numRecordedFronts++);
                            this.writeFeatureCombo(this.dirname + Integer.toString(hashed), tempFront);
                            tempFront.clear();
                        }
                    }
                    front = tempFront;
                }
            }
            currentLength = currentLength + 1;
        }
        long t1 = System.currentTimeMillis();
        System.out.println("...["+ this.getClass().getSimpleName() +"] evaluation done in: " + String.valueOf(t1 - t0) + " msec, with " + minedFeatures.size() + " features found");
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
                    apFeature.getPrecision(), apFeature.getRecall()));
        }
        return out;
    }

    private ArrayList<BitSet> computeFrequentItems(ArrayList<BitSet> candidates, double supportThreshold, double confidenceThreshold, double liftThreshold){

        ArrayList<BitSet> front = new ArrayList<>();

        for (BitSet featureCombo : candidates) {

            BitSet matches = getMatches(featureCombo);

            // Check if it passes minimum support threshold
            double[] metrics = computeMetrics(matches, labels);
            if (metrics[0] > supportThreshold) {
                // Add all features whose support is above threshold, add to candidates
                front.add(featureCombo);

                if (metrics[2] > confidenceThreshold) {
                    // If the metric is above the threshold, current feature is statistically significant
                    if(metrics[1] > liftThreshold) {
                        minedFeatures.add(new AprioriFeature(featureCombo, matches, metrics[0], metrics[1], metrics[2], metrics[3]));
                    }
                }
            }
        }
        return front;
    }

    private ArrayList<BitSet> joinFeature(ArrayList<BitSet> front, int featureIndex, int numberOfFeatures) {

        ArrayList<BitSet> candidates = new ArrayList<>();

        //The new candidates must be checked against the current front to make
        //sure that each length L subset in the new candidates must already
        //exist in the front to make sure that ABC never gets added if AB, AB,
        //or BC is missing from the front
        HashSet<BitSet> frontSet = new HashSet<>(front);

        int i = featureIndex;
        BitSet f1 = front.get(i);
        int lastSetIndex1 = f1.previousSetBit(numberOfFeatures - 1);
        for (int j = i + 1; j < front.size(); j++) {
            BitSet f2 = front.get(j);
            int lastSetIndex2 = f1.previousSetBit(numberOfFeatures - 1);

            //check to see that all the bits leading up to the minimum of the last set bits are equal
            //That is AB (11000) and AC (10100) should be combined but not AB (11000) and BC (01100)
            //AB and AC are combined because the first bits are equal
            //AB and BC are not combined because the first bits are not equal
            int lastSetIndex = Math.min(lastSetIndex1, lastSetIndex2);
            if (f1.get(0, lastSetIndex).equals(f2.get(0, lastSetIndex))) {
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
        return candidates;
    }


    /**
     * Joins the features together using the AbstractApriori algorithm. Ensures that
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
     * in the entire AbstractApriori algorithm
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

            if(i % 1000 == 0){
                System.out.println("front covered: " + i + ", candidateSize: " + candidates.size());
            }
        }
        return candidates;
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
    protected double[] computeMetrics(BitSet feature, BitSet labels) {
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
        BitSet bsCopy = (BitSet) bs.clone();
        for (int i = bsCopy.nextSetBit(0); i != -1; i = bsCopy.nextSetBit(i + 1)) {
            bs.clear(i);
            if (!toCheck.contains(bs)) {
                return false;
            }
            bs.set(i);
        }
        return true;
    }

    public List<BitSet> readFeatureCombo(String filename){

        List<BitSet> featureCombo = new ArrayList<>();

        FileInputStream fin = null;
        ObjectInputStream ois = null;

        try {
            fin = new FileInputStream(filename);
            ois = new ObjectInputStream(fin);
            featureCombo = (List<BitSet>) ois.readObject();

        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return featureCombo;
    }

    public void writeFeatureCombo(String filename, List<BitSet> featureCombo){

        File file = new File(filename);
        file.getParentFile().mkdirs();

        FileOutputStream fout = null;
        ObjectOutputStream oos = null;

        try {
            fout = new FileOutputStream(filename);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(featureCombo);

        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {

            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static int hashFrontSubset(double supp, double conf, int maxFrontSize, int currentLength, int index) {
        int hash = 13;
        hash = 23 * hash + Objects.hashCode(index);
        hash = 97 * hash + Objects.hashCode(currentLength);
        hash = 31 * hash + Objects.hashCode(supp);
        hash = 31 * hash + Objects.hashCode(conf);
        hash = 31 * hash + Objects.hashCode(maxFrontSize);
        return hash;
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
