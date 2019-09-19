/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.edl;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.DiscreteInputArchitecture;
import ifeed.local.params.BaseParams;
import ifeed.ontology.OntologyManager;

import java.util.*;

/**
 *
 * @author bang
 */
public class Params extends BaseParams {

    private List<String> decisionVarNames;
    private List<String> objectiveVarNames;
    private String labelVarName;
    private int[] maxValues;

    public Params(int numDecisions){
        this.maxValues = new int[numDecisions];
        for(int i = 0; i < this.maxValues.length; i++){
            this.maxValues[i] = -1;
        }
    }

    public void computeMaxValues(List<AbstractArchitecture> architectures){
        for(AbstractArchitecture arch: architectures){
            int[] inputs = ((DiscreteInputArchitecture) arch).getInputs();
            for(int i = 0; i < inputs.length; i++){
                if(inputs[i] > this.maxValues[i]){
                    this.maxValues[i] = inputs[i];
                }
            }
        }
    }

    public void setDecisionVarNames(List<String> decisionVarNames) {
        this.decisionVarNames = decisionVarNames;
    }

    public void setObjectiveVarNames(List<String> objectiveVarNames){
        this.objectiveVarNames = objectiveVarNames;
    }

    public void setLabelVarName(String labelVarName) {
        this.labelVarName = labelVarName;
    }

    public int[] getMaxValues(){
        return this.maxValues;
    }

    public List<String> getDecisionVarNames(){
        return this.decisionVarNames;
    }

    public List<String> getObjectiveVarNames(){
        return this.objectiveVarNames;
    }

    public String getLabelVarName(){
        return this.getLabelVarName();
    }
}
