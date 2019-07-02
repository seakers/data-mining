
package ifeed.problem.assigning.filtersWithException;

import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.filters.Absent;
import java.util.*;

/**
 *
 * @author bang
 */
public class AbsentWithException extends Absent{

    protected Set<Integer> orbitException;
    protected Set<Integer> instrumentException;

    public AbsentWithException(BaseParams params, int instrumentClass, Set<Integer> orbitException, Set<Integer> instrumentException){
        super(params, instrumentClass);
        if(!orbitException.isEmpty() && !instrumentException.isEmpty()){
            throw new IllegalStateException("Orbit and instrument exceptions cannot be used at the same time");
        }
        this.setOrbitException(orbitException);
        this.setInstrumentException(instrumentException);
    }

    public void setOrbitException(Set<Integer> exception){
        if(exception.size() >= super.params.getRightSetCardinality() / 2){
            throw new IllegalStateException("The number of exceptions should be smaller than half the number of the valid instances");
        }
        this.orbitException = exception;
    }

    public void setInstrumentException(Set<Integer> exception){
        if(!exception.isEmpty()){
            if(!super.isInstrumentClass(super.instrument)) {
                throw new IllegalStateException("Instrument class should be given as an argument to set instrument exceptions");
            }

            if(super.instrumentInstances != null){
                if(exception.size() >= super.instrumentInstances.size() / 2){
                    throw new IllegalStateException("The number of exceptions should be smaller than half the number of the valid instances");
                }
            }
        }
        this.instrumentException = exception;
    }

    @Override
    public boolean apply(BitSet input, int instrument){
        boolean out = true;
        if(super.isInstrumentClass(instrument)){
            // For each OWL instances that are members of a class
            for(int instrumentIndex: this.instrumentInstances){
                if(!this.apply(input, instrumentIndex)){
                    // If at least one of the tests fail, return false
                    out = false;
                    break;
                }
            }

        }else{
            for(int o = 0; o < this.params.getRightSetCardinality(); o++){
                if(this.orbitException.contains(o)){
                    continue;
                }
                if(input.get(o * this.params.getLeftSetCardinality() + instrument)){
                    // If any one of the instruments are not present
                    if(this.instrumentException.contains(instrument)){
                        continue;
                    }
                    out = false;
                    break;
                }
            }
        }
        return out;
    }

    @Override
    public String getDescription(){
        StringBuilder out = new StringBuilder();
        out.append(this.params.getLeftSetEntityName(this.instrument) + " is not used");

        if(!this.orbitException.isEmpty()){
            out.append(", except when it is assigned to ");
            if(this.orbitException .size() == 1){
                out.append("orbit " + this.params.getRightSetEntityName(this.orbitException.iterator().next()));
            }else{
                StringJoiner sj = new StringJoiner(", ");
                for(int o: this.orbitException){
                    sj.add(this.params.getRightSetEntityName(o));
                }
                out.append("one of the orbits {" + sj.toString() + "}");
            }

        } else if(!this.instrumentException.isEmpty()){
            out.append(", except for ");

            if(this.instrumentException .size() == 1){
                out.append("instrument " + this.params.getLeftSetEntityName(this.instrumentException.iterator().next()));
            }else{
                StringJoiner sj = new StringJoiner(", ");
                for(int i: this.instrumentException){
                    sj.add(this.params.getLeftSetEntityName(i));
                }
                out.append("the instruments {" + sj.toString() + "}");
            }
        }
        return out.toString();
    }

    @Override
    public String getName(){return "absent_except";}

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

        return "{absent[;" + this.instrument + ";]except["+ orbitExceptionString.toString() +";"+ instrumentExceptionString.toString() +";]}";
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 31 * hash + Objects.hashCode(this.instrument);
        hash = 31 * hash + Objects.hashCode(this.orbitException);
        hash = 31 * hash + Objects.hashCode(this.instrumentException);
        hash = 31 * hash + Objects.hashCode(this.getName());
        return hash;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof AbsentWithException){
            AbsentWithException other = (AbsentWithException) o;
            if(this.instrument != other.getInstrument()) return false;
            if(this.orbitException.equals(other.orbitException)) return false;
            if(this.instrumentException.equals(other.instrumentException)) return false;
            return true;
        }
        return false;
    }
}
