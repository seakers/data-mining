/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.feature;

import java.util.*;
import java.util.function.Function;

import ifeed.feature.logic.*;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFetcher;

import com.bpodgursky.jbool_expressions.rules.RuleSet;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;

import ifeed.expression.Utils;
import ifeed.expression.Symbols;
import org.moeaframework.core.PRNG;

/**
 *
 * @author bang
 */

public class FeatureExpressionHandler {

    private AbstractFeatureFetcher featureFetcher;
    private AbstractFilterFetcher filterFetcher;

    private boolean skipMatchCalculation;
    private HashMap<String, String> literal_featureName2varName;
    private HashMap<String, String> literal_varName2featureName;

    public FeatureExpressionHandler(){
        this.literal_featureName2varName = new HashMap<>();
        this.literal_varName2featureName = new HashMap<>();

        this.featureFetcher = null;
        this.filterFetcher = null;
        this.skipMatchCalculation = true;
    }

    public FeatureExpressionHandler(AbstractFeatureFetcher featureFetcher) {
        this.literal_featureName2varName = new HashMap<>();
        this.literal_varName2featureName = new HashMap<>();

        this.featureFetcher = featureFetcher;
        this.filterFetcher = featureFetcher.getFilterFetcher();
        this.skipMatchCalculation = false;
    }

    public void setSkipMatchCalculation(boolean skipMatchCalculation) {
        this.skipMatchCalculation = skipMatchCalculation;
    }

    public Connective generateFeatureTree(String expression){
        return this.generateFeatureTree(expression, false);
    }

    public Connective generateFeatureTree(String expression, boolean test){

        // Define a temporary node because addSubTree() requires a parent node as an argument
        Connective root;

        if(expression.contains(Symbols.placeholder_marker) || test){
            root  = new ConnectiveTester(LogicalConnectiveType.AND);

        }else{
            root = new Connective(LogicalConnectiveType.AND);

        }

        addSubTree(root, expression);

        while(true){
            // Remove the empty nodes at the uppermost level

            if(root.getConnectiveChildren().size() == 0){
                break;

            }else if(root.getConnectiveChildren().size() == 1 && root.getLiteralChildren().size() == 0){
                // No literal means this is an empty node
                root = root.getConnectiveChildren().get(0);
                root.removeParent();

            }else{ // If there are multiple logic nodes or there are more than 0 feature nodes
                break;
            }
        }

        return root;
    }

    public void addSubTree(Connective parent, String expression){

        Formula node;

        // Remove outer parenthesis
        String e = Utils.remove_outer_parentheses(expression);
        // Copy the expression
        String _e = e;

        if(Utils.getNestedParenLevel(e) == 0){

            // Given expression does not have a nested structure
            if(!e.contains(Symbols.logic_and) && !e.contains(Symbols.logic_or)){

                // There is no logical connective: Single filter expression
                if(e.contains(Symbols.placeholder_marker)){
                    ConnectiveTester tester = (ConnectiveTester) parent;
                    tester.setAddNewNode();

                }else{
                    boolean negation = false;
                    if(e.startsWith(Symbols.logic_not)){
                        e = e.substring(1);
                        negation = true;
                    }

                    BitSet filtered;
                    if(skipMatchCalculation){
                        filtered = new BitSet(0);
                    }else{
                        Feature thisFeature = this.featureFetcher.fetch(e);
                        filtered = thisFeature.getMatches();
                    }
                    parent.addLiteral(e, filtered, negation);
                }
                return;
            }

        }else{
            // Removes the nested structure by replacing the formula inside parentheses with special symbols.
            // The goal here is to figure out what the operations at the outermost level are, so that we can call
            // this function in recursive manner.
            // ( e.g. (a&b&(C||D)) -> (a&b&XXXXXX) )
            _e = Utils.collapseAllParenIntoSymbol(e);
        }

        if(_e.contains("_IF_") && _e.contains("_THEN_")){

            e = e.substring("_IF_".length());

            String conditionalExpression = e.split("_THEN_")[0];
            String consequentExpression = e.split("_THEN_")[1];

            Connective conditionalTempNode = new Connective(LogicalConnectiveType.AND);
            this.addSubTree(conditionalTempNode, conditionalExpression);
            Connective consequentTempNode = new Connective(LogicalConnectiveType.AND);
            this.addSubTree(consequentTempNode, consequentExpression);

            List<Literal> conditionalNodes = conditionalTempNode.getDescendantLiterals();
            List<Literal> consequentNodes = consequentTempNode.getDescendantLiterals();
            node = new IfThenStatement(conditionalNodes, consequentNodes);

        }else{
            LogicalConnectiveType logic;
            String logicString;
            if(_e.contains(Symbols.logic_and)){
                logic = LogicalConnectiveType.AND;
                logicString = Symbols.logic_and;

                if(_e.contains(Symbols.logic_or)){
                    throw new RuntimeException(Symbols.logic_and + " and " + Symbols.logic_or + "cannot both be in the same parenthesis");
                }

            }else{
                logic = LogicalConnectiveType.OR;
                logicString = Symbols.logic_or;
            }// We assume that there cannot be both && and || inside the same parenthesis.

            boolean first = true;
            boolean last = false;

            if(parent instanceof ConnectiveTester){
                node = new ConnectiveTester(logic);

            }else{
                node = new Connective(logic);
            }

            while(!last){

                String e_temp, _e_temp;

                if(first){
                    // The first filter in a series to be applied
                    first = false;

                }else{
                    _e = _e.substring(2);
                    e = e.substring(2);
                }

                if(_e.contains(logicString)){
                    if(logic == LogicalConnectiveType.OR){
                        _e_temp = _e.split(Symbols.logic_or_regex,2)[0];

                    }else{
                        _e_temp = _e.split(logicString,2)[0];
                    }

                    e_temp = e.substring(0,_e_temp.length());

                    _e = _e.substring(_e_temp.length());
                    e = e.substring(_e_temp.length());

                }else{
                    _e_temp = _e;
                    e_temp = e;
                    last = true;
                }
                this.addSubTree((Connective) node, e_temp);
            }
        }

        parent.addNode(node);
    }

