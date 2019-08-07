/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filtersWithException;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.filters.NotInOrbit;
import java.util.*;

/**
 *
 * @author bang
 */
public class NotInOrbitWithException extends NotInOrbit {

    protected Set<Integer> orbitException;
    protected Set<Integer> instrumentException;

    public NotInOrbitWithException(BaseParams params, int orbit, int[] instruments, Set<Integer> orbitException, Set<Integer> instrumentException){
        super(params, orbit, instruments);
        if(!orbitException.isEmpty() && !instrumentException.isEmpty()){
            throw new IllegalStateException("Orbit and instrument exceptions cannot be used at the same time");
        }
        this.setOrbitException(orbitException);
        this.setInstrumentException((instrumentException));
    }

    public NotInOrbitWithException(BaseParams params, int orbit, int instrument, Set<Integer> orbitException, Set<Integer> instrumentException){
        super(params, orbit, instrument);
        if(!orbitException.isEmpty() && !instrumentException.isEmpty()){
            throw new IllegalStateException("Orbit and instrument exceptions cannot be used at the same time");
        }
        this.setOrbitException(orbitException);
        this.setInstrumentException((instrumentException));
    }

    public NotInOrbitWithException(BaseParams params, int orbit, Collection<Integer> instruments, Set<Integer> orbitException, Set<Integer> instrumentException){
        super(params, orbit, Utils.intCollection2Array(instruments));
        if(!orbitException.isEmpty() && !instrumentException.isEmpty()){
            throw new IllegalStateException("Orbit and instrument exceptions cannot be used at the same time");
        }
        this.setOrbitException(orbitException);
        this.setInstrumentException((instrumentException));
    }

    public void setOrbitException(Set<Integer> exception){
        if(!exception.isEmpty()){
            if(!super.isOrbitClass(super.orbit)) {
                throw new IllegalStateException("Instrument class should be given as an argument to set instrument exceptions");
            }
        }

        if(exception.size() >= (double) super.params.getRightSetCardinality() / 2){
            throw new IllegalStateException("The number of exceptions should be smaller than half the number of the valid instances");
        }
        this.orbitException = exception;
    }

    public void setInstrumentException(Set<Integer> exception){
        if(!exception.isEmpty()){
            boolean generalizedVariableFound = false;
            for(int i: super.instruments){
                if(super.isInstrumentClass(i)) {
                    generalizedVariableFound = true;
                }
            }
            if(!generalizedVariableFound){
                throw new IllegalStateException("Instrument class should be given as an argument to set instrument exceptions");
            }
        }
        this.instrumentException = exception;
    }

    public void initializeInstances(){
        if(this.orbit >= this.params.getRightSetCardinality()){
            orbitInstances = this.instantiateOrbitClass(this.orbit);
        }else{
            orbitInstances = null;
        }

        this.instrumentInstancesMap = new HashMap<>();
        for(int instrument: instruments){
            if(instrument >= this.params.getLeftSetCardinality()){
                instrumentInstancesMap.put(instrument, this.instantiateInstrumentClass(instrument));
            }
        }

        if(instrumentInstancesMap.isEmpty()){
            instrumentInstancesMap = null;
        }
    }

    public int getOrbit(){ return this.orbit; }
    public Multiset<Integer> getInstruments(){ return this.instruments; }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public  boolean apply(BitSet input){
        return apply(input, this.orbit, this.instruments, new HashSet<>());
    }

