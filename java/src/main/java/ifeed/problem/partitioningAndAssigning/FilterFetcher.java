package ifeed.problem.partitioningAndAssigning;

import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFetcher;
import ifeed.local.params.BaseParams;
import ifeed.problem.partitioningAndAssigning.filters.*;

import java.util.List;
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
        int[] instr;
        String arg_instr;
        String[] instr_string;

        try{
            String type = names.get(0);
            String[] args = argSets.get(0);

            switch (type) {
                case "inOrbit":
                    orbit = Integer.parseInt(args[0]);

                    arg_instr = args[1];
                    instr_string = arg_instr.split(",");

                    instr = new int[instr_string.length];
                    for(int i = 0; i < instr_string.length; i++){
                        instr[i] = Integer.parseInt(instr_string[i]);
                    }
                    filter = new InOrbit(params, orbit, instr);
                    break;

                case "notInOrbit":
                    orbit = Integer.parseInt(args[0]);

                    arg_instr = args[1];
                    instr_string = arg_instr.split(",");

                    instr = new int[instr_string.length];
                    for(int i = 0; i < instr_string.length; i++){
                        instr[i] = Integer.parseInt(instr_string[i]);
                    }
                    filter = new NotInOrbit(params, orbit, instr);
                    break;

                case "together":
                    arg_instr = args[1];
                    instr_string = arg_instr.split(",");

                    instr = new int[instr_string.length];
                    for(int i = 0; i < instr_string.length; i++){
                        instr[i] = Integer.parseInt(instr_string[i]);
                    }
                    filter = new Together(params, instr);
                    break;

                case "separate":
                    arg_instr = args[1];
                    instr_string = arg_instr.split(",");

                    instr = new int[instr_string.length];
                    for(int i = 0; i < instr_string.length; i++){
                        instr[i] = Integer.parseInt(instr_string[i]);
                    }
                    filter = new Separate(params, instr);
                    break;

                case "emptyOrbit":
                    orbit = Integer.parseInt(args[0]);
                    filter = new EmptyOrbit(params, orbit);
                    break;

                case "numOrbits":
                    num = Integer.parseInt(args[2]);
                    filter = new NumOrbits(params, num);
                    break;

                case "numOfInstruments":
                    orbit = -1;

                    num = Integer.parseInt(args[2]);

                    // Number of instruments in an orbit
                    orbit = Integer.parseInt(args[0]);

                    filter = new NumOfInstruments(params, orbit, num);
                    break;

                default:
                    throw new RuntimeException("Could not find filter type of: " + type);
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
