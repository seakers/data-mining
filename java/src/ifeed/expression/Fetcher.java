package ifeed.expression;


public abstract class Fetcher {

    public String[] getNameAndArgs(String expression){

        String e = Utils.remove_outer_parentheses(expression);

        if(e.split(Symbols.argument_wrapper_open).length==1){
            throw new RuntimeException("Filter expression without arguments: " + expression);
        }

        String type = e.split(Symbols.argument_wrapper_open)[0];
        String argsCombined = e.substring(0, e.length() - Symbols.argument_wrapper_close.length()).split(Symbols.argument_wrapper_close)[1];
        String[] args = argsCombined.split(Symbols.argument_type_separator);

        String[] out = new String[args.length+1];
        out[0] = type;
        for(int i = 0; i < args.length; i++){
            out[i+1] = args[i];
        }
        return out;
    }
}
