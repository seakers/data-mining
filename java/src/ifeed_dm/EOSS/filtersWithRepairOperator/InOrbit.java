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

/**
 *
 * @author bang
 */
public class InOrbit extends ifeed_dm.EOSS.filters.InOrbit implements RepairOperators {

    public InOrbit(int o, int[] instruments){ super(o, instruments); }

    @Override
    public BitSet disrupt(BitSet input){

        if(!super.apply(input)){
            // Do nothing
            return input;
        }else{

            ArrayList<Integer> usedInstruments = new ArrayList<>();
            for(int i: super.instruments){
                if(input.get(super.orbit * EOSSParams.num_instruments + i)){
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
            out.clear(super.orbit * EOSSParams.num_instruments + randInstr);
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
                out.set(super.orbit * EOSSParams.num_instruments + i);
            }
            return out;
        }
    }
}
