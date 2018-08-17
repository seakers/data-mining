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
public class Separate extends AbstractGeneralizableFilter {

    protected Params params;
    protected Multiset<Integer> instruments;

    protected Map<Integer, List<Integer>> instrumentInstancesMap;
    protected Set<Integer> restrictedInstrumentSet;

    public Separate(BaseParams params, int[] instruments){
        super(params);
        this.params = (Params) params;
        this.instruments = HashMultiset.create();
        for(int inst:instruments){
            this.instruments.add(inst);
        }
        initializeInstances();
    }

    public Separate(BaseParams params, Collection<Integer> instruments){
        this(params, Utils.intCollection2Array(instruments));
    }

    public Separate(BaseParams params, Collection<Integer> instruments, Set<Integer> restrictedInstrumentSet){
        this(params, Utils.intCollection2Array(instruments));
        this.restrictedInstrumentSet = restrictedInstrumentSet;
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
        restrictedInstrumentSet = new HashSet<>();
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
        boolean out = true;

        if(instrumentInstancesMap != null){
            for(int instrumentClass: this.instrumentInstancesMap.keySet()){
                for(int instrumentIndex: this.instrumentInstancesMap.get(instrumentClass)){

                    if(restrictedInstrumentSet.contains(instrumentIndex)){
                        continue;

                    }else{
                        restrictedInstrumentSet.add(instrumentIndex);
                        Multiset tempInstruments = HashMultiset.create();
                        boolean currentClassFound = false;
                        for(int inst: this.instruments){
                            if(inst == instrumentClass && !currentClassFound){
                                currentClassFound = true;
                                continue;
                            }else{
                                tempInstruments.add(inst);
                            }
                        }
                        tempInstruments.add(instrumentIndex);

                        if(!(new Separate(this.params, tempInstruments, restrictedInstrumentSet)).apply(input)){
                            out = false;
                            break;
                        }
                    }

                }
                if(!out){
                    break;
                }
            }
            return out;
        }
        else{
            for(int o = 0; o< this.params.getNumOrbits(); o++){
                boolean sep = true;
                boolean found = false;
                for(int i:instruments){
                    if(input.get(o* this.params.getNumInstruments() +i)){
                        if(found){
                            sep=false;
                            break;
                        }else{
                            found=true;
                        }
                    }
                }
                if(!sep){
                    out=false;
                    break;
                }
            }
        }
        return out;
    }
    
    @Override
    public String getName(){return "separate";}
    
    @Override
    public String toString(){
        StringJoiner sj = new StringJoiner(",");
        for(int i:this.instruments){
            sj.add(Integer.toString(i));
        }        
        return "{separate[;" + sj.toString() + ";]}";
    }

    @Override
    public int hashCode() {
        int hash = 11;
        hash = 79 * hash + Objects.hashCode(this.instruments);
        hash = 79 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Separate){
            Separate other = (Separate) o;
            return this.instruments.equals(other.getInstruments());
        }
        return false;
    }

}
