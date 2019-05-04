package ifeed.feature.logic;

import ifeed.expression.Symbols;

import java.util.*;

public class IfThenStatementTester extends IfThenStatement implements LocalSearchTester{

    /**
     * Flag for adding the new literal under the current node
     */
    private boolean addNewNode;

    /**
     * Placeholder for the new node (could be either a literal or a logical connective)
     */
    private Formula newNode;


    public IfThenStatementTester(List<Literal> conditional, List<Literal> consequent){
        super(conditional, consequent);
        this.addNewNode = false;
        this.newNode = null;
    }

    public IfThenStatementTester(IfThenStatement original){
        super(original.getConditional(), original.getConsequent());
        this.setNegation(original.getNegation());
        this.addNewNode = false;
        this.newNode = null;
    }

    /**
     * Sets the current node to add a new literal. (no new branch created)
     */
    public void setAddNewNode(){
        this.addNewNode = true;
        this.newNode = null;
        this.consequentModified();
    }

    public void cancelAddNode(){
        this.addNewNode = false;
        this.newNode = null;
        this.consequentModified();
    }

    public Formula getNewNode(){ return this.newNode; }

    public boolean getAddNewNode(){
        return this.addNewNode;
    }

    public void setNewNode(Formula node){
        if(this.addNewNode){
            this.newNode = node;
            this.consequentModified();
        }
    }

    public void setNewNode(String name, BitSet matches){
        Literal node = new Literal(name, matches);
        this.setNewNode(node);
    }

    public void finalizeNewNodeAddition(){
        // New literal is added to the current node
        if(this.addNewNode && this.newNode != null){
            this.addToConsequent((Literal)this.newNode);
            this.cancelAddNode();
        }
    }


    // {_IF_()_THEN_()}
    @Override
    public String getName(){
        if(conditional.isEmpty() || consequent.isEmpty()){
            throw new IllegalStateException("Either of the conditional or the consequent cannot be empty");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(Symbols.compound_expression_wrapper_open);

        // Conditional part
        sb.append(Symbols.logic_conditional);
        StringJoiner conditionalStringJoiner = new StringJoiner(Symbols.logic_and);
        for(Literal node: this.conditional){
            conditionalStringJoiner.add(node.getName());
        }
        sb.append(Symbols.compound_expression_wrapper_open + conditionalStringJoiner.toString() + Symbols.compound_expression_wrapper_close);

        // Consequent part
        sb.append(Symbols.logic_consequent);
        StringJoiner consequentStringJoiner = new StringJoiner(Symbols.logic_and);
        for(Literal node: this.consequent){
            consequentStringJoiner.add(node.getName());
        }
        if(this.newNode != null){
            consequentStringJoiner.add(this.newNode.getName());
        }

        sb.append(Symbols.compound_expression_wrapper_open + consequentStringJoiner.toString() + Symbols.compound_expression_wrapper_close);

        sb.append(Symbols.compound_expression_wrapper_close);
        return sb.toString();
    }


    public BitSet getMatchesOriginalFeature(){
        if(conditional.isEmpty() || consequent.isEmpty()){
            throw new IllegalStateException("Either of the conditional or the consequent cannot be empty");
        }

        if(matches != null){
            return matches;
        }

        BitSet conditionalMatches = null;
        if(this.precomputedMatchesConditional == null){
            for(Literal node: this.conditional){
                if(conditionalMatches == null){
                    conditionalMatches = (BitSet) node.getMatches().clone();
                }else{
                    conditionalMatches.and(node.getMatches());
                }
            }
            this.precomputedMatchesConditional = conditionalMatches;
        }else{
            conditionalMatches = this.precomputedMatchesConditional;
        }

        BitSet consequentMatches = null;
        if(this.precomputedMatchesConsequent == null){
            for(Literal node: this.consequent){
                if(consequentMatches == null){
                    consequentMatches = (BitSet) node.getMatches().clone();
                }else{
                    consequentMatches.and(node.getMatches());
                }
            }
            this.precomputedMatchesConsequent = consequentMatches;
        }else{
            consequentMatches = this.precomputedMatchesConsequent;
        }

        BitSet out = new BitSet(conditionalMatches.size());
        for(int i = 0; i < conditionalMatches.size(); i++){
            if(conditionalMatches.get(i)){
                if(consequentMatches.get(i)){
                    out.set(i);
                }
            }else{
                out.set(i);
            }
        }

        this.matches = out;
        return this.matches;
    }

    @Override
    public BitSet getMatches(){
        if(conditional.isEmpty() || consequent.isEmpty()){
            throw new IllegalStateException("Either of the conditional or the consequent cannot be empty");
        }

        if(matches != null){
            return matches;
        }

        BitSet conditionalMatches = null;
        if(this.precomputedMatchesConditional == null){
            for(Literal node: this.conditional){
                if(conditionalMatches == null){
                    conditionalMatches = (BitSet) node.getMatches().clone();
                }else{
                    conditionalMatches.and(node.getMatches());
                }
            }
            this.precomputedMatchesConditional = conditionalMatches;
        }else{
            conditionalMatches = this.precomputedMatchesConditional;
        }

        BitSet consequentMatches = null;
        if(this.precomputedMatchesConsequent == null){
            for(Literal node: this.consequent){
                if(consequentMatches == null){
                    consequentMatches = (BitSet) node.getMatches().clone();
                }else{
                    consequentMatches.and(node.getMatches());
                }
            }

            if(this.addNewNode && this.newNode != null){
                BitSet newNodeMatches = (BitSet) this.newNode.getMatches().clone();
                consequentMatches.and(newNodeMatches);
            }
            this.precomputedMatchesConsequent = consequentMatches;
        }else{
            consequentMatches = this.precomputedMatchesConsequent;
        }

        BitSet out = new BitSet(conditionalMatches.size());
        for(int i = 0; i < conditionalMatches.size(); i++){
            if(conditionalMatches.get(i)){
                if(consequentMatches.get(i)){
                    out.set(i);
                }
            }else{
                out.set(i);
            }
        }

        this.matches = out;
        return this.matches;
    }

    @Override
    public Formula copy(){
        IfThenStatementTester copied = new IfThenStatementTester(new ArrayList<>(), new ArrayList<>());
        for(Literal node: this.conditional){
            copied.addToConditional(node.copy());
        }
        for(Literal node: this.consequent){
            copied.addToConsequent(node.copy());
        }
        copied.setNegation(this.negation);

        if(this.addNewNode){
            copied.setAddNewNode();
        }

        if(this.newNode != null){
            copied.setNewNode(this.newNode);
        }


        BitSet precomputedMatchedConditionalCopy = null;
        if(this.precomputedMatchesConditional != null){
            precomputedMatchedConditionalCopy = (BitSet)this.precomputedMatchesConditional.clone();
        }
        BitSet precomputedMatchedConsequentCopy = null;
        if(this.precomputedMatchesConsequent != null){
            precomputedMatchedConsequentCopy = (BitSet)this.precomputedMatchesConsequent.clone();
        }
        copied.setPrecomputedMatches(precomputedMatchedConditionalCopy, precomputedMatchedConsequentCopy);
        return copied;
    }
}
