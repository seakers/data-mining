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
    /**
     *  Existing literal to be combined with the new node to create a new branch
     */
    private Literal literalToBeCombined;

    private boolean addNewNodeToConsequent;

    public IfThenStatementTester(){
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public IfThenStatementTester(List<Formula> originalConditional, List<Formula> originalConsequent){
        this(originalConditional, originalConsequent, new ArrayList<>());
    }

    public IfThenStatementTester(List<Formula> originalConditional, List<Formula> originalConsequent, List<Formula> originalAlternative){
        super(new ArrayList<>(), new ArrayList<>());

        for(Formula node: originalConditional){
            if(node instanceof Connective && !(node instanceof ConnectiveTester)){
                this.addToConditional(new ConnectiveTester((Connective) node));
            }else if(node instanceof IfThenStatement && !(node instanceof IfThenStatementTester)){
                this.addToConditional(new IfThenStatementTester((IfThenStatement) node));
            }else{
                this.addToConditional(node.copy());
            }
        }
        for(Formula node: originalConsequent){
            if(node instanceof Connective && !(node instanceof ConnectiveTester)){
                this.addToConsequent(new ConnectiveTester((Connective) node));
            }else if(node instanceof IfThenStatement && !(node instanceof IfThenStatementTester)){
                this.addToConsequent(new IfThenStatementTester((IfThenStatement) node));
            }else{
                this.addToConsequent(node.copy());
            }
        }
        for(Formula node: originalAlternative){
            if(node instanceof Connective && !(node instanceof ConnectiveTester)){
                this.addToAlternative(new ConnectiveTester((Connective) node));
            }else if(node instanceof IfThenStatement && !(node instanceof IfThenStatementTester)){
                this.addToAlternative(new IfThenStatementTester((IfThenStatement) node));
            }else{
                this.addToAlternative(node.copy());
            }
        }
        this.addNewNode = false;
        this.newNode = null;
        this.literalToBeCombined = null;
        this.addNewNodeToConsequent = true;
    }

    public IfThenStatementTester(IfThenStatement original){
        this(original.getConditional(), original.getConsequent(), original.getAlternative());
        this.setNegation(original.getNegation());
    }

    @Override
    public void addToConditional(Formula node){
        Formula nodeToAdd;
        if(node instanceof Connective && !(node instanceof ConnectiveTester)){
            nodeToAdd = new ConnectiveTester((Connective) node);
        }else if(node instanceof IfThenStatement && !(node instanceof IfThenStatementTester)){
            nodeToAdd = new IfThenStatementTester((IfThenStatement) node);
        }else{
            nodeToAdd = node.copy();
        }
        nodeToAdd.setParent(this);
        this.conditional.add(nodeToAdd);
        this.conditionalModified();
    }

    @Override
    public void addToConsequent(Formula node){
        Formula nodeToAdd;
        if(node instanceof Connective && !(node instanceof ConnectiveTester)){
            nodeToAdd = new ConnectiveTester((Connective) node);
        }else if(node instanceof IfThenStatement && !(node instanceof IfThenStatementTester)){
            nodeToAdd = new IfThenStatementTester((IfThenStatement) node);
        }else{
            nodeToAdd = node.copy();
        }
        nodeToAdd.setParent(this);
        this.consequent.add(nodeToAdd);
        this.consequentModified();
    }

    @Override
    public void addToAlternative(Formula node){
        Formula nodeToAdd;
        if(node instanceof Connective && !(node instanceof ConnectiveTester)){
            nodeToAdd = new ConnectiveTester((Connective) node);
        }else if(node instanceof IfThenStatement && !(node instanceof IfThenStatementTester)){
            nodeToAdd = new IfThenStatementTester((IfThenStatement) node);
        }else{
            nodeToAdd = node.copy();
        }
        nodeToAdd.setParent(this);
        this.alternative.add(nodeToAdd);
        this.alternativeModified();
    }

    /**
     * Sets the current node to add a new literal. (no new branch created)
     */
    public void setAddNewNode(){
        this.addNewNode = true;
        this.newNode = null;
        this.literalToBeCombined = null;
        this.addNewNodeToConsequent = true;
        this.consequentModified();
    }

    public void setAddNewNodeToAlternative(){
        this.addNewNode = true;
        this.newNode = null;
        this.literalToBeCombined = null;
        this.addNewNodeToConsequent = false;
        this.alternativeModified();
    }

    /**
     * Sets the current node to add a new literal. (new branch will be created)
     * Literal literal
     */
    public void setAddNewNode(Literal literal){
        this.addNewNode = true;
        this.newNode = null;
        this.literalToBeCombined = null;

        if(super.isInConsequent(literal) && addNewNodeToConsequent){
            this.literalToBeCombined = literal;
            this.consequentModified();
        }else if(super.isInAlternative(literal) && !addNewNodeToConsequent){
            this.literalToBeCombined = literal;
            this.alternativeModified();
        }else{
            throw new RuntimeException();
        }
    }

    public void setAddNewNodeToAlternative(Literal literal){
        this.addNewNodeToConsequent = false;
        this.setAddNewNode(literal);
    }

    public void cancelAddNode(){
        this.addNewNode = false;
        this.newNode = null;
        this.literalToBeCombined = null;
        this.addNewNodeToConsequent = true;
        this.alternativeModified();
        this.consequentModified();
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
            if(addNewNodeToConsequent){
                this.consequentModified();
            }else{
                this.alternativeModified();
            }

        }else{
            if(addNewNodeToConsequent){
                for(Formula child: this.consequent){
                    if(child instanceof ConnectiveTester){
                        ((ConnectiveTester) child).setNewNode(node);
                    }else if(child instanceof IfThenStatementTester){
                        ((IfThenStatementTester) child).setNewNode(node);
                    }
                }
            }else{
                for(Formula child: this.alternative){
                    if(child instanceof ConnectiveTester){
                        ((ConnectiveTester) child).setNewNode(node);
                    }else if(child instanceof IfThenStatementTester){
                        ((IfThenStatementTester) child).setNewNode(node);
                    }
                }
            }
        }
    }

    public void setNewNode(String name, BitSet matches){
        Literal node = new Literal(name, matches);
        this.setNewNode(node);
    }

    public void finalizeNewNodeAddition(){

        // New literal is added to the current node
        if(this.addNewNode && this.newNode != null){
            if(this.literalToBeCombined != null){ // New branch is created

                if(addNewNodeToConsequent){
                    if(!isInConsequent(this.literalToBeCombined)){
                        throw new RuntimeException();
                    }
                }else{
                    if(!isInAlternative(this.literalToBeCombined)){
                        throw new RuntimeException();
                    }
                }

                // Remove the literal from the list
                this.removeNode(this.literalToBeCombined);

                // Create a new branch
                ConnectiveTester newBranch = new ConnectiveTester(LogicalConnectiveType.OR);
                newBranch.addLiteral(this.literalToBeCombined);
                newBranch.addNode(this.newNode);

                if(addNewNodeToConsequent){
                    this.addToConsequent(newBranch);
                }else{
                    this.addToAlternative(newBranch);
                }

            }else{
                // New literal is added to the current node
                this.addToConsequent(this.newNode);
                this.cancelAddNode();
            }
        }

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
            if(addNewNode && addNewNodeToConsequent && this.literalToBeCombined != null){
                if(node instanceof Literal
                        && node.hashCode() == this.literalToBeCombined.hashCode()
                        && this.newNode != null){

                    // The new literal is combined with an existing literal inside a new branch
                    StringJoiner newBranchName = new StringJoiner(Symbols.logic_or);
                    newBranchName.add(this.literalToBeCombined.getName());
                    newBranchName.add(this.newNode.getName());
                    consequentStringJoiner.add(Symbols.compound_expression_wrapper_open + newBranchName.toString() + Symbols.compound_expression_wrapper_close);
                }else{
                    consequentStringJoiner.add(node.getName());
                }
            }else{
                consequentStringJoiner.add(node.getName());
            }
        }
        if(addNewNode && addNewNodeToConsequent && this.literalToBeCombined == null){
            if(this.newNode != null){
                consequentStringJoiner.add(this.newNode.getName());
            }
        }
        sb.append(Symbols.compound_expression_wrapper_open + consequentStringJoiner.toString() + Symbols.compound_expression_wrapper_close);

        if(!this.alternative.isEmpty() || (!addNewNodeToConsequent && newNode != null)){
            // Alternative part
            sb.append(Symbols.logic_alternative);
            StringJoiner alternativeStringJoiner = new StringJoiner(Symbols.logic_and);
            for(Formula node: this.alternative){
                if(addNewNode && !addNewNodeToConsequent && this.literalToBeCombined != null){
                    if(node instanceof Literal
                            && node.hashCode() == this.literalToBeCombined.hashCode()
                            && this.newNode != null){

                        // The new literal is combined with an existing literal inside a new branch
                        StringJoiner newBranchName = new StringJoiner(Symbols.logic_or);
                        newBranchName.add(this.literalToBeCombined.getName());
                        newBranchName.add(this.newNode.getName());
                        alternativeStringJoiner.add(Symbols.compound_expression_wrapper_open + newBranchName.toString() + Symbols.compound_expression_wrapper_close);
                    }else{
                        alternativeStringJoiner.add(node.getName());
                    }
                }else{
                    alternativeStringJoiner.add(node.getName());
                }
            }

            if(addNewNode && !addNewNodeToConsequent && this.literalToBeCombined == null){
                if(this.newNode != null){
                    alternativeStringJoiner.add(this.newNode.getName());
                }
            }
            sb.append(Symbols.compound_expression_wrapper_open + alternativeStringJoiner.toString() + Symbols.compound_expression_wrapper_close);
        }

        sb.append(Symbols.compound_expression_wrapper_close);
        return sb.toString();
    }

    public BitSet getMatchesOriginalFeature(){
        if(conditional.isEmpty() || consequent.isEmpty()){
            throw new IllegalStateException("Either of the conditional or the consequent cannot be empty");
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
        return out;
    }

    @Override
    public BitSet getMatches(){
        if(conditional.isEmpty() || consequent.isEmpty()){
            throw new IllegalStateException("Either of the conditional or the consequent cannot be empty");
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

                if(addNewNode && addNewNodeToConsequent && this.literalToBeCombined != null){
                    if(node instanceof Literal
                            && node.hashCode() == this.literalToBeCombined.hashCode()
                            && this.newNode != null){
                        // skip this literal in computing the match, as it will later be combined with the newly added literal
                        continue;
                    }
                }
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

        // If the new node is to be added to the current node
        if (this.addNewNode
                && addNewNodeToConsequent
                && this.newNode != null) {

            // Get matches of the new literal
            BitSet newNodeMatches = (BitSet) this.newNode.getMatches().clone();
            if(this.literalToBeCombined != null){
                // The new literal is to be combined with an existing literal
                BitSet existingNodeMatches = this.literalToBeCombined.getMatches();
                newNodeMatches.or(existingNodeMatches);
            }
            if(consequentMatches == null){
                consequentMatches = newNodeMatches;
            }else{
                consequentMatches.and(newNodeMatches);
            }
        }

        BitSet alternativeMatches = null;
        if(this.precomputedMatchesAlternative == null){
            for(Formula node: this.alternative){

                if(addNewNode && !addNewNodeToConsequent && this.literalToBeCombined != null){
                    if(node instanceof Literal
                            && node.hashCode() == this.literalToBeCombined.hashCode()
                            && this.newNode != null){
                        // skip this literal in computing the match, as it will later be combined with the newly added literal
                        continue;
                    }
                }
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

        // If the new node is to be added to the current node
        if (this.addNewNode
                && !addNewNodeToConsequent
                && this.newNode != null) {

            // Get matches of the new literal
            BitSet newNodeMatches = (BitSet) this.newNode.getMatches().clone();
            if(this.literalToBeCombined != null){
                // The new literal is to be combined with an existing literal
                BitSet existingNodeMatches = this.literalToBeCombined.getMatches();
                newNodeMatches.or(existingNodeMatches);
            }
            if(alternativeMatches == null){
                alternativeMatches = newNodeMatches;
            }else{
                alternativeMatches.and(newNodeMatches);
            }
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

        return out;
    }

    @Override
    public Formula copy(){
        IfThenStatementTester copied = new IfThenStatementTester(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
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

        if(this.addNewNode){
            if(this.literalToBeCombined == null){
                copied.setAddNewNode();
            }else{
                copied.setAddNewNode(this.literalToBeCombined.copy());
            }
            copied.setAddNewNode();

            if(this.newNode != null){
                copied.setNewNode(this.newNode.copy());
            }
        }

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
}
