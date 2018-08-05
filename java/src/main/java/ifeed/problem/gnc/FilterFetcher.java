package ifeed.problem.gnc;

import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFetcher;
import ifeed.local.params.BaseParams;
import ifeed.problem.gnc.filters.*;

public class FilterFetcher extends AbstractFilterFetcher {

    public FilterFetcher(BaseParams params){
        super(params);
    }

    public AbstractFilter fetch(String type, String[] args){

        AbstractFilter filter;
        try{
            switch (type) {
                case "numSensors":
                    filter = new NumSensors(params, Integer.parseInt(args[0]));
                    break;

                case "numComputers":
                    filter = new NumComputers(params, Integer.parseInt(args[0]));
                    break;

                case "numTotalLinks":
                    filter = new NumTotalLinks(params, Integer.parseInt(args[0]));
                    break;

                case "numSensorOfType":
                    filter = new NumSensorOfType(params, Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                    break;

                case "numComputerOfType":
                    filter = new NumComputerOfType(params, Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                    break;

                case "minNSNC":
                    filter = new MinNSNC(params, Integer.parseInt(args[0]));
                    break;

                case "computerWithSpecificNumLinks":
                    filter = new ComputerWithSpecificNumLinks(params, Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                    break;

                case "sensorWithSpecificNumLinks":
                    filter = new ComputerWithSpecificNumLinks(params, Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                    break;

                default:
                    throw new RuntimeException("Could not find matching filter type of: " + type);
            }

        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Exc in fetching a feature of type: " + type);
        }

        return filter;
    }
}
