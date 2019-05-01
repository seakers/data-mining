/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filters;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.Params;

import java.util.BitSet;
import java.util.Objects;
import java.util.Set;

/**
 * Instrument i is not assigned to orbit class O, except for o,
 * where o is an instance of class O
 * @author bang
 */
public class NotInOrbitExceptOrbit extends AbstractGeneralizableFilter {

    protected Params params;
    protected int orbitClass;
    protected int orbitException;
    protected int instrument;

    protected Set<Integer> orbitInstances;
    protected Set<Integer> instrumentInstances;

    public NotInOrbitExceptOrbit(BaseParams params, int orbitClass, int orbitException, int instrument){
        super(params);
        this.params = (Params) params;
        this.instrument = instrument;
        if(orbitClass < this.params.getRightSetCardinality()){
            throw new IllegalStateException("Orbit class should be given. Given: " + orbitClass);
        }
        if(orbitException >= this.params.getRightSetCardinality()){
            throw new IllegalStateException("Orbit instance should be given. Given: " + orbitException);
        }
        this.orbitClass = orbitClass;
        this.orbitException = orbitException;
        initializeInstances();
    }

    public void initializeInstances(){
        if(this.orbitClass >= this.params.getRightSetCardinality()){
            orbitInstances = this.instantiateOrbitClass(this.orbitClass);
        }else{
            orbitInstances = null;
        }
        if(this.instrument >= this.params.getLeftSetCardinality()){
            instrumentInstances = this.instantiateInstrumentClass(this.instrument);
        }else{
            instrumentInstances = null;
        }
    }

    public int getOrbitClass(){
        return this.orbitClass;
    }

    public int getOrbitException(){ return this.orbitException; }

    public int getInstrument(){ return this.instrument; }


    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(BitSet input){
        return apply(input, this.orbitClass, this.orbitException, this.instrument);
    }

    public boolean apply(BitSet input, int orbitClass, int orbitException, int instrument){

        // Instrument i is not assigned to orbit class O, except for o,
        // where o is an instance of class O
        if(instrument >= this.params.getLeftSetCardinality()){
            boolean out = true;
            for(int instrIndex: this.instrumentInstances){
                if(this.apply(input, orbitClass, orbitException, instrIndex)){
                    // If there is at least one counterexample, then return false
                    out = false;
                    break;
                }
            }
            return out;

        }else {
            boolean out = true;
            for(int orbit: orbitInstances){
                if(orbit == orbitException){
                    continue;
                }else{
                    if(input.get(orbit * this.params.getLeftSetCardinality() + instrument)){
                        // If the instrument is assigned to the specified orbit, return false
                        out = false;
                        break;
                    }
                }
            }
            return out;
        }
    }

    @Override
    public String getDescription(){
        StringBuilder sb = new StringBuilder();
        sb.append("Instrument " + this.params.getLeftSetEntityName(this.instrument));
        sb.append(" is not assigned to" );
        sb.append(" any of the orbits in class " + this.params.getRightSetEntityName(this.orbitClass));
        sb.append(" except for ");
        sb.append(this.params.getRightSetEntityName(this.orbitException));
        return sb.toString();
    }

    @Override
    public String getName(){return "notInOrbitExceptOrbit";}

    @Override
    public String toString(){
        return "{notInOrbitExceptOrbit[" + this.orbitClass + "," + this.orbitException + ";" + this.instrument + ";]}";
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + this.orbitClass;
        hash = 31 * hash + this.orbitException;
        hash = 31 * hash + this.instrument;
        hash = 31 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof NotInOrbitExceptOrbit){
            NotInOrbitExceptOrbit other = (NotInOrbitExceptOrbit) o;
            return this.orbitClass == other.getOrbitClass()
                    && this.orbitException == other.getOrbitException()
                    && this.instrument == other.getInstrument();
        }
        return false;
    }

}
