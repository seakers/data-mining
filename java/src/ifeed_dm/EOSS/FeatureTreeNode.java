/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS;

import ifeed_dm.LogicOperator;
import java.util.BitSet;
import java.util.List;
import java.util.ArrayList;
import java.util.StringJoiner;

/**
 *
 * @author bang
 */

public class FeatureTreeNode {
    
    private FeatureTreeNode parent;
    private final LogicOperator logic;
    
    private BitSet matches;
    private StringJoiner name;

    private boolean featureAdded;
    
    private List<FeatureTreeNode> children;
    
    private boolean hasPlaceholder;
    private BitSet placeholderMatches;
    private String placeholderName;
    
    public FeatureTreeNode(FeatureTreeNode parent, LogicOperator logic){

        this.parent=parent;
        this.logic = logic;
        
        if(this.logic==LogicOperator.AND){
            name = new StringJoiner("&&");
        }else{
            name = new StringJoiner("||");
        }        
        this.featureAdded=false;
        this.hasPlaceholder=false;
        this.children = new ArrayList<>();
    }
    
    public void addPlaceholder(){
        this.hasPlaceholder=true;
    }
    public void removePlaceholder(){
        this.hasPlaceholder=false;
    }
    
    public void setPlaceholderFeature(BitSet matches, String name){
        if(this.hasPlaceholder){
            this.placeholderMatches = matches;
            this.placeholderName = name;
        }else{
            for(FeatureTreeNode node:children){
                node.setPlaceholderFeature(matches, name);
            }
        }
    }

    public void addFeature(BitSet matches, String name){
        this.name.add(name);
        if(!this.featureAdded){
            this.matches=matches;
            this.featureAdded=true;
        }else{
            if(this.logic==LogicOperator.AND){
                this.matches.and(matches);
            }else{
                this.matches.or(matches);
            }
        }
    }
    
    public void addChild(FeatureTreeNode node){
        this.children.add(node);
    }
    public void addChildren(List<FeatureTreeNode> nodes){
        this.children.addAll(nodes);
    }
    public int getChildIndex(FeatureTreeNode node){
        for(int i = 0; i < this.children.size(); i++){
            FeatureTreeNode child = this.children.get(i);
            if(node.getName() == child.getName()){
                return i;
            }
        }
        return -1;
    }
    public void removeChild(int index){
        this.children.remove(index);
    }
    public void setParent(FeatureTreeNode parent){
        this.parent = parent;
    }
    
    public String getName(){
        
        StringJoiner out;
        
        if(this.logic==LogicOperator.AND){
            out = new StringJoiner("&&");
        }else{
            out = new StringJoiner("||");
        }   
        
        if(this.featureAdded){
            out.add(name.toString());
        }

        for(FeatureTreeNode child:this.children){
            out.add(child.getName());
        }

        if(this.hasPlaceholder){
            out.add(this.placeholderName);
        }
        
        String outputString = out.toString();
        
        if(this.parent != null){
            outputString = "(" + outputString + ")";
        }
        
        return outputString;
    }
    
    public BitSet getMatches(){
        
        BitSet out;
        int startInd=0;
        
        if(this.featureAdded){ // This node contains at least one feature
            out = (BitSet) this.matches.clone();
        }else{
            out = (BitSet) this.children.get(0).getMatches().clone();
            startInd=1;
        }
        
        if(this.logic==LogicOperator.AND){
            
            for(int i=startInd;i<this.children.size();i++){
                FeatureTreeNode child = this.children.get(i);
                out.and(child.getMatches());
            }            
            
            if(this.hasPlaceholder){
                out.and(this.placeholderMatches);
            }

        }else{
            
            for(int i=startInd;i<this.children.size();i++){
                FeatureTreeNode child = this.children.get(i);
                out.or(child.getMatches());
            }              
            
            if(this.hasPlaceholder){
                out.or(this.placeholderMatches);
            }            

        }
        return out;
    }

    public List<FeatureTreeNode> getChildren(){
        return this.children;
    }

    public LogicOperator getLogic() {
        return logic;
    }
}
