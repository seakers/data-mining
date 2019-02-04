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

public class AbstractConstellationProblemParams extends BaseParams {

    protected boolean isNumSatsFixed;
    protected int numSats;
    protected String[] orbitalParameters;
    protected ArrayList<String> orbitalParametersList;

    public AbstractConstellationProblemParams(){
        isNumSatsFixed = false;
    }

    public AbstractConstellationProblemParams(AbstractConstellationProblemParams params){
        this.isNumSatsFixed = params.isNumSatsFixed;
        this.numSats = params.numSats;
        this.orbitalParameters = params.orbitalParameters;
        this.orbitalParametersList = params.orbitalParametersList;
    }

    public void setNumSats(int numSats) {
        this.numSats = numSats;
    }

    public void setOrbitalParameters(String[] orbitalParameters){
        this.orbitalParameters = orbitalParameters;
        this.orbitalParametersList = new ArrayList<>();
        for(String p: this.orbitalParameters){
            this.orbitalParametersList.add(p);
        }
    }

    public void setNumSatsFixed(boolean isNumSatsFixed){
        this.isNumSatsFixed = isNumSatsFixed;
    }

    public int getNumSats(){
        return this.numSats;
    }

    public String[] getOrbitalParameters(){ return this.orbitalParameters; }

    public ArrayList<String> getOrbitalParametersList(){return this.orbitalParametersList;}

    public boolean isNumSatsFixed(){
        return this.isNumSatsFixed;
    }
}
