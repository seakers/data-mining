/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.eossPartitioningAndAssignment.filterOperators;

import ifeed.problem.eoss.EOSSParams;
import ifeed.filter.BinaryInputFilterOperator;

import java.util.Random;
import java.util.BitSet;

/**
 *
 * @author bang
 */
public class EmptyOrbit extends ifeed.problem.eoss.filters.EmptyOrbit implements BinaryInputFilterOperator {

    public EmptyOrbit(int o){
        super(o);
    }

    @Override
    public BitSet disrupt(BitSet input){

        if(!super.apply(input)){
            // Do nothing
            return input;
        }else{
            // Satisfies all constraints
            Random random = new Random();
            int max = 11;
            int min = 0;
            int randInstr = random.nextInt(max + 1 - min) + min;

            BitSet out = (BitSet) input.clone();
            out.set(super.orbit * EOSSParams.num_instruments + randInstr);
            return out;
        }
    }

    @Override
    public BitSet repair(BitSet input){
        if(super.apply(input)){
            // Do nothing
            return input;
        }else{
            BitSet out = (BitSet) input.clone();
            for(int i = 0; i < EOSSParams.num_instruments; i++){
                out.clear(super.orbit * EOSSParams.num_instruments + i);
            }
            return out;
        }
    }

    @Override
    public void mutate(){
        int store = this.orbit;
        while(store == this.orbit){
            Random random = new Random();
            int max = EOSSParams.num_orbits;
            int min = 0;
            int randInt = random.nextInt(max + 1 - min) + min;
            this.orbit = randInt;
        }
    }
}