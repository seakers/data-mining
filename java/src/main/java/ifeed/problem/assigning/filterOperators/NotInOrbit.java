/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filterOperators;

import ifeed.problem.assigning.Params;
import ifeed.filter.BinaryInputFilterOperator;

import java.util.Iterator;
import java.util.Random;
import java.util.BitSet;

/**
 *
 * @author bang
 */
public class NotInOrbit extends ifeed.problem.assigning.filters.NotInOrbit implements BinaryInputFilterOperator {

    public NotInOrbit(int o, int[] instruments){ super(o, instruments); }

    @Override
    public BitSet disrupt(BitSet input){

        if(!super.apply(input)){
            // Do nothing
            return input;
        }else{
            // Satisfies all constraints
            Random random = new Random();
            int max = super.instruments.size() - 1;
            int min = 0;
            int randInstr = random.nextInt(max + 1 - min) + min;

            BitSet out = (BitSet) input.clone();
            out.set(super.orbit * Params.num_instruments + randInstr);
            return out;
        }
    }

    @Override
    public BitSet repair(BitSet input){
        if(!super.apply(input)){
            // Do nothing
            return input;
        }else{
            BitSet out = (BitSet) input.clone();
            for(int i:super.instruments){
                out.clear(super.orbit * Params.num_instruments + i);
            }
            return out;
        }
    }

    @Override
    public void mutate(){
        // Select one instrument
        Random random = new Random();
        int max = this.instruments.size();
        int min = 0;
        int randInt = random.nextInt(max + 1 - min) + min;
        int instrument_to_modify = randInt;

        int store = 0;
        Iterator iter = this.instruments.iterator();
        for(int i = 0; i < instrument_to_modify; i++){
            store = (Integer) iter.next();
        }

        // Remove the selected instrument
        this.instruments.remove(store);
        int new_instrument_to_add = store;
        while(store == new_instrument_to_add){
            random = new Random();
            max = Params.num_instruments;
            min = 0;
            randInt = random.nextInt(max + 1 - min) + min;
            new_instrument_to_add = randInt;
        }

        // Add a new instrument
        this.instruments.add(new_instrument_to_add);
    }
}
