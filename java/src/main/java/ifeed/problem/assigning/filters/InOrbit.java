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

    public void initializeInstances(){

        if(this.orbit >= this.params.getNumOrbits()){
            orbitInstances = this.instantiateOrbitClass(this.orbit);
        }else{
            orbitInstances = null;
        }

        this.instrumentInstancesMap = new HashMap<>();
        for(int instrument: instruments){
            if(instrument >= this.params.getNumInstruments()){
                instrumentInstancesMap.put(instrument, this.instantiateInstrumentClass(instrument));
            }
        }

        if(instrumentInstancesMap.isEmpty()){
            instrumentInstancesMap = null;
        }
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
        return apply(input, this.orbit, this.instruments, new HashSet<>());
    }

    public boolean apply(BitSet input, int orbit, Multiset<Integer> instruments, Set<Integer> checkedInstrumentSet){

//        System.out.println(checkedInstrumentSet.size() + ", "+ instruments.toString() + ": " + Utils.getMultisetHashCode(instruments));

        if(orbit >= this.params.getNumOrbits()){
            boolean out = false;
            for(int orbitIndex: this.orbitInstances){
                if(this.apply(input, orbitIndex, instruments, new HashSet<>())){
                    // If there is at least one case that satisfies the condition, return true
                    out = true;
                    break;
                }
            }
            return out;

        }else {
            boolean generalization_used = false;
            boolean out = false;

            for(int instrument: instruments){
                if(instrument >= this.params.getNumInstruments()){
                    generalization_used = true;
                    int instrumentClass = instrument;

                    for(int instrumentIndex: this.instrumentInstancesMap.get(instrumentClass)){
                        if(instruments.contains(instrumentIndex)){
                            // Skip to avoid repeated instruments
                            continue;

                        } else {
                            Multiset<Integer> tempInstruments = HashMultiset.create();
                            boolean classIndexSkipped = false;
                            for(int i: instruments){
                                if(i == instrumentClass && !classIndexSkipped){
                                    classIndexSkipped = true;
                                }else{
                                    tempInstruments.add(i);
                                }
                            }
                            tempInstruments.add(instrumentIndex);

//                            System.out.println(tempInstruments.toString() + ": " + Utils.getMultisetHashCode(tempInstruments));
                            if(!checkedInstrumentSet.contains(Utils.getMultisetHashCode(tempInstruments))){
                                checkedInstrumentSet.add(Utils.getMultisetHashCode(tempInstruments));
                                if(this.apply(input, orbit, tempInstruments, checkedInstrumentSet)){
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

            if(generalization_used){
                return out;

            }else{
                out = true;
                for(int instr: instruments){
                    if(!input.get(orbit * this.params.getNumInstruments() + instr)){
                        // If any one of the instruments are not present
                        out = false;
                        break;
                    }
                }
                return out;
            }
        }
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
        hash = 31 * hash + Utils.getMultisetHashCode(this.instruments);
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
