/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import javaInterface.Architecture;


/**
 *
 * @author bang
 */
public class FilterExpressionHandler {

    private int norb;
    private int ninstr;
    private JessExpressionAnalyzer jea;
    private ArrayList<Architecture> archs;
    private ArrayList<Integer> behavioral;
    private ArrayList<Integer> non_behavioral;
    private ArrayList<Integer> population;
    
    private int[] satList;
    
            
    public FilterExpressionHandler(){
        // The number of instruments and the number of orbits hard-coded: Needs improvement!
        norb = 5;
        ninstr = 12;
        jea = new JessExpressionAnalyzer();
    }

    public void setArchs(ArrayList<Architecture> inputArchs, ArrayList<Integer> b, ArrayList<Integer> nb, ArrayList<Integer> pop){
    	this.behavioral = b;
    	this.non_behavioral = nb;
    	this.population=pop;
    	archs = inputArchs;
    }
    
    
    
    
    public double[] processSingleFilterExpression_computeMetrics(String inputExpression){
        // Examples of feature expressions 
        // Preset filter: {presetName[orbits;instruments;numbers]}   
        
    	satList = new int[population.size()];
    	ArrayList<Integer> matchedArchIDs = new ArrayList<>();
    	
    	
        String exp;
        if(inputExpression.startsWith("{") && inputExpression.endsWith("}")){
            exp = inputExpression.substring(1,inputExpression.length()-1);
        }else{
            exp = inputExpression;
        }
        
    
        String presetName = exp.split("\\[")[0];
        String arguments = exp.substring(0,exp.length()-1).split("\\[")[1];
        
        String[] argSplit = arguments.split(";");
        String[] orbits = new String[1];
        String[] instruments = new String[1];
        String[] numbers = new String[1];
        
        if(argSplit.length>0){
            orbits = argSplit[0].split(",");
        }
        if(argSplit.length>1){
            instruments = argSplit[1].split(",");
        }
        if(argSplit.length>2){
            numbers = argSplit[2].split(",");
        }
        
    	double[] metrics = {0,0,0,0};
        double cnt_all= (double) non_behavioral.size() + behavioral.size();
        double cnt_F=0.0;
        double cnt_S= (double) behavioral.size();
        double cnt_SF=0.0;        

        for(Architecture a:archs){
            int ArchID = a.id;
            int[][] mat = booleanString2IntArray(a.bitString);
            if(comparePresetFilter(mat, presetName,orbits,instruments,numbers)){
                cnt_F++;
                if(behavioral.contains(ArchID)) cnt_SF++;
                matchedArchIDs.add(ArchID);
            }
        }
        
        satList = satisfactionArray(matchedArchIDs, this.population);
        
        
//        double cnt_NS = cnt_all-cnt_S;
//        double cnt_NF = cnt_all-cnt_F;
//        double cnt_S_NF = cnt_S-cnt_SF;
//        double cnt_F_NS = cnt_F-cnt_SF;
        
        double support = cnt_SF/cnt_all;
//        double support_F = cnt_F/cnt_all;
//        double support_S = cnt_S/cnt_all;
        
        double lift=0;
        double conf_given_F=0;
        if(cnt_F!=0){
            lift = (cnt_SF/cnt_S) / (cnt_F/cnt_all);
            conf_given_F = (cnt_SF)/(cnt_F);   // confidence (feature -> selection)
        }
        double conf_given_S = (cnt_SF)/(cnt_S);   // confidence (selection -> feature)

    	metrics[0] = support;
    	metrics[1] = lift;
    	metrics[2] = conf_given_F;
    	metrics[3] = conf_given_S;
    	
    	return metrics;        

    }    
        
    
    

    
    
    public ArrayList<Integer> processSingleFilterExpression(String inputExpression){
        // Examples of feature expressions 
        // Preset filter: {presetName[orbits;instruments;numbers]}   
        
        ArrayList<Integer> matchedArchIDs = new ArrayList<>();
        String exp;
        if(inputExpression.startsWith("{") && inputExpression.endsWith("}")){
            exp = inputExpression.substring(1,inputExpression.length()-1);
        }else{
            exp = inputExpression;
        }
        
        String presetName = exp.split("\\[")[0];
        String arguments = exp.substring(0,exp.length()-1).split("\\[")[1];
        
        String[] argSplit = arguments.split(";");
        String[] orbits = new String[1];
        String[] instruments = new String[1];
        String[] numbers = new String[1];
        
        if(argSplit.length>0){
            orbits = argSplit[0].split(",");
        }
        if(argSplit.length>1){
            instruments = argSplit[1].split(",");
        }
        if(argSplit.length>2){
            numbers = argSplit[2].split(",");
        }

        for(Architecture a:archs){
            int ArchID = a.id;
            int[][] mat = booleanString2IntArray(a.bitString);
            if(comparePresetFilter(mat, presetName,orbits,instruments,numbers)){
                matchedArchIDs.add(ArchID);
            }
        }
        
        return matchedArchIDs;
    }    
    
    

    
    
