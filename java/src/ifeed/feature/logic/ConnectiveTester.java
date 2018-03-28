/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.feature.logic;

import java.util.BitSet;
import java.util.StringJoiner;
import ifeed.expression.Symbols;

/**
 * Implementation of a logical connective in a feature tree
 *
 * @author bang
 */

public class ConnectiveTester extends Connective {

    /**
     * Flag for adding new literal under the current node
     */
    private boolean addNewLiteral;

    /**
     * Flag for indicating whether the placeholder has been filled
     */
    private boolean placeholderFilled;

    /**
     * Placeholder for new node (could be either a literal or a logical connective)
     */
    private Formula placeholder;

    /**
     * Index of the literal to be used to create a new branch
     */
    private int placeholderLiteralIndex;

    /**
     * Matches computed for all given literals in the current node
     */
    private BitSet precomputedMatches;


    public ConnectiveTester(LogicOperator logic){
        super(logic);
        this.addNewLiteral = false;
        this.placeholderFilled = false;
        this.placeholder = null;
        this.placeholderLiteralIndex = -1;
        this.precomputedMatches = null;
    }

    @Override
    public void addChild(Connective node){
        if(node instanceof ConnectiveTester){
            this.connectiveChildren.add(node);

        }else{
            ConnectiveTester tester = new ConnectiveTester(node.getLogic());

            tester.setNegation(node.getNegation());

            for(Literal literal: node.getLiteralChildren()){
                tester.addLiteral(literal);
            }

            // Recursively convert Connective class to ConnectiveTester
            for(Connective branch: node.getConnectiveChildren()){
                tester.addChild(branch);
            }

            this.connectiveChildren.add(tester);
        }
    }

    @Override
    public void removeLiteral(Literal literal){
        super.removeLiteral(literal);
        this.precomputedMatches = null;
    }

    @Override
    public void removeLiteral(int index){
        super.removeLiteral(index);
        this.precomputedMatches = null;
    }

    @Override
    public void addLiteral(Literal newLiteral){
       super.addLiteral(newLiteral);
       this.precomputedMatches = null;
    }

    @Override
    public void addLiteral(String name, BitSet matches, boolean negation){
        super.addLiteral(name, matches, negation);
        this.precomputedMatches = null;
    }

    @Override
    public void addLiteral(String name, BitSet matches){
        super.addLiteral(name, matches);
        this.precomputedMatches = null;
    }

    public void setAddNewLiteral(){ // Add a new literal to the current node (no new branch created)
        this.addNewLiteral = true;
        this.placeholderLiteralIndex = -1;
    }

    public void setAddNewLiteral(int index){ // Add a new literal to the current node (new branch will be created)
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
        this.placeholderFilled = false;
        this.placeholderLiteralIndex = -1;
        this.precomputedMatches = null;
    }

    public Formula getPlaceholder(){ return this.placeholder; }

    public void setPlaceholder(String name, BitSet matches){
        if(this.addNewLiteral){
            this.placeholderFilled = true;
            this.placeholder = new Literal(name, matches);

        }else{
            for(Connective branch: this.connectiveChildren){
                ConnectiveTester tester = (ConnectiveTester) branch;
                tester.setPlaceholder(name, matches);
            }
        }
    }

