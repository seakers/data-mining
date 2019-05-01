/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filters;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.Params;

import java.util.*;

/**
 * Instruments in instrument class A are not assigned to orbit o, except for a,
 * where a is an instance of class A
 * @author bang
 */
public class NotInOrbitExceptInstrument extends AbstractGeneralizableFilter {

    protected Params params;
    protected int orbit;
    protected int instrumentClass;
    protected Set<Integer> instrumentExceptions;
    protected Set<Integer> orbitInstances;
    protected Set<Integer> instrumentInstances;

    public NotInOrbitExceptInstrument(BaseParams params, int orbit, int instrumentClass, int instrumentException){
        super(params);
        this.params = (Params) params;
        this.orbit = orbit;
        if(instrumentClass < this.params.getLeftSetCardinality()){
            throw new IllegalStateException("Instrument class should be given. Given: " + instrumentClass);
        }
        if(instrumentException >= this.params.getLeftSetCardinality()){
            throw new IllegalStateException("Instrument instance should be given. Given: " + instrumentException);
        }
        this.instrumentClass = instrumentClass;
        this.instrumentExceptions = new HashSet<>();
        this.instrumentExceptions.add(instrumentException);
        initializeInstances();
    }

    public NotInOrbitExceptInstrument(BaseParams params, int orbit, int instrumentClass, Set<Integer> instrumentExceptions){
        super(params);
        this.params = (Params) params;
        this.orbit = orbit;
        if(instrumentClass < this.params.getLeftSetCardinality()){
            throw new IllegalStateException("Instrument class should be given. Given: " + instrumentClass);
        }
        for(int i: instrumentExceptions){
            if(i >= this.params.getLeftSetCardinality()){
                throw new IllegalStateException("Instrument instance should be given. Given: " + i);
            }
        }
        this.instrumentClass = instrumentClass;
        this.instrumentExceptions = instrumentExceptions;
        initializeInstances();
    }

    public void initializeInstances(){
        if(this.orbit >= this.params.getRightSetCardinality()){
            orbitInstances = this.instantiateOrbitClass(this.orbit);
        }else{
            orbitInstances = null;
        }
        if(this.instrumentClass >= this.params.getLeftSetCardinality()){
            instrumentInstances = this.instantiateInstrumentClass(this.instrumentClass);
        }else{
            instrumentInstances = null;
        }
    }

    public int getOrbit(){
        return this.orbit;
    }

    public int getInstrumentClass(){ return this.instrumentClass; }

    public Set<Integer> getInstrumentExceptions(){ return this.instrumentExceptions; }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(BitSet input){
        return apply(input, this.orbit, this.instrumentClass, this.instrumentExceptions);
    }

    public boolean apply(BitSet input, int orbit, int instrumentClass, Set<Integer> instrumentExceptions){

        // Instruments in instrument class A are not assigned to orbit o, except for a,
        // where a is an instance of class A

        if(orbit >= this.params.getRightSetCardinality()){
            boolean out = true;
            for(int orbitIndex: this.orbitInstances){
                if(this.apply(input, orbitIndex, instrumentClass, instrumentExceptions)){
                    // If there is at least one counterexample, then return false
                    out = false;
                    break;
                }
            }
            return out;
        }else {
            boolean out = true;
            for(int instr: instrumentInstances){
                if(instrumentExceptions.contains(instr)){
                    continue;
                }else{
                    if(input.get(orbit * this.params.getLeftSetCardinality() + instr)){
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
        sb.append("Instruments in class " + this.params.getLeftSetEntityName(this.instrumentClass));
        sb.append(" are not assigned to" );
        sb.append(" orbit " + this.params.getRightSetEntityName(this.orbit));
        sb.append(" except for ");
        if(this.instrumentExceptions.size() == 1){
            sb.append(this.params.getLeftSetEntityName(this.instrumentExceptions.iterator().next()));
        }else{
            StringJoiner sj = new StringJoiner(", ");
            for(int i: instrumentExceptions){
                sj.add(this.params.getLeftSetEntityName(i));
            }
            sb.append("any one of instruments {" + sj.toString() + "}");
        }
        return sb.toString();
    }

    @Override
    public String getName(){return "notInOrbitExceptInstrument";}

    @Override
    public String toString(){
        StringJoiner sj = new StringJoiner(",");
        for(int i: this.instrumentExceptions){
            sj.add(Integer.toString(i));
        }
        return "{notInOrbitExceptInstrument[" + orbit + ";" + this.instrumentClass + "," + sj.toString() + ";]}";
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + this.orbit;
        hash = 31 * hash + this.instrumentClass;
        hash = 31 * hash + this.instrumentExceptions.hashCode();
        hash = 31 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof NotInOrbitExceptInstrument){
            NotInOrbitExceptInstrument other = (NotInOrbitExceptInstrument) o;
            return this.orbit == other.getOrbit()
                    && this.instrumentClass == other.getInstrumentClass()
                    && this.instrumentExceptions.hashCode() == other.getInstrumentExceptions().hashCode();
        }
        return false;
    }

}
