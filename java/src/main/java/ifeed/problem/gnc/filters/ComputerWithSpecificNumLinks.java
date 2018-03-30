/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.gnc.filters;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.DiscreteInputArchitecture;
import ifeed.problem.gnc.GNCParams;
import ifeed.filter.Filter;

import java.util.ArrayList;

/**
 *
 * @author bang
 */
public class ComputerWithSpecificNumLinks extends Filter {

    private final int n;
    private final int computer;

    public ComputerWithSpecificNumLinks(int computer, int n){
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
        ArrayList<Integer> targetComputer = new ArrayList<>();

        int ns = input[0];
        int nc = input[1];

        for(int i =0;i<computerInput.length();i++){
            if(i == computerInput.length()-1){ // Last digit
                if(this.computer == Integer.parseInt(computerInput.substring(i))){
                    targetComputer.add(i);
                }
            }else{
                if(this.computer == Integer.parseInt(computerInput.substring(i,i+1))){
                    targetComputer.add(i);
                }
            }
        }

        for(int i = 0; i < nc; i++){
            if (targetComputer.contains(i)) {
                int cnt = 0;
                for(int j = 0; j < ns; j++){
                    int link = input[GNCParams.Ibin_1_index + j * nc + i];
                    if(link == 1 || link == 49){
                        cnt++;
                    }
                }
                if(cnt == n){
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public String getName(){return "computerWithSpecificNumLinks";}
    
    @Override
    public String toString(){
        return "{computerWithSpecificNumLinks[" + Integer.toString(this.computer) + ";" + this.n + "]}";
    }
}
