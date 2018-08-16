/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filters;

import java.util.*;

import com.sun.xml.internal.rngom.parse.host.Base;
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
public class InOrbit extends AbstractFilter {

    protected Params params;
    protected int orbit;
    protected HashSet<Integer> instruments;

    protected List<String> orbitInstances;
    protected Map<Integer, List<String>> instrumentInstancesMap;
    
    public InOrbit(BaseParams params, int o, int instrument){
        super(params);
        this.params = (Params) params;
        this.orbit = o;
        this.instruments = new HashSet<>();
        this.instruments.add(instrument);
        initializeInstances();
    }
    
    public InOrbit(BaseParams params, int o, int[] instruments){
        super(params);
        this.params = (Params) params;
        this.orbit = o;
        this.instruments = new HashSet<>();
        for(int i:instruments){
            this.instruments.add(i);
        }
        initializeInstances();
    }

    public void initializeInstances(){

        if(this.orbit >= this.params.getNumOrbits()){
            if(this.params.generalizationEnabled()){
                String orbitClassName = this.params.getOrbitIndex2Name().get(this.orbit);
                List<OWLNamedIndividual> instanceList = this.params.getOntologyManager().getIndividuals("Orbit", orbitClassName);
                orbitInstances = new ArrayList<>();
                for(OWLNamedIndividual instance: instanceList){
                    orbitInstances.add(instance.getIRI().getShortForm());
                }
            }else{
                throw new IllegalStateException("Instrument specification out of range: " + this.orbit);
            }
        }else{
            orbitInstances = null;
        }

        instrumentInstancesMap = new HashMap<>();
        for(int instrument:instruments){
            if(instrument >= this.params.getNumInstruments()){
                if(this.params.generalizationEnabled()){
                    String instrumentClass = this.params.getInstrumentIndex2Name().get(instrument);
                    List<OWLNamedIndividual> instanceList = this.params.getOntologyManager().getIndividuals("Instrument", instrumentClass);
                    List<String> instanceNames = new ArrayList<>();
                    for(OWLNamedIndividual instance: instanceList){
                        instanceNames.add(instance.getIRI().getShortForm());
                    }
                    instrumentInstancesMap.put(instrument, instanceNames);
                }else{
                    throw new IllegalStateException("Instrument specification out of range: " + instrument);
                }
            }
        }
        if(instrumentInstancesMap.isEmpty()){
            instrumentInstancesMap = null;
        }
    }

    public int getOrbit(){
        return this.orbit;
    }

    public HashSet<Integer> getInstruments(){ return this.instruments; }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(BitSet input){
        boolean out = true;

        if(this.orbit >= this.params.getNumOrbits()){
            out = false;
            for(String orbitName: this.orbitInstances){
                int index = this.params.getInstrumentName2Index().get(orbitName);
                int[] tempInstruments = new int[this.instruments.size()];
                int ind = 0;
                for(int i: this.instruments){
                    tempInstruments[ind] = i;
                    ind++;
                }
                if((new InOrbit(this.params, index, tempInstruments)).apply(input)){
                    out = true;
                    break;
                }
            }
            return out;

        }else if(this.instrumentInstancesMap != null){
            for(int instrument: this.instruments){
                if(instrument >= this.params.getNumInstruments()){

                    out = false;
                    for(String instrumentName: this.instrumentInstancesMap.get(instrument)){
                        int instrumentIndex = this.params.getInstrumentName2Index().get(instrumentName);

                        int[] tempInstruments = new int[this.instruments.size()];
                        int ind = 0;
                        for(int inst: this.instruments){
                            if(inst != instrument){
                                tempInstruments[ind] = inst;
                                ind++;
                            }
                        }
                        tempInstruments[ind] = instrumentIndex;
                        if((new InOrbit(this.params, this.orbit, tempInstruments)).apply(input)){
                            out = true;
                            break;
                        }
                    }

                    return out;
                }
            }

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
