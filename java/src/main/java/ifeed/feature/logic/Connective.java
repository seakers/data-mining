/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.feature.logic;

import java.util.*;

import ifeed.expression.Symbols;

/**
 * Implementation of a logical connective in a feature tree
 *
 * @author bang
 */

public class Connective extends Formula {
    
    protected LogicalConnectiveType logic;
    protected List<Formula> childNodes;

    public Connective(LogicalConnectiveType logic){
        super();
        this.logic = logic;
        this.childNodes = new ArrayList<>();
    }

    // Setters
    public void setLogic(LogicalConnectiveType logic){ this.logic = logic;}

    public void toggleLogic(){
        if(this.logic == LogicalConnectiveType.AND){
            this.logic = LogicalConnectiveType.OR;
        }else{
            this.logic = LogicalConnectiveType.AND;
        }
    }

    public void setNodes(List<Formula> nodes){
        this.childNodes = nodes;
    }

    public void addNode(Formula node){
        this.childNodes.add(node);
    }

    public void addNodes(Collection<Formula> nodes){
        this.childNodes.addAll(nodes);
    }

    public void removeNode(Formula node){
        this.childNodes.remove(node);
    }

    public void removeNodes(Collection<Formula> nodes){
        this.childNodes.removeAll(nodes);
    }

    public void addBranch(Connective branch){
        this.addNode(branch);
    }

    public void addLiteral(Literal literal){
        this.addNode(literal);
    }

    public void addLiteral(String name, BitSet matches, boolean negation){
        Literal node = new Literal(name, matches);
        node.setNegation(negation);
        this.addNode(node);
    }

    public void addLiteral(String name, BitSet matches){
        // Negation is false by default
        this.addLiteral(name, matches, false);
    }

    public void removeLiteral(Literal literal){
        this.removeNode(literal);
    }

    /**
     * Creates a new branch, and adds two literals. One of the literal is selected from pre-existing list of literals,
     * and another one is added from the function arguments.
     *
     * @param name
     * @param matches
     * @param literal
     */
    public void createNewBranch(Literal literal, String name, BitSet matches){

        // Get the literal to be combined with a new literal
        int index = this.getNodeIndex(literal);
        Literal node_to_be_combined_with = (Literal) this.childNodes.get(index);

        // Remove the literal from the list
        this.removeLiteral(node_to_be_combined_with);

        // Create a new branch
        LogicalConnectiveType newBranchLogic;
        if(this.logic == LogicalConnectiveType.AND){
            newBranchLogic = LogicalConnectiveType.OR;
        }else{
            newBranchLogic = LogicalConnectiveType.AND;
        }

        Connective newBranch = new Connective(newBranchLogic);
        newBranch.addLiteral(new Literal(name, matches));
        newBranch.addLiteral(node_to_be_combined_with);
        this.addBranch(newBranch);
    }


    public void removeBranches(){
        for(Connective branch: this.getConnectiveChildren()){
            this.childNodes.remove(branch);
        }
    }

    public void removeLiterals(){
        for(Literal node: this.getLiteralChildren()){
            this.childNodes.remove(node);
        }
    }

    // Getters
    public int getNodeIndex(Formula node){
        return this.childNodes.indexOf(node);
    }

    public List<Formula> getChildNodes(){
        return this.childNodes;
    }

    public List<Connective> getConnectiveChildren(){
        List<Connective> connectives = new ArrayList<>();
        for(Formula node: this.childNodes){
            if(node instanceof Connective){
                connectives.add((Connective) node);
            }
        }
        return connectives;
    }

    public List<Literal> getLiteralChildren(){
        List<Literal> literals = new ArrayList<>();
        for(Formula node: this.childNodes){
            if(node instanceof Literal){
                literals.add((Literal) node);
            }
        }
        return literals;
    }

    public LogicalConnectiveType getLogic() {
        return logic;
    }

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
            out.add(node.getName());
        }

        String outputString = out.toString();
        if(super.negation){
            outputString = Symbols.logic_not + outputString;
        }
        return  Symbols.compound_expression_wrapper_open + outputString + Symbols.compound_expression_wrapper_close;
    }

    public BitSet getMatchesBeforeNegation(){

        if(this.childNodes.isEmpty()){
            throw new IllegalStateException("No child node exists under a logical connective node");
        }

        BitSet out = null;
        for(Formula node: this.childNodes){

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
        this.matches = out;
        return out;
    }

    public BitSet getMatches(){
        BitSet out = this.getMatchesBeforeNegation();
        if(this.negation){
            out.flip(0, out.size());
        }
        return out;
    }

    public Connective copy(){
        Connective copied = new Connective(this.logic);
        copied.setNegation(this.negation);

        for(Formula node: this.childNodes){
            copied.addNode(node.copy());
        }
        return copied;
    }

    public void propagateNegationSign(){
        if(this.negation){
            this.negation = false;
            this.toggleLogic();

            for(Formula node:this.childNodes){
                node.applyNegation();
            }
        }

        for(Connective branch: this.getConnectiveChildren()){
            branch.propagateNegationSign();
        }
    }

    public List<Connective> getDescendantConnectives(boolean includeSelf){
        List<Connective> out = new ArrayList<>();
        out.addAll(this.getDescendantConnectives(LogicalConnectiveType.AND, includeSelf));
        out.addAll(this.getDescendantConnectives(LogicalConnectiveType.OR, includeSelf));
        return out;
    }

    /**
     * Returns a list containing all descendant formula (Instances of Connective class)
     * @param operator Logical connective (AND or OR)
     * @return
     */
    public List<Connective> getDescendantConnectives(LogicalConnectiveType operator, boolean includeSelf){

        List<Connective> out = new ArrayList<>();

        if(includeSelf){
            if(this.logic == operator){
                out.add(this);
            }
        }

        for(Connective branch:this.getConnectiveChildren()){
            out.addAll(branch.getDescendantConnectives(operator, true));
        }

        return out;
    }

    public List<Literal> getDescendantLiterals(boolean includeSelf){
        List<Literal> out = new ArrayList<>();

        if(includeSelf){
            out.addAll(this.getLiteralChildren());
        }

        for(Connective node: this.getConnectiveChildren()){
            out.addAll(node.getDescendantLiterals(true));
        }

        return out;
    }

    public List<Formula> getDescendantNodes(boolean includeSelf){
        List<Formula> out = new ArrayList<>();
        out.addAll(this.getDescendantConnectives(includeSelf));
        out.addAll(this.getDescendantLiterals(true));
        return out;
    }

    public int getNumDescendantNodes(boolean includeSelf){
        return this.getDescendantLiterals(includeSelf).size() + getDescendantConnectives(includeSelf).size();
    }

}
