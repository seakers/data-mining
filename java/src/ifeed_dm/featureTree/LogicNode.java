/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.featureTree;

import ifeed_dm.LogicOperator;
import java.util.BitSet;
import java.util.List;
import java.util.ArrayList;
import java.util.StringJoiner;

/**
 *
 * @author bang
 */

public class LogicNode extends Node{
    
    private final LogicOperator logic;
    private List<LogicNode> logicNodeChildren;
    private List<FeatureNode> featureNodeChildren;

    private boolean addNode;
    private boolean placeholderSet = false;
    private Node placeholder = null;
    private int placeholderFeatureIndex = -1;
    private BitSet precomputed = null;

    public LogicNode(LogicNode parent, LogicOperator logic){
        super(parent);

        this.logic = logic;

        if(this.logic == LogicOperator.AND){
            name = new StringJoiner("&&");
        }else{
            name = new StringJoiner("||");
        }

        this.logicNodeChildren = new ArrayList<>();
        this.featureNodeChildren = new ArrayList<>();
        this.addNode = false;
    }

    public List<LogicNode> getLogicNodeChildren(){
        return this.logicNodeChildren;
    }

    public List<FeatureNode> getFeatureNodeChildren(){
        return this.featureNodeChildren;
    }

    public void addChild(LogicNode node){
        this.logicNodeChildren.add(node);
    }

    public void addFeature(String name, BitSet matches){
        FeatureNode node = new FeatureNode(this, name, matches);
        this.featureNodeChildren.add(node);
    }

    public LogicOperator getLogic() {
        return logic;
    }

    public void setAddNode(){ // Add a new feature to the current node
        this.addNode = true;
    }

    public void setAddNode(FeatureNode featureToBeCombinedWith){ // Add a new feature to the current node
        this.addNode = true;
        this.placeholderFeatureIndex = -1;
        for(int i = 0; i < this.featureNodeChildren.size(); i++){
            if(this.featureNodeChildren.get(i) == featureToBeCombinedWith){
                this.placeholderFeatureIndex = i;
            }
        }
        if(this.placeholderFeatureIndex == -1){
            throw new RuntimeException("Exc in locating the feature :" + featureToBeCombinedWith.getName() + " in " + this.getName());
        }
    }

    public void cancelAddNode(){
        this.addNode = false;
        this.placeholder = null;
        this.placeholderSet = false;
        this.placeholderFeatureIndex = -1;
        this.precomputed = null;
    }

//    public void setAddNode(FeatureNode node){ // Replace a feature node with a new branch
//        this.addNode = false;
//        LogicOperator childOp;
//        if(this.logic == LogicOperator.AND){
//            childOp = LogicOperator.OR;
//        }else{
//            childOp = LogicOperator.AND;
//        }
//        // Remove a given feature node from featureNodeChildren and add it as a logic node
//        LogicNode newNode = new LogicNode(this, childOp);
//        newNode.addFeature(node.getName(), node.getMatches());
//        newNode.setAddNode();
//
//        this.featureNodeChildren.remove(node);
//        this.logicNodeChildren.add(newNode);
//    }

//    public void cancelAddNode(){
//        this.addNode = false;
//        if(this.logicNodeChildren.size() == 0 && this.featureNodeChildren.size() == 1){
//            // Remove the current node if there is only one feature added
//            FeatureNode node = this.featureNodeChildren.get(0);
//            this.parent.featureNodeChildren.add(node);
//            this.parent.logicNodeChildren.remove(this);
//        }
//    }



