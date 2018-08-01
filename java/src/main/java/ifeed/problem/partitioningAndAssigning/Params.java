/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.problem.partitioningAndAssigning;


import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

/**
 *
 * @author bang
 */
public class Params {
    public static boolean tallMatrix = false;
    public static int num_instruments = 5;
    public static int num_orbits = 5;
    public static boolean use_only_input_features = false;

    public static int[] repairInput(int[] input){

        int[] out = new int[input.length];

        Set<Integer> origianlSatIndices = new HashSet<>();
        Map<Integer, Integer> original2NewSatIndices= new HashMap<>();

        int index = 0;
        for(int i = 0; i < num_instruments; i++){
            int originalIndex = input[i];
            if(!origianlSatIndices.contains(originalIndex)){
                origianlSatIndices.add(originalIndex);
                original2NewSatIndices.put(originalIndex, index);
                index++;
            }

            out[i] = original2NewSatIndices.get(originalIndex);
        }

        for(int i = 0; i < num_instruments; i++){
            out[i + num_instruments] = -1;
        }
        for(int originalIndex:origianlSatIndices){
            int newIndex = original2NewSatIndices.get(originalIndex);
            out[newIndex + num_instruments] = input[originalIndex + num_instruments];
        }

        return out;
    }
}
