package ifeed.problem.partitioningAndAssigning;

import ifeed.filter.AbstractFilterOperatorFetcher;
import ifeed.filter.DiscreteInputFilterOperator;
import ifeed.filter.FilterOperator;
import ifeed.local.params.BaseParams;
import ifeed.problem.partitioningAndAssigning.filterOperators.*;

import java.util.List;
import java.util.StringJoiner;

public class FilterOperatorFetcher extends AbstractFilterOperatorFetcher {

    public FilterOperatorFetcher(BaseParams params){
        super(params);
    }

    public FilterOperator fetch(List<String> names, List<String[]> argSets){

        DiscreteInputFilterOperator repairOp;

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
                    repairOp = new InOrbit(super.params, orbit, instr);
                    break;

                case "notInOrbit":
                    orbit = Integer.parseInt(args[0]);

                    arg_instr = args[1];
                    instr_string = arg_instr.split(",");

                    instr = new int[instr_string.length];
                    for(int i = 0; i < instr_string.length; i++){
                        instr[i] = Integer.parseInt(instr_string[i]);
                    }
                    repairOp = new NotInOrbit(super.params, orbit, instr);
                    break;

                case "together":
                    arg_instr = args[1];
                    instr_string = arg_instr.split(",");

                    instr = new int[instr_string.length];
                    for(int i = 0; i < instr_string.length; i++){
                        instr[i] = Integer.parseInt(instr_string[i]);
                    }
                    repairOp = new Together(super.params, instr);
                    break;

                case "separate":
                    arg_instr = args[1];
                    instr_string = arg_instr.split(",");

                    instr = new int[instr_string.length];
                    for(int i = 0; i < instr_string.length; i++){
                        instr[i] = Integer.parseInt(instr_string[i]);
                    }
                    repairOp = new Separate(super.params, instr);
                    break;

                case "emptyOrbit":
                    orbit = Integer.parseInt(args[0]);
                    repairOp = new EmptyOrbit(super.params, orbit);
                    break;

                case "numOrbits":
                    num = Integer.parseInt(args[2]);
                    repairOp = new NumOrbits(super.params, num);
                    break;

                default:
                    throw new RuntimeException("Could not find repairOp type of: " + type);
            }

            return repairOp;

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
