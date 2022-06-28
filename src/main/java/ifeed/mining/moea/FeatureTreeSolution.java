/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.mining.moea;

import org.moeaframework.core.Solution;

import java.util.StringJoiner;

/**
 * The solution is defined by the types of decisions and the values of those
 * decisions
 *
 * @author hsbang
 */

public class FeatureTreeSolution extends Solution {

    private static final long serialVersionUID = -2195550924166538032L;

    public FeatureTreeSolution(Solution solution) {
        super(solution);
        if (solution instanceof FeatureTreeSolution) {
            FeatureTreeSolution feature = (FeatureTreeSolution) solution;
            FeatureTreeVariable tree = (FeatureTreeVariable) feature.getVariable(0);
            super.setVariable(0, tree.copy());
        }
    }

    public FeatureTreeSolution(FeatureTreeVariable input, int numberOfObjectives) {
        super(1, numberOfObjectives);
        super.setVariable(0, input);
    }

    @Override
    public Solution copy() {
        return new FeatureTreeSolution(this);
    }

    /**
     * Returns the values of each decision
     *
     * @return
     */
    @Override
    public String toString() {
        // TODO: To be implemented
        StringJoiner objectives = new StringJoiner(",");
        for (int i = 0; i < super.getNumberOfObjectives(); i++) {
            objectives.add(Double.toString(super.getObjective(i)));
        }
        return objectives.toString() + ":: " + super.getVariable(0).toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FeatureTreeSolution other = (FeatureTreeSolution) obj;

        FeatureTreeVariable var = (FeatureTreeVariable) this.getVariable(0);
        FeatureTreeVariable otherVar = (FeatureTreeVariable) other.getVariable(0);

        return var.equals(otherVar);
    }
}
