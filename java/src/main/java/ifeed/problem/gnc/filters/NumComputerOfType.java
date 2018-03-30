/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.gnc.filters;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.DiscreteInputArchitecture;
import ifeed.filter.Filter;
import ifeed.problem.gnc.GNCParams;

/**
 *
 * @author bang
 */
public class NumComputerOfType extends Filter {

    private final int n;
    private final int computer;

    public NumComputerOfType(int computer, int n){
        this.n = n;
        this.computer = computer;
    }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((DiscreteInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(int[] input){
        
        String computerInput = Integer.toString(input[GNCParams.computers_index]);
        int cnt = 0;

        int leng = computerInput.length();
        for(int i = 0; i < leng; i++){
            if(i == leng-1){ // Last digit
                if(this.computer == Integer.parseInt(computerInput.substring(i))){
                    cnt++;
                }
            }else{
                if(this.computer == Integer.parseInt(computerInput.substring(i,i+1))){
                    cnt++;
                }
            }
        }
        return cnt == this.n;
    }
    
    @Override
    public String getName(){return "numComputerOfType";}
    
    @Override
    public String toString(){
        return "{numComputerOfType[" + Integer.toString(this.computer) + ";" + this.n + "]}";
    }
}
