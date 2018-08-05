/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ifeed.mining.moea;

/**
 *
 * @author hsbang
 */

import org.moeaframework.core.Initialization;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;

public class FeatureExtractionInitialization implements Initialization {

    /**
     * The problem.
     */
    private final Problem problem;

    /**
     * The initial population size.
     */
    private final int populationSize;

    /**
     * type of initialization
     */
    private final String type;

    /**
     * Constructs a random initialization operator.
     *
     * @param problem the problem
     * @param populationSize the initial population size
     * @param type method to initialize the population
     */
    public FeatureExtractionInitialization(Problem problem, int populationSize, String type) {
        this.problem = problem;
        this.populationSize = populationSize;
        this.type = type;
    }

    @Override
    public Solution[] initialize() {
        Solution[] initialPopulation = new Solution[populationSize];

        for (int i = 0; i < populationSize; i++) {

            Solution solution = problem.newSolution();

            for (int j = 0; j < solution.getNumberOfVariables(); j++) {
                Variable variable = solution.getVariable(j);
                switch (type) {
                    case "random":
                        randInitializeVariable(variable);
                        break;
                    case "fullfactorial":
                        throw new UnsupportedOperationException("Full factorial enumeration is not yet supported");
                    default:
                        throw new IllegalArgumentException("No such initialization type: " + type);
                }
            }

            initialPopulation[i] = solution;
        }
        return initialPopulation;
    }

    /**
     * Initializes the specified decision variable randomly. This method
     * supports all built-in types, and can be extended to support custom types.
     *
     * @param variable the variable to be initialized
     */
    protected void randInitializeVariable(Variable variable) {
        variable.randomize();
    }
}
