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
        int[] nBounds;

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
                        nBounds = new int[2];
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

                        if(args[1].isEmpty()){
                            filter = new NumInstruments(super.params, -1, nBounds);

                        }else{
                            argInstrCombined = args[1];
                            argInstrSplit = argInstrCombined.split(",");

                            if(argInstrSplit.length == 1){
                                instrument = Integer.parseInt(argInstrSplit[0]);
                                filter = new NumInstruments(super.params, instrument, nBounds);
                            }else{
                                Set<Integer> intrumentSet = new HashSet<>();
                                for(int i = 0; i < argInstrSplit.length; i++){
                                    intrumentSet.add(Integer.parseInt(argInstrSplit[i]));
                                }
                                filter = new NumInstruments(super.params, intrumentSet, nBounds);
                            }
                        }
                        break;

                    case "numInstrumentsInOrbit":
                        nBounds = new int[2];
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

                        if(args[0].isEmpty()){
                            orbit = -1;
                            if(args[1].isEmpty()){
                                filter = new NumInstrumentsInOrbit(super.params, orbit, -1, nBounds);

                            }else{
                                argInstrCombined = args[1];
                                argInstrSplit = argInstrCombined.split(",");

                                if(argInstrSplit.length == 1){
                                    instrument = Integer.parseInt(argInstrSplit[0]);
                                    filter = new NumInstrumentsInOrbit(super.params, orbit, instrument, nBounds);
                                }else{
                                    Set<Integer> intrumentSet = new HashSet<>();
                                    for(int i = 0; i < argInstrSplit.length; i++){
                                        intrumentSet.add(Integer.parseInt(argInstrSplit[i]));
                                    }
                                    filter = new NumInstrumentsInOrbit(super.params, orbit, intrumentSet, nBounds);
                                }
                            }
                        } else {
                            orbit = Integer.parseInt(args[0]);
                            if(args[1].isEmpty()){
                                filter = new NumInstrumentsInOrbit(super.params, orbit, -1, nBounds);

                            }else{
                                argInstrCombined = args[1];
                                argInstrSplit = argInstrCombined.split(",");

                                if(argInstrSplit.length == 1){
                                    instrument = Integer.parseInt(argInstrSplit[0]);
                                    filter = new NumInstrumentsInOrbit(super.params, orbit, instrument, nBounds);
                                }else{
                                    Set<Integer> intrumentSet = new HashSet<>();
                                    for(int i = 0; i < argInstrSplit.length; i++){
                                        intrumentSet.add(Integer.parseInt(argInstrSplit[i]));
                                    }
                                    filter = new NumInstrumentsInOrbit(super.params, orbit, intrumentSet, nBounds);
                                }
                            }

                        }
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

                // Get arguments for the exception part
                String[] exceptionArgs = argSets.get(1);
                argOrbitCombined = null;
                argInstrCombined = null;
                argOrbitSplit = new String[0];
                argInstrSplit = new String[0];

                if(exceptionArgs.length > 0){
                    argOrbitCombined = argSets.get(1)[0];
                    argOrbitSplit = argOrbitCombined.split(",");
                }
                if(exceptionArgs.length > 1){
                    argInstrCombined = argSets.get(1)[1];
                    argInstrSplit = argInstrCombined.split(",");
                }

                Set<Integer> orbitException = new HashSet<>();
                Set<Integer> instrumentException = new HashSet<>();
                if(argOrbitCombined != null){
                    for(int i = 0; i < argOrbitSplit.length; i++){
                        if(argOrbitSplit[i].isEmpty()){
                            continue;
                        }
                        orbitException.add(Integer.parseInt(argOrbitSplit[i]));
                    }
                }
                if(argInstrCombined != null){
                    for(int i = 0; i < argInstrSplit.length; i++){
                        if(argInstrSplit[i].isEmpty()){
                            continue;
                        }
                        instrumentException.add(Integer.parseInt(argInstrSplit[i]));
                    }
                }

                switch (type) {
                    case "absent_except":
                        // Get argument for the main base feature
                        instrument = Integer.parseInt(argSets.get(0)[1]);
                        filter = new AbsentWithException(super.params, instrument, orbitException, instrumentException);
                        break;

                    case "emptyOrbit_except":
                        // Get argument for the main base feature
                        orbit = Integer.parseInt(argSets.get(0)[0]);
                        filter = new EmptyOrbitWithException(super.params, orbit, orbitException, instrumentException);
                        break;

                    case "notInOrbit_except":
                        // Get argument for the main base feature
                        orbit = Integer.parseInt(argSets.get(0)[0]);
                        argInstrCombined = argSets.get(0)[1];
                        argInstrSplit = argInstrCombined.split(",");
                        instruments = new int[argInstrSplit.length];
                        for(int i = 0; i < argInstrSplit.length; i++){
                            instruments[i] = Integer.parseInt(argInstrSplit[i]);
                        }
                        filter = new NotInOrbitWithException(super.params, orbit, instruments, orbitException, instrumentException);
                        break;

                    case "separate_except":
                        // Get argument for the main base feature
                        argInstrCombined = argSets.get(0)[1];
                        argInstrSplit = argInstrCombined.split(",");
                        instruments = new int[argInstrSplit.length];
                        for(int i = 0; i < argInstrSplit.length; i++){
                            instruments[i] = Integer.parseInt(argInstrSplit[i]);
                        }
                        filter = new SeparateWithException(super.params, instruments, orbitException, instrumentException);
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
