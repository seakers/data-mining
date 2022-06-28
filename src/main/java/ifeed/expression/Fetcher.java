package ifeed.expression;

import java.util.*;

public abstract class Fetcher {

    private String _expression = "";
    private List<String> _names;
    private List<String[]> _args;

    public List<String> getNames(String expression){
        if(!this._expression.equals(expression)){
            this.extractNameAndArgs(expression);
        }
        return this._names;
    }

    public List<String[]> getArgs(String expression){
        if(!this._expression.equals(expression)){
            this.extractNameAndArgs(expression);
        }
        return this._args;
    }

    private void extractNameAndArgs(String expression){
        this._expression = expression;

        String e = Utils.remove_outer_parentheses(expression);

        if(e.split(Symbols.argument_wrapper_open_regex).length == 1){
            throw new RuntimeException("AbstractFilter expression without arguments: " + expression);
        }

        // Remove individual feature wrapper
        if(e.startsWith(Symbols.individual_expression_wrapper_open) && e.endsWith(Symbols.individual_expression_wrapper_close)){
            e = e.substring(1, e.length() - 1);
        }

        List<String> names = new ArrayList<>();
        List<String[]> args = new ArrayList<>();

        // Check if there is a single or multiple elements
        if(e.indexOf(Symbols.argument_wrapper_open) != e.lastIndexOf(Symbols.argument_wrapper_open)){
            // Multiple elements
            String[] splitElements = e.split(Symbols.argument_wrapper_close_regex);

            for(String elem: splitElements){
                String elementName = elem.split(Symbols.argument_wrapper_open_regex)[0];
                String argCombined = elem.split(Symbols.argument_wrapper_open_regex)[1];
                String[] argSplit = argCombined.split(Symbols.argument_type_separator);
                names.add(elementName);
                args.add(argSplit);
            }

        }else{
            // Single element
            String argCombined = e.split(Symbols.argument_wrapper_open_regex)[1];
            argCombined = argCombined.substring(0, argCombined.length() - Symbols.argument_wrapper_close.length());
            String[] argSplit = argCombined.split(Symbols.argument_type_separator);

            names.add(e.split(Symbols.argument_wrapper_open_regex)[0]);
            args.add(argSplit);
        }

        this._names = names;
        this._args = args;
    }
}
