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
public class Present extends ifeed.problem.assigning.filters.Present implements BinaryInputFilterOperator {

    public Present(BaseParams params, int i){
        super(params, i);
    }

    @Override
    public BitSet breakSpecifiedCondition(BitSet input, List<Integer> instruments){
        return input;
    }

    @Override
    public BitSet disrupt(BitSet input){

        if(!super.apply(input)){
            // Do nothing
            return input;
        }else{
            // Satisfies all constraints
            BitSet out = (BitSet) input.clone();

            for(int o = 0; o< this.params.getRightSetCardinality(); o++){
                out.clear(o * this.params.getLeftSetCardinality() + super.instrument);
            }
            return out;
        }
    }

    @Override
    public BitSet repair(BitSet input){

        if(super.apply(input)){
            // Do nothing
            return input;
        }else{
            Random random = new Random();
            int max = this.params.getRightSetCardinality();
            int min = 0;
            int randOrb = random.nextInt(max + 1 - min) + min;

            BitSet out = (BitSet) input.clone();
            out.set(randOrb * this.params.getLeftSetCardinality() + super.instrument);
            return out;
        }
    }

    @Override
    public void mutate(){
        int store = this.instrument;
        while(store == this.instrument){
            Random random = new Random();
            int max = this.params.getLeftSetCardinality();
            int min = 0;
            int randInt = random.nextInt(max + 1 - min) + min;
            this.instrument = randInt;
        }
    }
}
