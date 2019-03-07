/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.feature.logic;

import java.util.*;

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
     *  Existing literal to be combined with the new node to create a new branch
     */
    private Literal literalToBeCombined;

    public ConnectiveTester(LogicalConnectiveType logic){
        super(logic);
        this.addNewNode = false;
        this.newNode = null;
        this.literalToBeCombined = null;
    }

    public ConnectiveTester(Connective original){
        super(original.getLogic());

        this.setNegation(original.getNegation());

        for(Formula node: original.getChildNodes()){
            if(node instanceof Connective && !(node instanceof ConnectiveTester)){
                this.addNode(new ConnectiveTester((Connective) node));
            }else{
                this.addNode(node.copy());
            }
        }

        if(original.precomputedMatchesLiteral != null){
            this.setPrecomputedMatchesLiteral((BitSet) original.precomputedMatchesLiteral.clone());
        }
        if(original.precomputedMatchesConnective != null){
            this.setPrecomputedMatchesConnective((BitSet) original.precomputedMatchesConnective.clone());
        }
    }

    @Override
    public void setNodes(List<Formula> nodes){
        List<Formula> temp = new ArrayList<>();
        for(Formula node: nodes){
            if(node instanceof Connective && !(node instanceof ConnectiveTester)){
                temp.add(new ConnectiveTester((Connective)node));
            }else{
                temp.add(node);
            }
        }
        super.setNodes(temp);
    }

    @Override
    public void addNode(Formula node){
        if(node instanceof  Connective && !(node instanceof ConnectiveTester)){
            super.addNode(new ConnectiveTester((Connective)node));
        }else{
            super.addNode(node);
        }
    }

    @Override
    public void addNode(int index, Formula node){
        if(node instanceof  Connective && !(node instanceof ConnectiveTester)){
            super.addNode(index, new ConnectiveTester((Connective)node));
        }else{
            super.addNode(index, node);
        }
    }

    @Override
    public void addNodes(Collection<Formula> nodes){
        List<Formula> temp = new ArrayList<>();
        for(Formula node: nodes){
            if(node instanceof Connective && !(node instanceof ConnectiveTester)){
                temp.add(new ConnectiveTester((Connective)node));
            }else{
                temp.add(node);
            }
        }
        super.addNodes(temp);
    }

    /**
     * Sets the current node to add a new literal. (no new branch created)
     */
    public void setAddNewNode(){
        this.addNewNode = true;
        this.newNode = null;
        this.literalToBeCombined = null;
        super.structureModified();
    }

    /**
     * Sets the current node to add a new literal. (new branch will be created)
     * Literal literal
     */
    public void setAddNewNode(Literal literal){
        this.addNewNode = true;
        this.newNode = null;
        this.literalToBeCombined = null;

        super.literalModified();

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
        super.structureModified();
    }

    public Formula getNewNode(){ return this.newNode; }

    public boolean getAddNewNode(){
        return this.addNewNode;
    }

    public Literal getLiteralToBeCombined(){
        return this.literalToBeCombined;
    }

    public void setNewNode(Formula node){
        if(this.addNewNode){
            this.newNode = node;

        }else{
            for(Connective branch: this.getConnectiveChildren()){
                ConnectiveTester tester = (ConnectiveTester) branch;
                tester.setNewNode(node);
            }
        }
    }

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

    public void finalizeNewNodeAddition(){

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
                newBranch.addNode(this.newNode);
                this.addBranch(newBranch);

            }else{
                // New literal is added to the current node
                this.addNode(this.newNode);
            }
            this.cancelAddNode();
        }

        for(Connective branch: this.getConnectiveChildren()){
            ConnectiveTester tester = (ConnectiveTester) branch;
            tester.finalizeNewNodeAddition();
        }
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
                if(this.newNode != null){
                    name.add(this.newNode.getName());
                }
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

    public BitSet getMatchesOriginalFeature() {
        BitSet out = this.getMatchesBeforeNegation(true);
        if(this.negation){
            out.flip(0, out.size());
        }
        return out;
    }

    @Override
    public BitSet getMatchesBeforeNegation(){
        return this.getMatchesBeforeNegation(false);
    }

    public BitSet getMatchesBeforeNegation(boolean computeOriginalMatches){

        if(this.childNodes.isEmpty()){
            throw new IllegalStateException("No child node exists under a logical connective node");
        }

        if(this.precomputedMatchesLiteral == null){
            this.precomputeMatchesLiteral(computeOriginalMatches);
        }

        if(this.precomputedMatchesConnective == null){
            this.precomputeMatchesConnective();
        }

        BitSet out;
        if(this.precomputedMatchesLiteral == null && this.precomputedMatchesConnective == null) {
            if(computeOriginalMatches){
                out = null;

            }if(this.addNewNode && this.newNode != null){
                out = null;

            }else{
                throw new IllegalStateException("Connective node without any child branch or literal");
            }

        }else if(this.precomputedMatchesLiteral == null) {
            out = (BitSet) this.precomputedMatchesConnective.clone();

        }else if(this.precomputedMatchesConnective == null){
            out = (BitSet) this.precomputedMatchesLiteral.clone();

        }else{
            out = (BitSet) this.precomputedMatchesLiteral.clone();
            if(this.logic == LogicalConnectiveType.AND){
                out.and(this.precomputedMatchesConnective);
            }else{
                out.or(this.precomputedMatchesConnective);
            }
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

        return out;
    }


    @Override
    public void precomputeMatchesLiteral(){
        this.precomputeMatchesLiteral(false);
    }

    /**
     * Computes the matches for all literals inside the current branch
     */
    public void precomputeMatchesLiteral(boolean computeOriginalMatches){

        if(this.precomputedMatchesLiteral == null){
            BitSet out = null;
            List<Literal> literals = super.getLiteralChildren();

            // If there exists at least one literal, calculate the match
            for(Literal node: literals){
                if(this.addNewNode && this.literalToBeCombined == node && !computeOriginalMatches){
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
            // If there exists only one literal in the current node, precomputedMatchesLiteral may still be null.
            this.precomputedMatchesLiteral = out;
        }

        // Recursively compute matches in all child branches
        for(Connective node: super.getConnectiveChildren()){
            node.precomputeMatchesLiteral();
        }
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
                copied.setAddNewNode(this.literalToBeCombined.copy());
            }

            if(this.newNode != null){
                copied.setNewNode(this.newNode.copy());
            }
        }

        for(Formula node: this.childNodes){
            copied.addNode(node.copy());
        }

        if(this.precomputedMatchesLiteral != null){
            copied.setPrecomputedMatchesLiteral((BitSet) this.precomputedMatchesLiteral.clone());
        }
        if(this.precomputedMatchesConnective != null){
            copied.setPrecomputedMatchesConnective((BitSet) this.precomputedMatchesConnective.clone());
        }

        return copied;
    }
}
