/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assignment.filterOperators;

import ifeed.problem.assignment.Params;
import ifeed.filter.BinaryInputFilterOperator;

import java.util.*;

/**
 *
 * @author bang
 */
public class Separate extends ifeed.problem.assignment.filters.Separate implements BinaryInputFilterOperator {

    public Separate(int[] instruments){
        super(instruments);
    }

    @Override
    public BitSet disrupt(BitSet input){

        if(!super.apply(input)){
            // Do nothing
            return input;
        }else{
            // Satisfies all constraints
            Random random = new Random();
            // Select orbit
            int max = 4;
            int min = 0;
            int randOrb = random.nextInt(max + 1 - min) + min;

            // Select two instruments
            int instr1;
            int instr2;
            if(super.instruments.size() == 2){
                Iterator<Integer> iter = super.instruments.iterator();
                instr1 = iter.next();
                instr2 = iter.next();
            }else{
                ArrayList<Integer> list = new ArrayList<>();
                for(int i:super.instruments){
                    list.add(i);
                }
                Collections.shuffle(list);

                instr1 = list.get(0);
                instr2 = list.get(1);
            }

            BitSet out = (BitSet) input.clone();
            out.set(randOrb * Params.num_instruments + instr1);
            out.set(randOrb * Params.num_instruments + instr2);
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
            ArrayList<Integer> orbitsWithTargetInstruments = new ArrayList<>();

            for(int o = 0; o< Params.num_orbits; o++){
                boolean together = true;
                for(int i:instruments){
                    if(!input.get(o * Params.num_instruments + i)){
                        // If any one of the instruments are not present
                        together=false;
                        break;
                    }
                }

                if(together){
                    orbitsWithTargetInstruments.add(o);
                    break;
                }
            }

            BitSet out = (BitSet) input.clone();

            for(int o:orbitsWithTargetInstruments){
                ArrayList<Integer> usedInstruments = new ArrayList<>();
                for(int i:instruments){
                    if(input.get(o * Params.num_instruments + i)){
                        usedInstruments.add(i);
                    }
                }

                // Select only one instrument and remove all the rest
                Collections.shuffle(usedInstruments);
                usedInstruments.remove(0);
                for(int i:usedInstruments){
                    out.clear(o * Params.num_instruments + i);
                }

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
            max = Params.num_instruments;
            min = 0;
            randInt = random.nextInt(max + 1 - min) + min;
            new_instrument_to_add = randInt;
        }

        // Add a new instrument
        this.instruments.add(new_instrument_to_add);
    }
}
