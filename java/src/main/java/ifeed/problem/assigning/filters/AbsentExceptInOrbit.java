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
 * Instrument i is absent, except when it is assigned to orbit o
 * @author bang
 */
public class AbsentExceptInOrbit extends AbstractGeneralizableFilter {

    protected Params params;
    protected Set<Integer> orbits;
    protected int instrument;

    protected Map<Integer, Set<Integer>> orbitInstancesMap;
    protected Set<Integer> instrumentInstances;

    public AbsentExceptInOrbit(BaseParams params, int orbit, int instrument){
        super(params);
        this.params = (Params) params;
        this.orbits = new HashSet<>();
        this.orbits.add(orbit);
        this.instrument = instrument;
        initializeInstances();
    }

    public AbsentExceptInOrbit(BaseParams params, Set<Integer> orbits, int instrument){
        super(params);
        this.params = (Params) params;
        this.orbits = orbits;
        this.instrument = instrument;
        initializeInstances();
    }

    public void initializeInstances(){
        orbitInstancesMap = new HashMap<>();
        for(int o: this.orbits){
            if(o >= this.params.getRightSetCardinality()){
                orbitInstancesMap.put(o, this.instantiateOrbitClass(o));
            }
        }
        if(this.instrument >= this.params.getLeftSetCardinality()){
            instrumentInstances = this.instantiateInstrumentClass(this.instrument);
        }else{
            instrumentInstances = null;
        }
    }

    public Set<Integer> getOrbits(){
        return this.orbits;
    }

    public int getInstrument(){ return this.instrument; }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(BitSet input){
        return apply(input, this.orbits, this.instrument);
    }

    public boolean apply(BitSet input, Set<Integer> orbits, int instrument){

        boolean out = true;
        if(instrument >= this.params.getLeftSetCardinality()){
            for(int instrumentIndex: this.instrumentInstances){
                if(this.apply(input, orbits, instrumentIndex)){
                    out = false;
                    break;
                }
            }
            return out;

        }else{
            Set<Integer> allowedOrbits = new HashSet<>();
            for(int o: orbits){
                allowedOrbits.add(o);
                if(o >= this.params.getRightSetCardinality()){
                    allowedOrbits.addAll(orbitInstancesMap.get(o));
                }
            }

            for(int o = 0; o < this.params.getRightSetCardinality(); o++){
                if(allowedOrbits.contains(o)){
                    continue;
                }else{
                    if(input.get(o * this.params.getLeftSetCardinality() + instrument)){
                        // If any one of the instruments is present in an orbit that is not the specified one
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
        sb.append("If instrument " + this.params.getLeftSetEntityName(this.instrument));
        sb.append(" exists, ");
        sb.append("it is assigned to ");
        if(this.orbits.size() == 1){
            sb.append("orbit " + this.params.getRightSetEntityName(this.orbits.iterator().next()));
        }else{
            StringJoiner sj = new StringJoiner(", ");
            for(int o: this.orbits){
                sj.add(this.params.getRightSetEntityName(o));
            }
            sb.append("any one of orbits {" + sj.toString() + "}");
        }
        return sb.toString();
    }

    @Override
    public String getName(){return "absentExceptInOrbit";}

    @Override
    public String toString(){
        StringJoiner sj = new StringJoiner(",");
        for(int o: this.orbits){
            sj.add(Integer.toString(o));
        }
        return "{absentExceptInOrbit[" + sj.toString() + ";" + instrument + ";]}";
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + this.orbits.hashCode();
        hash = 31 * hash + this.instrument;
        hash = 31 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof AbsentExceptInOrbit){
            AbsentExceptInOrbit other = (AbsentExceptInOrbit) o;
            return this.orbits.hashCode() == other.getOrbits().hashCode() && this.instrument == other.getInstrument();
        }
        return false;
    }
}
