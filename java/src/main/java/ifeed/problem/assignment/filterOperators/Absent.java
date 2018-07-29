/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assignment.filterOperators;

import ifeed.problem.assignment.Params;
import ifeed.filter.BinaryInputFilterOperator;

import java.util.Random;
import java.util.BitSet;

/**
 *
 * @author bang
 */
public class Absent extends ifeed.problem.assignment.filters.Absent implements BinaryInputFilterOperator {

    public Absent(int i){
        super(i);
    }

    @Override
    public BitSet disrupt(BitSet input){

        if(!super.apply(input)){
            // Do nothing
            return input;
        }else{
            // Satisfies all constraints
            Random random = new Random();
            int max = 4;
            int min = 0;
            int randOrb = random.nextInt(max + 1 - min) + min;

            BitSet out = (BitSet) input.clone();
            out.set(randOrb * Params.num_instruments + instrument);
            return out;
        }
    }

    @Override
    public BitSet repair(BitSet input){

        if(super.apply(input)){
            return input;

        }else{
            BitSet out = (BitSet) input.clone();
            for(int o = 0; o< Params.num_orbits; o++){
                if(input.get(o* Params.num_instruments+super.instrument)){
                    out.clear(o * Params.num_instruments + super.instrument);
                }
            }
            return out;
        }
    }

    @Override
    public void mutate(){
        int store = this.instrument;
        while(store == this.instrument){
            Random random = new Random();
            int max = Params.num_instruments;
            int min = 0;
            int randInt = random.nextInt(max + 1 - min) + min;
            this.instrument = randInt;
        }
    }
}
