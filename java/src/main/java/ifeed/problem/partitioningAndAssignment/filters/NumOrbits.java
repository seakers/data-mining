/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.partitioningAndAssignment.filters;

import java.util.Objects;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.DiscreteInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.problem.assignment.Params;

/**
 *
 * @author bang
 */
public class NumOrbits extends AbstractFilter {

    protected int num;
    
    public NumOrbits(int n){
        this.num = n;
    }

    public int getNum() {
        return num;
    }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((DiscreteInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(int[] input){
        int cnt = 0;
        for(int i = 0; i < Params.num_instruments; i++){
            if(input[Params.num_instruments + i] == -1){
                cnt++;
            }
        }
        return cnt==num;
    }
    
    @Override
    public String getName(){return "numOrbits";}
    
    @Override
    public String toString(){     
        return "{numOrbits[;;" + num + "]}";
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 31 * hash + Objects.hashCode(this.num);
        hash = 31 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof NumOrbits){
            NumOrbits other = (NumOrbits) o;
            return this.num == other.getNum();
        }
        return false;
    }

}
