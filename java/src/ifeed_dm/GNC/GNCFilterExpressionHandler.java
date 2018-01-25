/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.GNC;

import ifeed_dm.EOSS.*;
import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;

import ifeed_dm.BaseFeature;
import ifeed_dm.Utils;
import ifeed_dm.LogicOperator;


/**
 *
 * @author bang
 */


public class GNCFilterExpressionHandler{
    
    protected List<BaseFeature> baseFeatures;
    protected int numOfObservations;
    
    
    public GNCFilterExpressionHandler(int numOfObservations, List<BaseFeature> baseFeatures) {
      
        this.baseFeatures = new ArrayList<>(baseFeatures);  
        this.numOfObservations = numOfObservations;
    }
    

    public BitSet processSingleFilterExpression(String inputExpression){
        
        BaseFeature matchingFeature;
        // Examples of feature expressions: {name[arguments]}   
        try{
            
            String e;
            if(inputExpression.startsWith("{") && inputExpression.endsWith("}")){
                e = inputExpression.substring(1,inputExpression.length()-1);
            }else{
                e = inputExpression;
            }

            if(e.split("\\[").length==1){
                throw new Exception("Filter expression without brackets: " + inputExpression);
            }

            String name = e.split("\\[")[0];
            String args = e.substring(0,e.length()-1).split("\\[")[1];

            matchingFeature = findMatchingFeature(name,inputExpression);

            
        }catch(Exception e){
            System.out.println("Exc in processing a single feature expression");
            return new BitSet();
        }
        
        return matchingFeature.getMatches();
    }    
    
    
    
    public BaseFeature findMatchingFeature(String name, String fullExpression){

        BaseFeature match = null;
        
        try{
            for(BaseFeature feature:this.baseFeatures){
                if(fullExpression.equals(feature.getName())){
                    match = feature;
                    break;
                }
            }

            if(match==null){
                throw new Exception();
            }
            
        }catch(Exception e){
            System.out.println("Exc in finding the matching feature from the base features");
        }

        return match;
    }
    
    
    
    public FeatureTreeNode generateFeatureTree(String expression){
        
        FeatureTreeNode root = new FeatureTreeNode(null,LogicOperator.AND);
        
        AddSubTree(root,expression);
        
        return root;
    }

    

    
    public void AddSubTree(FeatureTreeNode parent,String expression){
        
        FeatureTreeNode node;

        // Remove outer parenthesis
        String e = Utils.remove_outer_parentheses(expression);
        // Copy the expression
        String _e = e;

        if(Utils.getNestedParenLevel(e)==0){
            
            // Given expression does not have a nested structure
            if(!e.contains("&&")&&!e.contains("||")){
                // There is no logical connective: Single filter expression
                if(e.contains("PLACEHOLDER")){
                    parent.addPlaceholder();
                }else{
                    BitSet filtered = processSingleFilterExpression(e);
                    parent.addFeature(filtered,e);
                }                
                return;
                
            }else{
                // Do nothing
            }
            
        }else{
            
            // Removes the nested structure
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
        node = new FeatureTreeNode(parent,logic);
        
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
                        
            this.AddSubTree(node,e_temp);
        }
        
        parent.addChildren(node);
    }
    
    

    
}
