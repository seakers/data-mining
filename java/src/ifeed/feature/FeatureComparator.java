/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.feature;

import java.util.Comparator;

/**
 * Comparator to compare features based on support, forward confidence, reverse
 * confidence, or lift
 *
 * @author nozomihitomi
 */
public class FeatureComparator implements Comparator<Feature> {

    private final FeatureMetric mode;

    public FeatureComparator(FeatureMetric mode) {
        this.mode = mode;
    }

    @Override
    public int compare(Feature f1, Feature f2) {
        switch (mode) {
            case SUPPORT:
                return Double.compare(f1.getSupport(), f2.getSupport());
            case FCONFIDENCE:
                return Double.compare(f1.getFConfidence(), f2.getFConfidence());
            case RCONFIDENCE:
                return Double.compare(f1.getRConfidence(), f2.getRConfidence());
            case LIFT:
                return Double.compare(f1.getLift(), f2.getLift());
            case DISTANCE2UP:
                return Double.compare(f1.getDistance2UP(), f2.getDistance2UP());
            default:
                throw new UnsupportedOperationException("unknown mode");
        }
    }

}
