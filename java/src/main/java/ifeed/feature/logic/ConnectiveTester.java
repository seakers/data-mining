/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.feature.logic;

import java.util.BitSet;
import java.util.List;
import java.util.StringJoiner;
import ifeed.expression.Symbols;

/**
 * Class to test adding a new literal or a new connecitve node.
 *
 * @author bang
 */

public class ConnectiveTester extends Connective {

    /**
     * Flag for adding the new literal under the current node
     */
    private boolean addNewNode;

    /**
     * Placeholder for the new node (could be either a literal or a logical connective)
     */
    private Formula newNode;

    /**
     * Index of the literal to be used to create a new branch
     */
    private Literal literalToBeCombined;

    /**
     * Matches computed for all given literals in the current node
     */
    private BitSet precomputedMatches;

    public ConnectiveTester(LogicalConnectiveType logic){
        super(logic);
        this.addNewNode = false;
        this.newNode = null;
        this.literalToBeCombined = null;
        this.precomputedMatches = null;
    }

    /**
     * Adds a new connectiveTester node as a child
     * @param node
     */
    @Override
    public void addBranch(Connective node){
        if(node instanceof ConnectiveTester){
            this.childNodes.add(node);

        }else{
            ConnectiveTester tester = new ConnectiveTester(node.getLogic());

            tester.setNegation(node.getNegation());

            for(Literal literal: node.getLiteralChildren()){
                tester.addLiteral(literal);
            }

            // Recursively convert Connective class to ConnectiveTester
            for(Connective branch: node.getConnectiveChildren()){
                tester.addBranch(branch);
            }

            this.childNodes.add(tester);
        }
    }

    @Override
    public void setLogic(LogicalConnectiveType logic){
        super.setLogic(logic);
        this.precomputedMatches = null;
    }

    @Override
    public void toggleLogic(){
        super.toggleLogic();
        this.precomputedMatches = null;
    }

    @Override
    public void addNode(Formula node){
        super.addNode(node);
        this.precomputedMatches = null;
    }

    @Override
    public void removeNode(Formula node){
        super.removeNode(node);
        this.precomputedMatches = null;
    }

