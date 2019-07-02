/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filters;

import java.util.*;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.BinaryInputArchitecture;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.Params;

/**
 *
 * @author bang
 */
public class NumInstruments extends AbstractGeneralizableFilter {

    protected Params params;
    private int[] nBounds;
    private int orbit;
    private int instrument;
    private Set<Integer> instruments;

    protected Set<Integer> orbitInstances;
    protected Map<Integer, Set<Integer>> instrumentInstanceMap;

    public NumInstruments(BaseParams params, int orbit, int instrument, int n){
        super(params);
        this.params = (Params) params;
        this.nBounds = new int[2];
        this.nBounds[0] = n;
        this.nBounds[1] = n;
        this.orbit = orbit;
        this.instrument = instrument;
        this.instruments = new HashSet<>();
        initializeInstances();
    }

    public NumInstruments(BaseParams params, int orbit, int instrument, int[] bounds){
        super(params);
        assert(bounds.length == 2);
        this.params = (Params) params;
        this.orbit = orbit;
        this.nBounds = bounds;
        this.instrument = instrument;
        this.instruments = new HashSet<>();
        initializeInstances();
    }

    public NumInstruments(BaseParams params, int orbit, Set<Integer> instruments, int n){
        super(params);
        this.params = (Params) params;
        this.nBounds = new int[2];
        this.nBounds[0] = n;
        this.nBounds[1] = n;
        this.orbit = orbit;
        this.instrument = -1;
        this.instruments = instruments;
        initializeInstances();
    }

    public NumInstruments(BaseParams params, int orbit, Set<Integer> instruments, int[] bounds){
        super(params);
        assert(bounds.length == 2);
        this.params = (Params) params;
        this.orbit = orbit;
        this.nBounds = bounds;
        this.instrument = -1;
        this.instruments = instruments;
        initializeInstances();
    }

    public NumInstruments(BaseParams params, int n){
        super(params);
        this.params = (Params) params;
        this.nBounds = new int[2];
        this.nBounds[0] = n;
        this.nBounds[1] = n;
        this.orbit = -1;
        this.instrument = -1;
        this.instruments = new HashSet<>();
        initializeInstances();
    }

    public void initializeInstances(){
        if(this.params.isGeneralizedConceptLeftSet(this.orbit)){
            this.orbitInstances = this.instantiateOrbitClass(this.orbit);
        }

        this.instrumentInstanceMap = new HashMap<>();
        if(this.instrument >= this.params.getLeftSetCardinality()){
            this.instrumentInstanceMap.put(this.instrument, this.instantiateInstrumentClass(this.instrument));
        }

        if(this.instruments != null){
            for(int instr: this.instruments){
                if(instr >= this.params.getLeftSetCardinality()){
                    this.instrumentInstanceMap.put(instr, this.instantiateInstrumentClass(instr));
                }
            }
        }
    }

    public int getOrbit(){ return this.orbit; }
    public int[] getNBounds(){ return this.nBounds; }
    public int getInstrument(){ return this.instrument; }
    public Set<Integer> getInstruments(){ return this.instruments; }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(BitSet input){
        int count = this.apply(input, this.orbit, this.instrument, this.instruments, new HashSet<>(), 0);
        return count >= nBounds[0] && count <= nBounds[1];
    }

