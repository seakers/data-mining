/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import org.hipparchus.util.Combinations;

/**
 *
 * @author bang
 */
public class AprioriPractice {
    

    /**
     * The base features that are combined to create the Hasse diagram in the
     * Apriori algorithm. Each BitSet corresponds to a feature and contains the
     * binary vector of the observations that match the feature
     *
     */
    private final BitSet[] baseFeaturesBit;

    /**
     * The features given to the Apriori algorithm
     *
     */
    private final ArrayList<DrivingFeature2> baseFeatures;

    /**
     * The features found by the Apriori algorithm that exceed the necessary
     * support and confidence thresholds
     */
    private ArrayList<AprioriFeature> viableFeatures;

    /**
     * The number of observations in the data
     */
    private final int numberOfObservations;

    /**
     * The threshold for support
     */
    private double supportThreshold;

    
    
    private ArrayList<ArrayList<BitSet>> candidates;
    private ArrayList<BitSet> transaction;
    

    /**
     * A constructor to initialize the apriori algorithm
     *
     * @param numberOfObservations the number of observations in the data
     * @param drivingFeatures the base driving features to combine with Apriori
     */
    public AprioriPractice(int numberOfObservations, Collection<DrivingFeature2> baseFeatures, ArrayList<BitSet> inputData) {
        
        this.numberOfObservations = numberOfObservations;
        this.baseFeatures = new ArrayList<>(baseFeatures);
        this.baseFeaturesBit = new BitSet[baseFeatures.size()];
        int i = 0;
        for (DrivingFeature2 feat : baseFeatures) {
            this.baseFeaturesBit[i] = feat.getMatches();
            i++;
        }
        this.transaction = inputData;
    }
    
    
    
    public AprioriPractice(){
    
        
        int k=0;
        Vector large=new Vector();
        Date d=new Date();
        long s1,s2;


        //start time
        d=new Date();
        s1=d.getTime();

        while (true)
        {
          k++;


          cande.htroot=null;
          candidate.addElement(cande);

          ((aprioriProcess.candidateelement)candidate.elementAt(k-1)).htroot=createcandidatehashtree(k);

    //      System.out.println("Now reading transactions, increment counters of itemset");
          transatraverse(k);

          createlargeitemset(k);
          System.out.println("Frequent "+k+"-itemsets:");
          System.out.println((Vector)(largeitemset.elementAt(k-1)));
        }

        aprioriProcess.hashtreenode htn=new aprioriProcess.hashtreenode();
        htn=((aprioriProcess.candidateelement)candidate.elementAt(k-2)).htroot;

        //end time
        d=new Date();
        s2=d.getTime();
        System.out.println();
        System.out.println("Execution time is: "+((s2-s1)/(double)1000) + " seconds.");

    
    
    }
    
    
    
    



    // Generates the root of an hash tree for candidates of length n
    public HashTreeNode createCandidateHashTree(ArrayList<BitSet> candidates){

        HashTreeNode root = new HashTreeNode();

        for (BitSet candidate:candidates){
            generateHashTreeNode(1,root,candidate);
        }

        return root;
    }

    
    

    public void generateHashTreeNode(int i, HashTreeNode node, BitSet candidate) {

        // Itemset size
        int n = candidate.cardinality();

        // i: relative depth 

        if (i==n) {
            node.setIsLeaf();
            // If the itemset size matches the depth
            node.setDepth(n);
            // Add each candidate to the node
            node.addFeature(candidate);

        }else {

            Hashtable<BitSet,HashTreeNode> table = node.getHashTable();
            HashTreeNode nextNode;

            BitSet bs = getNewBitSet();
            int ind = ithSetBit(i, bs);
            bs.set(ind);

            if (table.containsKey(bs)) {
                
                nextNode = table.get(bs);

            }else {
                // Create a new node
                nextNode = new HashTreeNode();
                
                if(i==n-1){
                    nextNode.setIsLeaf();
                }
                table.put(bs, nextNode);

            }
            generateHashTreeNode(i+1,nextNode,candidate);
        }
    }

    


    public void traverseData(){
    
        transa=transa.trim();
        transactionTraverseHash(0,htn,transa);
    
    }




//-------------------------------------------------------------
//  Method Name: transatrahash
//  Purpose    : called by transatraverse
//             : recursively traverse hash tree
//  Parameter  : htnf is a hashtreenode (when other method call this method,it is the root)
//             : cand : candidate itemset string
//             : int i : recursive depth,from i-th item, recursive
//  Return     :
//-------------------------------------------------------------
    
