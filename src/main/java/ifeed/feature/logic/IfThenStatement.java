package ifeed.feature.logic;

import ifeed.expression.Symbols;

import java.util.*;

public class IfThenStatement extends Formula implements FormulaWithChildren{

    public List<Formula> conditional;
    public List<Formula> consequent;
    public List<Formula> alternative;

    protected BitSet matches;
    protected BitSet precomputedMatchesConditional;
    protected BitSet precomputedMatchesConsequent;
    protected BitSet precomputedMatchesAlternative;

    public IfThenStatement(List<Formula> conditional, List<Formula> consequent, List<Formula> alternative){
        this.conditional = conditional;
        this.consequent = consequent;
        this.alternative = alternative;
        for(Formula node: this.conditional){
           node.setParent(this);
        }
        for(Formula node: this.consequent){
            node.setParent(this);
        }
        for(Formula node: this.alternative){
            node.setParent(this);
        }
        this.precomputedMatchesConditional = null;
        this.precomputedMatchesConsequent = null;
        this.precomputedMatchesAlternative = null;
        this.matches = null;
    }

    public IfThenStatement(List<Formula> conditional, List<Formula> consequent){
        this(conditional, consequent, new ArrayList<>());
    }

    public IfThenStatement(){
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public boolean isInConditional(Formula node){
        return isInConditional(node, false);
    }

    public boolean isInConditional(Formula node, boolean searchDescendants){
        for(Formula child: this.conditional){
            if(child.hashCode() == node.hashCode()){
                return true;
            }
        }

        if(searchDescendants){
            for(Formula child: this.conditional){
                if(child instanceof FormulaWithChildren){
                    if(((FormulaWithChildren) child).containsNode(node, true)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isInConsequent(Formula node){
        return isInConsequent(node, false);
    }

    public boolean isInConsequent(Formula node, boolean searchDescendants){
        for(Formula child: this.consequent){
            if(child.hashCode() == node.hashCode()){
                return true;
            }
        }

        if(searchDescendants){
            for(Formula child: this.consequent){
                if(child instanceof FormulaWithChildren){
                    if(((FormulaWithChildren) child).containsNode(node, true)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isInAlternative(Formula node){
        return isInAlternative(node, false);
    }

    public boolean isInAlternative(Formula node, boolean searchDescendants){
        for(Formula child: this.alternative){
            if(child.hashCode() == node.hashCode()){
                return true;
            }
        }

        if(searchDescendants){
            for(Formula child: this.alternative){
                if(child instanceof FormulaWithChildren){
                    if(((FormulaWithChildren) child).containsNode(node, true)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean containsNode(Formula node){
        return containsNode(node, false);
    }

    @Override
    public boolean containsNode(Formula node, boolean searchDescendants){
        if(this.isInConditional(node, searchDescendants)){
            return true;
        }else if(this.isInConsequent(node, searchDescendants)){
            return true;
        }else if(this.isInAlternative(node, searchDescendants)){
            return true;
        }
        return false;
    }

    public void addToConditional(Formula node){
        node.setParent(this);
        this.conditional.add(node);
        this.conditionalModified();
    }

    public void addToConsequent(Formula node){
        node.setParent(this);
        this.consequent.add(node);
        this.consequentModified();
    }

    public void addToAlternative(Formula node){
        node.setParent(this);
        this.alternative.add(node);
        this.consequentModified();
    }

    public void addToConditional(String name, BitSet matches){
        Literal node = new Literal(name, matches);
        this.addToConditional(node);
    }

    public void addToConsequent(String name, BitSet matches){
        Literal node = new Literal(name, matches);
        this.addToConsequent(node);
    }

    public void addToAlternative(String name, BitSet matches){
        Literal node = new Literal(name, matches);
        this.addToAlternative(node);
    }

    @Override
    public boolean removeNode(Formula node){
        return removeNode(node, false);
    }

    @Override
    public boolean removeNode(Formula node, boolean searchDescendants){
        if(this.removeFromConditional(node, searchDescendants)){
            return true;
        }else if(this.removeFromConsequent(node, searchDescendants)){
            return true;
        }else if(this.removeFromAlternative(node, searchDescendants)){
            return true;
        }else{
            throw new IllegalStateException();
        }
    }

    public boolean removeFromConditional(Formula node){
        return this.removeFromConditional(node, false);
    }

    public boolean removeFromConditional(Formula node, boolean searchDescendants){
        Formula toBeRemoved =  null;
        for(Formula child: this.conditional){
            if(child.hashCode() == node.hashCode()){
                toBeRemoved = child;
            }
        }

        if(toBeRemoved != null){
            this.conditional.remove(toBeRemoved);
            toBeRemoved.removeParent();
            this.conditionalModified();
            return true;
        }

        if(searchDescendants){
            for(Formula child: this.conditional){
                if(child instanceof FormulaWithChildren){
                    if(((FormulaWithChildren)child).removeNode(node, true)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean removeFromConsequent(Formula node){
        return this.removeFromConsequent(node, false);
    }

    public boolean removeFromConsequent(Formula node, boolean searchDescendants){
        Formula toBeRemoved =  null;
        for(Formula child: this.consequent){
            if(child.hashCode() == node.hashCode()){
                toBeRemoved = child;
            }
        }

        if(toBeRemoved != null){
            this.consequent.remove(toBeRemoved);
            toBeRemoved.removeParent();
            this.consequentModified();
            return true;
        }

        if(searchDescendants){
            for(Formula child: this.consequent){
                if(child instanceof FormulaWithChildren){
                    if(((FormulaWithChildren)child).removeNode(node, true)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean removeFromAlternative(Formula node){
        return this.removeFromAlternative(node, false);
    }

    public boolean removeFromAlternative(Formula node, boolean searchDescendants){
        Formula toBeRemoved =  null;
        for(Formula child: this.alternative){
            if(child.hashCode() == node.hashCode()){
                toBeRemoved = child;
            }
        }

        if(toBeRemoved != null){
            this.alternative.remove(toBeRemoved);
            toBeRemoved.removeParent();
            this.alternativeModified();
            return true;
        }

        if(searchDescendants){
            for(Formula child: this.alternative){
                if(child instanceof FormulaWithChildren){
                    if(((FormulaWithChildren)child).removeNode(node, true)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<Formula> getConditional(){
        List<Formula> out = new ArrayList<>();
        for(Formula node: this.conditional){
            out.add(node);
        }
        return out;
    }

    public List<Formula> getConsequent(){
        List<Formula> out = new ArrayList<>();
        for(Formula node: this.consequent){
            out.add(node);
        }
        return out;
    }

    public List<Formula> getAlternative(){
        List<Formula> out = new ArrayList<>();
        for(Formula node: this.alternative){
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

    public void alternativeModified(){
        this.precomputedMatchesAlternative = null;
        this.matches = null;
        if(this.parent != null){
            this.parent.childNodeModified();
        }
    }

    @Override
    public void childNodeModified(){
        this.conditionalModified();
        this.consequentModified();
        this.alternativeModified();
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
        for(Formula node: this.conditional){
            conditionalStringJoiner.add(node.getName());
        }
        sb.append(Symbols.compound_expression_wrapper_open + conditionalStringJoiner.toString() + Symbols.compound_expression_wrapper_close);

        // Consequent part
        sb.append(Symbols.logic_consequent);
        StringJoiner consequentStringJoiner = new StringJoiner(Symbols.logic_and);
        for(Formula node: this.consequent){
            consequentStringJoiner.add(node.getName());
        }
        sb.append(Symbols.compound_expression_wrapper_open + consequentStringJoiner.toString() + Symbols.compound_expression_wrapper_close);

        // Alternative part
        if(!this.alternative.isEmpty()){
            sb.append(Symbols.logic_alternative);
            StringJoiner alternativeStringJoiner = new StringJoiner(Symbols.logic_and);
            for(Formula node: this.alternative){
                alternativeStringJoiner.add(node.getName());
            }
            sb.append(Symbols.compound_expression_wrapper_open + alternativeStringJoiner.toString() + Symbols.compound_expression_wrapper_close);
        }

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
            for(Formula node: this.conditional){
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
            for(Formula node: this.consequent){
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

        BitSet alternativeMatches = null;
        if(this.precomputedMatchesAlternative == null){
            for(Formula node: this.alternative){
                if(alternativeMatches == null){
                    alternativeMatches = (BitSet) node.getMatches().clone();
                }else{
                    alternativeMatches.and(node.getMatches());
                }
            }
            this.precomputedMatchesAlternative = alternativeMatches;
        }else{
            alternativeMatches = this.precomputedMatchesAlternative;
        }

        BitSet out = new BitSet(conditionalMatches.size());
        if(alternativeMatches == null){
            for(int i = 0; i < conditionalMatches.size(); i++){
                if(conditionalMatches.get(i)){
                    if(consequentMatches.get(i)){
                        out.set(i);
                    }
                }else{
                    out.set(i);
                }
            }
        }else{
            for(int i = 0; i < conditionalMatches.size(); i++){
                if(conditionalMatches.get(i)){
                    if(consequentMatches.get(i)){
                        out.set(i);
                    }
                }else{
                    if(alternativeMatches.get(i)){
                        out.set(i);
                    }
                }
            }
        }
        this.matches = out;
        return this.matches;
    }

    public void setPrecomputedMatches(BitSet precomputedMatchesConditional,
                                      BitSet precomputedMatchesConsequent,
                                      BitSet precomputedMatchesAlternative){

        this.precomputedMatchesConditional = precomputedMatchesConditional;
        this.precomputedMatchesConsequent = precomputedMatchesConsequent;
        this.precomputedMatchesAlternative = precomputedMatchesAlternative;
        this.matches = null;
    }

    @Override
    public Formula copy(){
        IfThenStatement copied = new IfThenStatement(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        for(Formula node: this.conditional){
            copied.addToConditional(node.copy());
        }
        for(Formula node: this.consequent){
            copied.addToConsequent(node.copy());
        }
        for(Formula node: this.alternative){
            copied.addToAlternative(node.copy());
        }
        copied.setNegation(this.negation);

        BitSet precomputedMatchedConditionalCopy = null;
        if(this.precomputedMatchesConditional != null){
            precomputedMatchedConditionalCopy = (BitSet)this.precomputedMatchesConditional.clone();
        }
        BitSet precomputedMatchedConsequentCopy = null;
        if(this.precomputedMatchesConsequent != null){
            precomputedMatchedConsequentCopy = (BitSet)this.precomputedMatchesConsequent.clone();
        }
        BitSet precomputedMatchedAlternativeCopy = null;
        if(this.precomputedMatchesAlternative != null){
            precomputedMatchedAlternativeCopy = (BitSet)this.precomputedMatchesAlternative.clone();
        }

        copied.setPrecomputedMatches(precomputedMatchedConditionalCopy, precomputedMatchedConsequentCopy, precomputedMatchedAlternativeCopy);
        return copied;
    }


    @Override
    public List<Connective> getDescendantConnectives(){
        List<Connective> out = new ArrayList<>();
        for(Formula node: this.conditional){
            if(node instanceof FormulaWithChildren){
                out.addAll(((FormulaWithChildren) node).getDescendantConnectives());
            }
        }
        for(Formula node: this.consequent){
            if(node instanceof FormulaWithChildren){
                out.addAll(((FormulaWithChildren) node).getDescendantConnectives());
            }
        }
        for(Formula node: this.alternative){
            if(node instanceof FormulaWithChildren){
                out.addAll(((FormulaWithChildren) node).getDescendantConnectives());
            }
        }
        return out;
    }

    @Override
    public List<IfThenStatement> getDescendantIfThenStatements(){
        List<IfThenStatement> out = new ArrayList<>();
        out.add(this);
        for(Formula node: this.conditional){
            if(node instanceof FormulaWithChildren){
                out.addAll(((FormulaWithChildren) node).getDescendantIfThenStatements());
            }
        }
        for(Formula node: this.consequent){
            if(node instanceof FormulaWithChildren){
                out.addAll(((FormulaWithChildren) node).getDescendantIfThenStatements());
            }
        }
        for(Formula node: this.alternative){
            if(node instanceof FormulaWithChildren){
                out.addAll(((FormulaWithChildren) node).getDescendantIfThenStatements());
            }
        }
        return out;
    }

    @Override
    public List<Literal> getDescendantLiterals(){
        List<Literal> out = new ArrayList<>();
        for(Formula node: this.conditional){
            if(node instanceof Literal){
                out.add((Literal) node);
            }else if(node instanceof FormulaWithChildren){
                out.addAll(((FormulaWithChildren) node).getDescendantLiterals());
            }
        }
        for(Formula node: this.consequent){
            if(node instanceof Literal){
                out.add((Literal) node);
            }else if(node instanceof FormulaWithChildren){
                out.addAll(((FormulaWithChildren) node).getDescendantLiterals());
            }
        }
        for(Formula node: this.alternative){
            if(node instanceof Literal){
                out.add((Literal) node);
            }else if(node instanceof FormulaWithChildren){
                out.addAll(((FormulaWithChildren) node).getDescendantLiterals());
            }
        }
        return out;
    }

    @Override
    public List<Formula> getDescendantNodes(){
        List<Formula> out = new ArrayList<>();
        out.addAll(getDescendantConnectives());
        out.addAll(getDescendantIfThenStatements());
        out.addAll(getDescendantLiterals());
        return out;
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
        for(Formula node: this.alternative){
            hash = 31 * hash + Objects.hashCode(node);
        }
        return hash;
    }
}
