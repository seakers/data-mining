/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filters;

import java.util.BitSet;
import java.util.Objects;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.Params;

/**
 *
 * @author bang
 */
public class NumOrbits extends AbstractFilter {

    protected Params params;
    protected int num;
    
    public NumOrbits(BaseParams params, int n){
        super(params);
        this.params = (Params) params;
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
        for(int o = 0; o< this.params.getRightSetCardinality(); o++){
            boolean used = false;
            for(int i = 0; i< this.params.getLeftSetCardinality(); i++){
                if(input.get(o* this.params.getLeftSetCardinality() +i)){
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
