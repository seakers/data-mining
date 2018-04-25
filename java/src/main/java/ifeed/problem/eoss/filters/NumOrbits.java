/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.eoss.filters;

import java.util.BitSet;
import java.util.Objects;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.filter.Filter;
import ifeed.problem.eoss.EOSSParams;

/**
 *
 * @author bang
 */
public class NumOrbits extends Filter {

    protected int num;
    
    public NumOrbits(int n){
        this.num = n;
    }

    public int getNum() {
        return num;
    }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(BitSet input){

        int cnt = 0;
        for(int o=0;o<EOSSParams.num_orbits;o++){
            boolean used = false;
            for(int i=0;i<EOSSParams.num_instruments;i++){
                if(input.get(o*EOSSParams.num_instruments+i)){
                    used=true;
                    break;
                }
            }
            if(used){
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
