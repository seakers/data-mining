package ifeed.filter;

import ifeed.expression.Fetcher;
import java.util.Arrays;

public abstract class FilterFetcher extends Fetcher {

    public Filter fetch(String fullExpression){

        Filter out;

        try{

            String[] nameAndArgs = super.getNameAndArgs(fullExpression);
            String type = nameAndArgs[0];
            String[] args = Arrays.copyOfRange(nameAndArgs, 1, nameAndArgs.length + 1);

            out = this.fetch(type, args);

            if(out == null){
                throw new RuntimeException("Filter could not be fetched from: " + fullExpression);
            }

        }catch(Exception e){
            System.out.println("Exc in fetching a filter from an expression");
            e.printStackTrace();
            return null;
        }

        return out;
    }

    public abstract Filter fetch(String type, String[] args);
}
