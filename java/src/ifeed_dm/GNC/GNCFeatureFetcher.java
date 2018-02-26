package ifeed_dm.GNC;

import ifeed_dm.FeatureFetcher;
import ifeed_dm.Feature;
import ifeed_dm.Filter;
import ifeed_dm.GNC.filters.*;
import ifeed_dm.discreteInput.DiscreteInputArchitecture;

import java.util.List;
import java.util.BitSet;
import java.util.ArrayList;

public class GNCFeatureFetcher extends FeatureFetcher {

    List<DiscreteInputArchitecture> architectures;

    public GNCFeatureFetcher(List<Feature> baseFeatures){
        super(baseFeatures);
        this.architectures = new ArrayList<>();
    }

    public GNCFeatureFetcher(List<Feature> baseFeatures, List<DiscreteInputArchitecture> architectures){
        super(baseFeatures);
        this.architectures = architectures;
    }

    public Feature fetch(String type, String[] args){

        if(this.architectures.size() == 0){
            throw new RuntimeException("Exc in fetching a feature: architectures not setup");
        }

        Filter filter;
        try{
            switch (type) {
                case "numSensors":
                    filter = new NumSensors(Integer.parseInt(args[0]));
                    break;

                case "numComputers":
                    filter = new NumComputers(Integer.parseInt(args[0]));
                    break;

                case "numTotalLinks":
                    filter = new NumTotalLinks(Integer.parseInt(args[0]));
                    break;

                case "numSensorOfType":
                    filter = new NumSensorOfType(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                    break;

                case "numComputerOfType":
                    filter = new NumComputerOfType(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                    break;

                case "minNSNC":
                    filter = new MinNSNC(Integer.parseInt(args[0]));
                    break;

                default:
                    throw new RuntimeException("Could not find matching filter type of: " + type);
            }

        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Exc in fetching a feature of type: " + type);
        }

        BitSet matches = new BitSet(this.architectures.size());

        for(int i = 0; i < this.architectures.size(); i++){
            DiscreteInputArchitecture a = this.architectures.get(i);
            if(filter.apply(a.getInputs())){
                matches.set(i);
            }
        }

        return new Feature(filter.toString(), matches);
    }
}
