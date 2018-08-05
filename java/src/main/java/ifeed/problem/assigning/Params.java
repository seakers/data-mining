/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.assigning;


import ifeed.local.params.BaseParams;

/**
 *
 * @author bang
 */
public class Params extends BaseParams {

    protected boolean tallMatrix;
    protected int numInstruments;
    protected int numOrbits;
    protected boolean useOnlyInputFeatures;

    public Params(){
        tallMatrix = false;
        numInstruments = 12;
        numOrbits = 5;
        useOnlyInputFeatures = false;
    }

    public void setNumInstruments(int numInstruments) {
        this.numInstruments = numInstruments;
    }

    public void setNumOrbits(int numOrbits){
        this.numOrbits = numOrbits;
    }

    public int getNumOrbits(){
        return this.numOrbits;
    }

    public int getNumInstruments(){
        return this.numInstruments;
    }

    public boolean isTallMatrix(){
        return this.tallMatrix;
    }

    public boolean isUseOnlyInputFeatures(){
        return this.useOnlyInputFeatures;
    }
}