    public int apply(BitSet input, int orbit, int instrument, Set<Integer> instruments, Set<Integer> checkedInstrumentSet, int count){
        // Possible 6 cases
        //numOfInstruments[;;n]: Number of instruments in total
        //numOfInstruments[;i;n]: Number of instrument i
        //numOfInstruments[;i,j,k;n]: Number of instruments in set {i, j, k} across all orbits
        //numOfInstruments[o;;n]: Number of instruments in orbit o
        //numOfInstruments[o;i;n]: Number of instrument i (instrument class) in orbit o
        //numOfInstruments[o;i,j,k;n]: Number of instruments in set {i, j, k} in orbit o

        if(orbit == -1){
            if(instrument == -1 && instruments.isEmpty()){ //numOfInstruments[;;n]: Number of instruments in total
                for(int o = 0; o < this.params.getRightSetCardinality(); o++){
                    for(int i = 0; i < this.params.getLeftSetCardinality(); i++){
                        if(input.get(o * this.params.getLeftSetCardinality() + i)){
                            count++;
                        }
                    }
                }

            }else if(instrument > -1 && instruments.isEmpty()){ //numOfInstruments[;i;n]: Number of instrument i
                if(this.params.isGeneralizedConceptLeftSet(instrument)){
                    instruments = new HashSet<>();
                    instruments.addAll(this.instrumentInstanceMap.get(instrument));
                    count = this.apply(input, orbit, -1, instruments, new HashSet<>(), count);
                } else {
                    for(int o = 0; o < this.params.getRightSetCardinality(); o++){
                        if(input.get(o * this.params.getLeftSetCardinality() + instrument)){
                            count++;
                        }
                    }
                }

            }else if(instrument == -1 && !instruments.isEmpty()){ //numOfInstruments[;i,j,k;n]: Number of instruments in set {i, j, k} across all orbits
                for(int instr: instruments){
                    if(this.params.isGeneralizedConceptLeftSet(instr)){
                        Set<Integer> tempInstrumentSet = new HashSet<>();
                        tempInstrumentSet.addAll(this.instrumentInstanceMap.get(instr));
                        count = this.apply(input, orbit, -1, tempInstrumentSet, new HashSet<>(), count);
                    } else {
                        count = this.apply(input, orbit, instr, new HashSet<>(), new HashSet<>(), count);
                    }
                }

            }else if(instrument > -1 && !instruments.isEmpty()){
                throw new IllegalStateException();
            }

        } else {
            if(this.params.isGeneralizedConceptRightSet(orbit)){
                for(int orbitIndex: this.orbitInstances){
                    count = this.apply(input, orbitIndex, instrument, instruments, new HashSet<>(), count);
                }
            } else{
                if(instrument == -1 && instruments.isEmpty()){ //numOfInstruments[o;;n]: Number of instruments in orbit o
                    for(int i = 0; i < this.params.getLeftSetCardinality(); i++){
                        if(input.get(orbit * this.params.getLeftSetCardinality() + i)){
                            count++;
                        }
                    }

                }else if(instrument > -1 && instruments.isEmpty()){ //numOfInstruments[o;i;n]: Number of instrument i (instrument class) in orbit o
                    if(this.params.isGeneralizedConceptLeftSet(instrument)){
                        instruments = new HashSet<>();
                        instruments.addAll(this.instrumentInstanceMap.get(instrument));
                        count = this.apply(input, orbit, -1, instruments, new HashSet<>(), count);
                    } else {
                        if(input.get(orbit * this.params.getLeftSetCardinality() + instrument)){
                            count++;
                        }
                    }

                }else if(instrument == -1 && !instruments.isEmpty()){ //numOfInstruments[o;i,j,k;n]: Number of instruments in set {i, j, k} in orbit o
                    for(int instr: instruments){
                        if(this.params.isGeneralizedConceptLeftSet(instr)){
                            Set<Integer> tempInstrumentSet = new HashSet<>();
                            tempInstrumentSet.addAll(this.instrumentInstanceMap.get(instr));
                            count = this.apply(input, orbit, -1, tempInstrumentSet, new HashSet<>(), count);
                        } else {
                            count = this.apply(input, orbit, instr, new HashSet<>(), new HashSet<>(), count);
                        }
                    }

                }else if(instrument > -1 && !instruments.isEmpty()){
                    throw new IllegalStateException();
                }

            }
        }
        return count;
    }

    @Override
    public String getDescription(){
        String nBoundStr;
        if(this.nBounds[0] == this.nBounds[1]){
            nBoundStr = "" + this.nBounds[0];
        }else{
            nBoundStr = "Between " + this.nBounds[0] + "~" + this.nBounds[1];
        }

        String out = "";
        if(orbit == -1){
            if(instrument == -1 && instruments.isEmpty()){ //numOfInstruments[;;n]: Number of instruments in total
                out = nBoundStr + " instruments are used in total";

            }else if(instrument > -1 && instruments.isEmpty()){ //numOfInstruments[;i;n]: Number of instrument i
                out = nBoundStr + " of " + this.params.getLeftSetEntityName(this.instrument) + " are used";

            }else if(instrument == -1 && !instruments.isEmpty()){ //numOfInstruments[;i,j,k;n]: Number of instruments in set {i, j, k} across all orbits
                StringJoiner instrumentNamesJoiner = new StringJoiner(", ");
                for(int instr: instruments){
                    instrumentNamesJoiner.add(this.params.getLeftSetEntityName(instr));
                }
                out = nBoundStr + " instruments out of the set {" +instrumentNamesJoiner.toString() + "} are used";

            }else if(instrument > -1 && !instruments.isEmpty()){
                throw new IllegalStateException();
            }
        } else {
            if(instrument == -1 && instruments.isEmpty()){ //numOfInstruments[o;;n]: Number of instruments in orbit o
                out = nBoundStr + " instruments are assigned to " + this.params.getRightSetEntityName(this.orbit);

            }else if(instrument > -1 && instruments.isEmpty()){ //numOfInstruments[o;i;n]: Number of instrument i (instrument class) in orbit o
                out = nBoundStr + " of " + this.params.getLeftSetEntityName(this.instrument) + " are assigned to " + this.params.getRightSetEntityName(this.orbit);

            }else if(instrument == -1 && !instruments.isEmpty()){ //numOfInstruments[o;i,j,k;n]: Number of instruments in set {i, j, k} in orbit o
                StringJoiner instrumentNamesJoiner = new StringJoiner(", ");
                for(int instr: instruments){
                    instrumentNamesJoiner.add(this.params.getLeftSetEntityName(instr));
                }
                out = nBoundStr + " instruments out of the set {" + instrumentNamesJoiner.toString() + "} are assigned to " + this.params.getRightSetEntityName(this.orbit);;

            }else if(instrument > -1 && !instruments.isEmpty()){
                throw new IllegalStateException();
            }
        }
        return out;
    }