    public boolean comparePresetFilter(int[][] mat, String type, String[] orbits, String[] instruments, String[] numbers){
        
        if(type.equalsIgnoreCase("present")){
            int instrument = Integer.parseInt(instruments[0]);
            for (int i=0;i<norb;i++) {
                if (mat[i][instrument]==1) return true;
            }
            return false;
        } else if(type.equalsIgnoreCase("absent")){
            
            int instrument = Integer.parseInt(instruments[0]);
            for (int i = 0; i < norb; ++i) {
                if (mat[i][instrument]==1) return false;
            }
            return true;  
        } else if(type.equalsIgnoreCase("inOrbit")){
            int orbit = Integer.parseInt(orbits[0]);
            boolean together = true;
            for(int j=0;j<instruments.length;j++){
                int instrument = Integer.parseInt(instruments[j]);
                if(mat[orbit][instrument]==0){together=false;}
            }
            if(together){return true;}            
            return false;
        } else if(type.equalsIgnoreCase("notInOrbit")){
            
            int orbit = Integer.parseInt(orbits[0]);
            int instrument = Integer.parseInt(instruments[0]);
            return mat[orbit][instrument] == 0;
            
        } else if(type.equalsIgnoreCase("together")){
            
            for(int i=0;i<norb;i++){
                boolean together = true;
                for(int j=0;j<instruments.length;j++){
                    int instrument = Integer.parseInt(instruments[j]);
                    if(mat[i][instrument]==0){together=false;}
                }
                if(together){return true;}
            }
            return false;
            
        } else if(type.equalsIgnoreCase("separate")){
            
            for(int i=0;i<norb;i++){
                boolean together = true;
                for(int j=0;j<instruments.length;j++){
                    int instrument = Integer.parseInt(instruments[j]);
                    if(mat[i][instrument]==0){together=false;}
                }
                if(together){return false;}
            }
            return true;
            
        } else if(type.equalsIgnoreCase("emptyOrbit")){
            
            int orbit = Integer.parseInt(orbits[0]);
            for(int i=0;i<ninstr;i++){
                if(mat[orbit][i]==1){return false;}
            }
            return true;
           
        } else if(type.equalsIgnoreCase("numOrbits")){
            
            int num = Integer.parseInt(numbers[0]);
            int count = 0;
            for (int i = 0; i < norb; ++i) {
               boolean empty= true;
               for (int j=0; j< ninstr; j++){
                   if(mat[i][j]==1){
                       empty= false;
                   }
               }
               if(empty==false) count++;
            }
            return count==num;     
            
        } else if(type.equalsIgnoreCase("numOfInstruments")){
            // Three cases
            //numOfInstruments[;i;j]
            //numOfInstruments[i;;j]
            //numOfInstruments[;;i]
            
            int num = Integer.parseInt(numbers[0]);
            int count = 0;

            if(orbits[0]!=null && !orbits[0].isEmpty()){
                // Number of instruments in a specified orbit
                int orbit = Integer.parseInt(orbits[0]);
                for(int i=0;i<ninstr;i++){
                    if(mat[orbit][i]==1){count++;}
                }
            }else if(instruments[0]!=null && !instruments[0].isEmpty()){
                // Number of a specified instrument
                int instrument = Integer.parseInt(instruments[0]);
                for(int i=0;i<norb;i++){
                    if(mat[i][instrument]==1){count++;}
                }
            }else{
                // Number of instruments in all orbits
                for(int i=0;i<norb;i++){
                    for(int j=0;j<ninstr;j++){
                        if(mat[i][j]==1){count++;}
                    }
                }
            }
            if(count==num){return true;}
            return false;
            
        } else if(type.equalsIgnoreCase("subsetOfInstruments")){ 

            int orbit = Integer.parseInt(orbits[0]);
            int count = 0;

            for(int i=0;i<instruments.length;i++){
                int instrument = Integer.parseInt(instruments[i]);
                if(mat[orbit][instrument]==1){count++;}
            }
            if(numbers.length==1){
            	return count >= Integer.parseInt(numbers[0]);
            }else{
            	return (count>=Integer.parseInt(numbers[0]) && count<=Integer.parseInt(numbers[1]));
            }
        
        
        }
        
        return false;
    }
    
    
    