    public boolean apply(BitSet input, int orbit, Multiset<Integer> instruments, Set<Integer> checkedInstrumentSet){

        if(orbit >= this.params.getRightSetCardinality()){
            boolean out = true;
            for(int orbitIndex: this.orbitInstances){
                if(!this.apply(input, orbitIndex, instruments, new HashSet<>())){
                    // If there is at least one case that does not satisfy the condition, return false
                    out = false;
                    break;
                }
            }
            return out;

        }else{
            boolean generalization_used = false;
            boolean out = true;

            for(int instrument: instruments){
                if(instrument >= this.params.getLeftSetCardinality()){
                    int instrumentClass = instrument;
                    generalization_used = true;

                    Multiset<Integer> tempInstruments = HashMultiset.create();
                    boolean classIndexSkipped = false;
                    for(int i: instruments){
                        if(i == instrumentClass && !classIndexSkipped){
                            classIndexSkipped = true;
                        }else{
                            tempInstruments.add(i);
                        }
                    }

                    for(int instrumentIndex: this.instrumentInstancesMap.get(instrumentClass)){

                        if(instruments.contains(instrumentIndex)){
                            // Skip to avoid repeated instruments
                            continue;

                        } else {
                            tempInstruments.add(instrumentIndex);

                            if(!checkedInstrumentSet.contains(Utils.getMultisetHashCode(tempInstruments))){
                                checkedInstrumentSet.add(Utils.getMultisetHashCode(tempInstruments));
                                if(!this.apply(input, orbit, tempInstruments, checkedInstrumentSet)){
                                    out = false;
                                    break;
                                }
                            }
                            tempInstruments.remove(instrumentIndex);
                        }

                    }
                }
                if(!out){
                    break;
                }
            }

            if(generalization_used){
                return out;

            }else{
                out = true;
                for(int instr: instruments){

                    if(!this.orbitException.isEmpty() && !this.instrumentException.isEmpty()){
                        if(this.orbitException.contains(orbit) && this.instrumentException.contains(instr)){
                            continue;
                        }
                    }else if(this.orbitException.contains(orbit)){ // instrumentException is empty
                        continue;
                    }else if(this.instrumentException.contains(instr)){ // orbitException is empty
                        continue;
                    }

                    if(input.get(orbit * this.params.getLeftSetCardinality() + instr)){
                        // If any one of the instruments is present, return false
                        out = false;
                        break;
                    }
                }
                return out;
            }
        }
    }

    @Override
    public String getDescription(){
        StringJoiner instrumentNames = new StringJoiner(", ");
        for(int instr: this.instruments){
            instrumentNames.add(this.params.getLeftSetEntityName(instr));
        }

        boolean singleInstrument;
        if(this.instruments.size() == 1){
            singleInstrument = true;
        }else{
            singleInstrument = false;
        }

        StringBuilder out = new StringBuilder();
        if(singleInstrument){
            out.append("Instrument " + instrumentNames.toString() + " is ");
        }else{
            out.append("Instruments {" + instrumentNames.toString() + "} are ");
        }
        out.append("not assigned to orbit " + this.params.getRightSetEntityName(this.orbit));

        if(!this.orbitException.isEmpty()){
            out.append(", except when ");
            if(singleInstrument){
                out.append("it is assigned to ");
            }else{
                out.append("they are assigned to ");
            }

            if(this.orbitException .size() == 1){
                out.append("orbit " + this.params.getRightSetEntityName(this.orbitException.iterator().next()));
            }else{
                StringJoiner sj = new StringJoiner(", ");
                for(int o: this.orbitException){
                    sj.add(this.params.getRightSetEntityName(o));
                }
                out.append("orbits {" + sj.toString() + "}");
            }

        } else if(!this.instrumentException.isEmpty()){
            out.append(", except for ");

            if(this.instrumentException .size() == 1){
                out.append("instrument " + this.params.getLeftSetEntityName(this.instrumentException.iterator().next()));
            }else{
                StringJoiner sj = new StringJoiner(", ");
                for(int i: this.instrumentException){
                    sj.add(this.params.getLeftSetEntityName(i));
                }
                out.append("the instruments {" + sj.toString() + "}");
            }
        }

        return out.toString();
    }

    @Override
    public String getName(){return "notInOrbit_except";}

    @Override
    public String toString(){
        StringJoiner sj = new StringJoiner(",");
        for(int instr:this.instruments){
            sj.add(Integer.toString(instr));
        }
        StringJoiner orbitExceptionString = new StringJoiner(",");
        for(int o: this.orbitException){
            orbitExceptionString.add(Integer.toString(o));
        }
        StringJoiner instrumentExceptionString = new StringJoiner(",");
        for(int i: this.instrumentException){
            instrumentExceptionString.add(Integer.toString(i));
        }
        return "{notInOrbit[" + orbit + ";" + sj.toString() + ";]except["+ orbitExceptionString.toString() +";"+ instrumentExceptionString.toString() +";]}";
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 23 * hash + this.orbit;
        hash = 23 * hash + Utils.getMultisetHashCode(this.instruments);
        hash = 23 * hash + Objects.hashCode(this.orbitException);
        hash = 23 * hash + Objects.hashCode(this.instrumentException);
        hash = 23 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof NotInOrbitWithException){
            NotInOrbitWithException other = (NotInOrbitWithException) o;
            if(this.orbit != other.getOrbit()) return false;
            if(this.instruments.equals(other.getInstruments())) return false;
            if(this.orbitException.equals(other.orbitException)) return false;
            if(this.instrumentException.equals(other.instrumentException)) return false;
            return true;
        }
        return false;
    }
}
