/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.feature.logic;

import java.util.BitSet;
import ifeed.expression.Symbols;
/**
 *
 * @author bang
 */

public class Literal extends Formula {

    String name;

    public Literal(String name, BitSet matches){
        super();
        super.matches = matches;
        this.name = name;
    }

    public String getName(){

        String copy;

        if(this.name.contains(Symbols.individual_expression_wrapper_open)){
            copy = this.name;

        }else{
            copy = Symbols.individual_expression_wrapper_open + this.name + Symbols.individual_expression_wrapper_close;
        }

        if(super.negation){
            copy = copy.replace(Symbols.individual_expression_wrapper_open,
                        Symbols.individual_expression_wrapper_open + Symbols.logic_not);
        }

        return copy;
    }

    public BitSet getMatches(){
        if(super.negation){
            BitSet copy = (BitSet) super.matches.clone();
            copy.flip(0, copy.size());
            return copy;
        }else {
            return super.matches;
        }
    }

    public Literal copy(){
        Literal copied = new Literal(this.name, super.matches);
        copied.setNegation(this.negation);
        return copied;
    }
}
