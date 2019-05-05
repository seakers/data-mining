package ifeed.expression;

public class Symbols {

    public static final String logic_and = "&&";
    public static final String logic_or = "||";
    public static final String logic_or_regex = "\\|\\|";
    public static final String logic_not = "~";
    public static final String logic_conditional = "_IF_";
    public static final String logic_consequent = "_THEN_";
    public static final String logic_alternative = "_ELSE_";

    public static final String individual_expression_wrapper_open = "{";
    public static final String individual_expression_wrapper_close = "}";

    public static final String compound_expression_wrapper_open = "(";
    public static final String compound_expression_wrapper_close = ")";

    public static final String argument_wrapper_open = "[";
    public static final String argument_wrapper_close = "[";
    public static final String argument_wrapper_open_regex = "\\[";
    public static final String argument_wrapper_close_regex = "\\]";

    public static final String argument_type_separator = ";";
    public static final String argument_separator = ",";

    public static final String placeholder_marker = "PLACEHOLDER";

}
