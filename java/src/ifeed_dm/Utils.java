/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author bang
 */
public class Utils {
    
    /**
     * Gets the top n features according to the specified metric in descending
     * order. If n is greater than the number of features found by Apriori, all
     * features will be returned.
     *
     * @param n the number of features desired
     * @param metric the metric used to sort the features
     * @return the top n features according to the specified metric in
     * descending order
     */
    
    public static List<Feature> getTopFeatures(List<Feature> features, int n, FeatureMetric metric) {
    
        Collections.sort(features, new FeatureComparator(metric).reversed());
        
        return getTopFeatures(features,n);
    }
    
    
    
    public static List<Feature> getTopFeatures(List<Feature> features, int n) {

        List<Feature> subList;
        
        if (features.size() > n) {
            
            subList = features.subList(0, n);

        }else{
            subList = features;
        }        

        return subList;
    }

    
    public static boolean dominates(Feature f1, Feature f2, List<Comparator> comparators){
    
        boolean at_least_as_good_as = true;
        boolean better_than_in_one = false;
        
        for(int i=0;i<comparators.size();i++){

            if(comparators.get(i).compare(f1, f2) > 0){
                // First better than Second
                better_than_in_one=true;

            }else if(comparators.get(i).compare(f1, f2) < 0){
                // First is worse than Second
                at_least_as_good_as = false;
            }
        }

        return at_least_as_good_as && better_than_in_one; // First dominates Second
    }

    
    public static List<Feature> getFeatureFuzzyParetoFront(List<Feature> population, List<Comparator> comparators, int paretoRank){
        
        List<Feature> fuzzy_pareto_front = new ArrayList<>();
        
        ArrayList<Feature> current_population = new ArrayList<>();

        for (Feature f:population) {
            current_population.add(f);
        }
        
        int iteration=0;
        while(iteration <= paretoRank){
            
            ArrayList<Feature> features_next_iter = new ArrayList<>();
        
            for(int i=0;i<current_population.size();i++){

                boolean dominated = false;
                Feature f1 = current_population.get(i);

                for(int j=0;j<current_population.size();j++){
                    
                    if(i==j) continue;
                    
                    Feature f2 = current_population.get(j);
                    if(dominates(f2,f1,comparators)){ // f1 is dominated
                        dominated=true;
                        break;
                    }
                }

                if(!dominated){
                    fuzzy_pareto_front.add(f1);
                }else{
                    features_next_iter.add(f1);
                }

            }
            
            current_population = features_next_iter;
            
            iteration++;
        }

        return fuzzy_pareto_front;
    }
    
    
    
    
    

    
    public static String remove_outer_parentheses(String expression){
    	
    	if(expression.startsWith("(") && expression.endsWith(")")){
    		int l = expression.length();
    		int level = 0;
    		int paren_end = -1;
    		for(int i=0;i<l;i++){
    			if(expression.charAt(i)=='('){
    				level++;
    			}else if(expression.charAt(i)==')'){
    				level--;
    				if(level==0){
    					paren_end = i;
    					break;
    				}
    			}
    		}
    		if(paren_end==l-1){
    			String new_expression = expression.substring(1, l-1);
    			return remove_outer_parentheses(new_expression);
    		}else{
    			return expression;
    		}
    	}else{
    		return expression;
    	}
    }


    
    /**
     * This function checks if the input string contains a parenthesis
     * 
     * @param inputString
     * @return boolean
     */
    public static boolean checkParen(String inputString){
        return inputString.contains("(");
    }
    
    
    /**
     * This function counts the number of slots in an expression.
     * @param inputString
     * @return 
     */
    public static int getNumOfSlots(String inputString){
        int leng = inputString.length();
        int cnt = 0;
        int level = 0;
        for (int i = 0;i<leng;i++){
            if(inputString.charAt(i) == '('){
                level++;
                if (level == 1) cnt++;
            }
            if(inputString.charAt(i) == ')' ){
                level--;
            }
        }
        return cnt;
    }
    
    
    
