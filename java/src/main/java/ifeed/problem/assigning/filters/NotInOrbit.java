/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filters;

import java.util.*;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.Params;

/**
 *
 * @author bang
 */
public class NotInOrbit extends AbstractGeneralizableFilter {

    protected Params params;
    protected int orbit;
    protected Multiset<Integer> instruments;

    protected Set<Integer> orbitInstances;
    protected Map<Integer, Set<Integer>> instrumentInstancesMap;

    public NotInOrbit(BaseParams params, int o, int instrument){
        super(params);
        this.orbit = o;
        this.instruments = HashMultiset.create();
        instruments.add(instrument);
        this.params = (Params) params;
        initializeInstances();
    }    
   
    public NotInOrbit(BaseParams params, int o, int[] instruments){
        super(params);
        this.orbit = o;
        this.instruments = HashMultiset.create();
        for(int i:instruments){
            this.instruments.add(i);
        }
        this.params = (Params) params;
        initializeInstances();
    }

    public NotInOrbit(BaseParams params, int o, Collection<Integer> instruments){
        this(params, o, Utils.intCollection2Array(instruments));
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
                for(int instr:instruments){
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
    public String getName(){return "notInOrbit";}    
    
    @Override
    public String toString(){
        StringJoiner sj = new StringJoiner(",");
        for(int instr:this.instruments){
            sj.add(Integer.toString(instr));
        }
        return "{notInOrbit[" + orbit + ";" + sj.toString() + ";]}";
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 23 * hash + this.orbit;
        hash = 23 * hash + Utils.getMultisetHashCode(this.instruments);
        hash = 23 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof NotInOrbit){
            NotInOrbit other = (NotInOrbit) o;
            return this.orbit == other.getOrbit() && this.instruments.equals(other.getInstruments());
        }
        return false;
    }
    
}