    public void setPlaceholder(String name, BitSet matches){
        if(this.addNode){
            this.placeholderSet = true;
            this.placeholder = new FeatureNode(this, name, matches);
            this.precomputed = null;

        }else{
            for(LogicNode node: this.logicNodeChildren){
                node.setPlaceholder(name, matches);
            }
        }
    }

//    public int getChildIndex(LogicNode node){
//        for(int i = 0; i < this.children.size(); i++){
//            LogicNode child = this.children.get(i);
//            if(node.getName() == child.getName()){
//                return i;
//            }
//        }
//        return -1;
//    }

//    public void removeChild(int index){
//        this.children.remove(index);
//    }

    
    public String getName(){
        
        StringJoiner out;
        
        if(this.logic == LogicOperator.AND){
            out = new StringJoiner("&&");
        }else{
            out = new StringJoiner("||");
        }

        if(!this.featureNodeChildren.isEmpty()){
            for(int i = 0; i < this.featureNodeChildren.size(); i++){
                if(this.placeholderFeatureIndex == i){
                    continue;
                }
                FeatureNode node = this.featureNodeChildren.get(i);
                out.add(node.getName());
            }
        }

        if(!this.logicNodeChildren.isEmpty()){
            for(LogicNode node:this.logicNodeChildren){
                out.add(node.getName());
            }
        }

        if(this.addNode && placeholderSet){
            if(this.placeholderFeatureIndex > -1){ // Placeholder is combined with an existing feature to create a new branch
                StringJoiner name;
                if(this.logic == LogicOperator.AND){
                    name = new StringJoiner("||");
                }else{
                    name = new StringJoiner("&&");
                }
                name.add(this.featureNodeChildren.get(placeholderFeatureIndex).getName());
                name.add(this.placeholder.getName());
                out.add( "(" + name.toString() + ")");

            }else{ // Placeholder is simply added to the current node
                out.add(this.placeholder.getName());
            }
        }

        String outputString = out.toString();
        
        if(this.parent != null){
            outputString = "(" + outputString + ")";
        }
        
        return outputString;
    }

    public void precomputeMatches(){
        // Compute the matches for all feature nodes under the current node

        BitSet out = super.matches; // Initialize variable (not used)

        boolean first = true;
        for(int i = 0; i < this.featureNodeChildren.size(); i++){
            // There must be at least one feature defined inside a logic node
            if(this.placeholderSet){
                if(i == this.placeholderFeatureIndex){
                    continue;
                }
            }

            FeatureNode node = this.featureNodeChildren.get(i);
            if(first){
                out = (BitSet) node.getMatches().clone();
                first = false;
            }else{
                if(this.logic == LogicOperator.AND){
                    out.and(node.getMatches());
                }else{
                    out.or(node.getMatches());
                }
            }
        }

        this.precomputed = out;

        for(LogicNode node:this.logicNodeChildren){
            // Recursively pre-compute matches
            node.precomputeMatches();
        }
    }

    public BitSet getMatches(){

        BitSet out;
        Boolean precomputedIsNull = false;

        if(this.precomputed == null){
            this.precomputeMatches();
        }

        if(this.addNode && this.placeholderFeatureIndex != -1){
            // Placeholder is set to be one of the feature nodes
            if(this.featureNodeChildren.size() == 1) {
                // this.precomputed is null
                precomputedIsNull = true;
            }
        }

        if(precomputedIsNull){
            out = this.matches;
        }else{
            out = (BitSet) this.precomputed.clone();
        }

        if (this.placeholderSet && this.addNode) {
            BitSet placeholderMatches = (BitSet) this.placeholder.getMatches().clone();

            if(this.placeholderFeatureIndex > -1){
                BitSet featureMatches = this.featureNodeChildren.get(this.placeholderFeatureIndex).getMatches();
                if(this.logic == LogicOperator.AND){
                    // Combine with the feature and the placeholder matches using the opposite logical connective
                    placeholderMatches.or(featureMatches);
                }else{
                    placeholderMatches.and(featureMatches);
                }
            }

            if(precomputedIsNull){
                out = placeholderMatches;
            }else{
                if(this.logic == LogicOperator.AND){
                    out.and(placeholderMatches);
                }else{
                    out.or(placeholderMatches);
                }
            }
        }

        for(LogicNode node:this.logicNodeChildren){

            BitSet temp = node.getMatches();
            if(this.logic == LogicOperator.AND){
                out.and(temp);
            }else{
                out.or(temp);
            }
        }

        this.matches = out;
        return out;
    }

    public List<LogicNode> getDescendantNodes(){
        List<LogicNode> out = new ArrayList<>();
        out.addAll(this.getDescendantNodes(LogicOperator.AND));
        out.addAll(this.getDescendantNodes(LogicOperator.OR));
        return out;
    }

    public List<LogicNode> getDescendantNodes(LogicOperator operator){

        List<LogicNode> out = new ArrayList<>();
        if(this.logic == operator){
            out.add(this);
        }

        for(LogicNode child:this.logicNodeChildren){
            out.addAll(child.getDescendantNodes(operator));
        }
        return out;
    }

}
