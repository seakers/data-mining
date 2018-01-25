/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.BinaryInput;

import ifeed_dm.Architecture;
import java.util.BitSet;

/**
 *
 * @author bang
 */
public class BinaryInputArchitecture implements Architecture{
    
    private int id;
    private double[] outputs;
    private BitSet inputs;

    public BinaryInputArchitecture(int id, BitSet inputs, double[] outputs) {
        this.id = id;
        this.outputs = outputs;
        this.inputs = inputs;
    }
    
    public int getID(){return id;}
    public double[] getOutputs(){return outputs;}
    public BitSet getInputs(){return inputs;}

}
