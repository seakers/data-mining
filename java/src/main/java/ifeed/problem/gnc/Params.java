/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.gnc;


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
    public int NC_index;
    public int sensors_index;
    public int computers_index;
    public int Ibin_1_index;
    public int Ibin_9_index;
    public boolean use_only_input_features;

    public Params(){
        tallMatrix = false;

        String[] input_list = {"NS","NC","sensors","computers","Ibin_1","Ibin_2","Ibin_3","Ibin_4","Ibin_5","Ibin_6","Ibin_7","Ibin_8","Ibin_9","Inat_1","Inat_2","Inat_3"};
        String[] output_list = {"m","R","MTTF","Nlinks","N9"};

        this.inputList = input_list;
        this.outputList = output_list;

        NS_index = 0;
        NC_index = 1;
        sensors_index = 2;
        computers_index = 3;
        Ibin_1_index = 4;
        Ibin_9_index = 12;
        use_only_input_features = false;
    }

    public String[] getInputList() {
        return this.inputList;
    }

    public int getNS_index(){
        return this.NS_index;
    }

    public int getNC_index(){
        return this.NC_index;
    }

    public int getSensors_index(){
        return this.sensors_index;
    }

    public int getComputers_index(){
        return this.computers_index;
    }

    public int getIbin_1_index(){
        return this.Ibin_1_index;
    }

    public int getIbin_9_index(){
        return this.Ibin_9_index;
    }

}




