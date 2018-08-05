/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filters;

import java.util.BitSet;
import java.util.Objects;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.Params;

/**
 *
 * @author bang
 */
public class NumOfInstruments extends AbstractFilter {

    protected Params params;
    private int num;
    private int orb;
    private int instr;

    public NumOfInstruments(BaseParams params, int orb, int instr, int n){
        super(params);
        this.params = (Params) params;
        this.orb = orb;
        this.num = n;
        this.instr = instr;
    }

    public int getOrb(){ return this.orb; }
    public int getNum(){ return this.num; }
    public int getInstr(){ return this.instr; }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
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
            for(int i = 0; i < this.params.getNumInstruments(); i++){
                if(input.get(this.orb * this.params.getNumInstruments() + i)){
                    count++;
                }
            }
        }else if(this.instr > -1){
            // Number of a specific instrument
            for(int o = 0; o < this.params.getNumOrbits(); o++){
                if(input.get(o * this.params.getNumInstruments() + this.instr)){
                    count++;
                }
            }
        }else{
            // Number of instruments in total
            for(int o = 0; o < this.params.getNumOrbits(); o++){
                for(int i = 0; i < this.params.getNumInstruments(); i++){
                    if(input.get(o * this.params.getNumInstruments() + i)){
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

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 19 * hash + Objects.hashCode(this.orb);
        hash = 19 * hash + Objects.hashCode(this.num);
        hash = 19 * hash + Objects.hashCode(this.instr);
        hash = 19 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof NumOfInstruments){
            NumOfInstruments other = (NumOfInstruments) o;
            return this.orb == other.getOrb() && this.instr == other.getInstr() && this.num == other.getNum();
        }
        return false;
    }

}
