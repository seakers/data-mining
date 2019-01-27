package ifeed.problem.constellation;

import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFetcher;
import ifeed.local.params.BaseParams;
import ifeed.problem.constellation.filters.*;

public class FilterFetcher extends AbstractFilterFetcher {

    public FilterFetcher(BaseParams params){
        super(params);
    }

    @Override
    public AbstractFilter fetch(String type, String[] args){

        AbstractFilter filter;

        double lb;
        double ub;
        int[] cardinality = new int[2];

        try{

            switch (type) {
                case "inclinationRange":

                    lb = Double.parseDouble(args[0]);
                    ub = Double.parseDouble(args[1]);
                    if(args[2].contains("~")){
                        String[] card = args[2].split("~");
                        cardinality[0] = Integer.parseInt(card[0]);
                        cardinality[1] = Integer.parseInt(card[1]);
                    }

                    filter = new InclinationRange(super.params, lb, ub, cardinality);
                    break;

                case "altitudeRange":

                    lb = Double.parseDouble(args[0]);
                    ub = Double.parseDouble(args[1]);
                    if(args[2].contains("~")){
                        String[] card = args[2].split("~");
                        cardinality[0] = Integer.parseInt(card[0]);
                        cardinality[1] = Integer.parseInt(card[1]);
                    }

                    filter = new AltitudeRange(super.params, lb, ub, cardinality);
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