    public boolean fillPlaceholder(){

        if(this.addNewLiteral && this.placeholderFilled){

            if(this.placeholderLiteralIndex > -1){ // New literal is added to a new branch

                // Get the literal to be combined with a new literal
                Literal node = this.literalChildren.get(this.placeholderLiteralIndex);

                // Remove the literal from the list
                this.removeLiteral(this.placeholderLiteralIndex);

                // Create a new branch
                LogicOperator newBranchLogic;
                if(this.logic == LogicOperator.AND){
                    newBranchLogic = LogicOperator.OR;
                }else{
                    newBranchLogic = LogicOperator.AND;
                }

                ConnectiveTester newBranch = new ConnectiveTester(newBranchLogic);
                newBranch.addLiteral(node);
                newBranch.addLiteral((Literal) this.placeholder);
                this.addChild(newBranch);

            }else{
                this.addLiteral((Literal) this.placeholder);

            }
            this.cancelAddNode();
            return true;

        }else{
            for(Connective branch:this.connectiveChildren){
                ConnectiveTester tester = (ConnectiveTester) branch;
                if(tester.fillPlaceholder()){
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String getName(){

        if(this.literalChildren.isEmpty() && this.connectiveChildren.isEmpty()){
            throw new IllegalStateException("No child node exists under a logical connective node");
        }

        StringJoiner out;
        if(this.logic == LogicOperator.AND){
            out = new StringJoiner(Symbols.logic_and);
        }else{
            out = new StringJoiner(Symbols.logic_or);
        }

        if(!this.literalChildren.isEmpty()){
            for(int i = 0; i < this.literalChildren.size(); i++){
                if(this.placeholderLiteralIndex == i){
                    // This literal is going to be combined with the new literal, so skip it for now
                    continue;
                }
                // Add the name of each literal
                Literal node = this.literalChildren.get(i);
                out.add(node.getName());
            }
        }

        if(!this.connectiveChildren.isEmpty()){
            for(Connective node:this.connectiveChildren){
                out.add(node.getName());
            }
        }

        if(this.addNewLiteral && placeholderFilled){

            if(this.placeholderLiteralIndex > -1){
                // The new literal is combined with an existing literal inside a new branch
                StringJoiner name;

                // The new branch has an opposite logical connective
                if(this.logic == LogicOperator.AND){
                    name = new StringJoiner(Symbols.logic_or);
                }else{
                    name = new StringJoiner(Symbols.logic_and);
                }

                name.add(this.literalChildren.get(placeholderLiteralIndex).getName());
                name.add(this.placeholder.getName());
                out.add(Symbols.compound_expression_wrapper_open + name.toString() + Symbols.compound_expression_wrapper_close);

            }else{ // The new literal is simply added to the current branch
                out.add(this.placeholder.getName());
            }
        }

        String outputString = out.toString();
        if(super.negation){
            outputString = Symbols.logic_not + outputString;
        }

        return  Symbols.compound_expression_wrapper_open + outputString + Symbols.compound_expression_wrapper_close;
    }

    public void computeMatchesLiteral(){
        // Compute the matches for all literals inside the current branch

        BitSet out = null;

        for(int i = 0; i < this.literalChildren.size(); i++){
            // If there exists at least one literal, calculate the match

            if(this.addNewLiteral && this.placeholderLiteralIndex == i){
                // skip this literal, as it will later be combined with the new literal
                continue;

            }else{

                Literal node = this.literalChildren.get(i);

                if(out == null){
                    out = (BitSet) node.getMatches().clone();

                }else{
                    if(this.logic == LogicOperator.AND){
                        out.and(node.getMatches());
                    }else{
                        out.or(node.getMatches());
                    }
                }
            }
        }

        this.precomputedMatches = out;
        for(Connective node:this.connectiveChildren){
            // Recursively compute matches in the child branches
            ((ConnectiveTester) node).computeMatchesLiteral();
        }
    }

    @Override
    public BitSet getMatches(){

        if(this.literalChildren.isEmpty() && this.connectiveChildren.isEmpty()){
            throw new IllegalStateException("No child node exists under a logical connective node");
        }

        if(this.precomputedMatches == null){
            this.computeMatchesLiteral();
        }

        BitSet out = (BitSet) this.precomputedMatches.clone();

        if (this.addNewLiteral && this.placeholderFilled) {

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

            if(out == null){
                out = placeholderMatches;

            }else{
                if(this.logic == LogicOperator.AND){
                    out.and(placeholderMatches);
                }else{
                    out.or(placeholderMatches);
                }
            }
        }

        for(Connective branch:this.connectiveChildren){
            BitSet temp = branch.getMatches();
            if(out == null){
                out = (BitSet) temp.clone();

            }else{
                if(this.logic == LogicOperator.AND){
                    out.and(temp);
                }else{
                    out.or(temp);
                }
            }
        }

        if(this.negation){
            out.flip(0, out.size());
        }
        this.matches = out;
        return out;
    }

    @Override
    public ConnectiveTester copy(){

        ConnectiveTester copied = new ConnectiveTester(this.logic);
        copied.setNegation(this.negation);

        if(this.addNewLiteral){
            if(this.placeholderLiteralIndex == -1){
                copied.setAddNewLiteral();

            }else{
                copied.setAddNewLiteral(this.placeholderLiteralIndex);
            }

            if(this.placeholderFilled){
                copied.setPlaceholder(this.placeholder.getName(), (BitSet) this.placeholder.getMatches().clone());
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
