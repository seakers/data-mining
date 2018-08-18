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

    protected Map<Integer, List<Integer>> instrumentInstancesMap;
    protected Set<Multiset<Integer>> checkedInstrumentSet;
    
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

    public Together(BaseParams params, Collection<Integer> instruments, Set<Multiset<Integer>> checkedInstrumentSet){
        this(params, Utils.intCollection2Array(instruments));
        this.checkedInstrumentSet = checkedInstrumentSet;
    }

    public void initializeInstances(){

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

    public Multiset<Integer> getInstruments() {
        return instruments;
    }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(BitSet input){
        boolean out = false;

        if(this.instrumentInstancesMap != null){
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
                                if((new Together(this.params, tempInstruments, checkedInstrumentSet)).apply(input)){
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
            for(int o = 0; o < this.params.getNumOrbits(); o++){
                boolean sat = true;
                for(int i:instruments){
                    if(!input.get(o * this.params.getNumInstruments() + i)){
                        // If any one of the instruments are not present
                        sat = false;
                        break;
                    }
                }
                if(sat){
                    out=true;
                    break;
                }
            }
        }
        return out;
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
        hash = 43 * hash + Objects.hashCode(this.instruments);
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
