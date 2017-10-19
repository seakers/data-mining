/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;

import java.util.List;
import java.util.ArrayList;
/**
 *
 * @author bang
 */
public abstract class DataMining {
    
    private double support_threshold;
    private double confidence_threshold;
    private double lift_threshold;
    private double [] thresholds;
    
    private List<BinaryInputArchitecture> architectures;
    
    private List<Integer> behavioral;
    private List<Integer> non_behavioral;
    private List<Integer> population;

    
    public DataMining(List<Integer> behavioral, List<Integer> non_behavioral, List<BinaryInputArchitecture> architectures,
                            double supp, double conf, double lift){
    
        this.support_threshold = supp;
        this.confidence_threshold = conf;
        this.lift_threshold = lift;
        
        this.thresholds = new double[3];
        thresholds[0] = support_threshold;
        thresholds[1] = lift_threshold;
        thresholds[2] = confidence_threshold;

        this.architectures = architectures;
        this.behavioral = behavioral;
        this.non_behavioral = non_behavioral;

        this.population = new ArrayList<>();
        this.population.addAll(this.behavioral);
        this.population.addAll(this.non_behavioral);
        
    }
    
    
    public void run(){
    }
    
    public List<BinaryInputArchitecture> getArchitectures(){return this.architectures;}
    public List<Integer> getBehavioral(){return this.behavioral;}
    public List<Integer> getNon_behavioral(){return this.non_behavioral;}
    public List<Integer> getPopulation(){return this.population;}
    
}
