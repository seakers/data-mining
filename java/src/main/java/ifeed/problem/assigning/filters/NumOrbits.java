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
    private int[] nBounds;

    public NumOrbits(BaseParams params, int n){
        super(params);
        this.params = (Params) params;
        this.nBounds = new int[2];
        this.nBounds[0] = n;
        this.nBounds[1] = n;
    }

    public NumOrbits(BaseParams params, int[] bounds){
        super(params);
        assert(bounds.length == 2);
        this.params = (Params) params;
        this.nBounds = bounds;
    }

    public int[] getBounds() {
        return this.nBounds;
    }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(BitSet input){
        int cnt = 0;
        for(int o = 0; o < this.params.getRightSetCardinality(); o++){
            boolean used = false;
            for(int i = 0; i < this.params.getLeftSetCardinality(); i++){
                if(input.get(o * this.params.getLeftSetCardinality() +i)){
                    used = true;
                    break;
                }
            }
            if(used){
                cnt++;
            }
        }
        return cnt >= nBounds[0] && cnt <= nBounds[1];
    }


    @Override
    public String getDescription(){
        if(this.nBounds[0] == this.nBounds[1]){
            if(this.nBounds[0] == 1){
                return "Only one orbit is used";
            }else{
                return this.nBounds[0] + " orbits are used";
            }
        }else{
            return "Between " + this.nBounds[0] + " and " + this.nBounds[1] + " orbits are used";
        }
    }
    
    @Override
    public String getName(){return "numOrbits";}
    
    @Override
    public String toString(){
        if(this.nBounds[0] == this.nBounds[1]){
            return "{numOrbits[;;" + this.nBounds[0] + "]}";
        }else{
            return "{numOrbits[;;" + this.nBounds[0] + "," + this.nBounds[1] + "]}";
        }
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 31 * hash + Objects.hashCode(this.nBounds);
        hash = 31 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof NumOrbits){
            NumOrbits other = (NumOrbits) o;
            return this.nBounds[0] == other.getBounds()[0] && this.nBounds[1] == other.getBounds()[1];
        }
        return false;
    }
}
