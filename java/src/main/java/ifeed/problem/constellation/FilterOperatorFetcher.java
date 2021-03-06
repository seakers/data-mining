//package ifeed.problem.constellation;
//
//import ifeed.filter.AbstractFilterOperatorFetcher;
//import ifeed.filter.BinaryInputFilterOperator;
//import ifeed.filter.FilterOperator;
//import ifeed.local.params.BaseParams;
//import ifeed.problem.constellation.filterOperators.*;
//
//public class FilterOperatorFetcher extends AbstractFilterOperatorFetcher {
//
//    public FilterOperatorFetcher(BaseParams params){
//        super(params);
//    }
//
//    public FilterOperator fetch(String type, String[] args){
//
//        BinaryInputFilterOperator repairOp;
//
//        int orbit;
//        int num;
//        int[] instr;
//        String arg_instr;
//        String[] instr_string;
//
//        try{
//
//            switch (type) {
//                case "present":
//                    repairOp = new Present(params, Integer.parseInt(args[1]));
//                    break;
//
//                case "absent":
//                    repairOp = new Absent(params, Integer.parseInt(args[1]));
//                    break;
//
//                case "inOrbit":
//                    orbit = Integer.parseInt(args[0]);
//
//                    arg_instr = args[1];
//                    instr_string = arg_instr.split(",");
//
//                    instr = new int[instr_string.length];
//                    for(int i = 0; i < instr_string.length; i++){
//                        instr[i] = Integer.parseInt(instr_string[i]);
//                    }
//                    repairOp = new InOrbit(params, orbit, instr);
//                    break;
//
//                case "notInOrbit":
//                    orbit = Integer.parseInt(args[0]);
//
//                    arg_instr = args[1];
//                    instr_string = arg_instr.split(",");
//
//                    instr = new int[instr_string.length];
//                    for(int i = 0; i < instr_string.length; i++){
//                        instr[i] = Integer.parseInt(instr_string[i]);
//                    }
//                    repairOp = new NotInOrbit(params, orbit, instr);
//                    break;
//
//                case "together":
//                    arg_instr = args[1];
//                    instr_string = arg_instr.split(",");
//
//                    instr = new int[instr_string.length];
//                    for(int i = 0; i < instr_string.length; i++){
//                        instr[i] = Integer.parseInt(instr_string[i]);
//                    }
//                    repairOp = new Together(params, instr);
//                    break;
//
//                case "separate":
//                    arg_instr = args[1];
//                    instr_string = arg_instr.split(",");
//
//                    instr = new int[instr_string.length];
//                    for(int i = 0; i < instr_string.length; i++){
//                        instr[i] = Integer.parseInt(instr_string[i]);
//                    }
//                    repairOp = new Separate(params, instr);
//                    break;
//
//                case "emptyOrbit":
//                    orbit = Integer.parseInt(args[0]);
//                    repairOp = new InclinationRange(params, orbit);
//                    break;
//
//                case "numOrbits":
//                    num = Integer.parseInt(args[2]);
//                    repairOp = new NumOrbits(params, num);
//                    break;
//
//                default:
//                    throw new RuntimeException("Could not find repairOp type of: " + type);
//            }
//
//            return repairOp;
//
//        }catch(Exception e){
//            e.printStackTrace();
//            throw new RuntimeException("Exc in fetching a feature of type: " + type);
//        }
//    }
//
//}
