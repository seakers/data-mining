/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.dshield_opt2;


import ifeed.local.params.BaseParams;

/**
 *
 * @author bang
 */
public class Params extends BaseParams{

    public boolean tallMatrix;
    public String[] inputList;
    public String[] outputList;

    public int NS_index;

    public int NP_index;

    public boolean use_only_input_features;

    public Params(){
        tallMatrix = false;

        String[] input_list = {"sats","planes","inc"};
        String[] output_list = {"cost","rev"};

        this.inputList = input_list;
        this.outputList = output_list;
        this.NS_index = 0;
        this.NP_index = 1;

        use_only_input_features = false;
    }

    public String[] getInputList() {
        return this.inputList;
    }

    public int getNS_index(){
        return this.NS_index;
    }
    public int getNP_index(){
        return this.NP_index;
    }

}




