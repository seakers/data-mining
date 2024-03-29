/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning.filtersWithException;

import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.filters.EmptyOrbit;
import java.util.BitSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/**
 * 
 *
 * @author bang
 */
public class EmptyOrbitWithException extends EmptyOrbit {

    protected Set<Integer> orbitException;
    protected Set<Integer> instrumentException;

    public EmptyOrbitWithException(BaseParams params, int orbit, Set<Integer> orbitException, Set<Integer> instrumentException){
        super(params, orbit);
        if(!orbitException.isEmpty() && !instrumentException.isEmpty()){
            throw new IllegalStateException("Orbit and instrument exceptions cannot be used at the same time");
        }
        this.setOrbitException(orbitException);
        this.setInstrumentException(instrumentException);
    }

    public void setOrbitException(Set<Integer> exception){
        if(exception.size() >= (double) super.params.getRightSetCardinality() / 2){
            throw new IllegalStateException("The number of exceptions should be smaller than half the number of the valid instances");
        }
        this.orbitException = exception;
    }

    public void setInstrumentException(Set<Integer> exception){
        if(exception.size() >= (double) super.params.getLeftSetCardinality() / 2){
            throw new IllegalStateException("The number of exceptions should be smaller than half the number of the valid instances");
        }
        this.instrumentException = exception;
    }

    public boolean apply(BitSet input, int orbit){
        boolean out = true; // empty
        if(orbit >= this.params.getRightSetCardinality()){
            // For each orbit instance under the given class
            for(int orbitIndex: this.orbitInstances){

                // If one of the tests fail, return false
                if(!this.apply(input, orbitIndex)){
                    out = false;
                    break;
                }
            }

        }else{
            for(int i = 0; i< this.params.getLeftSetCardinality(); i++){

                if(!this.orbitException.isEmpty() && !this.instrumentException.isEmpty()){
                    if(this.orbitException.contains(orbit) && this.instrumentException.contains(i)){
                        continue;
                    }
                }else if(this.orbitException.contains(orbit)){ // instrumentException is empty
                    continue;
                }else if(this.instrumentException.contains(i)){ // orbitException is empty
                    continue;
                }

                if(input.get(orbit * this.params.getLeftSetCardinality() +i)){
                    out = false; // instrument found inside the orbit
                    break;
                }
            }
        }
        return out;
    }

    @Override
    public String getDescription(){
        StringBuilder out = new StringBuilder();

        if(this.params.isGeneralizedConceptRightSet(this.orbit)){
            out.append(this.params.getRightSetEntityName(this.orbit) + " is empty");
        }else{
            out.append("Orbit " + this.params.getRightSetEntityName(this.orbit) + " is empty");
        }

        if(!this.orbitException.isEmpty()){
            out.append(", except for ");
            if(this.orbitException .size() == 1){
                out.append("orbit " + this.params.getRightSetEntityName(this.orbitException.iterator().next()));
            }else{
                StringJoiner sj = new StringJoiner(", ");
                for(int o: this.orbitException){
                    sj.add(this.params.getRightSetEntityName(o));
                }
                out.append("orbits {" + sj.toString() + "}");
            }

        } else if(!this.instrumentException.isEmpty()){
            out.append(", except when ");

            if(this.instrumentException .size() == 1){
                out.append("instrument " + this.params.getLeftSetEntityName(this.instrumentException.iterator().next()) + " is ");
            }else{
                StringJoiner sj = new StringJoiner(", ");
                for(int i: this.instrumentException){
                    sj.add(this.params.getLeftSetEntityName(i));
                }
                out.append("the instruments {" + sj.toString() + "} are ");
            }
            out.append("assigned");
        }

        return out.toString();
    }
    
    @Override
    public String getName(){return "emptyOrbit_except";}
    
    @Override
    public String toString(){
        StringJoiner orbitExceptionString = new StringJoiner(",");
        for(int o: this.orbitException){
            orbitExceptionString.add(Integer.toString(o));
        }
        StringJoiner instrumentExceptionString = new StringJoiner(",");
        for(int i: this.instrumentException){
            instrumentExceptionString.add(Integer.toString(i));
        }
        return "{emptyOrbit[" + this.orbit + ";;]except["+ orbitExceptionString +";"+ instrumentExceptionString +";]}";
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 31 * hash + this.orbit;
        hash = 31 * hash + Objects.hashCode(this.getName());
        hash = 31 * hash + Objects.hashCode(this.orbitException);
        hash = 31 * hash + Objects.hashCode(this.instrumentException);
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof EmptyOrbitWithException){
            EmptyOrbitWithException other = (EmptyOrbitWithException) o;
            if(this.orbit != other.getOrbit()) return false;
            if(this.orbitException.equals(other.orbitException)) return false;
            if(this.instrumentException.equals(other.instrumentException)) return false;
        }
        return false;
    }
}
