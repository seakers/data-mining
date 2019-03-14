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
public class Together extends AbstractGeneralizableFilter {

    protected Params params;
    protected Multiset<Integer> instruments;

    protected Map<Integer, Set<Integer>> instrumentInstancesMap;

    public Together(BaseParams params, int[] instruments){
        super(params);
        this.params = (Params) params;
        this.instruments = HashMultiset.create();
        for(int i:instruments){
            this.instruments.add(i);
        }
        initializeInstances();
    }

    public Together(BaseParams params, Collection<Integer> instruments){
        this(params, Utils.intCollection2Array(instruments));
    }

    public void initializeInstances(){
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
        boolean generalization_used = false;
        boolean out = false;
        for(int instrument: instruments){
            if(instrument >= this.params.getLeftSetCardinality()){
                generalization_used = true;
                int instrumentClass = instrument;

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
                            if(this.apply(input, tempInstruments, checkedInstrumentSet)){
                                out = true;
                                break;
                            }
                        }
                        tempInstruments.remove(instrumentIndex);
                    }
                }
            }
            if(out){
                break;
            }
        }

        if(generalization_used){
            return out;
        }
        else{
            out = false;
            for(int o = 0; o < this.params.getRightSetCardinality(); o++){
                boolean sat = true;
                for(int i:instruments){
                    if(!input.get(o * this.params.getLeftSetCardinality() + i)){
                        // If any one of the instruments are not present
                        sat = false;
                        break;
                    }
                }
                if(sat){
                    out = true;
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
        return "Instruments " + instrumentNames.toString() + " are assigned to the same orbit";
    }
    
    @Override
    public String getName(){return "together";}
    
    @Override
    public String toString(){
        StringJoiner sj = new StringJoiner(",");
        for(int i:instruments){
            sj.add(Integer.toString(i));
        }        
        return "{together[;" + sj.toString() + ";]}";
    }

    @Override
    public int hashCode() {
        int hash = 11;
        hash = 43 * hash + Utils.getMultisetHashCode(this.instruments);
        hash = 43 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Together){
            Together other = (Together) o;
            return this.instruments.equals(other.getInstruments());
        }
        return false;
    }


}
