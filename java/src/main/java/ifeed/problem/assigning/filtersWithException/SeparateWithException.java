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
import ifeed.problem.assigning.Params;
import ifeed.problem.assigning.filters.AbstractGeneralizableFilter;
import ifeed.problem.assigning.filters.Separate;

import java.util.*;

/**
 *
 * @author bang
 */
public class SeparateWithException extends Separate {

    protected Set<Integer> orbitException;
    protected Set<Integer> instrumentException;

    public SeparateWithException(BaseParams params, int[] instruments, Set<Integer> orbitException, Set<Integer> instrumentException){
        super(params, instruments);
        if(!orbitException.isEmpty() && !instrumentException.isEmpty()){
            throw new IllegalStateException("Orbit and instrument exceptions cannot be used at the same time");
        }
        this.setOrbitException(orbitException);
        this.setInstrumentException((instrumentException));
    }

    public SeparateWithException(BaseParams params, Collection<Integer> instruments, Set<Integer> orbitException, Set<Integer> instrumentException){
        super(params, Utils.intCollection2Array(instruments));
        if(!orbitException.isEmpty() && !instrumentException.isEmpty()){
            throw new IllegalStateException("Orbit and instrument exceptions cannot be used at the same time");
        }
        this.setOrbitException(orbitException);
        this.setInstrumentException((instrumentException));
    }

    public void setOrbitException(Set<Integer> exception){
        if(exception.size() >= (double) super.params.getRightSetCardinality() / 2){
            throw new IllegalStateException("The number of exceptions should be smaller than half the number of the valid instances");
        }
        this.orbitException = exception;
    }

    public void setInstrumentException(Set<Integer> exception){
        if(!exception.isEmpty()){
            boolean generalizedVariableFound = false;
            for(int i: super.instruments){
                if(!super.isInstrumentClass(i)) {
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
        this.instrumentInstancesMap = new HashMap<>();
        for(int instrument: this.instruments){
            if(instrument >= this.params.getLeftSetCardinality()){
                instrumentInstancesMap.put(instrument, this.instantiateInstrumentClass(instrument));
            }
        }
        if(instrumentInstancesMap.isEmpty()){
            instrumentInstancesMap = null;
        }
    }

    public Multiset<Integer> getInstruments() {
        return instruments;
    }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(BitSet input){
        return apply(input, this.instruments, new HashSet<>());
    }

    public boolean apply(BitSet input, Multiset<Integer> instruments, Set<Integer> checkedInstrumentSet){
        boolean out = true;
        boolean generalization_used = false;

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
                            if(!this.apply(input, tempInstruments, checkedInstrumentSet)){
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

        } else{
            out = true;
            for(int o = 0; o < this.params.getRightSetCardinality(); o++){
                boolean sep = true;
                boolean found = false;

                if(!this.orbitException.isEmpty() && this.instrumentException.isEmpty()){
                    // Apply orbit exception when instrumentException is empty.
                    // When both are used, the exception is applied as conjunction of both conditions
                    if(this.orbitException.contains(o)){
                        continue;
                    }
                }

                for(int i:instruments){
                    if(this.instrumentException.contains(i)){
                        if(this.orbitException.isEmpty()){
                            continue;
                        }else{
                            if(this.orbitException.contains(o)){
                                continue;
                            }
                        }
                    }

                    if(input.get(o * this.params.getLeftSetCardinality() + i)){
                        if(found){
                            sep = false;
                            break;
                        }else{
                            found = true;
                        }
                    }
                }
                if(!sep){
                    out = false;
                    break;
                }
            }
            return out;
        }
    }

    @Override
    public String getDescription(){
        StringJoiner instrumentNames = new StringJoiner(", ");
        for(int instr: this.instruments){
            instrumentNames.add(this.params.getLeftSetEntityName(instr));
        }

        StringBuilder out = new StringBuilder();
        out.append("Instruments {" + instrumentNames.toString() + "} are not assigned to the same orbit");

        if(!this.orbitException.isEmpty()){
            out.append(", except when they are assigned to ");

            if(this.orbitException .size() == 1){
                out.append("orbit " + this.params.getRightSetEntityName(this.orbitException.iterator().next()));
            }else{
                StringJoiner sj = new StringJoiner(", ");
                for(int o: this.orbitException){
                    sj.add(this.params.getRightSetEntityName(o));
                }
                out.append("one of the orbits {" + sj.toString() + "}");
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
    public String getName(){return "separate_except";}
    
    @Override
    public String toString(){
        StringJoiner sj = new StringJoiner(",");
        for(int i:this.instruments){
            sj.add(Integer.toString(i));
        }
        StringJoiner orbitExceptionString = new StringJoiner(",");
        for(int o: this.orbitException){
            orbitExceptionString.add(Integer.toString(o));
        }
        StringJoiner instrumentExceptionString = new StringJoiner(",");
        for(int i: this.instrumentException){
            instrumentExceptionString.add(Integer.toString(i));
        }
        return "{separate[;" + sj.toString() + ";]except["+ orbitExceptionString +";"+ instrumentExceptionString +";]}";
    }

    @Override
    public int hashCode() {
        int hash = 11;
        hash = 79 * hash + Utils.getMultisetHashCode(this.instruments);
        hash = 79 * hash + Objects.hashCode(this.getName());
        hash = 79 * hash + Objects.hashCode(this.orbitException);
        hash = 79 * hash + Objects.hashCode(this.instrumentException);
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof SeparateWithException){
            SeparateWithException other = (SeparateWithException) o;
            if(!this.instruments.equals(other.getInstruments())) return false;
            if(this.orbitException.equals(other.orbitException)) return false;
            if(this.instrumentException.equals(other.instrumentException)) return false;
            return true;
        }
        return false;
    }
}
