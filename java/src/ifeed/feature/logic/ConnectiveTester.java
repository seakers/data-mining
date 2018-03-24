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
    private boolean placeholderFilled = false;

    /**
     * Placeholder for new node (could be either a literal or a logical connective)
     */
    private Formula placeholder = null;

    /**
     * Index of the literal to be used to create a new branch
     */
    private int placeholderLiteralIndex = -1;


    public ConnectiveTester(LogicOperator logic){
        super(logic);
        this.addNewLiteral = false;
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

        StringJoiner out;

        if(this.literalChildren.isEmpty() && this.connectiveChildren.isEmpty()){

            if(this.logic == LogicOperator.AND){
                out = new StringJoiner(Symbols.logic_or);
            }else{
                out = new StringJoiner(Symbols.logic_and);
            }

        }else{
            out = super.getNameStringJoiner();

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

    @Override
    public BitSet getMatches(){

        BitSet out;
        if(this.literalChildren.isEmpty() && this.connectiveChildren.isEmpty()){
            out = null;

        }else{
            out = super.getMatchesIgnoreNegation();
        }

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

        this.matches = out;

        if(this.negation){
            out.flip(0,out.size());
        }

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
