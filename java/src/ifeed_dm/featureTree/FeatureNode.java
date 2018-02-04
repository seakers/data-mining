/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.featureTree;

import ifeed_dm.LogicOperator;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.StringJoiner;

/**
 *
 * @author bang
 */

public class FeatureNode extends Node {

    public FeatureNode(LogicNode parent, String name, BitSet matches){
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
