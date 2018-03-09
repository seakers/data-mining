/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS.filtersWithRepairOperator;

import ifeed_dm.EOSS.EOSSParams;

import java.util.Random;
import java.util.BitSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 *
 * @author bang
 */
public class Together extends ifeed_dm.EOSS.filters.Together implements RepairOperators {

    public Together(int[] instruments){
        super(instruments);
    }

    @Override
    public BitSet disrupt(BitSet input){

        if(!super.apply(input)){
            // Do nothing
            return input;
        }else{

            ArrayList<Integer> orbit_with_target_instruments = new ArrayList<>();
            for(int o=0;o<EOSSParams.num_orbits;o++){
                boolean sat = true;
                for(int i:super.instruments){
                    if(!input.get(o*EOSSParams.num_instruments+i)){
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
            out.clear(randOrb * EOSSParams.num_instruments + randInstr);
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

            for(int o=0;o<EOSSParams.num_orbits;o++){
                for(int i:instruments){
                    if(input.get(o * EOSSParams.num_instruments + i)){
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
                out.set(o * EOSSParams.num_instruments + i);
            }
            return out;
        }
    }
}
