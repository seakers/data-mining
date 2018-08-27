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
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.Params;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 *
 * @author bang
 */
public class NotInOrbit extends AbstractGeneralizableFilter {

    protected Params params;
    protected int orbit;
    protected Multiset<Integer> instruments;

    protected List<Integer> orbitInstances;
    protected Map<Integer, List<Integer>> instrumentInstancesMap;

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
    }

    public int getOrbit(){ return this.orbit; }
    public Multiset<Integer> getInstruments(){ return this.instruments; }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public  boolean apply(BitSet input){
        return apply(input, new HashSet<>());
    }

    public boolean apply(BitSet input, Set<Multiset<Integer>> checkedInstrumentSet){
        boolean out = true;

        if(this.orbitInstances != null){
            for(int orbitIndex: this.orbitInstances){
                if(!(new NotInOrbit(this.params, orbitIndex, this.instruments)).apply(input)){
                    // If there is at least one case that does not satisfy the condition, return false
                    out = false;
                    break;
                }
            }
            return out;

        }else if(this.instrumentInstancesMap != null){
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
                                if(!(new NotInOrbit(this.params, this.orbit, tempInstruments)).apply(input, checkedInstrumentSet)){
                                    out = false;
                                    break;
                                }
                            }

                        }

                    }
                }
                if(!out){
                    break;
                }
            }
            return out;

        }else {
            for(int instr:instruments){
                if(input.get(orbit * this.params.getNumInstruments() + instr)){
                    // If any one of the instruments is present, return false
                    out = false;
                    break;
                }
            }
        }

        return out;
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
        hash = 23 * hash + Objects.hashCode(this.instruments);
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
