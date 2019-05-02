package ifeed.feature.logic;

import ifeed.expression.Symbols;

import java.util.*;

public class IfThenStatement extends Formula implements FormulaWithChildren{

    public List<Literal> conditional;
    public List<Literal> consequent;

    protected BitSet matches;
    protected BitSet precomputedMatchesConditional;
    protected BitSet precomputedMatchesConsequent;

    public IfThenStatement(List<Literal> conditional, List<Literal> consequent){
        this.conditional = conditional;
        this.consequent = consequent;
        this.precomputedMatchesConditional = null;
        this.precomputedMatchesConsequent = null;
        this.matches = null;
    }

    public void addToConditional(Literal node){
        node.setParent(this);
        this.conditional.add(node);
        this.conditionalModified();
    }

    public void addToConsequent(Literal node){
        node.setParent(this);
        this.consequent.add(node);
        this.consequentModified();
    }

    public void removeFromConditional(Literal node){
        Formula toBeRemoved =  null;
        for(Formula child: this.conditional){
            if(child.hashCode() == node.hashCode()){
                toBeRemoved = child;
            }
        }
        this.conditional.remove(toBeRemoved);
        node.removeParent();
        this.conditionalModified();
    }

    public void removeFromConsequent(Literal node){
        Formula toBeRemoved =  null;
        for(Formula child: this.consequent){
            if(child.hashCode() == node.hashCode()){
                toBeRemoved = child;
            }
        }
        this.consequent.remove(toBeRemoved);
        node.removeParent();
        this.consequentModified();
    }

    public List<Literal> getConditional(){
        List<Literal> out = new ArrayList<>();
        for(Literal node: this.conditional){
            out.add(node);
        }
        return out;
    }

    public List<Literal> getConsequent(){
        List<Literal> out = new ArrayList<>();
        for(Literal node: this.consequent){
            out.add(node);
        }
        return out;
    }

    public void conditionalModified(){
        this.precomputedMatchesConditional = null;
        this.matches = null;
        if(this.parent != null){
            this.parent.childNodeModified();
        }
    }

    public void consequentModified(){
        this.precomputedMatchesConsequent = null;
        this.matches = null;
        if(this.parent != null){
            this.parent.childNodeModified();
        }
    }

    @Override
    public void childNodeModified(){
        this.conditionalModified();
        this.consequentModified();
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
        sb.append(Symbols.compound_expression_wrapper_open + consequentStringJoiner.toString() + Symbols.compound_expression_wrapper_close);

        sb.append(Symbols.compound_expression_wrapper_close);
        return sb.toString();
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

    public void setPrecomputedMatches(BitSet precomputedMatchesConditional, BitSet precomputedMatchesConsequent){
        this.precomputedMatchesConditional = precomputedMatchesConditional;
        this.precomputedMatchesConsequent = precomputedMatchesConsequent;
        this.matches = null;
    }

    @Override
    public Formula copy(){
        List<Literal> conditional = new ArrayList<>();
        for(Literal node: this.conditional){
            conditional.add(node.copy());
        }
        List<Literal> consequent = new ArrayList<>();
        for(Literal node: this.consequent){
            consequent.add(node.copy());
        }

        IfThenStatement copied = new IfThenStatement(conditional, consequent);
        copied.setNegation(this.negation);
        copied.setPrecomputedMatches((BitSet)this.precomputedMatchesConditional.clone(),
                            (BitSet) this.precomputedMatchesConsequent.clone());
        return copied;
    }

    @Override
    public int hashCode() {
        int hash = 79;
        for(Formula node: this.conditional){
            hash = 31 * hash + Objects.hashCode(node);
        }
        for(Formula node: this.consequent){
            hash = 31 * hash + Objects.hashCode(node);
        }
        return hash;
    }
}
