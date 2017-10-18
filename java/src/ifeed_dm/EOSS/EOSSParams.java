/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS;


/**
 *
 * @author bang
 */
public class EOSSParams {

    public static boolean tallMatrix = false;

    public static String[] instrument_list = {"ACE_ORCA","ACE_POL","ACE_LID","CLAR_ERB","ACE_CPR","DESD_SAR","DESD_LID","GACM_VIS","GACM_SWIR","HYSP_TIR","POSTEPS_IRS","CNES_KaRIN"};
    public static String[] orbit_list = {"LEO-600-polar-NA", "SSO-600-SSO-AM", "SSO-600-SSO-DD","SSO-800-SSO-DD", "SSO-800-SSO-PM"};

    public static int num_instruments = 12;
    public static int num_orbits = 5;
    
}