    public Connective applyDeMorgansLaw(Connective root){
        // Recursively apply De Morgan's Law to all Connective class nodes
        if(root.getNegation()){
            root.setNegation(false);
            for(Connective branch:root.getConnectiveChildren()){
                branch.applyNegation();
            }
            for(Literal lit:root.getLiteralChildren()){
                lit.applyNegation();
            }
        }
        // Recursively call this function
        for(Connective branch:root.getConnectiveChildren()) {
            this.applyDeMorgansLaw(branch);
        }
        return root;
    }

    public String convertToJBoolExpression(String featureExpression){

        String out = featureExpression;

        literal_featureName2varName = new HashMap();
        literal_varName2featureName = new HashMap();

        // Change ~ to !
        out = out.replaceAll(Symbols.logic_not, "!");
        // Change && to &
        out = out.replaceAll(Symbols.logic_and, " & ");
        // Change || to |
        out = out.replaceAll(Symbols.logic_or_regex," \\| ");

        int varNameIndex = 0;

        while(out.contains(Symbols.individual_expression_wrapper_open)){
            int start = out.indexOf(Symbols.individual_expression_wrapper_open);
            int end = out.indexOf(Symbols.individual_expression_wrapper_close);
            String feature = out.substring(start+1,end);
            String s1 = out.substring(0,start);
            String s2 = out.substring(end+1);

            if(literal_featureName2varName.containsKey(feature)){
                out = s1 + literal_featureName2varName.get(feature) + s2;

            }else{
                String varName = ((char) ('A'+ varNameIndex)) + "";
                varNameIndex++;
                literal_featureName2varName.put(feature, varName);
                literal_varName2featureName.put(varName, feature);

                out = s1 + varName + s2;
            }
        }
        return out;
    }

    public String convertBackFromJBoolExpression(String jBoolExpression){

        String e = jBoolExpression;

        // Remove all white spaces
        e = e.replaceAll("\\s+","");
        // Change ! to ~
        e = e.replaceAll("!", Symbols.logic_not);
        // Change & to &&
        e = e.replaceAll("&", Symbols.logic_and);
        // Change | to ||
        e = e.replaceAll("\\|", Symbols.logic_or);

        ArrayList<String> varNames = new ArrayList<>();

        String out = "";

        for(int i = 0; i < e.length(); i++){

            String temp;
            if(i < e.length() - 1){
                temp = e.substring(i,i+1);
            }else{
                temp = e.substring(i);
            }

            if (literal_varName2featureName.containsKey(temp)) {
                String featureName = literal_varName2featureName.get(temp);
                out = out + Symbols.individual_expression_wrapper_open + featureName + Symbols.individual_expression_wrapper_close;
            }else{
                out = out + temp;
            }
        }

        return out;
    }

