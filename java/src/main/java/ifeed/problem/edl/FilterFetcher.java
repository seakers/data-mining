package ifeed.problem.edl;

import ifeed.expression.Symbols;
import ifeed.expression.Utils;
import ifeed.filter.AbstractFilter;
import ifeed.filter.AbstractFilterFetcher;
import ifeed.local.params.BaseParams;

import java.util.ArrayList;
import java.util.List;

public class FilterFetcher extends AbstractFilterFetcher {

    Params params;

    public FilterFetcher(BaseParams params){
        super(params);
        this.params = (Params) params;
    }

    @Override
    public AbstractFilter fetch(List<String> names, List<String[]> argSets){

        AbstractFilter filter;
        try{
            // Assume that names.size() == 1 && argSets.size() == 1
            String filterName = names.get(0);
            String[] args = argSets.get(0);

            String varName = args[0];
            String value = args[1];
            filter = new DiscreteValueFilter(params, varName, Integer.parseInt(value));

        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Exc in fetching a feature using the variable: " + names.get(0));
        }
        return filter;
    }
}
