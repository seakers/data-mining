/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.GNC.filters;

import ifeed_dm.discreteInput.DiscreteInputFilter;
import ifeed_dm.GNC.GNCParams;

/**
 *
 * @author bang
 */
public class NumSensorOfType implements DiscreteInputFilter {

    private final int n;
    private final int sensor;

    public NumSensorOfType(int sensor, int n){
        this.n = n;
        this.sensor = sensor;
    }
    
    @Override
    public boolean apply(int[] input){
        
        String sensorInput = Integer.toString(input[GNCParams.sensors_index]);
        int cnt = 0;

        int leng = sensorInput.length();
        for(int i = 0; i < leng; i++){
            if(i == leng-1){ // Last digit
                if(this.sensor == Integer.parseInt(sensorInput.substring(i))){
                    cnt++;
                }
            }else{
                if(this.sensor == Integer.parseInt(sensorInput.substring(i,i+1))){
                    cnt++;
                }
            }
        }
        return cnt == this.n;
    }
    
    @Override
    public String getName(){return "numSensorOfType";}
    
    @Override
    public String toString(){
        return "{numSensorOfType[" + Integer.toString(this.sensor) + ";" + this.n + "]}";
    }
}
