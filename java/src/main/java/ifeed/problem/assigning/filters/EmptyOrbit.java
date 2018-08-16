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
 *
 * @author bang
 */
public class EmptyOrbit extends AbstractFilter {
    
    protected int orbit;
    protected Params params;
    protected List<String> orbitInstances;

    public EmptyOrbit(BaseParams params, int o){
        super(params);
        this.params = (Params) params;
        this.orbit = o;

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
    }

    public int getOrbit(){ return this.orbit; }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((BinaryInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(BitSet input){
        
        boolean out = true; // empty

        if(this.orbit >= this.params.getNumOrbits()){
            for(String orbitName: this.orbitInstances){
                int index = this.params.getInstrumentName2Index().get(orbitName);
                if(!(new EmptyOrbit(this.params, index)).apply(input)){
                    out = false;
                    break;
                }
            }

        }else{
            for(int i = 0; i< this.params.getNumInstruments(); i++){
                if(input.get(orbit* this.params.getNumInstruments() +i)){
                    out=false; // instrument found inside the orbit
                    break;
                }
            }
        }
        return out;
    }
    
    @Override
    public String getName(){return "emptyOrbit";}    
    
    @Override
    public String toString(){
        return "{emptyOrbit[" + this.orbit + ";;]}";
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 31 * hash + this.orbit;
        hash = 31 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof EmptyOrbit){
            EmptyOrbit other = (EmptyOrbit) o;
            return this.orbit == other.getOrbit();
        }
        return false;
    }
}
