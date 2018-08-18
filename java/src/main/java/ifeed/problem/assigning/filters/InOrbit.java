/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filters;

import java.util.*;

import ifeed.Utils;
import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.Params;
import com.google.common.collect.Multiset;
import com.google.common.collect.HashMultiset;

/**
 *
 * @author bang
 */
public class InOrbit extends AbstractGeneralizableFilter {

    protected Params params;
    protected int orbit;
    protected Multiset<Integer> instruments;

    protected List<Integer> orbitInstances;
    protected Map<Integer, List<Integer>> instrumentInstancesMap;
    protected Set<Multiset<Integer>> checkedInstrumentSet;
    
    public InOrbit(BaseParams params, int o, int instrument){
        super(params);
        this.params = (Params) params;
        this.orbit = o;
        this.instruments = HashMultiset.create();
        this.instruments.add(instrument);
        initializeInstances();
    }
    
    public InOrbit(BaseParams params, int o, int[] instruments){
        super(params);
        this.params = (Params) params;
        this.orbit = o;
        this.instruments = HashMultiset.create();
        for(int i:instruments){
            this.instruments.add(i);
        }
        initializeInstances();
    }

    public InOrbit(BaseParams params, int o, Collection<Integer> instruments){
        this(params, o, Utils.intCollection2Array(instruments));
    }

    public InOrbit(BaseParams params, int o, Collection<Integer> instruments, Set<Multiset<Integer>> checkedInstrumentSet){
        this(params, o, Utils.intCollection2Array(instruments));
        this.checkedInstrumentSet = checkedInstrumentSet;
    }

    public void initializeInstances(){

        if(this.orbit >= this.params.getNumOrbits()){
            orbitInstances = this.instantiateOrbitClass(this.orbit);
        }else{
            orbitInstances = null;
        }

        Multiset<Integer> instrumentClassIndices = HashMultiset.create();
        for(int instrument: instruments){
            if(instrument >= this.params.getNumInstruments()){
                instrumentClassIndices.add(instrument);
            }
        }
        instrumentInstancesMap = this.instantiateInstrumentClass(instrumentClassIndices);

        if(instrumentClassIndices.isEmpty()){
            instrumentInstancesMap = null;
        }
        checkedInstrumentSet = new HashSet<>();
    }

    public int getOrbit(){
        return this.orbit;
    }

    public Multiset<Integer> getInstruments(){ return this.instruments; }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(BitSet input){
        boolean out = true;

        if(this.orbitInstances != null){
            out = false;
            for(int orbitIndex: this.orbitInstances){
                if((new InOrbit(this.params, orbitIndex, this.instruments)).apply(input)){
                    // If there is at least one case that satisfies the condition, return true
                    out = true;
                    break;
                }
            }
            return out;

        }else if(this.instrumentInstancesMap != null){

            out = false;
            for(int instrument: this.instruments){
                if(instrument >= this.params.getNumInstruments()){
                    int instrumentClass = instrument;
                    for(int instrumentIndex: this.instrumentInstancesMap.get(instrumentClass)){

                        if(this.instruments.contains(instrumentIndex)){
                            // Skip to avoid repeated instruments
                            continue;

                        } else {
                            Multiset tempInstruments = HashMultiset.create(instruments);
                            tempInstruments.remove(instrumentClass);
                            tempInstruments.add(instrumentIndex);

                            if(checkedInstrumentSet.contains(tempInstruments)){
                                continue;

                            }else{
                                checkedInstrumentSet.add(tempInstruments);
                                if((new InOrbit(this.params, this.orbit, tempInstruments, checkedInstrumentSet)).apply(input)){
                                    out = true;
                                    break;
                                }
                            }

                        }

                    }
                }
                if(out){
                    break;
                }
            }
            return out;

        }else{
            for(int instr:this.instruments){
                if(!input.get(orbit* this.params.getNumInstruments() +instr)){
                    // If any one of the instruments are not present
                    out=false;
                    break;
                }
            }
        }

        return out;
    }
    
    @Override
    public String getName(){return "inOrbit";}    
    
    @Override
    public String toString(){
        StringJoiner sj = new StringJoiner(",");
        for(int instr:this.instruments){
            sj.add(Integer.toString(instr));
        }
        return "{inOrbit[" + orbit + ";" + sj.toString() + ";]}";
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + this.orbit;
        hash = 31 * hash + Objects.hashCode(this.instruments);
        hash = 31 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof InOrbit){
            InOrbit other = (InOrbit) o;
            return this.orbit == other.getOrbit() && this.instruments.equals(other.getInstruments());
        }
        return false;
    }
    
}
