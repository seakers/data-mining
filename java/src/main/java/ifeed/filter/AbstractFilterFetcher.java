package ifeed.filter;

import ifeed.expression.Fetcher;
import java.util.Arrays;

public abstract class AbstractFilterFetcher extends Fetcher {

    public AbstractFilter fetch(String fullExpression){

        AbstractFilter out;

        try{

            String[] nameAndArgs = super.getNameAndArgs(fullExpression);
            String type = nameAndArgs[0];
            String[] args = Arrays.copyOfRange(nameAndArgs, 1, nameAndArgs.length + 1);

            out = this.fetch(type, args);

            if(out == null){
                throw new RuntimeException("AbstractFilter could not be fetched from: " + fullExpression);
            }

        }catch(Exception e){
            System.out.println("Exc in fetching a filter from an expression: " + fullExpression);
            e.printStackTrace();
            return null;
        }

        return out;
    }

    public abstract AbstractFilter fetch(String type, String[] args);
}
