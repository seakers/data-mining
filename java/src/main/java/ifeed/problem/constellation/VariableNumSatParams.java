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

public class VariableNumSatParams extends AbstractConstellationProblemParams {

    final private int maxNumSats = 20;
    final private String[] orbitalParams = {"sma","inc","raan","ta"};

    public VariableNumSatParams(){
        super.setNumSats(maxNumSats);
        super.setOrbitalParameters(orbitalParams);
        super.setNumSatsFixed(false);
    }
}
