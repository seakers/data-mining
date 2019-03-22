/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filters;

import java.util.*;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.Params;

/**
 *
 * @author bang
 */
public class NumInstruments extends AbstractGeneralizableFilter {

    protected Params params;
    private int[] nBounds;
    private int orbit;
    private int instrument;
    protected Set<Integer> instrumentInstances;

    public NumInstruments(BaseParams params, int orbit, int instrument, int n){
        super(params);
        this.params = (Params) params;
        this.nBounds = new int[2];
        this.nBounds[0] = n;
        this.nBounds[1] = n;
        this.orbit = orbit;
        this.instrument = instrument;
        initializeInstances();
    }

    public NumInstruments(BaseParams params, int orbit, int instrument, int[] bounds){
        super(params);
        assert(bounds.length == 2);
        this.params = (Params) params;
        this.orbit = orbit;
        this.nBounds = bounds;
        this.instrument = instrument;
        initializeInstances();
    }

    public NumInstruments(BaseParams params, int n){
        super(params);
        this.params = (Params) params;
        this.nBounds = new int[2];
        this.nBounds[0] = n;
        this.nBounds[1] = n;
        this.orbit = -1;
        this.instrument = -1;
        initializeInstances();
    }

    public void initializeInstances(){
        if(this.instrument >= this.params.getLeftSetCardinality()){
            instrumentInstances = this.instantiateInstrumentClass(this.instrument);
        }else{
            instrumentInstances = null;
        }
    }

    public int getOrbit(){ return this.orbit; }
    public int[] getNBounds(){ return this.nBounds; }
    public int getInstrument(){ return this.instrument; }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(BitSet input){
        // Three cases
        //numOfInstruments[;i;j]
        //numOfInstruments[i;;j]
        //numOfInstruments[;;j]

        // Number of instruments in total
        // Number of instruments in an orbit
        // Number of a specific instrument in all orbits

        int count = 0;

        if(this.orbit >= this.params.getRightSetCardinality()){
            throw new IllegalStateException("Orbit argument cannot be a high-level class");
        }

        if(this.orbit > -1 && this.instrument > -1){

            if(this.instrument < this.params.getLeftSetCardinality()){
                throw new IllegalStateException("Instrument argument must be a high-level class");
            }

            for(int instrIndex: this.instrumentInstances){
                if(input.get(this.orbit * this.params.getLeftSetCardinality() + instrIndex)){
                    count++;
                }
            }

        } else if(this.orbit > -1){ // Number of instruments in an orbit

            for(int i = 0; i < this.params.getLeftSetCardinality(); i++){
                if(input.get(this.orbit * this.params.getLeftSetCardinality() + i)){
                    count++;
                }
            }

        }else if(this.instrument > -1){ // Number of a specific instrument

            if(this.instrument >= this.params.getLeftSetCardinality()){
                for(int o = 0; o < this.params.getRightSetCardinality(); o++){
                    for(int instrIndex: this.instrumentInstances){
                        if(input.get(o * this.params.getLeftSetCardinality() + instrIndex)){
                            count++;
                        }
                    }
                }

            }else{
                for(int o = 0; o < this.params.getRightSetCardinality(); o++){
                    if(input.get(o * this.params.getLeftSetCardinality() + this.instrument)){
                        count++;
                    }
                }
            }

        }else{

            // Number of instruments in total
            for(int o = 0; o < this.params.getRightSetCardinality(); o++){
                for(int i = 0; i < this.params.getLeftSetCardinality(); i++){
                    if(input.get(o * this.params.getLeftSetCardinality() + i)){
                        count++;
                    }
                }
            }
        }

        return count >= nBounds[0] && count <= nBounds[1];
    }

    @Override
    public String getDescription(){

        String nBoundStr;
        if(this.nBounds[0] == this.nBounds[1]){
            nBoundStr = "" + this.nBounds[0];
        }else{
            nBoundStr = "Between " + this.nBounds[0] + "~" + this.nBounds[1];
        }

        if(this.orbit > -1){
            return nBoundStr + " instruments are assigned to " + this.params.getRightSetEntityName(this.orbit);
        }else if(this.instrument > -1){
            return nBoundStr + " of " + this.params.getLeftSetEntityName(this.instrument) + " are used";
        }else{
            return nBoundStr + " instruments are used in total";
        }
    }

    @Override
    public String getName(){return "numInstruments";}

    @Override
    public String toString(){

        String nBoundStr;
        if(this.nBounds[0] == this.nBounds[1]){
            nBoundStr = "" + this.nBounds[0];
        }else{
            nBoundStr = this.nBounds[0] + "," + this.nBounds[1];
        }
        if(this.orbit > -1){
            return "{numInstruments[" + this.orbit + ";;" + nBoundStr + "]}";
        }else if(this.instrument > -1){
            return "{numInstruments[;" + this.instrument + ";" + nBoundStr + "]}";
        }else{
            return "{numInstruments[;;" + nBoundStr + "]}";
        }
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 19 * hash + Objects.hashCode(this.orbit);
        hash = 19 * hash + Objects.hashCode(this.nBounds);
        hash = 19 * hash + Objects.hashCode(this.instrument);
        hash = 19 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof NumInstruments){
            NumInstruments other = (NumInstruments) o;
            return this.orbit == other.getOrbit() && this.instrument == other.getInstrument() && this.nBounds == other.getNBounds();
        }
        return false;
    }

}
