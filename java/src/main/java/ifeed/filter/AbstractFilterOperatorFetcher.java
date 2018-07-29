package ifeed.filter;

import ifeed.expression.Fetcher;
import java.util.Arrays;

public abstract class AbstractFilterOperatorFetcher extends Fetcher{

    public FilterOperator fetch(String fullExpression){

        FilterOperator out;

        try{
            String[] nameAndArgs = super.getNameAndArgs(fullExpression);
            String type = nameAndArgs[0];
            String[] args = Arrays.copyOfRange(nameAndArgs, 1, nameAndArgs.length + 1);

            out = this.fetch(type, args);

            if(out == null){
                throw new RuntimeException("AbstractFilter could not be fetched from: " + fullExpression);
            }

        }catch(Exception e){
            System.out.println("Exc in fetching a filter from an expression");
            e.printStackTrace();
            return null;
        }

        return out;
    }

    public abstract FilterOperator fetch(String type, String[] args);

}
