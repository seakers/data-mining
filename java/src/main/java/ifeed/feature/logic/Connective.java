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
    
    protected LogicOperator logic;
    protected List<Connective> connectiveChildren;
    protected List<Literal> literalChildren;

    public Connective(LogicOperator logic){
        super();
        this.logic = logic;
        this.connectiveChildren = new ArrayList<>();
        this.literalChildren = new ArrayList<>();
    }

    // Setters
    public void setLogic(LogicOperator logic){ this.logic = logic;}

    public void toggleLogic(){
        if(this.logic == LogicOperator.AND){
            this.logic = LogicOperator.OR;
        }else{
            this.logic = LogicOperator.AND;
        }
    }

    public void addChild(Connective node){
        this.connectiveChildren.add(node);
    }

    public void removeLiteral(Literal literal){
        this.literalChildren.remove(literal);
    }

    public void removeLiteral(int index){
        this.literalChildren.remove(index);
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

    public void createNewBranch(String name, BitSet matches, int literalIndex){

        // Get the literal to be combined with a new literal
        Literal node_to_be_combined_with = this.literalChildren.get(literalIndex);

        // Remove the literal from the list
        this.removeLiteral(literalIndex);

        // Create a new branch
        LogicOperator newBranchLogic;
        if(this.logic == LogicOperator.AND){
            newBranchLogic = LogicOperator.OR;
        }else{
            newBranchLogic = LogicOperator.AND;
        }

        Connective newBranch = new Connective(newBranchLogic);
        newBranch.addLiteral(new Literal(name, matches));
        newBranch.addLiteral(node_to_be_combined_with);
        this.addChild(newBranch);
    }

    // Getters
    public List<Connective> getConnectiveChildren(){
        return this.connectiveChildren;
    }

    public List<Literal> getLiteralChildren(){
        return this.literalChildren;
    }

    public LogicOperator getLogic() {
        return logic;
    }

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

        for(Literal node: this.literalChildren){
            out.add(node.getName());
        }

        for(Connective node:this.connectiveChildren){
            out.add(node.getName());
        }

        String outputString = out.toString();
        if(super.negation){
            outputString = Symbols.logic_not + outputString;
        }
        return  Symbols.compound_expression_wrapper_open + outputString + Symbols.compound_expression_wrapper_close;
    }

    public BitSet getMatchesBeforeNegation(){

        if(this.literalChildren.isEmpty() && this.connectiveChildren.isEmpty()){
            throw new IllegalStateException("No child node exists under a logical connective node");
        }

        BitSet out = null;

        if(this.literalChildren.isEmpty()){
            // Skip

        }else{
            // Compute the matches for all literals inside the current branch
            for(Literal node: this.literalChildren){
                // If there exists at least one literal, calculate the match
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

        for(Connective node:this.connectiveChildren){

            BitSet temp = node.getMatches();
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

        for(Connective branch:this.connectiveChildren){
            copied.addChild(branch.copy());
        }

        for(Literal lit: this.literalChildren){
            copied.addLiteral(lit.copy());
        }
        return copied;
    }

    public void propagateNegationSign(){
        if(this.negation){

            this.negation = false;
            this.toggleLogic();

            for(Connective branch:this.connectiveChildren){
                branch.toggleNegation();
            }

            for(Literal leaf:this.literalChildren){
                leaf.toggleNegation();
            }
        }

        for(Connective branch:this.connectiveChildren){
            branch.propagateNegationSign();
        }
    }

    public List<Connective> getDescendantConnectives(boolean includeSelf){
        List<Connective> out = new ArrayList<>();
        out.addAll(this.getDescendantConnectives(LogicOperator.AND, includeSelf));
        out.addAll(this.getDescendantConnectives(LogicOperator.OR, includeSelf));
        return out;
    }

    /**
     * Returns a list containing all descendant formula (Instances of Connective class)
     * @param operator Logical connective (AND or OR)
     * @return
     */
    public List<Connective> getDescendantConnectives(LogicOperator operator, boolean includeSelf){

        List<Connective> out = new ArrayList<>();

        if(includeSelf){
            if(this.logic == operator){
                out.add(this);
            }
        }

        for(Connective branch:this.connectiveChildren){
            out.addAll(branch.getDescendantConnectives(operator, true));
        }

        return out;
    }

    public List<Literal> getDescendantLiterals(boolean includeSelf){
        List<Literal> out = new ArrayList<>();

        if(includeSelf){
            out.addAll(this.literalChildren);
        }

        for(Connective node: this.connectiveChildren){
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