    @Override
    public void addLiteral(Literal literal){
        super.addLiteral(literal);
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

    @Override
    public void removeLiteral(Literal literal){
        super.removeLiteral(literal);
        this.precomputedMatches = null;
    }

    @Override
    public void removeLiterals(){
        super.removeLiterals();
        this.precomputedMatches = null;
    }


    /**
     * Sets the current node to add a new literal. (no new branch created)
     */
    public void setAddNewNode(){
        this.addNewNode = true;
        this.newNode = null;
        this.literalToBeCombined = null;
        this.precomputedMatches = null;
    }

    /**
     * Sets the current node to add a new literal. (new branch will be created)
     * Literal literal
     */
    public void setAddNewNode(Literal literal){
        this.addNewNode = true;
        this.newNode = null;
        this.literalToBeCombined = null;
        this.precomputedMatches = null;

        if(super.getLiteralChildren().contains(literal)){
            this.literalToBeCombined = literal;
        } else{
            throw new RuntimeException("Exc in locating the feature :" + literal.getName() + " in " + this.getName());
        }
    }

    public void cancelAddNode(){
        this.addNewNode = false;
        this.newNode = null;
        this.literalToBeCombined = null;
        this.precomputedMatches = null;
    }

    public Formula getNewNode(){ return this.newNode; }

    public void setNewNode(String name, BitSet matches){
        if(this.addNewNode){
            this.newNode = new Literal(name, matches);

        }else{
            for(Connective branch: this.getConnectiveChildren()){
                ConnectiveTester tester = (ConnectiveTester) branch;
                tester.setNewNode(name, matches);
            }
        }
    }

    public boolean finalizeNewNodeAddition(){

        if(this.addNewNode && this.newNode != null){

            if(this.literalToBeCombined != null){ // New branch is created

                // Remove the literal from the list
                this.removeLiteral(this.literalToBeCombined);

                // Create a new branch
                LogicalConnectiveType newBranchLogic;
                if(this.logic == LogicalConnectiveType.AND){
                    newBranchLogic = LogicalConnectiveType.OR;
                }else{
                    newBranchLogic = LogicalConnectiveType.AND;
                }

                ConnectiveTester newBranch = new ConnectiveTester(newBranchLogic);
                newBranch.addLiteral(this.literalToBeCombined);
                newBranch.addLiteral((Literal) this.newNode);
                this.addBranch(newBranch);

            }else{
                this.addLiteral((Literal) this.newNode);

            }
            this.cancelAddNode();
            return true;

        }else{
            // New literal is added to a pre-existing branch
            for(Connective branch: this.getConnectiveChildren()){
                ConnectiveTester tester = (ConnectiveTester) branch;
                if(tester.finalizeNewNodeAddition()){
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String getName(){

        if(this.childNodes.isEmpty()){
            throw new IllegalStateException("No child node exists under a logical connective node");
        }

        StringJoiner out;
        if(this.logic == LogicalConnectiveType.AND){
            out = new StringJoiner(Symbols.logic_and);
        }else{
            out = new StringJoiner(Symbols.logic_or);
        }

        for(Formula node: this.childNodes){
            if(node instanceof Literal && node == this.literalToBeCombined){

                // The new literal is combined with an existing literal inside a new branch
                StringJoiner name;

                // The new branch has an opposite logical connective
                if(this.logic == LogicalConnectiveType.AND){
                    name = new StringJoiner(Symbols.logic_or);
                }else{
                    name = new StringJoiner(Symbols.logic_and);
                }

                name.add(this.literalToBeCombined.getName());
                name.add(this.newNode.getName());
                out.add(Symbols.compound_expression_wrapper_open + name.toString() + Symbols.compound_expression_wrapper_close);

            }else{
                out.add(node.getName());

            }
        }

        if(this.literalToBeCombined == null){
            if(this.newNode != null){
                out.add(this.newNode.getName());
            }
        }

        String outputString = out.toString();
        if(super.negation){
            outputString = Symbols.logic_not + outputString;
        }

        return  Symbols.compound_expression_wrapper_open + outputString + Symbols.compound_expression_wrapper_close;
    }

    /**
     * Computes the matches for all literals inside the current branch
     */
    public void preComputeMatchesLiteral(){

        if(this.precomputedMatches == null){

            BitSet out = null;

            List<Literal> literals = super.getLiteralChildren();

            // If there exists at least one literal, calculate the match
            for(Literal node: literals){

                if(this.addNewNode && this.literalToBeCombined == node){
                    // skip this literal in computing the match, as it will later be combined with the newly added literal
                    continue;

                }else{
                    if(out == null){
                        out = (BitSet) node.getMatches().clone();

                    }else{
                        if(this.logic == LogicalConnectiveType.AND){
                            out.and(node.getMatches());
                        }else{
                            out.or(node.getMatches());
                        }
                    }
                }
            }

            // If there exists only one literal in the current node, precomputedMatches may still be null.
            this.precomputedMatches = out;
        }

        // Recursively compute matches in all child branches
        for(Connective node: super.getConnectiveChildren()){
            ((ConnectiveTester) node).preComputeMatchesLiteral();
        }
    }

    @Override
    public BitSet getMatches(){

        if(super.childNodes.isEmpty()){
            throw new IllegalStateException("No child node exists under a logical connective node");
        }

        // If there exist no precomputed matches, compute it
        if(this.precomputedMatches == null){
            this.preComputeMatchesLiteral();
        }

        BitSet out;
        if(this.precomputedMatches == null){
            out = null;

        }else{
            out = (BitSet) this.precomputedMatches.clone();
        }

        // If the new node is to be added to the current node
        if (this.addNewNode && this.newNode != null) {

            // Get matches of the new literal
            BitSet newNodeMatches = (BitSet) this.newNode.getMatches().clone();

            if(this.literalToBeCombined != null){

                // The new literal is to be combined with an existing literal
                BitSet existingNodeMatches = this.literalToBeCombined.getMatches();

                if(this.logic == LogicalConnectiveType.AND){
                    // Combine the matches of the new literal and an existing literal using the opposite logical connective
                    // This is basically creating a new branch (Connective class) that includes both literals
                    newNodeMatches.or(existingNodeMatches);
                }else{
                    newNodeMatches.and(existingNodeMatches);
                }
            }

            if(out == null){
                out = newNodeMatches;

            }else{
                if(this.logic == LogicalConnectiveType.AND){
                    out.and(newNodeMatches);
                }else{
                    out.or(newNodeMatches);
                }
            }
        }

        for(Connective branch: super.getConnectiveChildren()){
            BitSet temp = branch.getMatches();
            if(out == null){
                out = (BitSet) temp.clone();

            }else{
                if(this.logic == LogicalConnectiveType.AND){
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

    /**
     * Performs deep copy of the current node
     * @return
     */
    @Override
    public ConnectiveTester copy(){

        ConnectiveTester copied = new ConnectiveTester(this.logic);
        copied.setNegation(this.negation);

        if(this.addNewNode){
            if(this.literalToBeCombined == null){
                copied.setAddNewNode();

            }else{
                copied.setAddNewNode(this.literalToBeCombined);
            }

            if(this.newNode != null){
                copied.setNewNode(this.newNode.getName(), (BitSet) this.newNode.getMatches().clone());
            }
        }

        for(Formula node: this.childNodes){
            copied.addNode(node.copy());
        }
        return copied;
    }
}
