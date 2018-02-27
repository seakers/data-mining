/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.logic;

import java.util.BitSet;
import java.util.StringJoiner;

/**
 *
 * @author bang
 */

public class Literal extends Formula {

    public Literal(String name, BitSet matches){
        super();
        super.name = new StringJoiner(",").add(name);
        super.matches = matches;
    }

    public String getName(){
        if(super.negation){
            String name = super.name.toString();
            if(name.contains("{")){
                name = name.replace("{","{~");
            }
            return name;
        }else{
            return super.name.toString();
        }
    }

    public BitSet getMatches(){
        if(super.negation){
            BitSet copy = (BitSet) super.matches.clone();
            copy.flip(0,copy.size());
            return copy;
        }else {
            return super.matches;
        }
    }

    public Literal copy(){
        BitSet copiedMatches = (BitSet) super.matches;
        boolean negation = this.negation;
        Literal copied = new Literal(this.name.toString(), copiedMatches);
        copied.setNegation(negation);
        return copied;
    }
}
