/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.constellation;

import java.util.ArrayList;
import ifeed.local.params.BaseParams;

/**
 *
 * @author bang
 */

public class Params extends BaseParams {

    protected int numSats = 10;
    protected String[] orbitalParams = {"sma","inc","raan","ta"};
    protected ArrayList<String>  orbitalParamsList;

    public Params(){
        orbitalParamsList = new ArrayList<String>();
        for(String p: this.orbitalParams){
            orbitalParamsList.add(p);
        }
    }

    public Params(Params params){
        this.numSats = params.numSats;
        this.orbitalParams = params.orbitalParams;
    }

    public void setNumSats(int numSats) {
        this.numSats = numSats;
    }

    public void setOrbitalParams(String[] orbitalParams){
        this.orbitalParams = orbitalParams;
    }

    public int getNumSats(){
        return this.numSats;
    }

    public String[] getOrbitalParams(){ return this.orbitalParams; }

    public ArrayList<String> getOrbitalParamsList(){return this.orbitalParamsList;}

}