    public int[][] booleanString2IntArray(String booleanString){
        int[][] mat = new int[norb][ninstr];
        for(int i=0;i<norb;i++){
            for(int j=0;j<ninstr;j++){
                int loc = i*ninstr+j;
                mat[i][j] = Integer.parseInt(booleanString.substring(loc,loc+1));
            }
        }
        return mat;
    }
    
    

    public ArrayList<Integer> processFilterExpression(String filterExpression){
    	return processFilterExpression(filterExpression, this.population, "&&");
    }
    
    
    public ArrayList<Integer> processFilterExpression(String filterExpression, ArrayList<Integer> prevMatched, String prevLogic){
        String e=filterExpression;
        // Remove outer parenthesis
        e = remove_outer_parentheses(e);
        
        
        ArrayList<Integer> currMatched = new ArrayList<>();
        boolean first = true;
        
        String e_collapsed;
        if(jea.getNestedParenLevel(e)==0){
            // Given expression does not have a nested structure
            if(e.contains("&&")||e.contains("||")){
               e_collapsed=e; 
            }else{
            	// Single filter expression
                currMatched = this.processSingleFilterExpression(filterExpression);
                return compareMatchedIDSets(prevLogic, currMatched, prevMatched);
            }
        }else{
            // Removes the nested structure
            e_collapsed = jea.collapseAllParenIntoSymbol(e);
        }

        while(true){
            String current_collapsed;
            String prev;
            
            if(first){
                // The first filter in a series to be applied
                prev = "&&";
                currMatched = prevMatched;
                first = false;
            }else{
                prev = e_collapsed.substring(0,2);
                e_collapsed = e_collapsed.substring(2);
                e = e.substring(2);
            }
            
            String next; // The immediate next logical connective
            int and = e_collapsed.indexOf("&&");
            int or = e_collapsed.indexOf("||");
            if(and==-1 && or==-1){
                next = "";
            } else if(and==-1){ 
                next = "||";
            } else if(or==-1){
                next = "&&";
            } else if(and < or){
                next = "&&";
            } else{
                next = "||";
            }
            
            if(!next.isEmpty()){
                if(next.equals("||")){
                    current_collapsed = e_collapsed.split("\\|\\|",2)[0];
                }else{
                    current_collapsed = e_collapsed.split(next,2)[0];
                }
                String current = e.substring(0,current_collapsed.length());
                e_collapsed = e_collapsed.substring(current_collapsed.length());
                e = e.substring(current_collapsed.length());
                
                if(prev.equals("||")){
                    ArrayList<Integer> tempMatched = processFilterExpression(current,prevMatched,"&&");
                    currMatched = compareMatchedIDSets(prev, currMatched, tempMatched);
                }else{
                	currMatched = processFilterExpression(current,currMatched,prev);
                }                
            }else{
                if(prev.equals("||")){
                    ArrayList<Integer> tempMatched = processFilterExpression(e,prevMatched,"&&");
                    currMatched = compareMatchedIDSets(prev, currMatched, tempMatched);
                }else{
                	currMatched = processFilterExpression(e,currMatched,prev);
                }                 	
                break;
            }
        }
        return currMatched;
    }
    
    
    
    
    private int[] satisfactionArray(ArrayList<Integer> matchedArchIDs, ArrayList<Integer> allArchIDs){
        int[] satArray = new int[allArchIDs.size()];
        for(int i=0;i<allArchIDs.size();i++){
            int id = allArchIDs.get(i);
            if(matchedArchIDs.contains(id)){
                satArray[i]=1;
            }else{
                satArray[i]=0;
            }
        }
        return satArray;
    }       
    
    public int[] getSatisfactionArray(){
    	return this.satList;
    }
    
    
    
    public ArrayList<Integer> compareMatchedIDSets(String logic, ArrayList<Integer> set1, ArrayList<Integer> set2){
        ArrayList<Integer> output = new ArrayList<>();
        if(logic.equals("&&")){
            for(int i:set1){
                if(set2.contains(i)){
                    output.add(i);
                }
            }
        }else{
            for(int i:set1){
                output.add(i);
            }
            for(int i:set2){
                if(!output.contains(i)){
                    output.add(i);
                }
            }
        }
        return output;
    }
    
    public String remove_outer_parentheses(String expression){
    	
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

    public int[][] booleanString2intArray(String booleanString){
        int leng = booleanString.length();
        int[][] mat = new int[this.norb][this.ninstr];
        int cnt=0;
        for(int i=0;i<this.norb;i++){
            for(int j=0;j<this.ninstr;j++){
                if(booleanString.charAt(cnt)=='0'){
                    mat[i][j] = 0;
                }else{
                    mat[i][j] = 1;
                }
                cnt++;
            }
        }
        return mat;
    }
    
    

    
}
