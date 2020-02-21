/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filterOperators;

import ifeed.local.params.BaseParams;
import ifeed.filter.BinaryInputFilterOperator;

import java.util.*;

/**
 *
 * @author bang
 */
public class InOrbit extends ifeed.problem.assigning.filters.InOrbit implements BinaryInputFilterOperator {

    public InOrbit(BaseParams params, int o, int[] instruments){ super(params, o, instruments); }

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

            ArrayList<Integer> usedInstruments = new ArrayList<>();
            for(int i: super.instruments){
                if(input.get(super.orbit * this.params.getLeftSetCardinality() + i)){
                    usedInstruments.add(i);
                }
            }

            // Satisfies all constraints
            Random random = new Random();
            int max = usedInstruments.size() - 1;
            int min = 0;
            int randInt = random.nextInt(max + 1 - min) + min;
            int randInstr = usedInstruments.get(randInt);

            BitSet out = (BitSet) input.clone();
            out.clear(super.orbit * this.params.getLeftSetCardinality() + randInstr);
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
            for(int i:super.instruments){
                out.set(super.orbit * this.params.getLeftSetCardinality() + i);
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
            max = this.params.getLeftSetCardinality();
            min = 0;
            randInt = random.nextInt(max + 1 - min) + min;
            new_instrument_to_add = randInt;
        }

        // Add a new instrument
        this.instruments.add(new_instrument_to_add);
    }
}
