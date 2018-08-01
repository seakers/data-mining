package ifeed.local;

import ifeed.filter.AbstractFilter;
import ifeed.problem.assigning.FeatureFetcher;
import ifeed.problem.assigning.Params;
import ifeed.feature.TypicalityCalculator;
import ifeed.problem.assigning.filterOperators.*;
import ifeed.feature.FeatureExpressionHandler;
import ifeed.filter.BinaryInputFilterOperator;
import ifeed.feature.logic.Connective;
import ifeed.feature.logic.Literal;


import java.util.*;


public class IDETC2018 {

    public static void main(String args[]){

        String input = "AGHL/BK/F//FHJK";
        String feature = "({separate[;5,4,11;]}&&{inOrbit[0;11;]}&&{inOrbit[0;6;]}&&{inOrbit[0;0;]}&&{notInOrbit[0;3;]}&&{notInOrbit[0;5;]}&&{notInOrbit[0;9;]}&&{present[;5;]}&&{inOrbit[4;10;]}&&{inOrbit[1;10;]})";
        int reqDiff = 4;

        System.out.println("input: " + input);

        String[] inputSplit = input.split("/");
        BitSet inputs = new BitSet(60);

        int norb = 5;
        int ninstr = 12;
        String[] instrumentsArray = {"A","B","C","D","E","F","G","H","I","J","K","L"};
        String[] orbitArray = {"1000","2000","3000","4000","5000"};
        ArrayList<String> instruments = new ArrayList<>(Arrays.asList(instrumentsArray));

        for(int o = 0; o < inputSplit.length; o++){
            String thisOrbit = inputSplit[o];
            for(int i = 0; i < thisOrbit.length(); i++){
                String thisInstr = thisOrbit.charAt(i) + "";
                int instrIndex = instruments.indexOf(thisInstr);
                inputs.set(o * ninstr + instrIndex);
            }
        }

        FeatureFetcher featureFetcher = new FeatureFetcher(new ArrayList<>());
        FeatureExpressionHandler expressionHandler = new FeatureExpressionHandler();
        Random random = new Random();

        // Create a feature tree
        Connective root = expressionHandler.generateFeatureTree(feature);

        // Convert to CNF
        root = expressionHandler.convertToCNF(root);

        System.out.println("CNF: " + root.getName());

        BitSet stashedInputs = (BitSet) inputs.clone();
        int iter = 0;
        while(iter < 50){
            // Try a fixed number of iterations to increase the diff

            // Calculate the algebraic typicality
            TypicalityCalculator calculator = new TypicalityCalculator(inputs, feature, featureFetcher);
            int[] out = calculator.run();

            // Get the difference
            int diff = out[1] - out[0];
            String displayOutput = printInputs(inputs, instrumentsArray);

            System.out.println(displayOutput + ": " + diff);

            if(diff < reqDiff){ // Need to break more rules

                // Stash the current inputs
                stashedInputs = inputs;

                List<Connective> satisfiedBranches = getSatisfiedBranches(root.getConnectiveChildren(), featureFetcher, inputs);
                List<Literal> satisfiedLeafs = getSatisfiedLeafs(root.getLiteralChildren(), featureFetcher, inputs);

                // Select one of the rules to break
                int max = satisfiedBranches.size() + satisfiedLeafs.size() - 1;
                int min = 0;
                int randInt = random.nextInt(max + 1 - min) + min;

                BinaryInputFilterOperator filter;
                if(randInt < satisfiedLeafs.size()){

                    Literal leaf = satisfiedLeafs.get(randInt);
                    filter = fetchRepairOperator(leaf.getName());

                    System.out.println("Break: " + getSingleFeatureDisplayName(leaf.getName(), orbitArray, instrumentsArray));

                    if(leaf.getNegation()){
                        inputs = filter.repair(inputs);
                    }else{
                        inputs = filter.disrupt(inputs);
                    }

                }else{
                    randInt = randInt - satisfiedLeafs.size();
                    Connective branch = satisfiedBranches.get(randInt);

                    // Break all features in the current branch
                    for(Literal leaf:branch.getLiteralChildren()){
                        filter = fetchRepairOperator(leaf.getName());

                        System.out.println("Break: " + getSingleFeatureDisplayName(leaf.getName(), orbitArray, instrumentsArray));

                        inputs = filter.disrupt(inputs);
                    }
                }

            }else if(diff > reqDiff){
                // Revert back, as the diff is larger than the required value
                inputs = stashedInputs;

            }else{
                System.out.println("Successfully generated new input with diff of: " + reqDiff);
                break;
            }

            iter++;
        }

        // Calculate the algebraic typicality
        TypicalityCalculator calculator = new TypicalityCalculator(inputs, feature, featureFetcher);
        int[] out = calculator.run();
        // Get the difference
        int diff = out[1] - out[0];
        System.out.println("New input diff: " + diff);

        printInputs(inputs, instrumentsArray);
    }

