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
public class Together extends ifeed.problem.assigning.filters.Together implements BinaryInputFilterOperator {

    public Together(BaseParams params, int[] instruments){
        super(params, instruments);
    }

    @Override
    public BitSet disrupt(BitSet input){

        if(!super.apply(input)){
            // Do nothing
            return input;
        }else{

            ArrayList<Integer> orbit_with_target_instruments = new ArrayList<>();
            for(int o = 0; o< this.params.getRightSetCardinality(); o++){
                boolean sat = true;
                for(int i:super.instruments){
                    if(!input.get(o* this.params.getLeftSetCardinality() +i)){
                        // If any one of the instruments are not present
                        sat = false;
                        break;
                    }
                }

                if(sat){
                    orbit_with_target_instruments.add(o);
                    break;
                }
            }

            Random random = new Random();
            // Select orbit
            int max = orbit_with_target_instruments.size();
            int min = 0;
            int randInt = random.nextInt(max + 1 - min) + min;
            int randOrb = orbit_with_target_instruments.get(randInt);

            // Select one instrument
            int randInstr;

            ArrayList<Integer> list = new ArrayList<>();
            for(int i:super.instruments){
                list.add(i);
            }
            Collections.shuffle(list);
            randInstr = list.get(0);

            BitSet out = (BitSet) input.clone();
            out.clear(randOrb * this.params.getLeftSetCardinality() + randInstr);
            return out;
        }
    }

    @Override
    public BitSet repair(BitSet input) {
        if(super.apply(input)){
            // Do nothing
            return input;
        }else{

            Random random = new Random();
            ArrayList<Integer> orbitsWithAtLeastOneTargetInstrument = new ArrayList<>();

            for(int o = 0; o< this.params.getRightSetCardinality(); o++){
                for(int i:instruments){
                    if(input.get(o * this.params.getLeftSetCardinality() + i)){
                        // If any one of the instruments are not present
                        orbitsWithAtLeastOneTargetInstrument.add(o);
                        break;
                    }
                }
            }

            BitSet out = (BitSet) input.clone();
            Collections.shuffle(orbitsWithAtLeastOneTargetInstrument);
            int o = orbitsWithAtLeastOneTargetInstrument.get(0);
            for(int i: super.instruments){
                out.set(o * this.params.getLeftSetCardinality() + i);
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
        Iterator<Integer> iter = this.instruments.iterator();
        for(int i = 0; i < instrument_to_modify; i++){
            store = iter.next();
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
