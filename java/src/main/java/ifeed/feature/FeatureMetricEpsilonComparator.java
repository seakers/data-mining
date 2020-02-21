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
 * @author hsbang
 */
public class FeatureMetricEpsilonComparator implements Comparator<Feature> {

    private final FeatureMetric mode;

    private final double epsilon;

    public FeatureMetricEpsilonComparator(FeatureMetric mode, double epsilon) {
        this.mode = mode;
        this.epsilon = epsilon;
    }

    public int compareEpsilonDouble(double d1, double d2){
        if (d1 < d2 - epsilon)
            return -1;           // Neither val is NaN, thisVal is smaller
        if (d1 > d2 + epsilon)
            return 1;            // Neither val is NaN, thisVal is larger
        if (d1 >= d2 - epsilon && d1 <= d2 + epsilon){
            return 0;
        }
        // Cannot use doubleToRawLongBits because of possibility of NaNs.
        long thisBits    = Double.doubleToLongBits(d1);
        long anotherBits = Double.doubleToLongBits(d2);

        return (thisBits == anotherBits ?  0 : // Values are equal
                (thisBits < anotherBits ? -1 : // (-0.0, 0.0) or (!NaN, NaN)
                        1));
    }

    @Override
    public int compare(Feature f1, Feature f2) {
        switch (mode) {
            case SUPPORT:
                return compareEpsilonDouble(f1.getSupport(), f2.getSupport());
            case PRECISION:
                return compareEpsilonDouble(f1.getPrecision(), f2.getPrecision());
            case RECALL:
                return compareEpsilonDouble(f1.getRecall(), f2.getRecall());
            case LIFT:
                return compareEpsilonDouble(f1.getLift(), f2.getLift());
            case DISTANCE2UP:
                return compareEpsilonDouble(f1.getDistance2UP(), f2.getDistance2UP());
            default:
                throw new UnsupportedOperationException("unknown mode");
        }
    }

}