    private static List<Literal> getSatisfiedLeafs(List<Literal> leafs, FeatureFetcher fetcher, BitSet inputs){

        List<Literal> satisfiedLeafs = new ArrayList<>();

        for(Literal leaf: leafs){
            AbstractFilter filter = fetcher.getFilterFetcher().fetch(leaf.getName());
            boolean satisfied = filter.apply(inputs);

            if(leaf.getNegation()){
                satisfied = !satisfied;
            }

            if(satisfied){
                satisfiedLeafs.add(leaf);
            }
        }

        return satisfiedLeafs;
    }

    private static List<Connective> getSatisfiedBranches(List<Connective> branches, FeatureFetcher fetcher, BitSet inputs){

        List<Connective> satisfiedBranches = new ArrayList<>();

        for(Connective branch: branches){

            // Go through all leaf nodes and check if at least one of them is satisfied
            for(Literal leaf:branch.getLiteralChildren()){

                AbstractFilter filter = fetcher.getFilterFetcher().fetch(leaf.getName());
                boolean satisfied = filter.apply(inputs);

                if(leaf.getNegation()){
                    satisfied = !satisfied;
                }

                if(satisfied){
                    // If one of the leaf nodes is satisfied, then consider the whole branch as satisfied
                    satisfiedBranches.add(branch);
                    break;
                }
            }
        }

        return satisfiedBranches;
    }

    private static String printInputs(BitSet inputs, String[] instrumentsArray){
        // Convert back to display format
        int cnt = 0;
        StringJoiner sj = new StringJoiner("/");
        for(int o = 0; o < Params.num_orbits; o++){
            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < Params.num_instruments; i++){
                if(inputs.get(o * Params.num_instruments + i)){
                    sb.append(instrumentsArray[i]);
                }
            }

            sj.add(sb.toString());
        }

        return sj.toString();
    }

    private static BinaryInputFilterOperator fetchRepairOperator(String expression){

        String e = expression;
        if(e.startsWith("{") && e.endsWith("}")){
            e = e.substring(1,e.length()-1);
        }else{
            e = e;
        }

        if(e.split("\\[").length==1){
            throw new RuntimeException("AbstractFilter expression without brackets: " + expression);
        }

        String type = e.split("\\[")[0];
        String argsCombined = e.substring(0,e.length()-1).split("\\[")[1];
        String[] args = argsCombined.split(";");

        return fetchRepairOperator(type, args);
    }

    private static BinaryInputFilterOperator fetchRepairOperator(String type, String[] args){

        BinaryInputFilterOperator filter;

        int orbit;
        int num;
        int[] instr;
        String arg_instr;
        String[] instr_string;

        try{

            switch (type) {
                case "present":
                    filter = new Present(Integer.parseInt(args[1]));
                    break;

                case "absent":
                    filter = new Absent(Integer.parseInt(args[1]));
                    break;

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

                default:
                    throw new RuntimeException("Could not find filter type of: " + type);
            }
            return filter;

        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Exc in fetching a feature of type: " + type);
        }
    }

    private static String getSingleFeatureDisplayName(String e, String[] orbitNames, String[] instrumentNames){

        if(e.startsWith("{") && e.endsWith("}")){
            e = e.substring(1,e.length()-1);
        }

        String type = e.split("\\[")[0];
        String argsCombined = e.substring(0,e.length()-1).split("\\[")[1];
        String[] args = argsCombined.split(";");

        StringJoiner argsJoiner = new StringJoiner(";");

        for(int i = 0; i < args.length; i++){

            if(args[i].isEmpty()){
                argsJoiner.add("");
                continue;
            }

            if(i==0){
                int orb = Integer.parseInt(args[i]);
                argsJoiner.add(orbitNames[orb]);

            }else if(i==1){
                StringJoiner instJoiner = new StringJoiner(",");
                String[] instrs = args[i].split(",");
                for(int j = 0; j < instrs.length; j++){
                    int inst = Integer.parseInt(instrs[j]);

                    instJoiner.add(instrumentNames[inst]);
                }
                argsJoiner.add(instJoiner.toString());
            }else{
                argsJoiner.add(args[i]);
            }
        }
        return type + "[" + argsJoiner.toString() + "]";
    }

}
