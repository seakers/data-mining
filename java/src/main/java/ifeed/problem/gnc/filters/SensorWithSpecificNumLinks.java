/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.gnc.filters;

import ifeed.architecture.AbstractArchitecture;
import ifeed.architecture.DiscreteInputArchitecture;
import ifeed.filter.AbstractFilter;
import ifeed.local.params.BaseParams;
import ifeed.problem.gnc.Params;

import java.util.ArrayList;

/**
 *
 * @author bang
 */
public class SensorWithSpecificNumLinks extends AbstractFilter {

    protected Params params;
    private final int n;
    private final int sensor;

    public SensorWithSpecificNumLinks(BaseParams params, int sensor, int n){
        super(params);
        this.params = (Params) params;
        this.n = n;
        this.sensor = sensor;
    }

    @Override
    public boolean apply(AbstractArchitecture a){
        return this.apply(((DiscreteInputArchitecture) a).getInputs());
    }

    @Override
    public boolean apply(int[] input){
        
        String sensorInput = Integer.toString(input[params.getSensors_index()]);
        ArrayList<Integer> targetSensors = new ArrayList<>();

        int ns = input[0];
        int nc = input[1];

        for(int i =0;i<sensorInput.length();i++){
            if(i == sensorInput.length()-1){ // Last digit
                if(this.sensor == Integer.parseInt(sensorInput.substring(i))){
                    targetSensors.add(i);
                }
            }else{
                if(this.sensor == Integer.parseInt(sensorInput.substring(i,i+1))){
                    targetSensors.add(i);
                }
            }
        }

        for(int i = 0; i < ns; i++){
            if (targetSensors.contains(i)) {
                // Count the number of links connected to each sensor of interest
                int cnt = 0;
                for(int j = 0; j < nc; j++){
                    int link = input[params.getIbin_1_index() + i * nc + j];
                    if(link == 1 || link == 49){
                        cnt++;
                    }
                }
                if(cnt == n){
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public String getName(){return "sensorWithSpecificNumLinks";}
    
    @Override
    public String toString(){
        return "{sensorWithSpecificNumLinks[" + Integer.toString(this.sensor) + ";" + this.n + "]}";
    }
}