    public boolean isDNF(Connective root){

        if(root.getLogic() != LogicalConnectiveType.OR){
            // Check if the root node is disjunction
            return false;
        }

        for(Connective branch: root.getConnectiveChildren()){
            if(branch.getLogic() == LogicalConnectiveType.OR){
                // All sub-nodes should be conjunctions
                return false;
            } else if(!branch.getConnectiveChildren().isEmpty()){
                // AND nodes should not have any child branches
                return false;
            }
        }
        return true;
    }

    public boolean isCNF(Connective root){

        if(root.getLogic() != LogicalConnectiveType.AND){
            // Check if the root node is conjunction
            return false;
        }

        for(Connective branch: root.getConnectiveChildren()){
            if(branch.getLogic() == LogicalConnectiveType.AND){
                // All sub-nodes should be disjunctions
                return false;
            } else if(!branch.getConnectiveChildren().isEmpty()){
                // OR nodes should not have any child branches
                return false;
            }
        }
        return true;
    }

    public String convertToCNF(String expression){
        Connective root = this.generateFeatureTree(expression);
        Connective out = this.convertToCNF(root);
        return out.getName();
    }

    public Connective convertToCNF(Connective root){

        // Push the negation down to the leaves using De Morgan's law and double-negation elimination
        Connective currentRoot = this.convertToNNF(root);

        // Bring disjunctions to the top using the distributive law: 𝑎∧(𝑏∨𝑐)=(𝑎∧𝑏)∨(𝑎∧𝑐)
        while(true){
            Connective out = findLowestLevelLogicalConnectiveNode(currentRoot, LogicalConnectiveType.OR);

            if(out == null){
                break;

            }else{
                applyDistributiveLaw(out);

            }
        }

        return currentRoot;

    }

    public String convertToDNF(String expression){
        Connective root = this.generateFeatureTree(expression);
        Connective out = this.convertToDNF(root);
        return out.getName();
    }

    public Connective convertToDNF(Connective root){

        // Push the negation down to the leaves using De Morgan's law and double-negation elimination
        Connective currentRoot = this.convertToNNF(root);

        // Bring disjunctions to the top using the distributive law: 𝑎∧(𝑏∨𝑐)=(𝑎∧𝑏)∨(𝑎∧𝑐)
        while(true){
            Connective out = findLowestLevelLogicalConnectiveNode(currentRoot, LogicalConnectiveType.AND);

            if(out == null){
                break;

            }else{
                applyDistributiveLaw(out);

            }
        }

        return currentRoot;
    }

    public Connective findLowestLevelLogicalConnectiveNode(Connective root, LogicalConnectiveType logic){

        if(root.getConnectiveChildren().isEmpty()){
            // Has not branches
            return null;

        }else{
            // Has branches
            Connective out = null;
            for(Connective branch: root.getConnectiveChildren()){
                Connective temp = this.findLowestLevelLogicalConnectiveNode(branch, logic);
                if(temp != null){
                    out = temp;
                }
            }

            if(out == null){
                if(root.getLogic() == logic){
                    // Target logical connective node
                    return root;
                }else{
                    // No target logical connective node found at lower level,
                    // and the current node is the opposite logical connective
                    return null;
                }
            }else{
                // There exist the target logical connective node at lower-level
                return out;
            }
        }
    }

