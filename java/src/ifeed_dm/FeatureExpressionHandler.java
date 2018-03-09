/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;

import java.util.*;


import ifeed_dm.logic.Connective;
import ifeed_dm.logic.Literal;

import com.bpodgursky.jbool_expressions.rules.RuleSet;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;

/**
 *
 * @author bang
 */


public class FeatureExpressionHandler {

    private FeatureFetcher fetcher;
    private boolean IgnoreMatchCalculation;
    private HashMap<String, String> literal_featureName2varName;
    private HashMap<String, String> literal_varName2featureName;

    public FeatureExpressionHandler(){
        this.literal_featureName2varName = new HashMap<>();
        this.literal_varName2featureName = new HashMap<>();
        this.fetcher = null;
        this.IgnoreMatchCalculation = true;
    }

    public FeatureExpressionHandler(FeatureFetcher fetcher) {
        this.literal_featureName2varName = new HashMap<>();
        this.literal_varName2featureName = new HashMap<>();
        this.fetcher = fetcher;
        if(this.fetcher.emptyArchitectures()){
            this.IgnoreMatchCalculation = true;
        }
    }

    public void setIgnoreMatchCalculation(boolean ignoreMatchCalculation) {
        IgnoreMatchCalculation = ignoreMatchCalculation;
    }

    public Connective generateFeatureTree(String expression){

        // Define a temporary node because addSubTree() requires a parent node as an argument
        Connective root = new Connective(LogicOperator.AND);

        addSubTree(root, expression);

        while(true){

            if(root.getConnectiveChildren().size() == 0){
                break;

            }else if(root.getConnectiveChildren().size() == 1 && root.getLiteralChildren().size() == 0){
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
            if(!e.contains("&&")&&!e.contains("||")){
                // There is no logical connective: Single filter expression
                if(e.contains("PLACEHOLDER")){
                    parent.setAddNewLiteral();
                }else{
                    boolean negation = false;
                    if(e.startsWith("~")){
                        e = e.substring(1);
                        negation = true;
                    }

                    BitSet filtered;
                    if(IgnoreMatchCalculation){
                        filtered = new BitSet(0);
                    }else{
                        Feature thisFeature = this.fetcher.fetch(e);
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
        if(_e.contains("&&")){
            logic=LogicOperator.AND;
            logicString="&&";

            if(_e.contains("||")){
                System.out.println("&& and || cannot both be in the same parenthesis");
                //throw new Exception("");
            }

        }else{
            logic=LogicOperator.OR;
            logicString="||";
        }// We assume that there cannot be both && and || inside the same parenthesis.

        boolean first = true;
        boolean last = false;
        node = new Connective(logic);

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
                    _e_temp = _e.split("\\|\\|",2)[0];
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
        out = out.replaceAll("~", "!");
        // Change && to &
        out = out.replaceAll("&&", " & ");
        // Change || to |
        out = out.replaceAll("\\|\\|"," \\| ");

        int varNameIndex = 0;

        while(out.contains("{")){
            int start = out.indexOf("{");
            int end = out.indexOf("}");
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
        e = e.replaceAll("!", "~");
        // Change & to &&
        e = e.replaceAll("&", "&&");
        // Change | to ||
        e = e.replaceAll("\\|","\\|\\|");

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
                out = out + "{" + featureName + "}";
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

}