    /**
     * This function returns the indices of the parenthesis in a string
     * @param inputString
     * @param n: This function looks for nth appearance of the parenthesis
     * @return int[]: Integer array containing the indices of the parentheses within a string
     */
    public static int[] locateParen(String inputString,int n){ // locate nth parentheses
        
        int level = 0;
        int nth = 0;
        int leng = inputString.length();
        int[] parenLoc = new int[2];

        for (int i = 0; i<leng ;i++){
            char ch = inputString.charAt(i);
            if(ch == '('){
                level++;
                if (level == 1) nth++;
                if ((nth == n) && (level == 1))  parenLoc[0] = i;
            }
            if(ch == ')' ){
                level--;
            }
            if((level == 0) && (nth == n)) {
                parenLoc[1] = i;
                break;
            }
        }
        return parenLoc;
    }
    
    

    /**
     * This function replaces the contents of all parentheses with a character 'X'.
     * This is used to analyze the outermost structure of the given expression (by removing all nested structure).
     * @param inputExpression
     * @return 
     */
    public static String collapseAllParenIntoSymbol(String inputExpression){
        
        // If the given expression doesn't contain any parenthesis, return
        if (checkParen(inputExpression) == false) return inputExpression; 
        
        
        int num = getNumOfSlots(inputExpression);
        String expression = inputExpression;
        
        for (int i = 0;i<num;i++){
            int[] loc = locateParen(expression,i+1);
            String s1 = expression.substring(0, loc[0]+1);
            String s2 = expression.substring(loc[1]);
            String symbol = "";
            for (int j = 0;j< loc[1]-loc[0]-1 ;j++) symbol = symbol.concat("X");
            expression = s1 + symbol + s2;
        }
        return expression;
    }
    
    
    public static ArrayList<Integer> locateNestedParen(String inputString,int focusLevel){ // locate all parentheses at specified level
        
        int level = 0;
        int nth = 0;
        int leng = inputString.length();
        ArrayList<Integer> parenLoc = new ArrayList<>();

        for (int i = 0; i<leng ;i++){
            if(inputString.charAt(i) == '('){
                level++;
                if (level == focusLevel)  parenLoc.add(i);
            }
            if(inputString.charAt(i) == ')' ){
                if (level == focusLevel) parenLoc.add(i);
                level--;
            }
        }
        return parenLoc;
    }
       
    
    public static int getNestedParenLevel(String inputString){
        int leng = inputString.length();
        int cnt = 0;
        int level = 0;
        int maxLevel = 0;
        
        for (int i = 0;i<leng;i++){
            if(inputString.charAt(i) == '('){
                level++;
                if (level > maxLevel) maxLevel = level;
            }
            if(inputString.charAt(i) == ')' ){
                level--;
            }
        }
        return maxLevel;
    }
    
    
    
    
    
    
    public static double[] computeMetrics(BitSet feature, BitSet labels, int numberOfObservations){
        return computeMetrics(feature,labels,numberOfObservations,0.0);
    }
    
    
    /**
     * Computes the metrics of a feature. The feature is represented as the
     * bitset that specifies which base features define it. If the support
     * threshold is not met, then the other metrics are not computed.
     *
     * @param feature the bit set specifying which base features define it
     * @param labels the behavioral/non-behavioral labeling
     * @return a 4-tuple containing support, lift, fcondfidence, and
     * rconfidence. If the support threshold is not met, all metrics will be NaN
     */
    public static double[] computeMetrics(BitSet feature, BitSet labels, int numberOfObservations, double supportThreshold) {
        double[] out = new double[4];

        BitSet copyMatches = (BitSet) feature.clone();
        copyMatches.and(labels);
        double cnt_SF = (double) copyMatches.cardinality();
        out[0] = cnt_SF / (double) numberOfObservations; //support

        // Check if it passes minimum support threshold
        if (out[0] > supportThreshold) {
            //compute the confidence and lift
            double cnt_S = (double) labels.cardinality();
            double cnt_F = (double) feature.cardinality();
            out[1] = (cnt_SF / cnt_S) / (cnt_F / (double) numberOfObservations); //lift
            out[2] = (cnt_SF) / (cnt_F);   // confidence (feature -> selection)
            out[3] = (cnt_SF) / (cnt_S);   // confidence (selection -> feature)
        } else {
            Arrays.fill(out, Double.NaN);
        } 
        return out;
    }

    public static int countMatchesInString(String input, String targetString){
        return input.length() - input.replace(targetString, "").length();
    }
}
