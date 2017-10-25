/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed_dm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author bang
 */
public class Utils {
    
    /**
     * Gets the top n features according to the specified metric in descending
     * order. If n is greater than the number of features found by Apriori, all
     * features will be returned.
     *
     * @param n the number of features desired
     * @param metric the metric used to sort the features
     * @return the top n features according to the specified metric in
     * descending order
     */
    
    public List<Feature> getTopFeatures(List<Feature> features, int n, FeatureMetric metric) {
    
        Collections.sort(features, new FeatureComparator(metric).reversed());
        
        return getTopFeatures(features,n);
    }
    
    
    
    public List<Feature> getTopFeatures(List<Feature> features, int n) {

        List<Feature> subList;
        
        if (features.size() > n) {
            
            subList = features.subList(0, n);

        }else{
            subList = features;
        }        

        return subList;
    }

    
    public boolean dominates(Feature f1, Feature f2, List<Comparator> comparators){
    
        boolean at_least_as_good_as = true;
        boolean better_than_in_one = false;
        
        for(int i=0;i<comparators.size();i++){

            if(comparators.get(i).compare(f1, f2) > 0){
                // First better than Second
                better_than_in_one=true;

            }else if(comparators.get(i).compare(f1, f2) < 0){
                // First is worse than Second
                at_least_as_good_as = false;
            }
        }

        return at_least_as_good_as && better_than_in_one; // First dominates Second
    }

    
    public List<Feature> getFeatureFuzzyParetoFront(List<Feature> population, List<Comparator> comparators, int paretoRank){
        
        List<Feature> fuzzy_pareto_front = new ArrayList<>();
        
        ArrayList<Feature> current_population = new ArrayList<>();

        for (Feature f:population) {
            current_population.add(f);
        }
        
        int iteration=0;
        while(iteration <= paretoRank){
            
            ArrayList<Feature> features_next_iter = new ArrayList<>();
        
            for(int i=0;i<current_population.size();i++){

                boolean dominated = false;
                Feature f1 = current_population.get(i);

                for(int j=0;j<current_population.size();j++){
                    
                    if(i==j) continue;
                    
                    Feature f2 = current_population.get(j);
                    if(dominates(f2,f1,comparators)){ // f1 is dominated
                        dominated=true;
                        break;
                    }
                }

                if(!dominated){
                    fuzzy_pareto_front.add(f1);
                }else{
                    features_next_iter.add(f1);
                }

            }
            
            current_population = features_next_iter;
            
            iteration++;
        }

        return fuzzy_pareto_front;
    }
    
    
    
    
    
}