    /**
     * Applies the distributive law: 𝑎∧(𝑏∨𝑐)=(𝑎∧𝑏)∨(𝑎∧𝑐)
     * @param root
     * @return
     */
    public void applyDistributiveLaw(Connective root){

        LogicalConnectiveType logic = root.getLogic();

        List<Literal> nodes = root.getLiteralChildren();
        List<Connective> branches = root.getConnectiveChildren();

        // Toggle logical connective
        root.toggleLogic();

        // Remove all child nodes
        root.removeBranches();
        root.removeLiterals();

        if(branches.isEmpty()){
            // Cannot apply distributive law when there is no subtree
            return;

        } else if(branches.size() == 1){

            Connective branch = branches.get(0);
            List<Formula> childNodes = branch.getChildNodes();

            for(int i = 0; i < childNodes.size(); i++){
                Connective newBranch = new Connective(logic);

                // Add all literals
                for(int j = 0; j < nodes.size(); j++){
                    newBranch.addLiteral(nodes.get(j).copy());
                }

                // Add node from subtrees
                newBranch.addNode(childNodes.get(i).copy());
                root.addBranch(newBranch);
            }

        }else{

            List<List<Formula>> childrenOfSubtree = new ArrayList<>();
            List<List<?>> childrenOfSubtreeIndices = new ArrayList<>();

            for(int i = 0; i < branches.size(); i++){
                Connective branch = branches.get(i);
                List<Formula> childNodes = branch.getChildNodes();
                childrenOfSubtree.add(childNodes);
                List<Object> childNodesIndices = new ArrayList<>();
                for(int j = 0; j < childNodes.size(); j++){
                    childNodesIndices.add(j);
                }
                childrenOfSubtreeIndices.add(childNodesIndices);
            }

            // Get Cartesian product of the indices of the child nodes of all branches
            List<List<Object>> cartesianProducts = ifeed.Utils.cartesianProduct(childrenOfSubtreeIndices);
            for(List<Object> cartesianProduct: cartesianProducts){
                Collections.reverse(cartesianProduct);
            }

            for(int i = 0; i < cartesianProducts.size(); i++){
                Connective newBranch = new Connective(logic);

                // Add all literals
                for(int j = 0; j < nodes.size(); j++){
                    newBranch.addLiteral(nodes.get(j).copy());
                }

                // Add nodes from subtrees
                List<Object> indices = cartesianProducts.get(i);
                for(int j = 0; j < indices.size(); j++){
                    Formula node = childrenOfSubtree.get(j).get((Integer) indices.get(j));
                    newBranch.addNode(node.copy());
                }

                root.addBranch(newBranch);
            }
        }
    }

    /**
     * Convert a logical expression into the negative normal form (NNF)
     * @param root
     * @return
     */
    public Connective convertToNNF(Connective root){
        Connective rootCopy = root.copy();
        rootCopy.propagateNegationSign();
        return rootCopy;
    }

    public Connective convertToCNFJBool(Connective root){

        // Convert the original expression to JBool expression
        String jboolExpression = this.convertToJBoolExpression(root.getName());
        Expression<String> parsedExpression = ExprParser.parse(jboolExpression);
        Expression<String> simplifiedExpression = RuleSet.simplify(parsedExpression);

        // Convert to CNF
        Expression<String> posForm = RuleSet.toCNF(simplifiedExpression);

        // Recover the expression
        String recoveredForm = this.convertBackFromJBoolExpression(posForm.toString());
        Connective out = this.generateFeatureTree(recoveredForm);
        return out;
    }

    public Connective convertToDNFJBool(Connective root){

        // Convert the original expression to JBool expression
        String jboolExpression = this.convertToJBoolExpression(root.getName());
        Expression<String> parsedExpression = ExprParser.parse(jboolExpression);
        Expression<String> simplifiedExpression = RuleSet.simplify(parsedExpression);

        // Convert to DNF
        Expression<String> posForm = RuleSet.toDNF(simplifiedExpression);

        // Recover the expression
        String recoveredForm = this.convertBackFromJBoolExpression(posForm.toString());
        Connective out = this.generateFeatureTree(recoveredForm);
        return out;
    }

