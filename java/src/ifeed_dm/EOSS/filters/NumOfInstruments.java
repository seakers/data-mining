/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS.filters;

import java.util.BitSet;
import ifeed_dm.Filter;
import ifeed_dm.EOSS.EOSSParams;

/**
 *
 * @author bang
 */
public class NumOfInstruments extends Filter {

    private final int num;
    private final int orb;
    private final int instr;

    public NumOfInstruments(int orb, int instr, int n){
        this.orb = orb;
        this.num = n;
        this.instr = instr;
    }

    @Override
    public boolean apply(BitSet input){
        // Three cases
        //numOfInstruments[;i;j]
        //numOfInstruments[i;;j]
        //numOfInstruments[;;i]

        // Number of instruments in total
        // Number of instruments in an orbit
        // Number of a specific instrument in all orbits

        int count = 0;
        if(this.orb > -1){
            // Number of instruments in an orbit
            for(int i = 0; i < EOSSParams.num_instruments; i++){
                if(input.get(this.orb * EOSSParams.num_instruments + i)){
                    count++;
                }
            }
        }else if(this.instr > -1){
            // Number of a specific instrument
            for(int o = 0; o < EOSSParams.num_orbits; o++){
                if(input.get(o * EOSSParams.num_instruments + this.instr)){
                    count++;
                }
            }
        }else{
            // Number of instruments in total
            for(int o = 0; o < EOSSParams.num_orbits; o++){
                for(int i = 0; i < EOSSParams.num_instruments; i++){
                    if(input.get(o * EOSSParams.num_instruments + i)){
                        count++;
                    }
                }
            }
        }

        return count == num;
    }

    @Override
    public String getName(){return "numOfInstruments";}

    @Override
    public String toString(){
        if(this.orb > -1){
            return "{numOfInstruments[" + this.orb + ";;" + num + "]}";
        }else if(this.instr > -1){
            return "{numOfInstruments[;" + this.instr + ";" + num + "]}";
        }else{
            return "{numOfInstruments[;;" + num + "]}";
        }
    }
}
