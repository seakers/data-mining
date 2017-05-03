/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;


import java.util.Hashtable;
import java.util.ArrayList;
import java.util.BitSet;

/**
 *
 * @author bang
 */
public class HashTreeNode {
    
    private boolean isLeaf;
    private int depth;
    
    private Hashtable<BitSet, HashTreeNode> hashTable;
    private ArrayList<BitSet> features;
    private ArrayList<Integer> FCounter;
    private ArrayList<Integer> SCounter;
    
    public HashTreeNode(){
        isLeaf = false;
        features = new ArrayList<>();
        FCounter = new ArrayList<>();
        SCounter = new ArrayList<>();
        hashTable = new Hashtable<BitSet, HashTreeNode>();
    }
    

    public void setIsLeaf(){
        this.isLeaf = true;
    }
    
    public void setDepth(int n){
        this.depth = n;
    }
    
    public void addFeature(BitSet f){
        features.add(f);
        FCounter.add(0);
        SCounter.add(0);
    }
    
    public Hashtable getHashTable(){
        return this.hashTable;
    }
    
    public ArrayList<BitSet> getFeatures(){
        return this.features;
    }
    
    public boolean getIsLeaf(){
        return this.isLeaf;
    }
    
    public int getDepth(){
        return this.depth;
    }
    
    public ArrayList<Integer> getFCounter(){
        return FCounter;
    }

    public ArrayList<Integer> getSCounter(){
        return SCounter;
    }
    
    public void addToFCounter(int i){
        int tmp = FCounter.get(i)+1;
        FCounter.set(i, tmp);
    }
    public void addToSCounter(int i){
        int tmp = SCounter.get(i)+1;
        SCounter.set(i, tmp);
    }
    
}
