/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filters;

import java.util.*;

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
public class Together extends AbstractFilter {

    protected Params params;
    protected HashSet<Integer> instruments;

    protected Map<Integer, List<String>> instrumentInstancesMap;
    
    public Together(BaseParams params, int[] instruments){
        super(params);
        this.params = (Params) params;
        this.instruments = new HashSet<>();
        for(int i:instruments){
            this.instruments.add(i);
        }
        initializeInstances();
    }

    public void initializeInstances(){
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

    public HashSet<Integer> getInstruments() {
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
                        if((new Together(this.params, tempInstruments)).apply(input)){
                            out = true;
                            break;
                        }
                    }

                    return out;
                }
            }

        }else{
            for(int o = 0; o < this.params.getNumOrbits(); o++){
                boolean sat = true;
                for(int i:instruments){
                    if(!input.get(o * this.params.getNumInstruments() + i)){
                        // If any one of the instruments are not present
                        sat=false;
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