    public HashMap<Integer, Integer> getPowerSpectrum(Connective CNFFormula){

        HashMap<Integer, Integer> powerSpectrum = new HashMap<>();

        try{
            // Assumes that there is no placeholder for new literal
            powerSpectrum.put(0, CNFFormula.getLiteralChildren().size());

            for(Connective branch: CNFFormula.getConnectiveChildren()){

                int numElements = branch.getLiteralChildren().size();
                int degree = numElements - 1;

                if(branch.getConnectiveChildren().size() > 0 || degree < 1){
                    System.out.println(branch.getConnectiveChildren().size());
                    System.out.println(degree);
                    throw new Exception("Exception in getting the power spectrum: the input to getPowerSpectrum() has to be in Conjunctive Normal Form (CNF).");
                }

                if(powerSpectrum.containsKey(degree)){
                    int cnt = powerSpectrum.get(degree);
                    powerSpectrum.put(degree, ++cnt);
                }else{
                    powerSpectrum.put(degree, 1);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return powerSpectrum;
    }

    public double computeAlgebraicComplexity(HashMap<Integer, Integer> powerSpectrum){

        int maxDegree = this.literal_varName2featureName.size();

        double sum = 0;
        double[] weights = new double[maxDegree];

        String weightingScheme = "k+1";

        if(weightingScheme.equalsIgnoreCase("k+1")){
            for(int i = 0; i < maxDegree; i++){
                weights[i] = i+1;
            }
        }else if(weightingScheme.equalsIgnoreCase("k")){
            for(int i = 0; i < maxDegree; i++){
                weights[i] = i;
            }
        }else{
            // The sum of weights = 0, and the sum of absolute values of weights = 1
            double mean = (double) (maxDegree - 1) / 2;

            for(int i = 0; i < maxDegree; i++){
                weights[i] = i - mean;
                sum += java.lang.Math.abs(weights[i]);
            }

            for(int i = 0; i < maxDegree; i++){
                weights[i] = weights[i] / sum;
            }
        }

        double complexity = 0;
        for(Integer key:powerSpectrum.keySet()){
            complexity += (double) powerSpectrum.get(key) * weights[key];
        }

        return complexity;
    }

    /**
     * Returns the parent node of a given target node
     * @param root Tree to be searched
     * @param target Node whose parent is to be searched for
     * @return
     */
    public List<Connective> findParentNode(Connective root, Formula target){

        List<Connective> out = new ArrayList<>();

        if(target instanceof Connective){
            for(Connective branch:root.getConnectiveChildren()){
                if(featureTreeEquals((Connective)target, branch)){
                    out.add(root);
                }
            }

        }else{
            for(Literal literal:root.getLiteralChildren()){
                if(literalEquals((Literal)target, literal)){
                    out.add(root);
                }
            }
        }

        for(Connective branch: root.getConnectiveChildren()){
            List<Connective> temp = this.findParentNode(branch, target);
            if(!temp.isEmpty()){
                out.addAll(temp);
            }
        }
        return out;
    }

    /**
     * Finds a node that matches the target Formula from a feature tree
     * @param root
     * @param target
     * @return
     */
    public List<Formula> findMatchingNodes(Connective root, Formula target){

        List<Formula> out = new ArrayList<>();

        if(target instanceof Connective){

            if(featureTreeEquals((Connective)target, root)){
                out.add(root);
            }

            for(Connective branch:root.getConnectiveChildren()){
                if(featureTreeEquals((Connective)target, branch)){
                    out.add(branch);
                    break;
                }
            }

        }else if(target instanceof Literal){
            for(Literal literal:root.getLiteralChildren()){
                if(literalEquals((Literal)target, literal)){
                    out.add(literal);
                    break;
                }
            }
        }else{
            throw new IllegalStateException("Unexpected type: " + target.getClass().toString());
        }

        for(Connective branch: root.getConnectiveChildren()){
            List<Formula> temp = this.findMatchingNodes(branch, target);
            if(!temp.isEmpty()){
                out.addAll(temp);
            }
        }
        return out;
    }

    public Formula selectRandomNode(Connective root, Class type){
        if(type == Connective.class){
            List<Connective> candidates = root.getDescendantConnectives(true);
            int randInt = PRNG.nextInt(candidates.size());
            return candidates.get(randInt);

        }else if(type == Literal.class){
            List<Literal> candidates = root.getDescendantLiterals(true);
            int randInt = PRNG.nextInt(candidates.size());
            return candidates.get(randInt);

        }else if(type == IfThenStatement.class){
            List<IfThenStatement> candidates = root.getDescendantIfThenStatements();
            int randInt = PRNG.nextInt(candidates.size());
            return candidates.get(randInt);

        }else{
            List<Formula> candidates = root.getDescendantNodes(true);
            int randInt = PRNG.nextInt(candidates.size());
            return candidates.get(randInt);
        }
    }

    public boolean NodeEquals(Formula node1, Formula node2){
        if(node1.getClass() != node2.getClass()){
            return false;

        }else{
            if(node1 instanceof Connective){
                return featureTreeEquals(((Connective) node1), ((Connective) node2));

            }else{
                return literalEquals(((Literal) node1), ((Literal) node2));
            }
        }
    }

    /**
     * Compares two Literal instances
     * @param l1
     * @param l2
     * @return
     */
    public boolean literalEquals(Literal l1, Literal l2){

        if(this.filterFetcher == null){
            throw new IllegalStateException("AbstractFilterFetcher needs to be defined to compare features");
        }

        AbstractFilter filter1 = this.filterFetcher.fetch(l1.getName());
        AbstractFilter filter2 = this.filterFetcher.fetch(l2.getName());
        return filter1.hashCode() == filter2.hashCode() && l1.getNegation() == l2.getNegation();
    }

    public boolean featureTreeEquals(Connective f1, Connective f2){

        if(this.filterFetcher == null){
            throw new IllegalStateException("AbstractFilterFetcher needs to be defined to compare features");
        }

        if(f1.getNumDescendantNodes(false) != f2.getNumDescendantNodes(false)){
            // The number of nodes has to match exactly
            return false;
        }

        if(f1.getNegation()){
            f1.propagateNegationSign();
        }

        if(f2.getNegation()){
            f2.propagateNegationSign();
        }

        // Compare all literals: All literals have to be matched 1-to-1
        ArrayList<Integer> f2_literals_found_match = new ArrayList<>();

        for(Literal l1:f1.getLiteralChildren()){

            boolean foundMatch = false;
            for(int i = 0; i < f2.getLiteralChildren().size(); i++){

                if(f2_literals_found_match.contains(i)){
                    continue;
                }

                Literal l2 = f2.getLiteralChildren().get(i);
                if(this.literalEquals(l1, l2)){
                    foundMatch = true;
                    f2_literals_found_match.add(i);
                    break;
                }
            }

            if(!foundMatch){
                return false;
            }
        }

        if(f2_literals_found_match.size() != f2.getLiteralChildren().size()){
            return false;
        }

        // Compare children branches: all branches have to be matches 1-1
        ArrayList<Integer> f2_branches_found_match = new ArrayList<>();
        for(Connective b1:f1.getConnectiveChildren()){

            boolean foundMatch = false;
            for(int i = 0; i < f2.getConnectiveChildren().size(); i++){

                if(f2_branches_found_match.contains(i)){
                    continue;
                }

                Connective b2 = f2.getConnectiveChildren().get(i);
                if(this.featureTreeEquals(b1, b2)){
                    foundMatch = true;
                    f2_branches_found_match.add(i);
                    break;
                }
            }

            if(!foundMatch){
                return false;
            }
        }

        if(f2_branches_found_match.size() != f2.getConnectiveChildren().size()){
            return false;
        }

        return true;
    }

    public static Formula visitNodes(Connective root, Function<Formula, Formula> lambda){

        for(Literal node:root.getLiteralChildren()){
            Formula temp = lambda.apply(node);
            if(temp != null){
                return temp;
            }
        }

        for(Connective branch:root.getConnectiveChildren()){
            Formula temp = lambda.apply(branch);
            if(temp != null){
                return temp;
            }
        }

        return null;
    }

    /**
     * Repairs the feature tree structure by making following changes:
     * 1) Remove child branches with the same logical connective as their parents
     * 2) Remove branches without any child nodes
     * 3) Remove redundant features
     * @param root
     */
    public void repairFeatureTreeStructure(Connective root){

        LogicalConnectiveType thisLogic = root.getLogic();

        while(true){
            List<Connective> branches = root.getConnectiveChildren();

            List<Formula> nodesToAdd = new ArrayList<>();
            List<Connective> branchesToRemove = new ArrayList<>();

            for(Connective branch: branches){
                if(thisLogic == branch.getLogic()){ // 1. Remove child branches with the same logical connective node

                    // Remove this branch
                    if(branch.getNegation()){
                        branch.propagateNegationSign();
                    }
                    for(Formula node: branch.getChildNodes()){
                        nodesToAdd.add(node);
                    }
                    branchesToRemove.add(branch);

                }else if(branch.getChildNodes().isEmpty()){ // 2. Remove branches without any child nodes
                    branchesToRemove.add(branch);
                }
            }

            if(branchesToRemove.isEmpty()){
                break;

            }else{
                for(Connective branch: branchesToRemove){
                    root.removeNode(branch);
                }

                for(Formula node: nodesToAdd){
                    root.addNode(node);
                }
            }
        }

        // 3. Remove redundant features
        List<Literal> literalsToRemove = new ArrayList<>();

        List<Literal> literals = root.getLiteralChildren();
        for(int i = 0; i < literals.size(); i ++){
            for(int j = i + 1; j < literals.size(); j++){
                if(this.literalEquals(literals.get(i), literals.get(j))){
                    literalsToRemove.add(literals.get(j));
                }
            }
        }

        for(Literal literal: literalsToRemove){
            root.removeLiteral(literal);
        }

        for(Connective branch: root.getConnectiveChildren()){
            repairFeatureTreeStructure(branch);
        }
    }

    public void createNewRootNode(Connective root){

        Connective copy = root.copy();

        // Reset the current node
        root.toggleLogic();
        root.removeBranches();
        root.removeLiterals();
        root.setNegation(false);

        // Add the newly generated node to the root node
        root.addBranch(copy);
    }
}
