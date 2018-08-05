package ifeed.mining.moea.operators;

import ifeed.mining.moea.MOEABase;
import org.moeaframework.core.Solution;
import org.moeaframework.core.comparator.ParetoDominanceComparator;

public abstract class AbstractHillClimbingCrossover extends AbstractFeatureCrossover{

    protected int maxIter;
    protected final ParetoDominanceComparator comparator;

    public AbstractHillClimbingCrossover(double probability, MOEABase base, int maxIter){
        super(probability, base);
        this.maxIter = maxIter;
        this.comparator = new ParetoDominanceComparator();
    }

    public boolean nonDominated(Solution[] parents, Solution[] offspring){
        for(Solution p:parents){
            for(Solution o:offspring){
                if(nonDominated(p,o)){
                    return true;
                }
            }
        }

        return false;
    }

    public boolean nonDominated(Solution parent, Solution offspring){
        switch (comparator.compare(offspring, parent)) {
            case -1:
                // Offspring dominates
                return true;
            case 0:
                // No one dominates
                return true;
            default:
                // Parent dominates
                return false;
        }
    }

}
