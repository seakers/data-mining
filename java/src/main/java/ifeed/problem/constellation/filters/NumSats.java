/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.constellation.filters;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.ContinuousInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.constellation.AbstractConstellationProblemParams;

import java.util.Objects;
import java.util.StringJoiner;

/**
 *
 * @author bang
 */

public class NumSats extends AbstractFilter{

    protected AbstractConstellationProblemParams params;
    protected Integer num;
    protected Integer[] numRange;

    public NumSats(BaseParams params, Integer num){
        super(params);
        this.params = (AbstractConstellationProblemParams) params;
        this.num = num;
        this.numRange = null;
    }

    public NumSats(BaseParams params, Integer[] numRange){
        super(params);
        this.params = (AbstractConstellationProblemParams) params;
        this.num = null;
        this.numRange = numRange;
        if(this.numRange[0] != null || numRange[1] != null){
            if(this.numRange[0] == null){
                this.numRange[0] = 0;
            }
            if(this.numRange[1] == null){
                this.numRange[1] = 999;
            }
        }
    }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((ContinuousInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(double[] input){

        boolean out;

        int numSats;
        if(this.params.isNumSatsFixed()){
            numSats = this.params.getNumSats();
        }else{
            numSats = input.length / this.params.getOrbitalParameters().length;
        }

        if(num != null){
            if(num == numSats){
                out = true;
            }else{
                out = false;
            }
        }else{
            if(numSats >= numRange[0] && numSats <= numRange[1]){
                out = true;
            }else{
                out = false;
            }
        }

        return out;
    }

    @Override
    public String getName(){return "numSats";}

    @Override
    public String toString(){

        String arg;
        if(num != null){
            arg = Integer.toString(num);
        }else{
            StringBuilder sb = new StringBuilder();
            if(numRange[0] != null){
                sb.append(Integer.toString(numRange[0]));
            }
            sb.append("~");
            if(numRange[1] != null){
                sb.append(Integer.toString(numRange[1]));
            }
            arg = sb.toString();
        }
        return "{numSats["+ arg +"]}";
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 31 * hash + Objects.hashCode(this.num);
        hash = 31 * hash + Objects.hashCode(this.numRange);
        hash = 31 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof NumSats){
            NumSats other = (NumSats) o;
            if(this.num == other.num && this.numRange == other.numRange){
                return true;
            }
        }
        return false;
    }
}
