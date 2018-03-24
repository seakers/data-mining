/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.feature.logic;

import java.util.BitSet;
import java.util.List;
import java.util.ArrayList;
import java.util.StringJoiner;
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
    protected BitSet precomputed;

    public Connective(LogicOperator logic){
        super();
        this.logic = logic;
        this.connectiveChildren = new ArrayList<>();
        this.literalChildren = new ArrayList<>();
        this.precomputed = null;
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
        this.precomputed = null;
    }

    public void removeLiteral(int index){
        this.literalChildren.remove(index);
        this.precomputed = null;
    }

    public void addLiteral(Literal newLiteral){
        this.literalChildren.add(newLiteral);
        this.precomputed = null;
    }

    public void addLiteral(String name, BitSet matches, boolean negation){
        Literal node = new Literal(name, matches);
        node.setNegation(negation);
        this.literalChildren.add(node);
        this.precomputed = null;
    }

    public void addLiteral(String name, BitSet matches){
        // Negation is false by default
        this.addLiteral(name, matches, false);
        this.precomputed = null;
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

    public StringJoiner getNameStringJoiner(){

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

        return out;
    }

    public String getName(){
        StringJoiner sj = this.getNameStringJoiner();
        String outputString = sj.toString();
        if(super.negation){
            outputString = Symbols.logic_not + outputString;
        }
        return  Symbols.compound_expression_wrapper_open + outputString + Symbols.compound_expression_wrapper_close;
    }

    public void computeMatchesLiteral(){
        // Compute the matches for all literals inside the current branch

        BitSet out = super.matches; // Initialize variable (not used)

        boolean first = true;
        for(Literal node: this.literalChildren){
            // If there exists at least one literal, calculate the match

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
            node.computeMatchesLiteral();
        }
    }

    public BitSet getMatchesIgnoreNegation(){

        if(this.literalChildren.isEmpty() && this.connectiveChildren.isEmpty()){
            throw new IllegalStateException("No child node exists under a logical connective node");
        }

        BitSet out = null;

        if(this.literalChildren.isEmpty()){
            // Skip

        }else{
            if(this.precomputed == null) {
                this.computeMatchesLiteral();
            }
            // this.precomputed is no longer null since this.literalChildren is not empty
            out = (BitSet) this.precomputed.clone();
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

        BitSet out = this.getMatchesIgnoreNegation();

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

    public int getNumDescendantNodes(boolean includeSelf){
        return this.getDescendantLiterals(includeSelf).size() + getDescendantConnectives(includeSelf).size();
    }
}
