/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

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

    
    
    
    
    private int populationSize;
    private ArrayList<ArrayList<BitSet>> candidates;
    

    
    
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
    
    
    
    
//-------------------------------------------------------------
//  Method Name: genhash
//  Purpose    : called by createcandidatehashtree
//             : recursively generate hash tree node
//  Parameter  : htnf is a hashtreenode (when other method call this method,it is the root)
//             : cand : candidate itemset string
//             : int i : recursive depth,from i-th item, recursive
//  Return     :
//-------------------------------------------------------------
  
public void generateHashTreeNode(int i, HashTreeNode node, BitSet candidate) {

    // Itemset size
    int n = candidate.cardinality();
    
    // i: relative depth 
    
    if (i==n) {
        
        // If the itemset size matches the depth
        node.setDepth(n);
        // Add each candidate to the node
        node.addFeature(candidate);
        
    }else {
        
        if (node.getHashTable().containsKey(candidate)) {
            
            Hashtable<BitSet,HashTreeNode> table = node.getHashTable();
            BitSet bs = getNewBitSet();
            bs.set(i);
            HashTreeNode thisNode = table.get(bs);
            generateHashTreeNode(i+1,thisNode,candidate);
            
        }
        else {
            HashTreeNode thisNode = new HashTreeNode();
            
            Hashtable<BitSet,HashTreeNode> table = node.getHashTable();
            BitSet bs = getNewBitSet();
            bs.set(i);
            
            table.put(bs, thisNode);
            
            generateHashTreeNode(i+1,thisNode,candidate);
          
        }
    }
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
  

    
    
    public BitSet getNewBitSet(){
        return new BitSet(populationSize);
    }
    
    public boolean ithBit(int i, BitSet bs){
        int ind = 0;
        for(int j=0;j<i;j++){
            ind = bs.nextSetBit(ind);
        }
        return bs.get(ind);
    }

    
}



