/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.edl;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.DiscreteInputArchitecture;
import ifeed.expression.Symbols;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.edl.Params;

import java.util.Objects;

/**
 *
 * @author bang
 */
public class DiscreteValueFilter extends AbstractFilter {

    protected int varIndex;
    protected int value;
//    protected List<Integer> values;
    protected Params params;

    public DiscreteValueFilter(BaseParams params, int varIndex, int value){
        super(params);
        this.params = (Params) params;
        this.varIndex = varIndex;
        this.value = value;
    }

    public DiscreteValueFilter(BaseParams params, String varName, int value){
        super(params);
        this.params = (Params) params;
        try{
            this.varIndex = this.params.getDecisionVarNames().indexOf(varName);
        } catch (Exception e){
            System.out.println("Variable " + varName + " not recognized");
            e.printStackTrace();
        }
        this.value = value;
    }

    public int getVarIndex(){
        return this.varIndex;
    }

    public int getValue(){
        return this.getValue();
    }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((DiscreteInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(int[] input){
        if(input[this.varIndex] == value){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getDescription(){
        return this.params.getDecisionVarNames().get(this.varIndex) +
                " has value " + this.value;
    }

    @Override
    public String getName(){return this.toString();}

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(Symbols.individual_expression_wrapper_open);
        sb.append("DiscreteValueFilter");
        sb.append(Symbols.argument_wrapper_open);
        sb.append(this.params.getDecisionVarNames().get(this.varIndex));
        sb.append(";");
        sb.append(Integer.toString(this.value));
        sb.append(Symbols.argument_wrapper_close);
        sb.append(Symbols.individual_expression_wrapper_close);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 31 * hash + Objects.hashCode(this.params.getDecisionVarNames().get(this.varIndex));
        hash = 31 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof DiscreteValueFilter){
            DiscreteValueFilter other = (DiscreteValueFilter) o;
            return this.varIndex == other.getVarIndex() && this.value == other.getValue();
        }
        return false;
    }
}
