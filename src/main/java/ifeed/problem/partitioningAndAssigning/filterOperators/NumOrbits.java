/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.partitioningAndAssigning.filterOperators;

import ifeed.filter.DiscreteInputFilterOperator;
import ifeed.local.params.BaseParams;

import java.util.Random;

/**
 *
 * @author bang
 */
public class NumOrbits extends ifeed.problem.partitioningAndAssigning.filters.NumOrbits implements DiscreteInputFilterOperator {

    public NumOrbits(BaseParams params, int n){
        super(params, n);
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
        int store = this.num;
        while(store == this.num){
            Random random = new Random();
            int max = params.getNumOrbits();
            int min = 1;
            int randInt = random.nextInt(max + 1 - min) + min;
            this.num = randInt;
        }
    }
}
