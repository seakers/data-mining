package ifeed.filter;

import ifeed.expression.Fetcher;
import ifeed.local.params.BaseParams;

import java.util.List;
import java.util.Set;

public abstract class AbstractFilterFetcher extends Fetcher {

    protected BaseParams params;

    public AbstractFilterFetcher(BaseParams params){
        this.params = params;
    }

    public AbstractFilter fetch(String expression){
        AbstractFilter out;
        try{
            List<String> names = super.getNames(expression);
            List<String[]> args = super.getArgs(expression);
            out = this.fetch(names, args);
            if(out == null){
                throw new RuntimeException("AbstractFilter could not be fetched from: " + expression);
            }
        }catch(Exception e){
            System.out.println("Exc in fetching a filter from an expression: " + expression);
            e.printStackTrace();
            return null;
        }
        return out;
    }

    public abstract AbstractFilter fetch(List<String> type, List<String[]> args);
}
