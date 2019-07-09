/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.feature;


import java.util.BitSet;

/**
 * A binary feature
 *
 * @author bang
 */

public class GeneralizableFeature extends Feature {

    private int numGeneralizations;
    private int numExceptionVariables;

    public GeneralizableFeature(String name, BitSet matches, double support, double lift, double precision, double recall, double complexity) {
        super(name, matches, support, lift, precision, recall, complexity);
        this.numExceptionVariables = 0;
        this.numGeneralizations = 0;
    }

    public GeneralizableFeature(String name, BitSet matches, double support, double lift, double precision, double recall) {
        this(name, matches, support, lift, precision, recall, -1);
    }

    public GeneralizableFeature(String name, BitSet matches) {
        this(name, matches, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    public GeneralizableFeature(BitSet matches, double support, double lift, double precision, double recall) {
        this(null, matches, support, lift, precision, recall);
    }

    public GeneralizableFeature(Feature inputFeature){
        this(inputFeature.getName(), inputFeature.getMatches(), inputFeature.getSupport(),
                inputFeature.getLift(), inputFeature.getPrecision(), inputFeature.getRecall(), inputFeature.getComplexity());
    }

    @Override
    public GeneralizableFeature copy(){
        GeneralizableFeature copied = new GeneralizableFeature(name, matches, support, lift, precision, recall, complexity);
        copied.setNumExceptionVariables(this.numExceptionVariables);
        copied.setNumGeneralizations(this.numGeneralizations);
        return copied;
    }

    public void setNumExceptionVariables(int numExceptions) {
        this.numExceptionVariables = numExceptions;
    }

    public void setNumGeneralizations(int numGeneralizations) {
        this.numGeneralizations = numGeneralizations;
    }

    public int getNumExceptionVariables() {
        return numExceptionVariables;
    }

    public int getNumGeneralizations() {
        return numGeneralizations;
    }
}
