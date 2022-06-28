/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.feature;

/**
 * An Enum for the metrics for a feature (support, forward and reverse confidence, and lift)
 * @author nozomihitomi
 */
public enum FeatureMetric {
    
    SUPPORT, //support
    PRECISION, //forward confidence
    RECALL,
    LIFT,
    DISTANCE2UP
    
}
