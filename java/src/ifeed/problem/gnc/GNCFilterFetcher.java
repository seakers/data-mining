package ifeed.problem.gnc;

import ifeed.filter.FilterFetcher;
import ifeed.filter.Filter;
import ifeed.problem.gnc.filters.*;

public class GNCFilterFetcher extends FilterFetcher {

    public Filter fetch(String type, String[] args){

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

        return filter;
    }
}