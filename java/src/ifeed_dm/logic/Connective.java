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
    
    private LogicOperator logic;
    private List<Connective> connectiveChildren;
    private List<Literal> literalChildren;
    
    private boolean addNewLiteral; 

    private boolean placeholderSet = false;
    private Formula placeholder = null;
    private int placeholderLiteralIndex = -1;
    private BitSet precomputed = null;

    public Connective(LogicOperator logic){

        super();

        this.logic = logic;

        if(this.logic == LogicOperator.AND){
            name = new StringJoiner("&&");
        }else{
            name = new StringJoiner("||");
        }

        this.connectiveChildren = new ArrayList<>();
        this.literalChildren = new ArrayList<>();
        this.addNewLiteral = false;
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

    public void addLiteral(Literal newLiteral){
        this.literalChildren.add(newLiteral);
    }

    public void addLiteral(String name, BitSet matches, boolean negation){
        Literal node = new Literal(name, matches);
        node.setNegation(negation);
        this.literalChildren.add(node);
    }

    public void addLiteral(String name, BitSet matches){
        // Negation is false by default
        this.addLiteral(name, matches, false);
    }

    public LogicOperator getLogic() {
        return logic;
    }

    public void setAddNewLiteral(){ // Add a new literal to the current node
        this.addNewLiteral = true;
    }

    public void setAddNewLiteral(int index){
        this.addNewLiteral = true;
        this.placeholderLiteralIndex = index;
    }

    public void setAddNewLiteral(Literal literalToBeCombinedWith){ // Add a new literal to the current node
        this.addNewLiteral = true;
        this.placeholderLiteralIndex = -1;
        for(int i = 0; i < this.literalChildren.size(); i++){
            if(this.literalChildren.get(i) == literalToBeCombinedWith){
                this.placeholderLiteralIndex = i;
            }
        }
        if(this.placeholderLiteralIndex == -1){
            throw new RuntimeException("Exc in locating the feature :" + literalToBeCombinedWith.getName() + " in " + this.getName());
        }
    }

    public void cancelAddNode(){
        this.addNewLiteral = false;
        this.placeholder = null;
        this.placeholderSet = false;
        this.placeholderLiteralIndex = -1;
        this.precomputed = null;
    }

    public void setPlaceholder(String name, BitSet matches){
        if(this.addNewLiteral){
            this.placeholderSet = true;
            this.placeholder = new Literal(name, matches);
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
                if(this.placeholderLiteralIndex == i){
                    // This literal is going to be combined with the new literal
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

        if(this.addNewLiteral && placeholderSet){
            if(this.placeholderLiteralIndex > -1){
                // The new literal is combined with an existing literal inside a new branch
                StringJoiner name;

                // The new branch has an opposite logical connective
                if(this.logic == LogicOperator.AND){
                    name = new StringJoiner("||");
                }else{
                    name = new StringJoiner("&&");
                }

                name.add(this.literalChildren.get(placeholderLiteralIndex).getName());
                name.add(this.placeholder.getName());
                out.add( "(" + name.toString() + ")");

            }else{ // The new literal is simply added to the current branch
                out.add(this.placeholder.getName());
            }
        }

        String outputString = out.toString();

        if(super.negation){
            outputString = "~" + outputString;
        }
        return  "(" + outputString + ")";
    }

    public void precomputeMatches(){
        // Compute the matches for all literals inside the current branch

        BitSet out = super.matches; // Initialize variable (not used)

        boolean first = true;
        for(int i = 0; i < this.literalChildren.size(); i++){
            // If there exists at least one literal, calculate the match
            if(this.placeholderSet){
                // If this literal is to be used for creating a new branch, skip it
                if(i == this.placeholderLiteralIndex){
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
            // Recursively compute matches in the child branches
            node.precomputeMatches();
        }
    }

    public BitSet getMatches(){

        BitSet out = this.matches;
        Boolean precomputedIsNull = false;

        if(this.addNewLiteral && this.placeholderLiteralIndex != -1){
            // The new literal is to be combined with an existing literal
            if(this.literalChildren.size() == 1) {
                // this.precomputed is null because the only literal is being used to create the new branch
                precomputedIsNull = true;
            }

        }else if(this.literalChildren.size() == 0){
            // this.precomputed is null because there is no literal
            precomputedIsNull = true;

        }

        if(precomputedIsNull == false){
            if(this.precomputed == null) {
                this.precomputeMatches();
            }
            out = (BitSet) this.precomputed.clone();
        }

        if (this.addNewLiteral && this.placeholderSet) {

            // Get matches of the new literal
            BitSet placeholderMatches = (BitSet) this.placeholder.getMatches().clone();

            if(this.placeholderLiteralIndex > -1){
                // The new literal is to be combined with an existing literal
                BitSet featureMatches = this.literalChildren.get(this.placeholderLiteralIndex).getMatches();
                if(this.logic == LogicOperator.AND){
                    // Combine the matches of the new literal and an existing literal using the opposite logical connective
                    // This is basically creating a new branch (Connective class) that includes both literals
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

        if(this.negation){
            BitSet copy = (BitSet) out.clone();
            copy.flip(0,copy.size());
            out = copy;
        }

        return out;
    }


    public List<Connective> getDescendants(){
        List<Connective> out = new ArrayList<>();
        out.addAll(this.getDescendants(LogicOperator.AND));
        out.addAll(this.getDescendants(LogicOperator.OR));
        return out;
    }

    /**
     * Returns a list containing all descendant formula (Instances of Connective class)
     * @param operator Logical connective (AND or OR)
     * @return
     */
    public List<Connective> getDescendants(LogicOperator operator){
        List<Connective> out = new ArrayList<>();
        if(this.logic == operator){
            out.add(this);
        }
        for(Connective child:this.connectiveChildren){
            out.addAll(child.getDescendants(operator));
        }
        return out;
    }


    public Connective copy(){

        Connective copied = new Connective(this.logic);
        copied.setNegation(this.negation);

        if(this.addNewLiteral){
            if(this.placeholderLiteralIndex == -1){
                copied.setAddNewLiteral();
            }else{
                copied.setAddNewLiteral(this.placeholderLiteralIndex);
            }

            if(this.placeholderSet){
                copied.setPlaceholder(this.placeholder.getName(), this.placeholder.getMatches());
            }
        }

        for(Connective branch:this.connectiveChildren){
            copied.addChild(branch.copy());
        }

        for(Literal lit: this.literalChildren){
            copied.addLiteral(lit.copy());
        }

        return copied;
    }

}
