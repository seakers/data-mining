/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.eossPartitioningAndAssignment.filterOperators;

import ifeed.problem.eoss.EOSSParams;
import ifeed.filter.BinaryInputFilterOperator;

import java.util.Collections;
import java.util.Random;
import java.util.BitSet;
import java.util.ArrayList;

/**
 *
 * @author bang
 */
public class NumOrbits extends ifeed.problem.eoss.filters.NumOrbits implements BinaryInputFilterOperator {

    public NumOrbits(int n){
        super(n);
    }

    @Override
    public BitSet disrupt(BitSet input){

        if(!super.apply(input)){
            // Do nothing
            return input;
        }else{
            ArrayList<Integer> nonEmptyOrbits = new ArrayList<>();

            for(int o=0;o<EOSSParams.num_orbits;o++){
                boolean used = false;
                for(int i=0;i<EOSSParams.num_instruments;i++){
                    if(input.get(o*EOSSParams.num_instruments+i)){
                        used=true;
                        break;
                    }
                }
                if(used){
                    nonEmptyOrbits.add(o);
                }
            }

            // Satisfies all constraints
            Random random = new Random();
            int max = nonEmptyOrbits.size() - 1;
            int min = 0;
            int randInt = random.nextInt(max + 1 - min) + min;
            int randOrb = nonEmptyOrbits.get(randInt);

            BitSet out = (BitSet) input.clone();
            for(int i = 0; i < EOSSParams.num_instruments; i++){
                out.clear(randOrb * EOSSParams.num_instruments + i);
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
            ArrayList<Integer> nonEmptyOrbits = new ArrayList<>();
            ArrayList<Integer> emptyOrbits = new ArrayList<>();

            for(int o=0;o<EOSSParams.num_orbits;o++){
                boolean used = false;
                for(int i=0;i<EOSSParams.num_instruments;i++){
                    if(input.get(o*EOSSParams.num_instruments+i)){
                        used=true;
                        break;
                    }
                }
                if(used){
                    nonEmptyOrbits.add(o);
                }else{
                    emptyOrbits.add(o);
                }
            }

            Random random = new Random();
            BitSet out = (BitSet) input.clone();
            int diff = super.num - nonEmptyOrbits.size();

            for(int i = 0; i < Math.abs(diff); i++){
                if(diff > 0){
                    // Add instruments to empty orbits
                    Collections.shuffle(emptyOrbits);
                    int o = emptyOrbits.get(0);
                    emptyOrbits.remove(0);

                    // Get random instrument to add
                    int max = EOSSParams.num_instruments - 1;
                    int min = 0;
                    int randInt = random.nextInt(max + 1 - min) + min;
                    out.set(o * EOSSParams.num_instruments + randInt);
                }else{
                    // Remove instruments from nonEmpty orbits
                    // Add instruments to empty orbits
                    Collections.shuffle(nonEmptyOrbits);
                    int o = nonEmptyOrbits.get(0);
                    nonEmptyOrbits.remove(0);

                    for(int j = 0; j < EOSSParams.num_instruments; j++){
                        out.clear(o * EOSSParams.num_instruments + j);
                    }
                }
            }

            return out;
        }
    }

    @Override
    public void mutate(){
        int store = this.num;
        while(store == this.num){
            Random random = new Random();
            int max = EOSSParams.num_orbits;
            int min = 1;
            int randInt = random.nextInt(max + 1 - min) + min;
            this.num = randInt;
        }
    }
}
