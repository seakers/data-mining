/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.constellation;

/**
 *
 * @author bang
 */

public class FixedNumSatParams extends AbstractConstellationProblemParams {

    final private int numSats = 10;
    final private String[] orbitalParams = {"sma","inc","raan","ta"};

    public FixedNumSatParams(int numSats){
        super.setNumSats(numSats);
        super.setOrbitalParameters(orbitalParams);
        super.setNumSatsFixed(true);
    }

    public FixedNumSatParams(){
        super.setNumSats(numSats);
        super.setOrbitalParameters(orbitalParams);
        super.setNumSatsFixed(true);
    }
}
