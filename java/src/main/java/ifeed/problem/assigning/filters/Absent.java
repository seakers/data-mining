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
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 *
 * @author bang
 */
public class Absent extends AbstractFilter {

    protected int instrument;
    protected Params params;
    protected List<String> instrumentInstances;

    public Absent(BaseParams params, int i){
        super(params);
        this.params = (Params) params;
        this.instrument = i;

        if(this.instrument >= this.params.getNumInstruments()){
            if(this.params.generalizationEnabled()){
                String instrumentClass = this.params.getInstrumentIndex2Name().get(this.instrument);
                List<OWLNamedIndividual> instanceList = this.params.getOntologyManager().getIndividuals("Instrument", instrumentClass);
                instrumentInstances = new ArrayList<>();
                for(OWLNamedIndividual instance: instanceList){
                    instrumentInstances.add(instance.getIRI().getShortForm());
                }
            }else{
                throw new IllegalStateException("Instrument specification out of range: " + this.instrument);
            }
        }else{
            instrumentInstances = null;
        }

    }

    public int getInstrument(){
        return this.instrument;
    }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(BitSet input){
        boolean out = true;

        if(this.instrument >= this.params.getNumInstruments()){
            for(String instrumentName: this.instrumentInstances){
                int index = this.params.getInstrumentName2Index().get(instrumentName);
                if(!(new Absent(this.params, index)).apply(input)){
                    out = false;
                    break;
                }
            }

        }else{
            for(int o = 0; o< this.params.getNumOrbits(); o++){
                if(input.get(o * this.params.getNumInstruments() + instrument)){
                    // If any one of the instruments are not present
                    out=false;
                    break;
                }
            }
        }

        return out;
    }

    @Override
    public String getName(){return "absent";}

    @Override
    public String toString(){
        return "{absent[;" + this.instrument + ";]}";
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 31 * hash + Objects.hashCode(this.instrument);
        hash = 31 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Absent){
            Absent other = (Absent) o;
            return this.instrument == other.getInstrument();
        }
        return false;
    }
}
