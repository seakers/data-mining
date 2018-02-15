/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.logic;

import ifeed_dm.LogicOperator;
import java.util.BitSet;
import java.util.List;
import java.util.ArrayList;
import java.util.StringJoiner;

/**
 *
 * @author bang
 */

public class Connective extends Formula {
    
    private final LogicOperator logic;
    private List<Connective> connectiveChildren;
    private List<Literal> literalChildren;

    private boolean addNode;
    private boolean placeholderSet = false;
    private Formula placeholder = null;
    private int placeholderFeatureIndex = -1;
    private BitSet precomputed = null;

    public Connective(Connective parent, LogicOperator logic){
        super(parent);

        this.logic = logic;

        if(this.logic == LogicOperator.AND){
            name = new StringJoiner("&&");
        }else{
            name = new StringJoiner("||");
        }

        this.connectiveChildren = new ArrayList<>();
        this.literalChildren = new ArrayList<>();
        this.addNode = false;
    }

    public List<Connective> getConnectiveChildren(){
        return this.connectiveChildren;
    }

    public List<Literal> getLiteralChildren(){
        return this.literalChildren;
    }

    public Formula getPlaceholder(){ return this.placeholder; }

    public void addChild(Connective node){
        this.connectiveChildren.add(node);
    }

    public void addFeature(String name, BitSet matches){
        Literal node = new Literal(this, name, matches);
        this.literalChildren.add(node);
    }

    public LogicOperator getLogic() {
        return logic;
    }

    public void setAddNode(){ // Add a new feature to the current node
        this.addNode = true;
    }

    public void setAddNode(Literal featureToBeCombinedWith){ // Add a new feature to the current node
        this.addNode = true;
        this.placeholderFeatureIndex = -1;
        for(int i = 0; i < this.literalChildren.size(); i++){
            if(this.literalChildren.get(i) == featureToBeCombinedWith){
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

    public void setPlaceholder(String name, BitSet matches){
        if(this.addNode){
            this.placeholderSet = true;
            this.placeholder = new Literal(this, name, matches);
            this.precomputed = null;

        }else{
            for(Connective node: this.connectiveChildren){
                node.setPlaceholder(name, matches);
            }
        }
    }


    public String getName(){
        
        StringJoiner out;
        
        if(this.logic == LogicOperator.AND){
            out = new StringJoiner("&&");
        }else{
            out = new StringJoiner("||");
        }

        if(!this.literalChildren.isEmpty()){
            for(int i = 0; i < this.literalChildren.size(); i++){
                if(this.placeholderFeatureIndex == i){
                    continue;
                }
                Literal node = this.literalChildren.get(i);
                out.add(node.getName());
            }
        }

        if(!this.connectiveChildren.isEmpty()){
            for(Connective node:this.connectiveChildren){
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
                name.add(this.literalChildren.get(placeholderFeatureIndex).getName());
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
        for(int i = 0; i < this.literalChildren.size(); i++){
            // If there exists at least one feature node, calculate the matches
            if(this.placeholderSet){
                // If the only one feature node is currently set as a placeholder, ignore it
                if(i == this.placeholderFeatureIndex){
                    continue;
                }
            }

            Literal node = this.literalChildren.get(i);
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

        for(Connective node:this.connectiveChildren){
            // Recursively pre-compute matches
            node.precomputeMatches();
        }
    }

    public BitSet getMatches(){

        BitSet out = this.matches;
        Boolean precomputedIsNull = false;

        if(this.addNode && this.placeholderFeatureIndex != -1){
            // Placeholder is set to be one of the feature nodes
            if(this.literalChildren.size() == 1) {
                // this.precomputed is null because the only feature node is the placeholder
                precomputedIsNull = true;
            }

        }else if(this.literalChildren.size() == 0){
            // this.precomputed is null because there is no feature node
            precomputedIsNull = true;

        }

        if(precomputedIsNull == false){

            if(this.precomputed == null) {
                this.precomputeMatches();
            }

            out = (BitSet) this.precomputed.clone();
        }

        if (this.placeholderSet && this.addNode) {

            BitSet placeholderMatches = (BitSet) this.placeholder.getMatches().clone();

            if(this.placeholderFeatureIndex > -1){
                BitSet featureMatches = this.literalChildren.get(this.placeholderFeatureIndex).getMatches();
                if(this.logic == LogicOperator.AND){
                    // Combine with the feature and the placeholder matches using the opposite logical connective
                    // This is basically creating a parent logic node that includes the placeholder and the newly added feature
                    placeholderMatches.or(featureMatches);
                }else{
                    placeholderMatches.and(featureMatches);
                }
            }

            if(precomputedIsNull){
                out = placeholderMatches;
                precomputedIsNull = false;

            }else{
                if(this.logic == LogicOperator.AND){
                    out.and(placeholderMatches);
                }else{
                    out.or(placeholderMatches);
                }
            }
        }

        for(Connective node:this.connectiveChildren){

            BitSet temp = node.getMatches();

            if(precomputedIsNull){
                out = (BitSet) temp.clone();
                precomputedIsNull = false;

            }else{
                if(this.logic == LogicOperator.AND){
                    out.and(temp);
                }else{
                    out.or(temp);
                }
            }
        }

        this.matches = out;
        return out;
    }

    public List<Connective> getDescendantNodes(){
        List<Connective> out = new ArrayList<>();
        out.addAll(this.getDescendantNodes(LogicOperator.AND));
        out.addAll(this.getDescendantNodes(LogicOperator.OR));
        return out;
    }

    public List<Connective> getDescendantNodes(LogicOperator operator){

        List<Connective> out = new ArrayList<>();
        if(this.logic == operator){
            out.add(this);
        }

        for(Connective child:this.connectiveChildren){
            out.addAll(child.getDescendantNodes(operator));
        }
        return out;
    }

}
