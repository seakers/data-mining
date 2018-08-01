/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.partitioningAndAssigning.filterOperators;

import ifeed.filter.DiscreteInputFilterOperator;
import ifeed.problem.partitioningAndAssigning.Params;
import java.util.Random;

/**
 *
 * @author bang
 */
public class EmptyOrbit extends ifeed.problem.partitioningAndAssigning.filters.EmptyOrbit implements DiscreteInputFilterOperator {

    public EmptyOrbit(int o){
        super(o);
    }

    @Override
    public int[] disrupt(int[] input){
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] repair(int[] input){
        throw new UnsupportedOperationException();
    }

    @Override
    public void mutate(){
        int store = this.orbit;
        while(store == this.orbit){
            Random random = new Random();
            int max = Params.num_orbits;
            int min = 0;
            int randInt = random.nextInt(max + 1 - min) + min;
            this.orbit = randInt;
        }
    }
}
