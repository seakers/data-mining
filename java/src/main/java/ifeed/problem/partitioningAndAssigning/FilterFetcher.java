package ifeed.problem.partitioningAndAssigning;

import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFetcher;
import ifeed.problem.partitioningAndAssigning.filters.*;

public class FilterFetcher extends AbstractFilterFetcher {

    @Override
    public AbstractFilter fetch(String type, String[] args){

        AbstractFilter filter;
        int orbit;
        int num;
        int[] instr;
        String arg_instr;
        String[] instr_string;

        try{

            switch (type) {
                case "inOrbit":
                    orbit = Integer.parseInt(args[0]);

                    arg_instr = args[1];
                    instr_string = arg_instr.split(",");

                    instr = new int[instr_string.length];
                    for(int i = 0; i < instr_string.length; i++){
                        instr[i] = Integer.parseInt(instr_string[i]);
                    }
                    filter = new InOrbit(orbit, instr);
                    break;

                case "notInOrbit":
                    orbit = Integer.parseInt(args[0]);

                    arg_instr = args[1];
                    instr_string = arg_instr.split(",");

                    instr = new int[instr_string.length];
                    for(int i = 0; i < instr_string.length; i++){
                        instr[i] = Integer.parseInt(instr_string[i]);
                    }
                    filter = new NotInOrbit(orbit, instr);
                    break;

                case "together":
                    arg_instr = args[1];
                    instr_string = arg_instr.split(",");

                    instr = new int[instr_string.length];
                    for(int i = 0; i < instr_string.length; i++){
                        instr[i] = Integer.parseInt(instr_string[i]);
                    }
                    filter = new Together(instr);
                    break;

                case "separate":
                    arg_instr = args[1];
                    instr_string = arg_instr.split(",");

                    instr = new int[instr_string.length];
                    for(int i = 0; i < instr_string.length; i++){
                        instr[i] = Integer.parseInt(instr_string[i]);
                    }
                    filter = new Separate(instr);
                    break;

                case "emptyOrbit":
                    orbit = Integer.parseInt(args[0]);
                    filter = new EmptyOrbit(orbit);
                    break;

                case "numOrbits":
                    num = Integer.parseInt(args[2]);
                    filter = new NumOrbits(num);
                    break;

                case "numOfInstruments":
                    orbit = -1;

                    num = Integer.parseInt(args[2]);

                    // Number of instruments in an orbit
                    orbit = Integer.parseInt(args[0]);

                    filter = new NumOfInstruments(orbit, num);
                    break;

                default:
                    throw new RuntimeException("Could not find filter type of: " + type);
            }

            return filter;

        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Exc in fetching a feature of type: " + type);
        }
    }

}
