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

    public Literal(Connective parent, String name, BitSet matches){
        super(parent);
        super.name = new StringJoiner(",").add(name);
        super.matches = matches;
    }

    public String getName(){
        return super.name.toString();
    }

    public BitSet getMatches(){
        return super.matches;
    }
}
