package ifeed_dm.EOSS;

import ifeed_dm.EOSS.filters.*;
import ifeed_dm.FeatureFetcher;
import ifeed_dm.Feature;
import ifeed_dm.Filter;
import ifeed_dm.binaryInput.BinaryInputArchitecture;

import java.util.List;
import java.util.BitSet;
import java.util.ArrayList;

public class EOSSFeatureFetcher extends FeatureFetcher {

    List<BinaryInputArchitecture> architectures;

    public EOSSFeatureFetcher(List<BinaryInputArchitecture> architectures){
        super(new ArrayList<>());
        this.architectures = architectures;
    }

    public EOSSFeatureFetcher(List<Feature> baseFeatures, List<BinaryInputArchitecture> architectures){
        super(baseFeatures);
        this.architectures = architectures;
    }

    public Feature fetch(String type, String[] args){

        if(this.architectures.size() == 0){
            throw new RuntimeException("Exc in fetching a feature: architectures not setup");
        }

        Filter filter;

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

                case "togetherInOrbit":
                    orbit = Integer.parseInt(args[0]);

                    arg_instr = args[1];
                    instr_string = arg_instr.split(",");

                    instr = new int[instr_string.length];
                    for(int i = 0; i < instr_string.length; i++){
                        instr[i] = Integer.parseInt(instr_string[i]);
                    }
                    filter = new TogetherInOrbit(orbit, instr);
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
                    int instrument = -1;
                    orbit = -1;
                    num = Integer.parseInt(args[2]);

                    if(args[0].isEmpty() && args[1].isEmpty()){
                        // Number of instruments in total
                    } else if (args[1].isEmpty()) {
                        // Number of instruments in an orbit
                        orbit = Integer.parseInt(args[0]);
                    }else{
                        // Number of a specific instrument
                        instrument = Integer.parseInt(args[1]);
                    }

                    filter = new NumOfInstruments(orbit, instrument, num);
                    break;


                default:
                    throw new RuntimeException("Could not find filter type of: " + type);
            }

        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Exc in fetching a feature of type: " + type);
        }

        BitSet matches = new BitSet(this.architectures.size());

        for(int i = 0; i < this.architectures.size(); i++){
            BinaryInputArchitecture a = this.architectures.get(i);
            if(filter.apply(a.getInputs())){
                matches.set(i);
            }
        }

        return new Feature(filter.toString(), matches);
    }
}
