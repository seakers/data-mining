package ifeed.expression;

import java.util.ArrayList;

public class Utils {

    public static String remove_outer_parentheses(String expression){

        if(expression.startsWith(Symbols.compound_expression_wrapper_open) && expression.endsWith(Symbols.compound_expression_wrapper_close)){
            int l = expression.length();
            int level = 0;
            int paren_end = -1;
            for(int i = 0; i < l; i++){
                if(expression.charAt(i) == Symbols.compound_expression_wrapper_open.charAt(0)){
                    level++;

                }else if(expression.charAt(i) == Symbols.compound_expression_wrapper_close.charAt(0)){
                    level--;

                    if(level == 0){
                        paren_end = i;
                        break;
                    }
                }
            }
            if(paren_end == l-1){
                String new_expression = expression.substring(1, l-1);
                return remove_outer_parentheses(new_expression);
            }else{
                return expression;
            }
        }else{
            return expression;
        }
    }

    /**
     * This function checks if the input string contains a parenthesis
     *
     * @param inputString
     * @return boolean
     */
    public static boolean checkParen(String inputString){
        return inputString.contains(Symbols.compound_expression_wrapper_open);
    }

    /**
     * This function counts the number of slots in an expression.
     * @param inputString
     * @return
     */
    public static int getNumOfSlots(String inputString){
        int leng = inputString.length();
        int cnt = 0;
        int level = 0;
        for (int i = 0;i<leng;i++){
            if(inputString.charAt(i) == Symbols.compound_expression_wrapper_open.charAt(0)){
                level++;
                if (level == 1) cnt++;
            }
            if(inputString.charAt(i) == Symbols.compound_expression_wrapper_close.charAt(0)){
                level--;
            }
        }
        return cnt;
    }

    /**
     * This function returns the indices of the parenthesis in a string
     * @param inputString
     * @param n: This function looks for nth appearance of the parenthesis
     * @return int[]: Integer array containing the indices of the parentheses within a string
     */
    public static int[] locateParen(String inputString,int n){ // locate nth parentheses

        int level = 0;
        int nth = 0;
        int leng = inputString.length();
        int[] parenLoc = new int[2];

        for (int i = 0; i<leng ;i++){
            char ch = inputString.charAt(i);
            if(ch == Symbols.compound_expression_wrapper_open.charAt(0)){
                level++;
                if (level == 1) nth++;
                if ((nth == n) && (level == 1))  parenLoc[0] = i;
            }
            if(ch == Symbols.compound_expression_wrapper_close.charAt(0) ){
                level--;
            }
            if((level == 0) && (nth == n)) {
                parenLoc[1] = i;
                break;
            }
        }
        return parenLoc;
    }



    /**
     * This function replaces the contents of all parentheses with a character 'X'.
     * This is used to analyze the outermost structure of the given expression (by removing all nested structure).
     * @param inputExpression
     * @return
     */
    public static String collapseAllParenIntoSymbol(String inputExpression){

        // If the given expression doesn't contain any parenthesis, return
        if (checkParen(inputExpression) == false) return inputExpression;


        int num = getNumOfSlots(inputExpression);
        String expression = inputExpression;

        for (int i = 0;i<num;i++){
            int[] loc = locateParen(expression,i+1);
            String s1 = expression.substring(0, loc[0]+1);
            String s2 = expression.substring(loc[1]);
            String symbol = "";
            for (int j = 0;j< loc[1]-loc[0]-1 ;j++) symbol = symbol.concat("X");
            expression = s1 + symbol + s2;
        }
        return expression;
    }


    public static ArrayList<Integer> locateNestedParen(String inputString, int focusLevel){ // locate all parentheses at specified level

        int level = 0;
        int nth = 0;
        int leng = inputString.length();
        ArrayList<Integer> parenLoc = new ArrayList<>();

        for (int i = 0; i<leng ;i++){
            if(inputString.charAt(i) == Symbols.compound_expression_wrapper_open.charAt(0)){
                level++;
                if (level == focusLevel)  parenLoc.add(i);
            }
            if(inputString.charAt(i) == Symbols.compound_expression_wrapper_close.charAt(0) ){
                if (level == focusLevel) parenLoc.add(i);
                level--;
            }
        }
        return parenLoc;
    }


    public static int getNestedParenLevel(String inputString){
        int leng = inputString.length();
        int cnt = 0;
        int level = 0;
        int maxLevel = 0;

        for (int i = 0;i<leng;i++){
            if(inputString.charAt(i) == Symbols.compound_expression_wrapper_open.charAt(0)){
                level++;
                if (level > maxLevel) maxLevel = level;
            }
            if(inputString.charAt(i) == Symbols.compound_expression_wrapper_close.charAt(0) ){
                level--;
            }
        }
        return maxLevel;
    }

    public static int countMatchesInString(String input, String targetString){
        return input.length() - input.replace(targetString, "").length();
    }

}
