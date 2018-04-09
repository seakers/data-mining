/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.feature;

import java.util.*;

import ifeed.feature.logic.*;
import ifeed.filter.Filter;
import ifeed.filter.FilterFetcher;

import com.bpodgursky.jbool_expressions.rules.RuleSet;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;

import ifeed.expression.Utils;
import ifeed.expression.Symbols;

/**
 *
 * @author bang
 */

public class FeatureExpressionHandler {

    private FeatureFetcher featureFetcher;
    private FilterFetcher filterFetcher;

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

    public FeatureExpressionHandler(FeatureFetcher featureFetcher) {
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
            root  = new ConnectiveTester(LogicOperator.AND);

        }else{
            root = new Connective(LogicOperator.AND);

        }

        addSubTree(root, expression);

        while(true){
            // Remove the empty nodes at the outermost level

            if(root.getConnectiveChildren().size() == 0){
                break;

            }else if(root.getConnectiveChildren().size() == 1 && root.getLiteralChildren().size() == 0){
                // No literal means this is an empty node
                root = root.getConnectiveChildren().get(0);

            }else{ // If there are multiple logic nodes or there are more than 0 feature nodes
                break;
            }
        }

        return root;
    }

    public void addSubTree(Connective parent, String expression){

        Connective node;

        // Remove outer parenthesis
        String e = Utils.remove_outer_parentheses(expression);
        // Copy the expression
        String _e = e;

        if(Utils.getNestedParenLevel(e)==0){

            // Given expression does not have a nested structure
            if(!e.contains(Symbols.logic_and) && !e.contains(Symbols.logic_or)){
                // There is no logical connective: Single filter expression
                if(e.contains(Symbols.placeholder_marker)){
                    ConnectiveTester tester = (ConnectiveTester) parent;
                    tester.setAddNewLiteral();

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

            }else{
                // Do nothing
            }

        }else{

            // Removes the nested structure ( e.g. (a&b&(C||D)) -> (a&b&XXXXXX) )
            _e = Utils.collapseAllParenIntoSymbol(e);
        }

        LogicOperator logic;
        String logicString;
        if(_e.contains(Symbols.logic_and)){
            logic = LogicOperator.AND;
            logicString = Symbols.logic_and;

            if(_e.contains(Symbols.logic_or)){
                throw new RuntimeException(Symbols.logic_and + " and " + Symbols.logic_or + "cannot both be in the same parenthesis");
            }

        }else{
            logic = LogicOperator.OR;
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
                if(logic==LogicOperator.OR){
                    _e_temp = _e.split(Symbols.logic_or_regex,2)[0];

                }else{
                    _e_temp = _e.split(logicString,2)[0];
                }

                e_temp = e.substring(0,_e_temp.length());

                _e = _e.substring(_e_temp.length());
                e = e.substring(_e_temp.length());

            }else{
                _e_temp=_e;
                e_temp=e;
                last=true;
            }
            this.addSubTree(node, e_temp);
        }

        parent.addChild(node);
    }

    public Connective applyDeMorgansLaw(Connective root){
        // Recursively apply De Morgan's Law to all Connective class nodes
        if(root.getNegation()){
            root.setNegation(false);
            for(Connective branch:root.getConnectiveChildren()){
                branch.toggleNegation();
            }
            for(Literal lit:root.getLiteralChildren()){
                lit.toggleNegation();
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

    public String convertToCNF(String expression){
        Connective root = this.generateFeatureTree(expression);
        Connective out = this.convertToCNF(root);
        return out.getName();
    }

    public Connective convertToCNF(Connective root){

        //System.out.println(root.getName());

        String jboolExpression = this.convertToJBoolExpression(root.getName());

        //System.out.println(jboolExpression);

        Expression<String> parsedExpression = ExprParser.parse(jboolExpression);

        Expression<String> simplifiedExpression = RuleSet.simplify(parsedExpression);

        Expression<String> posForm = RuleSet.toCNF(simplifiedExpression);

        //System.out.println(posForm);

        String recoveredForm = this.convertBackFromJBoolExpression(posForm.toString());

        //System.out.println(recoveredForm);

        Connective out = this.generateFeatureTree(recoveredForm);

        return out;
    }

    public String convertToDNF(String expression){
        Connective root = this.generateFeatureTree(expression);
        Connective out = this.convertToDNF(root);
        return out.getName();
    }

    public Connective convertToDNF(Connective root){

        //System.out.println(root.getName());

        String jboolExpression = this.convertToJBoolExpression(root.getName());

        Expression<String> parsedExpression = ExprParser.parse(jboolExpression);

        Expression<String> simplifiedExpression = RuleSet.simplify(parsedExpression);

        Expression<String> posForm = RuleSet.toDNF(simplifiedExpression);

        //System.out.println(posForm);

        String recoveredForm = this.convertBackFromJBoolExpression(posForm.toString());

        //System.out.println(recoveredForm);

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
    public Connective findParentNode(Connective root, Formula target){

        if(target instanceof Connective){
            for(Connective branch:root.getConnectiveChildren()){
                if(branch == target){
                    return root;
                }
            }

        }else{
            for(Literal literal:root.getLiteralChildren()){
                if(literal == target){
                    return root;
                }
            }
        }

        for(Connective branch: root.getConnectiveChildren()){
            Connective temp = this.findParentNode(branch, target);
            if(temp != null){
                return temp;
            }
        }
        return null;
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

    public boolean literalEquals(Literal l1, Literal l2){

        if(this.filterFetcher == null){
            throw new IllegalStateException("Feature featureFetcher needs to be defined to compare features");
        }

        Filter filter1 = this.filterFetcher.fetch(l1.getName());
        Filter filter2 = this.filterFetcher.fetch(l2.getName());
        return filter1.equals(filter2) && l1.getNegation() == l2.getNegation();
    }

    public boolean featureTreeEquals(Connective f1, Connective f2){
        // Ignores placeholder

        if(this.filterFetcher == null){
            throw new IllegalStateException("Feature featureFetcher needs to be defined to compare features");
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

        // Compare all literals
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

        // All literals have been matched 1-to-1

        // Compare children branches
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

    /**
     * Repairs the feature tree structure by making following changes:
     * 1) Remove child branches with the same logical connective as their parents
     * 2) Remove redundant features
     * @param root
     */
    public void repairFeatureTreeStructure(Connective root){

        // 1. Remove child branches with the same logical connective node
        LogicOperator thisLogic = root.getLogic();
        List<Literal> literals = root.getLiteralChildren();
        List<Connective> branches = root.getConnectiveChildren();

        for(Connective branch:branches){
            if(thisLogic == branch.getLogic()){

                // Remove this branch
                if(branch.getNegation()){
                    branch.propagateNegationSign();
                }
                for(Literal literal: branch.getLiteralChildren()){
                    literals.add(literal);
                }
                for(Connective subBranch: branch.getConnectiveChildren()){
                    branches.add(subBranch);
                }
                branches.remove(branch);
            }
        }

        // 2. Remove redundant features
        for(int i = 0; i < literals.size(); i ++){
            for(int j = i + 1; j < literals.size(); j++){
                if(this.literalEquals(literals.get(i), literals.get(j))){
                    literals.remove(literals.get(j));
                }
            }
        }
    }
}
