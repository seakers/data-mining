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
    
    
    public HashTreeNode(){
        isLeaf = false;
        features = new ArrayList<>();
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
    
}
