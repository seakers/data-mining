package ifeed.filter;

import ifeed.expression.Fetcher;
import ifeed.local.params.BaseParams;
import java.util.List;

public abstract class AbstractFilterOperatorFetcher extends Fetcher{

    protected BaseParams params;

    public AbstractFilterOperatorFetcher(BaseParams params){
        this.params = params;
    }

    public FilterOperator fetch(String expression){

        FilterOperator out;

        try{
            List<String> names = super.getNames(expression);
            List<String[]> args = super.getArgs(expression);
            out = this.fetch(names, args);

            if(out == null){
                throw new RuntimeException("AbstractFilter could not be fetched from: " + expression);
            }

        }catch(Exception e){
            System.out.println("Exc in fetching a filter from an expression");
            e.printStackTrace();
            return null;
        }

        return out;
    }

    public abstract FilterOperator fetch(List<String> names, List<String[]> args);
}
