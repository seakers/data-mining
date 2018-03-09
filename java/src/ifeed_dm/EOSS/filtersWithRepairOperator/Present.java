/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS.filtersWithRepairOperator;

import ifeed_dm.EOSS.EOSSParams;

import java.util.ArrayList;
import java.util.Random;
import java.util.BitSet;

/**
 *
 * @author bang
 */
public class Present extends ifeed_dm.EOSS.filters.Present implements RepairOperators {

    public Present(int i){
        super(i);
    }

    @Override
    public BitSet disrupt(BitSet input){

        if(!super.apply(input)){
            // Do nothing
            return input;
        }else{
            // Satisfies all constraints
            BitSet out = (BitSet) input.clone();

            for(int o=0;o<EOSSParams.num_orbits;o++){
                out.clear(o * EOSSParams.num_instruments + super.instrument);
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
            int max = EOSSParams.num_orbits;
            int min = 0;
            int randOrb = random.nextInt(max + 1 - min) + min;

            BitSet out = (BitSet) input.clone();
            out.set(randOrb * EOSSParams.num_instruments + super.instrument);
            return out;
        }
    }
}
