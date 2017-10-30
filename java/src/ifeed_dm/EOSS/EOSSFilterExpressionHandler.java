/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS;

import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;

import ifeed_dm.FilterExpressionHandler;
import ifeed_dm.BinaryInputFeature;
import ifeed_dm.Utils;
import ifeed_dm.LogicOperator;


/**
 *
 * @author bang
 */


public class EOSSFilterExpressionHandler implements FilterExpressionHandler{
    
    protected List<BinaryInputFeature> baseFeatures;
    protected int numOfObservations;
    
    
    public EOSSFilterExpressionHandler(int numOfObservations, List<BinaryInputFeature> baseFeatures) {
      
        this.baseFeatures = new ArrayList<>(baseFeatures);  
        this.numOfObservations = numOfObservations;
    }
    
    
    public String replacePlaceholder(String fullExpression, String filterExpression){
        return fullExpression.replaceFirst("{PLACEHOLDER}", filterExpression);
    }
    
    
    @Override
    public BitSet processSingleFilterExpression(String inputExpression){
        
        // Examples of feature expressions: {name[arguments]}   
        
        String e;
        if(inputExpression.startsWith("{") && inputExpression.endsWith("}")){
            e = inputExpression.substring(1,inputExpression.length()-1);
        }else{
            e = inputExpression;
        }
        
        String name = e.split("\\[")[0];
        String args = e.substring(0,e.length()-1).split("\\[")[1];
        
        BinaryInputFeature matchingFeature = findMatchingFeature(name,inputExpression);
        
        return matchingFeature.getMatches();
    }    
    
    
    
    public BinaryInputFeature findMatchingFeature(String name, String fullExpression){

        BinaryInputFeature match = null;
        
        try{
            for(BinaryInputFeature feature:this.baseFeatures){

                if(name.equals(feature.getName())){
                    if(fullExpression.equals(feature.toString())){
                        match = feature;
                        break;
                    }
                }
            }

            if(match==null){
                throw new Exception();
            }
            
        }catch(Exception e){
            System.out.println("Exc in find the matching feature from the base features");
        }

        return match;
    }
    
    
    
    @Override
    public BitSet processFilterExpression(String expression){
        
        BitSet matches = new BitSet(this.numOfObservations);
        // Set all bits
        matches.set(0, this.numOfObservations);
        
        return processFilterExpression(expression, matches);
    }

    @Override
    public BitSet processFilterExpression(String expression, BitSet inputMatches){
        
        String e, _e;
        e=expression;
        
        //Remove outer parenthesis
        e = Utils.remove_outer_parentheses(e);
        _e = e;

        boolean first = true;
        boolean last = false;
        
        BitSet matches = (BitSet) inputMatches.clone();
        
        LogicOperator logic = LogicOperator.AND;

        if(Utils.getNestedParenLevel(e)==0){
            // Given expression does not have a nested structure
            if(!e.contains("&&")&&!e.contains("||")){
                // There is no logical connective: Single filter expression
                BitSet filtered = processSingleFilterExpression(e);
                if(logic==LogicOperator.AND){
                    matches.and(filtered);
                }else{
                    matches.or(filtered);
                }
                return matches;
                
            }else{
                // Do nothing
            }
        }else{
            // Removes the nested structure
            _e = Utils.collapseAllParenIntoSymbol(e);
        }
        
        while(!last){
            
            String e_temp, _e_temp;
            
            if(first){
                // The first filter in a series to be applied
                first = false;
            }else{
                
                if(_e.substring(0,2).equals("&&")){
                    logic=LogicOperator.AND;
                }else{
                    logic=LogicOperator.OR;
                }
                _e = _e.substring(2);
                e = e.substring(2);
            }
            
            String next; // The immediate next logical connective
            int and = _e.indexOf("&&");
            int or = _e.indexOf("||");
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

                _e_temp = _e.split(next,2)[0];
                e_temp = e.substring(0,_e_temp.length());
                    
                _e = _e.substring(_e_temp.length());
                e = e.substring(_e_temp.length());

            }else{
                _e_temp=_e;
                e_temp=e;
                last=true;
            }
            
            BitSet filtered = this.processFilterExpression(e_temp,matches);
            
            if(logic==LogicOperator.OR){
                matches.or(filtered);
            }else{
                matches.and(filtered);
            }
            
        }
        return matches;
    }
    
}
