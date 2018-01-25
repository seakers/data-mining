/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.DiscreteInput;

import ifeed_dm.Architecture;

/**
 *
 * @author bang
 */
public class DiscreteInputArchitecture implements Architecture{
    
    private int id;
    private double[] outputs;
    private int[] inputs;
    
    public DiscreteInputArchitecture(int id, int[] inputs, double[] outputs){
        this.id = id;
        this.outputs = outputs;
        this.inputs = inputs;
    }
    
    public int getID(){return id;}
    public double[] getOutputs(){return outputs;}
    public int[] getInputs(){return inputs;}
}

