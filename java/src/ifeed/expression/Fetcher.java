package ifeed.expression;


public abstract class Fetcher {

    public String[] getNameAndArgs(String expression){

        String e = Utils.remove_outer_parentheses(expression);

        if(e.split(Symbols.argument_wrapper_open_regex).length == 1){
            throw new RuntimeException("Filter expression without arguments: " + expression);
        }

        // Remove individual feature wrapper
        if(e.startsWith(Symbols.individual_expression_wrapper_open) && e.endsWith(Symbols.individual_expression_wrapper_close)){
            e = e.substring(1, e.length() - 1);
        }

        String type = e.split(Symbols.argument_wrapper_open_regex)[0];
        String argsCombined = e.split(Symbols.argument_wrapper_open_regex)[1];
        argsCombined = argsCombined.substring(0, argsCombined.length() - Symbols.argument_wrapper_close.length());
        String[] args = argsCombined.split(Symbols.argument_type_separator);

        String[] out = new String[args.length+1];
        out[0] = type;
        for(int i = 0; i < args.length; i++){
            out[i+1] = args[i];
        }
        return out;
    }
}