    @Override
    public String getName(){return "numInstruments";}

    @Override
    public String toString(){
        String nBoundStr;
        if(this.nBounds[0] == this.nBounds[1]){
            nBoundStr = "" + this.nBounds[0];
        }else{
            nBoundStr = this.nBounds[0] + "," + this.nBounds[1];
        }

        StringBuilder out = new StringBuilder();
        StringJoiner args = new StringJoiner(";");
        out.append("{numInstruments[");
        if(orbit == -1){
            args.add(""); // empty orbit arg
            if(instrument == -1 && instruments.isEmpty()){ //numOfInstruments[;;n]: Number of instruments in total
                args.add(""); // empty instrument arg
            }else if(instrument > -1 && instruments.isEmpty()){ //numOfInstruments[;i;n]: Number of instrument i
                args.add(Integer.toString(instrument));
            }else if(instrument == -1 && !instruments.isEmpty()){ //numOfInstruments[;i,j,k;n]: Number of instruments in set {i, j, k} across all orbits
                StringJoiner instrumentArgJoiner = new StringJoiner(",");
                for(int instr: instruments){
                    instrumentArgJoiner.add(Integer.toString(instr));
                }
                args.add(instrumentArgJoiner.toString());
            }else if(instrument > -1 && !instruments.isEmpty()){
                throw new IllegalStateException();
            }
        } else {
            args.add(Integer.toString(orbit));
            if(instrument == -1 && instruments.isEmpty()){ //numOfInstruments[o;;n]: Number of instruments in orbit o
                args.add("");
            }else if(instrument > -1 && instruments.isEmpty()){ //numOfInstruments[o;i;n]: Number of instrument i (instrument class) in orbit o
                args.add(Integer.toString(instrument));
            }else if(instrument == -1 && !instruments.isEmpty()){ //numOfInstruments[o;i,j,k;n]: Number of instruments in set {i, j, k} in orbit o
                StringJoiner instrumentArgJoiner = new StringJoiner(",");
                for(int instr: instruments){
                    instrumentArgJoiner.add(Integer.toString(instr));
                }
                args.add(instrumentArgJoiner.toString());
            }else if(instrument > -1 && !instruments.isEmpty()){
                throw new IllegalStateException();
            }
        }
        args.add(nBoundStr);
        out.append(args.toString() + "]}");
        return out.toString();
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 19 * hash + Objects.hashCode(this.getName());
        hash = 19 * hash + Objects.hashCode(this.orbit);
        hash = 19 * hash + Objects.hashCode(this.nBounds[1]);
        hash = 19 * hash + Objects.hashCode(this.nBounds[0]);
        hash = 19 * hash + Objects.hashCode(this.instrument);
        hash = 19 * hash + Objects.hashCode(this.instruments);
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof NumInstruments){
            NumInstruments other = (NumInstruments) o;
            if(this.orbit != other.getOrbit()) return false;
            if(this.instrument != other.getInstrument()) return false;
            if(this.instruments == null){
                if(other.getInstruments() != null) return false;
            }else{
                if(!this.instruments.equals(other.getInstruments())) return false;
            }
            if(this.nBounds[0] != other.nBounds[0]) return false;
            if(this.nBounds[1] != other.nBounds[1]) return false;
            return true;
        }
        return false;
    }
}
