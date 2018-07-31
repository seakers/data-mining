/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.partitioningAndAssignment.filters;

import java.util.BitSet;
import java.util.Objects;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.architecture.DiscreteInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.problem.assignment.Params;

/**
 *
 * @author bang
 */
public class NumOfInstruments extends AbstractFilter {

    private int num;
    private int orb;

    public NumOfInstruments(int orb, int n){
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
        for(int i = 0; i < Params.num_instruments; i++){
            if(input[Params.num_instruments + i] == this.orb){
                satIndex = i;
            }
        }

        for(int i = 0; i < Params.num_instruments; i++){
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