    public void transactionTraverseHash(int i, HashTreeNode node, BitSet transaction){
        
        Vector itemsetlist = new Vector();
        int j,lastpos,len;
        String d;
        StringTokenizer st;

        if (node.getIsLeaf()){
            
            ArrayList<BitSet> features = node.getFeatures();
            
            for (j=0;j<features.size();j++){
                
                BitSet bs = features.get(j);
                BitSet item = 
                d=getitemat(htnf.depth,tmpnode.itemset);


                while(st.hasMoreTokens())
                {
                    if(st.nextToken().compareToIgnoreCase(d)==0)
                        ((aprioriProcess.itemsetnode)(itemsetlist.elementAt(j))).counter++;
                }

            }
            return;
        }
        else  //HT
          for (int b=i+1;b<=itemsetsize(transa);b++)
            if (htnf.ht.containsKey((getitemat(b,transa))))
              transatrahash(i,(aprioriProcess.hashtreenode)htnf.ht.get((getitemat(b,transa))),transa);

    } 



  
 

    /**
     * Runs the Apriori algorithm to identify features and compound features
     * that surpass the support and confidence thresholds
     *
     * @param labels a BitSet containing information about which observations
     * are behavioral (1) and which are not (0).
     * @param supportThreshold The threshold for support
     * @param fConfidenceThreshold The threshold for forward confidence
     * @param maxLength the maximum length of a compound feature
     */
    public void run(BitSet labels, double supportThreshold, double fConfidenceThreshold, int maxLength) {
        this.supportThreshold = supportThreshold;

        long t0 = System.currentTimeMillis();

        System.out.println("...[Apriori2] size of the input matrix: " + numberOfObservations + " X " + baseFeatures.size());

        //these metric double sare computed during Apriori
        double metrics[];

        // Define the initial set of features
        viableFeatures = new ArrayList<>();
        
        // Define front. front is the set of features whose length is L and passes significant test
        ArrayList<BitSet> front = new ArrayList();
        for (int i = 0; i < baseFeatures.size(); i++) {
            metrics = computeMetrics(baseFeaturesBit[i], labels);
            if (!Double.isNaN(metrics[0])) {
                BitSet featureCombo = new BitSet(baseFeatures.size());
                featureCombo.set(i, true);
                front.add(featureCombo);
                                
                if (metrics[2] > fConfidenceThreshold) {
                    //only add feature to output list if it passes support and confidence thresholds
                    AprioriFeature feat = new AprioriFeature(featureCombo, metrics[0], metrics[1], metrics[2], metrics[3]);
                    viableFeatures.add(feat);
                }
            }
        }

        int currentLength = 2;
        // While there are features still left to explore
        while (front.size() > 0) {
            if (currentLength - 1 == maxLength) {
                break;
            }
            // Candidates to form the frontier with length L+1
            //updated front with new instance only containing the L+1 combinations of features
            ArrayList<BitSet> candidates = join(front, baseFeatures.size());
            front.clear();

            System.out.println("...[Apriori2] number of candidates (length " + currentLength + "): " + candidates.size());

            for (BitSet featureCombo : candidates) {
                int ind = featureCombo.nextSetBit(0);
                BitSet matches = (BitSet) baseFeaturesBit[ind].clone();

                //find feature indices
                for (int j = featureCombo.nextSetBit(ind + 1); j != -1; j = featureCombo.nextSetBit(j + 1)) {
                    matches.and(baseFeaturesBit[j]);
                }

                // Check if it passes minimum support threshold
                metrics = computeMetrics(matches, labels);
                if (!Double.isNaN(metrics[0])) {
                    // Add all features whose support is above threshold, add to candidates
                    front.add(featureCombo);

                    if (metrics[2] > fConfidenceThreshold) {
                        // If the metric is above the threshold, current feature is statistically significant
                        viableFeatures.add(new AprioriFeature(featureCombo, metrics[0], metrics[1], metrics[2], metrics[3]));
                    }

                }
            }
            System.out.println("...[Apriori2] number of valid candidates (length " + currentLength + "): " + front.size());
            currentLength = currentLength + 1;
        }

        long t1 = System.currentTimeMillis();
        System.out.println("...[Apriori2] evaluation done in: " + String.valueOf(t1 - t0) + " msec, with " + viableFeatures.size() + " features found");
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

    

    
    
    public BitSet getNewBitSet(){
        return new BitSet(this.baseFeatures.size());
    }
    
    public int ithSetBit(int i, BitSet bs){
        int ind = 0;
        for(int j=0;j<i;j++){
            ind = bs.nextSetBit(ind);
        }
        return ind;
    }

    
}



