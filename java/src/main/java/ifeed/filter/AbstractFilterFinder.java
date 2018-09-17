package ifeed.filter;

import java.util.HashSet;
import java.util.Set;

/**
 * Class that is used to specify the filter for setting up constraints and the matched filters
 */
public abstract class AbstractFilterFinder {

    protected Set<Class> constraintSetterClasses;
    protected Set<Class> matchingClasses;

    protected AbstractFilterFinder(){
    }

    protected AbstractFilterFinder(Class constraintSetterClass){
        this.constraintSetterClasses = new HashSet<>();
        this.constraintSetterClasses.add(constraintSetterClass);
        this.matchingClasses = null;
    }

    protected AbstractFilterFinder(Class constraintSetterClass, Class matchingClass){
        this.constraintSetterClasses = new HashSet<>();
        this.constraintSetterClasses.add(constraintSetterClass);
        this.matchingClasses = new HashSet<>();
        this.matchingClasses.add(matchingClass);
    }

    public boolean hasMatchingClass(){
        if(matchingClasses == null){
            return false;
        }else{
            return true;
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

    public boolean allConditionsSatisfied(){
        return true;
    }
}
