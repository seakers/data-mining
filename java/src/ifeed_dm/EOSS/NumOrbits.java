/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm.EOSS;

import java.util.BitSet;
import ifeed_dm.AbstractFeatureBinary;
import ifeed_dm.EOSS.EOSSParams;
/**
 *
 * @author bang
 */
public class NumOrbits implements AbstractFeatureBinary {
    
    private int num;
    
    public NumOrbits(int n){
        this.num = n;
    }
    
    public boolean apply(BitSet input){

        int cnt = 0;
        for(int o=0;o<EOSSParams.num_orbits;o++){
            boolean used = false;
            for(int i=0;i<EOSSParams.num_instruments;i++){
                if(input.get(o*EOSSParams.num_instruments+i)){
                    used=true;
                    break;
                }
            }
            if(used){
                cnt++;
            }
        }

        return cnt==num;
    }
}
