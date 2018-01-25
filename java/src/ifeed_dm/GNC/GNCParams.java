/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.GNC;

import ifeed_dm.EOSS.*;


/**
 *
 * @author bang
 */
public class GNCParams {

    public static boolean tallMatrix = false;
    
    public static String[] input_list = {"NS","NC","sensors","computers","Ibin_1","Ibin_2","Ibin_3","Ibin_4","Ibin_5","Ibin_6","Ibin_7","Ibin_8","Ibin_9","Inat_1","Inat_2","Inat_3"};
    public static String[] output_list = {"m","R","MTTF","Nlinks","N9"};
    
    public static int NS_index = 0;
    public static int NC_index = 1;
    public static int Ibin_1_index = 4;
    public static int Ibin_9_index = 12;
    
    public static boolean use_only_input_features = false;
}
