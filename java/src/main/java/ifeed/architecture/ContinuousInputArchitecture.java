/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.architecture;

import java.util.BitSet;

/**
 *
 * @author bang
 */
public class ContinuousInputArchitecture implements AbstractArchitecture {

    private int id;
    private double[] outputs;
    private double[] inputs;

    public ContinuousInputArchitecture(int id, double[] inputs, double[] outputs) {
        this.id = id;
        this.outputs = outputs;
        this.inputs = inputs;
    }
    
    public int getID(){return id;}
    public double[] getOutputs(){return outputs;}
    public double[] getInputs(){return inputs;}

}
