/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.partitioningAndAssigning.filters;

import java.util.Objects;
import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.DiscreteInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.partitioningAndAssigning.Params;

/**
 *
 * @author bang
 */
public class NumOfInstruments extends AbstractFilter {

    protected Params params;
    private int num;
    private int orb;

    public NumOfInstruments(BaseParams params, int orb, int n){
        super(params);
        this.params = (Params) params;
        this.orb = orb;
        this.num = n;
    }

    public int getOrb(){ return this.orb; }
    public int getNum(){ return this.num; }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((DiscreteInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(int[] input){
        // Three cases
        //numOfInstruments[;i;j]
        //numOfInstruments[i;;j]
        //numOfInstruments[;;i]

        // Number of instruments in total
        // Number of instruments in an orbit
        // Number of a specific instrument in all orbits

        int count = 0;
        int satIndex = -1;
        // Number of instruments in an orbit
        for(int i = 0; i < params.getNumInstruments(); i++){
            if(input[params.getNumInstruments() + i] == this.orb){
                satIndex = i;
            }
        }

        for(int i = 0; i < params.getNumInstruments(); i++){
            if(input[i] == satIndex){
                count++;
            }
        }
        return count == num;
    }

    @Override
    public String getName(){return "numOfInstruments";}

    @Override
    public String toString(){
        return "{numOfInstruments[" + this.orb + ";;" + num + "]}";
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 19 * hash + Objects.hashCode(this.orb);
        hash = 19 * hash + Objects.hashCode(this.num);
        hash = 19 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof NumOfInstruments){
            NumOfInstruments other = (NumOfInstruments) o;
            return this.orb == other.getOrb() && this.num == other.getNum();
        }
        return false;
    }

}
