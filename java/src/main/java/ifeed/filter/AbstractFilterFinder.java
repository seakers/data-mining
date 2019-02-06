package ifeed.filter;

import java.util.HashSet;
import java.util.Set;

/**
 * Class that is used to specify the filter for setting up constraints and the filters to be matched
 */
public abstract class AbstractFilterFinder {

    protected Set<Class> constraintSetterClasses;
    protected Set<Class> matchingClasses;
    protected int expectedNumMatchingFilter;

    protected AbstractFilterFinder(){}

    protected AbstractFilterFinder(Class constraintSetterClass){
        this.constraintSetterClasses = new HashSet<>();
        this.constraintSetterClasses.add(constraintSetterClass);
        this.matchingClasses = null;
        this.expectedNumMatchingFilter = 0;
    }

    protected AbstractFilterFinder(Class constraintSetterClass, Class matchingClass){
        this.constraintSetterClasses = new HashSet<>();
        this.constraintSetterClasses.add(constraintSetterClass);
        this.matchingClasses = new HashSet<>();
        this.matchingClasses.add(matchingClass);
        this.expectedNumMatchingFilter = 1;
    }

    protected AbstractFilterFinder(Class constraintSetterClass, Class matchingClass, int expectedNumMatchingFilter){
        this.constraintSetterClasses = new HashSet<>();
        this.constraintSetterClasses.add(constraintSetterClass);
        this.matchingClasses = new HashSet<>();
        this.matchingClasses.add(matchingClass);
        this.expectedNumMatchingFilter = expectedNumMatchingFilter;
    }

    public boolean hasMatchingClass(){
        if(this.expectedNumMatchingFilter != 0){
            return true;
        }else{
            return false;
        }
    }

    protected void setConstraintSetterClasses(Set<Class> constraintSetterClasses){
        this.constraintSetterClasses = constraintSetterClasses;
    }

    public Set<String> getMatchingClassName(){
        Set<String> names = new HashSet<>();
        for(Class c:this.matchingClasses){
            names.add(c.getSimpleName());
        }
        return names;
    }

    public boolean isConstraintSetterType(Class c){
        for(Class constraintSetter: this.constraintSetterClasses){
            if(c == constraintSetter){
                return true;
            }
        }
        return false;
    }

    public boolean isMatchingType(Class c){
        if(this.matchingClasses != null){
            if(this.matchingClasses.contains(c)){
                return true;
            }
        }
        return false;
    }

    public abstract void setConstraints(AbstractFilter constraintSetter);
    public abstract void clearConstraints();

    public boolean check(){
        throw new UnsupportedOperationException();
    }

    public boolean check(AbstractFilter filterToTest){
        throw new UnsupportedOperationException();
    }

    public boolean check(Set<AbstractFilter> filtersToTest){
        throw new UnsupportedOperationException();
    }

    public boolean allConditionsSatisfied(Set<AbstractFilter> matchingFilters){
        throw new UnsupportedOperationException();
    }

    public int getExpectedNumMatchingFilter(){
        return this.expectedNumMatchingFilter;
    }
}
