/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.partitioningAndAssigning.filterOperators;

import ifeed.filter.DiscreteInputFilterOperator;
import ifeed.local.params.BaseParams;

import java.util.*;

/**
 *
 * @author bang
 */
public class Together extends ifeed.problem.partitioningAndAssigning.filters.Together implements DiscreteInputFilterOperator {

    public Together(BaseParams params, int[] instruments){
        super(params, instruments);
    }

    @Override
    public int[] disrupt(int[] input){
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] repair(int[] input) {
        throw new UnsupportedOperationException();
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
        Iterator<Integer> iter = this.instruments.iterator();
        for(int i = 0; i < instrument_to_modify; i++){
            store = iter.next();
        }

        // Remove the selected instrument
        this.instruments.remove(store);

        int new_instrument_to_add = store;
        while(store == new_instrument_to_add){
            random = new Random();
            max = params.getNumInstruments();
            min = 0;
            randInt = random.nextInt(max + 1 - min) + min;
            new_instrument_to_add = randInt;
        }

        // Add a new instrument
        this.instruments.add(new_instrument_to_add);
    }
}
