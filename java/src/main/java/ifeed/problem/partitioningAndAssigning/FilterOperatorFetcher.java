package ifeed.problem.partitioningAndAssigning;

import ifeed.filter.AbstractFilterOperatorFetcher;
import ifeed.filter.DiscreteInputFilterOperator;
import ifeed.filter.FilterOperator;
import ifeed.problem.partitioningAndAssigning.filterOperators.*;

public class FilterOperatorFetcher extends AbstractFilterOperatorFetcher {

    public FilterOperator fetch(String type, String[] args){

        DiscreteInputFilterOperator repairOp;

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
                    repairOp = new InOrbit(orbit, instr);
                    break;

                case "notInOrbit":
                    orbit = Integer.parseInt(args[0]);

                    arg_instr = args[1];
                    instr_string = arg_instr.split(",");

                    instr = new int[instr_string.length];
                    for(int i = 0; i < instr_string.length; i++){
                        instr[i] = Integer.parseInt(instr_string[i]);
                    }
                    repairOp = new NotInOrbit(orbit, instr);
                    break;

                case "together":
                    arg_instr = args[1];
                    instr_string = arg_instr.split(",");

                    instr = new int[instr_string.length];
                    for(int i = 0; i < instr_string.length; i++){
                        instr[i] = Integer.parseInt(instr_string[i]);
                    }
                    repairOp = new Together(instr);
                    break;

                case "separate":
                    arg_instr = args[1];
                    instr_string = arg_instr.split(",");

                    instr = new int[instr_string.length];
                    for(int i = 0; i < instr_string.length; i++){
                        instr[i] = Integer.parseInt(instr_string[i]);
                    }
                    repairOp = new Separate(instr);
                    break;

                case "emptyOrbit":
                    orbit = Integer.parseInt(args[0]);
                    repairOp = new EmptyOrbit(orbit);
                    break;

                case "numOrbits":
                    num = Integer.parseInt(args[2]);
                    repairOp = new NumOrbits(num);
                    break;

                default:
                    throw new RuntimeException("Could not find repairOp type of: " + type);
            }

            return repairOp;

        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Exc in fetching a feature of type: " + type);
        }
    }

}
