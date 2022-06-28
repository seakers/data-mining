package ifeed.problem.dshield_opt1;

import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFetcher;
import ifeed.local.params.BaseParams;
import ifeed.problem.dshield_opt1.filters.NumSatellites;

import java.util.List;

public class FilterFetcher extends AbstractFilterFetcher {

    public FilterFetcher(BaseParams params){
        super(params);
    }

    public AbstractFilter fetch(List<String> names, List<String[]> argSets){

        AbstractFilter filter;
        String type = names.get(0);
        String[] args = argSets.get(0);

        try{
            switch (type) {
                case "numSatellites":
                    filter = new NumSatellites(params, Integer.parseInt(args[0]));
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
