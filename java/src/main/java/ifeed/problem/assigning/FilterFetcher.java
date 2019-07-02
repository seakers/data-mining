package ifeed.problem.assigning;

import ifeed.filter.AbstractFilterFetcher;
import ifeed.local.params.BaseParams;
import ifeed.problem.assigning.filters.*;
import ifeed.filter.AbstractFilter;
import ifeed.problem.assigning.filtersWithException.AbsentWithException;
import ifeed.problem.assigning.filtersWithException.EmptyOrbitWithException;
import ifeed.problem.assigning.filtersWithException.NotInOrbitWithException;
import ifeed.problem.assigning.filtersWithException.SeparateWithException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public class FilterFetcher extends AbstractFilterFetcher {

    public FilterFetcher(BaseParams params){
        super(params);
    }

    @Override
    public AbstractFilter fetch(List<String> names, List<String[]> argSets){

        AbstractFilter filter;
        int orbit;
        int num;
        int instrument;
        int[] instruments;

        String argOrbitCombined;
        String argInstrCombined;
        String[] argOrbitSplit;
        String[] argInstrSplit;

        try{
            if(names.size() == 1){
                String type = names.get(0);
                String[] args = argSets.get(0);
                switch (type) {
                    case "present":
                        filter = new Present(super.params, Integer.parseInt(args[1]));
                        break;

                    case "absent":
                        filter = new Absent(super.params, Integer.parseInt(args[1]));
                        break;

                    case "inOrbit":
                        orbit = Integer.parseInt(args[0]);

                        argInstrCombined = args[1];
                        argInstrSplit = argInstrCombined.split(",");

                        instruments = new int[argInstrSplit.length];
                        for(int i = 0; i < argInstrSplit.length; i++){
                            instruments[i] = Integer.parseInt(argInstrSplit[i]);
                        }
                        filter = new InOrbit(super.params, orbit, instruments);
                        break;

                    case "notInOrbit":
                        orbit = Integer.parseInt(args[0]);

                        argInstrCombined = args[1];
                        argInstrSplit = argInstrCombined.split(",");

                        instruments = new int[argInstrSplit.length];
                        for(int i = 0; i < argInstrSplit.length; i++){
                            instruments[i] = Integer.parseInt(argInstrSplit[i]);
                        }
                        filter = new NotInOrbit(super.params, orbit, instruments);
                        break;

                    case "together":
                        argInstrCombined = args[1];
                        argInstrSplit = argInstrCombined.split(",");

                        instruments = new int[argInstrSplit.length];
                        for(int i = 0; i < argInstrSplit.length; i++){
                            instruments[i] = Integer.parseInt(argInstrSplit[i]);
                        }
                        filter = new Together(super.params, instruments);
                        break;

//                case "togetherInOrbit":
//                    orbit = Integer.parseInt(args[0]);
//
//                    arg_instr = args[1];
//                    instr_string = arg_instr.split(",");
//
//                    instr = new int[instr_string.length];
//                    for(int i = 0; i < instr_string.length; i++){
//                        instr[i] = Integer.parseInt(instr_string[i]);
//                    }
//                    filter = new TogetherInOrbit(orbit, instr);
//                    break;

                    case "separate":
                        argInstrCombined = args[1];
                        argInstrSplit = argInstrCombined.split(",");

                        instruments = new int[argInstrSplit.length];
                        for(int i = 0; i < argInstrSplit.length; i++){
                            instruments[i] = Integer.parseInt(argInstrSplit[i]);
                        }
                        filter = new Separate(super.params, instruments);
                        break;

                    case "emptyOrbit":
                        orbit = Integer.parseInt(args[0]);
                        filter = new EmptyOrbit(super.params, orbit);
                        break;

                    case "numOrbits":
                        num = Integer.parseInt(args[2]);
                        filter = new NumOrbits(super.params, num);
                        break;

                    case "numInstruments":
                        instrument = -1;
                        orbit = -1;

                        int[] nBounds = new int[2];
                        if(args[2].contains(",")){
                            String[] temp = args[2].split(",");
                            nBounds[0] = Integer.parseInt(temp[0]);
                            nBounds[1] = Integer.parseInt(temp[1]);
                        }else if(args[2].contains("-")){
                            String[] temp = args[2].split("-");
                            nBounds[0] = Integer.parseInt(temp[0]);
                            nBounds[1] = Integer.parseInt(temp[1]);
                        }else{
                            nBounds[0] = Integer.parseInt(args[2]);
                            nBounds[1] = Integer.parseInt(args[2]);
                        }

                        if(args[0].isEmpty() && args[1].isEmpty()){
                            // Number of instruments in total
                        } else if (args[1].isEmpty()) {
                            // Number of instruments in an orbit
                            orbit = Integer.parseInt(args[0]);
                        }else{
                            // Number of a specific instrument
                            instrument = Integer.parseInt(args[1]);
                        }

                        filter = new NumInstruments(super.params, orbit, instrument, nBounds);
                        break;

                    case "absentExceptInOrbit":
                        String orb_string = args[0];
                        String[] orb_string_split = orb_string.split(",");

                        Set<Integer> orbits = new HashSet<>();
                        for(String orb: orb_string_split){
                            orbits.add(Integer.parseInt(orb));
                        }
                        instrument = Integer.parseInt(args[1]);
                        filter = new AbsentExceptInOrbit(super.params, orbits, instrument);
                        break;

                    case "notInOrbitExceptInstrument":
                        orbit = Integer.parseInt(args[0]);

                        argInstrCombined = args[1];
                        argInstrSplit = argInstrCombined.split(",");

                        int instrClass = Integer.parseInt(argInstrSplit[0]);
                        int instrException = Integer.parseInt(argInstrSplit[1]);

                        filter = new NotInOrbitExceptInstrument(super.params, orbit, instrClass, instrException);
                        break;

                    case "notInOrbitExceptOrbit":
                        orb_string = args[0];
                        orb_string_split = orb_string.split(",");
                        int orbitClass = Integer.parseInt(orb_string_split[0]);
                        int orbitException = Integer.parseInt(orb_string_split[1]);

                        instrument = Integer.parseInt(args[1]);

                        filter = new NotInOrbitExceptOrbit(super.params, orbitClass, orbitException, instrument);
                        break;

                    default:
                        throw new RuntimeException("Could not find filter type of: " + type);
                }

            } else {
                StringJoiner sj = new StringJoiner("_");
                for(String name: names){
                    sj.add(name);
                }
                String type = sj.toString();

                Set<Integer> orbitException;
                Set<Integer> instrumentException;

                switch (type) {
                    case "absent_except":
                        // Get argument for the main base feature
                        instrument = Integer.parseInt(argSets.get(0)[1]);

                        // Get arguments for the exception part
                        if(argSets.get(1).length > 0){
                            argOrbitCombined = argSets.get(1)[0];
                            argOrbitSplit = argOrbitCombined.split(",");
                        } else {
                            argOrbitSplit = new String[0];
                        }

                        if(argSets.get(1).length > 1){
                            argInstrCombined = argSets.get(1)[1];
                            argInstrSplit = argInstrCombined.split(",");
                        }else{
                            argInstrSplit = new String[0];
                        }

                        orbitException = new HashSet<>();
                        instrumentException = new HashSet<>();
                        for(int i = 0; i < argOrbitSplit.length; i++){
                            if(!argOrbitSplit[i].isEmpty()) {
                                orbitException.add(Integer.parseInt(argOrbitSplit[i]));
                            }
                        }
                        for(int i = 0; i < argInstrSplit.length; i++){
                            if(!argInstrSplit[i].isEmpty()){
                                instrumentException.add(Integer.parseInt(argInstrSplit[i]));
                            }
                        }
                        filter = new AbsentWithException(super.params, instrument, orbitException, instrumentException);
                        break;

                    case "emptyOrbit_except":
                        // Get argument for the main base feature
                        orbit = Integer.parseInt(argSets.get(0)[0]);

                        // Get arguments for the exception part
                        if(argSets.get(1).length > 0){
                            argOrbitCombined = argSets.get(1)[0];
                            argOrbitSplit = argOrbitCombined.split(",");
                        } else {
                            argOrbitSplit = new String[0];
                        }

                        if(argSets.get(1).length > 1){
                            argInstrCombined = argSets.get(1)[1];
                            argInstrSplit = argInstrCombined.split(",");
                        }else{
                            argInstrSplit = new String[0];
                        }

                        orbitException = new HashSet<>();
                        instrumentException = new HashSet<>();
                        for(int i = 0; i < argOrbitSplit.length; i++){
                            if(!argOrbitSplit[i].isEmpty()) {
                                orbitException.add(Integer.parseInt(argOrbitSplit[i]));
                            }
                        }
                        for(int i = 0; i < argInstrSplit.length; i++){
                            if(!argInstrSplit[i].isEmpty()){
                                instrumentException.add(Integer.parseInt(argInstrSplit[i]));
                            }
                        }
                        filter = new EmptyOrbitWithException(super.params, orbit, orbitException, instrumentException);
                        break;

                    case "notInOrbit_except":
                        // Get argument for the main base feature
                        orbit = Integer.parseInt(argSets.get(0)[0]);
                        instrument = Integer.parseInt(argSets.get(0)[1]);

                        // Get arguments for the exception part
                        if(argSets.get(1).length > 0){
                            argOrbitCombined = argSets.get(1)[0];
                            argOrbitSplit = argOrbitCombined.split(",");
                        } else {
                            argOrbitSplit = new String[0];
                        }

                        if(argSets.get(1).length > 1){
                            argInstrCombined = argSets.get(1)[1];
                            argInstrSplit = argInstrCombined.split(",");
                        }else{
                            argInstrSplit = new String[0];
                        }

                        orbitException = new HashSet<>();
                        instrumentException = new HashSet<>();
                        for(int i = 0; i < argOrbitSplit.length; i++){
                            if(!argOrbitSplit[i].isEmpty()) {
                                orbitException.add(Integer.parseInt(argOrbitSplit[i]));
                            }
                        }
                        for(int i = 0; i < argInstrSplit.length; i++){
                            if(!argInstrSplit[i].isEmpty()){
                                instrumentException.add(Integer.parseInt(argInstrSplit[i]));
                            }
                        }
                        filter = new NotInOrbitWithException(super.params, orbit, instrument, orbitException, instrumentException);
                        break;

                    case "notInOrbit":
                        // Get argument for the main base feature
                        orbit = Integer.parseInt(argSets.get(0)[0]);
                        argInstrCombined = argSets.get(0)[1];
                        argInstrSplit = argInstrCombined.split(",");
                        instruments = new int[argInstrSplit.length];
                        for(int i = 0; i < argInstrSplit.length; i++){
                            instruments[i] = Integer.parseInt(argInstrSplit[i]);
                        }

                        // Get arguments for the exception part
                        argOrbitCombined = argSets.get(1)[0];
                        argOrbitSplit = argOrbitCombined.split(",");
                        argInstrCombined = argSets.get(1)[1];
                        argInstrSplit = argInstrCombined.split(",");

                        orbitException = new HashSet<>();
                        instrumentException = new HashSet<>();
                        for(int i = 0; i < argOrbitSplit.length; i++){
                            orbitException.add(Integer.parseInt(argOrbitSplit[i]));
                        }
                        for(int i = 0; i < argInstrSplit.length; i++){
                            instrumentException.add(Integer.parseInt(argInstrSplit[i]));
                        }
                        filter = new NotInOrbitWithException(super.params, orbit, instruments, orbitException, instrumentException);
                        break;

                    case "separate":
                        // Get argument for the main base feature
                        argInstrCombined = argSets.get(0)[1];
                        argInstrSplit = argInstrCombined.split(",");
                        instruments = new int[argInstrSplit.length];
                        for(int i = 0; i < argInstrSplit.length; i++){
                            instruments[i] = Integer.parseInt(argInstrSplit[i]);
                        }

                        // Get arguments for the exception part
                        argOrbitCombined = argSets.get(1)[0];
                        argOrbitSplit = argOrbitCombined.split(",");
                        argInstrCombined = argSets.get(1)[1];
                        argInstrSplit = argInstrCombined.split(",");

                        orbitException = new HashSet<>();
                        instrumentException = new HashSet<>();
                        for(int i = 0; i < argOrbitSplit.length; i++){
                            orbitException.add(Integer.parseInt(argOrbitSplit[i]));
                        }
                        for(int i = 0; i < argInstrSplit.length; i++){
                            instrumentException.add(Integer.parseInt(argInstrSplit[i]));
                        }
                        filter = new SeparateWithException(super.params, instruments, orbitException, instrumentException);
                        break;

                    case "emptyOrbit":
                        // Get argument for the main base feature
                        orbit = Integer.parseInt(argSets.get(0)[0]);

                        // Get arguments for the exception part
                        argOrbitCombined = argSets.get(1)[0];
                        argOrbitSplit = argOrbitCombined.split(",");
                        argInstrCombined = argSets.get(1)[1];
                        argInstrSplit = argInstrCombined.split(",");

                        orbitException = new HashSet<>();
                        instrumentException = new HashSet<>();
                        for(int i = 0; i < argOrbitSplit.length; i++){
                            orbitException.add(Integer.parseInt(argOrbitSplit[i]));
                        }
                        for(int i = 0; i < argInstrSplit.length; i++){
                            instrumentException.add(Integer.parseInt(argInstrSplit[i]));
                        }
                        filter = new EmptyOrbitWithException(super.params, orbit, orbitException, instrumentException);
                        break;

                    default:
                        throw new RuntimeException("Could not find filter type of: " + type);
                }
            }

            return filter;

        }catch(Exception e){
            e.printStackTrace();
            StringJoiner sj = new StringJoiner("_");
            for(String s: names){
                sj.add(s);
            }
            throw new RuntimeException("Exc in fetching a feature of type: " + sj.toString());
        }
    }

}
