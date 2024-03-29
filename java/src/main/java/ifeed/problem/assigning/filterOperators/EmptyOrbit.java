/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filterOperators;

import ifeed.local.params.BaseParams;
import ifeed.filter.BinaryInputFilterOperator;

import java.util.List;
import java.util.Random;
import java.util.BitSet;

/**
 *
 * @author bang
 */
public class EmptyOrbit extends ifeed.problem.assigning.filters.EmptyOrbit implements BinaryInputFilterOperator {

    public EmptyOrbit(BaseParams params, int o){
        super(params, o);
    }

    @Override
    public BitSet breakSpecifiedCondition(BitSet input, List<Integer> instruments){
        if(!super.apply(input)){
            // Do nothing
            return input;
        }else{
            // Satisfies all constraints
            BitSet out = (BitSet) input.clone();
            for(int i: instruments){
                out.set(super.orbit * this.params.getLeftSetCardinality() + i);
            }
            return out;
        }
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
            out.set(super.orbit * this.params.getLeftSetCardinality() + randInstr);
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
            for(int i = 0; i < this.params.getLeftSetCardinality(); i++){
                out.clear(super.orbit * this.params.getLeftSetCardinality() + i);
            }
            return out;
        }
    }

    @Override
    public void mutate(){
        int store = this.orbit;
        while(store == this.orbit){
            Random random = new Random();
            int max = this.params.getRightSetCardinality();
            int min = 0;
            int randInt = random.nextInt(max + 1 - min) + min;
            this.orbit = randInt;
        }
    }
}
